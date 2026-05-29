# Coinbase — Log Parsing

解析日志行, 按 thread_id 分组, 按 timestamp 排序; 逐步加查询/流式能力. 面试时 CoderPad 上**一个 Part 给一个**, 做完再放下一个.

来源: Coinbase VO 高频题(`coinbase-vo-problem-origins.md` 里的 problem #7).

---

## 数据格式 (Setup)

每行格式: `"<timestamp> <thread_id> <level> <message>"`

- `timestamp` — `long` (epoch ms)
- `thread_id` — 任意字符串 (无空格)
- `level` — `INFO | WARN | ERROR | DEBUG` (本题不强校验)
- `message` — 行尾剩余部分, **可以包含空格**

解析: 按空格切, 但**前 3 个 token 切完后, 剩下整段都是 message**(用 `String.split(" ", 4)` 之类).

格式错误的行(token 数 < 4, 或 timestamp 不是 long)**静默丢弃**, 不抛.

`LogEntry` 是 record:

```java
public static record LogEntry(long timestamp, String threadId, String level, String message) {}
```

---

## API 全貌

```java
// Part 1
new LogParserPart1()
    .parseAndGroup(List<String> lines)
      -> Map<String, List<LogEntry>>          // 每个 thread 的列表按 timestamp 升序

// Part 2
new LogParserPart2()
    .parseAndGroup(List<String> lines, Predicate<LogEntry> keep)
      -> Map<String, List<LogEntry>>          // 仅保留 keep 通过的, 空 thread 不进 map

// Part 3
new LogParserPart3(List<String> lines)        // 构造时一次性 parse + 索引
    .query(long fromInclusive, long toExclusive, String threadId)
      -> List<LogEntry>                       // threadId==null 表示所有 thread 合并

// Part 4
new LogParserPart4()
    .append(String line)                      // 增量 insert, 保持每个 thread 内部按 timestamp 排序
    .recent(String threadId, int n)
      -> List<LogEntry>                       // 最近 n 条, oldest-first
```

---

## 8 个 Part 的核心

> Part 1–4 是面经原题 (Coinbase VO 渐进 4-part).
> Part 5–8 是**超越面经**的 follow-up —— Coinbase 团队明确说重点考"数据增长 / 并发 / 取舍",
> 这些就是面试官真要追问、而面经经常省略的部分.

| Part | 一句话 | 易踩的坑 / 讨论点 |
|------|--------|----------|
| 1 | parse + groupBy + sort | malformed 静默丢弃; message 含空格不能丢; 每个 group 内排好序 |
| 2 | 加一个 `Predicate<LogEntry>` filter | 过滤后 thread 没东西 → key 不进 map |
| 3 | 一次性 parse, 后续多次窗口查询 | `[from, to)` 半开; `threadId==null` 是 "全部" |
| 4 | 流式 append + 维持顺序 | out-of-order 到达, 要插对位置; `recent(n)` 返回末尾 n 个但**oldest-first** |
| 5 | **滑动时间窗口聚合** (errorRate / countByLevel / topN threads) | 增量维护 vs 全扫; 时间桶 vs 精确踢出; out-of-order late data |
| 6 | **并发 ingestion + 查询** (Coinbase 必问) | 读 1: 写 100k 该选什么? 撕裂读 (numerator/denominator 不配对) |
| 7 | **冷热分层 + 压缩归档** (S3 / gzip / zstd) | rollover 触发条件; 跨 tier query 边界去重; GDPR 选择性删除 |
| 8 | **倒排索引 + 分片 + compaction** (近 ES / Loki) | tokenization 选择; 时间分片 vs term hash; LSM segment compaction |

---

### Part 1 — Parse & Group

```java
Map<String, List<LogEntry>> parseAndGroup(List<String> lines)
```

- 每行 parse 成 `LogEntry`, 按 `threadId` 分组.
- 每组按 `timestamp` 升序.
- malformed 行(< 4 个 token / timestamp 不是 long) 静默 skip.

**样例:**

```
"1000 t1 INFO start"
"1500 t2 WARN slow"
"1200 t1 ERROR boom in core module"
↓
{
  "t1": [(1000, t1, INFO, "start"), (1200, t1, ERROR, "boom in core module")],
  "t2": [(1500, t2, WARN, "slow")]
}
```

---

### Part 2 — Filter by Predicate

```java
Map<String, List<LogEntry>> parseAndGroup(List<String> lines, Predicate<LogEntry> keep)
```

跟 Part 1 一样, 多了一个 `keep` 过滤. **过滤后没条目的 thread 完全不出现在 map 里**.

典型用法: `e -> e.level().equals("ERROR")` 只看 ERROR.

---

### Part 3 — Time Window Query

```java
new LogParserPart3(List<String> lines)
List<LogEntry> query(long fromInclusive, long toExclusive, String threadId)
```

- 构造时一次性 parse 整批, 内部建索引.
- `query` 多次调用应该都很快(面试加分点: 提到按 thread 分桶并保持升序, 二分定位 window).
- `[from, to)` 半开, 跟数据库时间窗一致.
- `threadId == null` → 所有 thread 的条目按 timestamp 合并并返回.

---

### Part 4 — Streaming Append

```java
void append(String line)
List<LogEntry> recent(String threadId, int n)
```

- `append`: 单行进来, parse 后插入; **支持 out-of-order**, 插到该 thread 的列表中正确位置(按 timestamp).
- `recent(threadId, n)`: 该 thread 最近 `n` 条 (timestamp 最大的 n 条), 返回时**oldest-first**(即升序排列的 tail).
- 该 thread 不存在 / 没条目 → 空 list.
- `n` 大于实际数 → 返回所有.

**面试加分点**: 提 max-heap / TreeMap / 双端队列, trade-off 类似 IMDB Part 2.

---

## Part 5 — 滑动时间窗口聚合 (实时告警 / Dashboard)

**问题**: 线上服务每秒涌入几十万条日志, 业务方要看的不是原始 entries, 而是**聚合指标**:

- 最近 5 分钟的 `errorRate` (用来告警)
- 最近 1 小时按 `level` 的分布 (画 dashboard 饼图)
- 最近 1 小时哪些 `thread` / `service` 报错最多 (排障)

窗口大小 `windowMs` 在构造时固定, query 时点 `nowMs` 任意; 老于 `nowMs - windowMs` 的数据要从统计里踢出去 (不要求立刻物理删).

**面试官最常追问的 4 个 follow-up**:

1. **"如果 query QPS 比 append QPS 还高, 你怎么做?"**
   → 引出**增量维护**:append/expire 时更新累计 counter, query 是 O(1). 跟"每次 query 全扫窗口"对比.
2. **"窗口 5min, 一条一条踢出最老的条目代价高, 怎么摊销?"**
   → 引出**时间桶 (bucketing)**:把窗口切成 K 个 sub-bucket, 过期整桶丢. 代价是边界精度损失 ≤ 一个桶宽度.
3. **"topN threads 要精确还是能近似?"**
   → 精确: HashMap counter + 排序, 内存 O(unique threads); 近似: Count-Min Sketch / heavy hitters (Misra-Gries), 内存常数但有误差. 看业务能不能接受 ±5% 误差.
4. **"如果一条 entry 的 timestamp 比当前 watermark 老 (late arrival), 怎么办?"**
   → 简单方案:直接丢; 严谨方案:watermark + late record reassign 回去 (Flink 模型). 大多数告警场景选丢.

**4 种实现, 各自的取舍**:

| 方案 | append | query | 内存 | 何时选 |
|------|--------|-------|------|--------|
| 每次 query 全扫窗口 | O(1) | O(window) | O(window) | query QPS 极低 |
| 增量 counter + deque | O(1) 摊销 | O(1) | O(window) | 通用默认 |
| **时间桶 (Hopping window)** | O(1) | O(K buckets) | O(K) | 大窗口 + 高 QPS 推荐 |
| Count-Min Sketch (近似 topN) | O(1) | O(1) | O(d × w) 常数 | 不可枚举的大 cardinality |

**自检题**:
- 我的实现里, 一条 ts=t 的 entry 到达后, 在哪个时间点才被 "踢出"? 是 lazy 还是 active?
- 如果 `nowMs` 倒退了 (NTP 校正), 会发生什么?
- `topN(0)` 和 `topN(Integer.MAX_VALUE)` 都应该 work, 我处理了吗?

---

## Part 6 — 并发 Streaming + Querying (Coinbase 必问)

**问题**: Ingestion pipeline 通常有上千个 worker 并发往进 push 日志, 同时 dashboard 有几十个 reader 并发查聚合指标. 要求**所有操作线程安全**.

**核心 race conditions**:
- (a) `appendPart6` 不能丢数据 (counter 更新丢失)
- (b) `errorRatePart6` 看到的 `(errorCount, totalCount)` 必须**配对**, 不能撕裂 (numerator 是新值 / denominator 是旧值)
- (c) writer 之间不能严重 contention -- append 是 hot path

**面试官最常追问的 4 个 follow-up**:

1. **"99% 是 append, 1% 是 query, 选哪个并发方案?"**
   → ConcurrentHashMap + LongAdder (写无锁热点优化), 或者 thread-local buffer + 定时 merge. 反过来 99% 读则可以容忍写时全锁.
2. **"你这个 errorRate 在并发下能保证 numerator <= denominator 吗?"**
   → 朴素分开读两个 LongAdder 会撕裂. 解法:把 `(totalCount, errorCount)` 打包成 immutable record / `AtomicReference`, 或者用 StampedLock optimistic read 重试.
3. **"如果 append 来自不同时间桶, 桶之间的 counter 怎么不打架?"**
   → 每个 bucket 自己一把锁 / 自己的 ConcurrentHashMap; bucket roll-over 是单线程后台任务做.
4. **"测试并发正确性怎么做? sleep + assert 靠谱吗?"**
   → 不靠谱. 该用 stress test (大量 writer + reader 反复跑) + 不变式断言 (`sum(per-level counts) == total`), 或者 `JCStress` 工具.

**4 种并发策略对比**:

| 方案 | append | query 一致性 | 代码量 | 何时选 |
|------|--------|-------------|--------|--------|
| 一把 `synchronized` | 串行排队 | 强 | 极小 | 原型 / 低 QPS |
| `ConcurrentHashMap` + `LongAdder` | 无锁热点 | 可能撕裂 | 小 | 大多数生产场景 |
| Thread-local buffer + merge | 完全无锁 | 弱 (有延迟) | 中 | 写 >> 读, 容忍延迟 |
| Disruptor / SPSC ring | 单写者无锁 | 强 (单写) | 大 | 极致吞吐 (LMAX 风格) |

**自检题**:
- 我的 `errorRate` 在 1000 writer + 10 reader 并发下, 会不会算出 > 1 的值?
- bucket 过期是哪个线程负责的? 它跟 writer/reader 是同一把锁吗?
- 我用了 ConcurrentHashMap 但 `putIfAbsent + new LongAdder()` 这种 pattern 有没有 race?

---

## Part 7 — 冷/热分层 + 归档压缩

**问题**: 日志每天产生 TB 级, 不能全留在内存. 必须分层:

- **hot** (最近 1h):内存 + 本地 SSD, 查询毫秒级
- **warm** (最近 7d):本地磁盘, 已压缩, 查询秒级
- **cold** (> 7d, 保留 90d 合规):S3 + 强压缩, 查询分钟级

设计 `rolloverPart7(cutoffMs)` 把老数据搬到冷层, 以及 `queryUnifiedPart7(...)` 自动跨 tier 查询.

**面试官最常追问的 4 个 follow-up**:

1. **"gzip 还是 zstd 还是 lz4? 为什么?"**
   → 三者权衡: lz4 速度极快但压缩率低 (hot→warm 用); zstd 压缩率高解压快 (warm→cold 推荐, 现在事实标准); gzip 兼容性好但已过时.
2. **"跨 tier 查询时怎么避免边界 entry 出现两次或漏掉?"**
   → rollover 必须是**原子切换**:写一个新 segment 文件→fsync→从 hot 删除对应条目, 任何崩溃恢复都能保证"要么 hot 有要么 cold 有". 查询时 hot+cold 都查再 dedupe by entry_id.
3. **"GDPR 要求删除某用户所有日志, 冷层是个压缩文件, 怎么 '选择性删'?"**
   → 立刻物理删代价巨大. 工业做法:写 tombstone 到一个 deletion log, 查询时过滤; 下次 compaction 时 rewrite 排除. 类似 LSM tombstone.
4. **"rollover 在 append 高峰期触发, 会不会卡住写入?"**
   → rollover 应该在后台线程做, 用 immutable snapshot 思路:freeze 当前 hot segment → 新 append 进新 segment → 后台压缩旧的. 跟 Part 6 的并发模型直接相关.

**3 种 rollover 策略对比**:

| 方案 | 触发条件 | 文件数量 | 查询路由难度 |
|------|----------|---------|------------|
| 时间驱动 (每小时一卷) | 固定 cron | 可预测 | 文件名 `[start_ts, end_ts]` 直接挑 |
| 大小驱动 (满 1GB 卷一次) | 字节阈值 | 取决于流量 | 需要 metadata 索引 |
| 混合 (whichever first) | 时间 OR 大小 | 自适应 | 中 |

**自检题**:
- 我的 `queryUnifiedPart7([from,to])` 在 cutoff 横切 [from,to] 区间时, 还能保证不漏不重吗?
- 冷层文件不读时是不是真的不占内存 (mmap vs 全部 load)?
- 如果 rollover 写到一半进程崩了, 重启后会出现 "数据既在 hot 又在 cold" 吗?

---

## Part 8 — 倒排索引 + 分片 + Compaction (近 ES / Loki)

**问题**: 查询升级:不再只是按 thread / 时间窗, 而是**按 message keyword 全文检索** (例如 "找过去 7 天 message 含 'timeout' 的所有日志"). 朴素 `contains` 全扫 7 天数据 O(N) 不可接受 → 必须建倒排索引. 但索引本身随时间无限增长 → 必须 shard + 后台 compaction.

**面试官最常追问的 4 个 follow-up**:

1. **"Tokenization 怎么做?"**
   → 朴素 `split(" ")` 会让 "ERROR:" 跟 "ERROR" 不匹配. 工业方案: lowercase → 去标点 → 可选 stem / stop-words / n-gram. 另一种思路是字段化 (`level:ERROR message:timeout`), 跟 ES 一样.
2. **"Postings list 用什么数据结构?"**
   → 排序 `(entry_id, ts)` 列表 + delta encoding + varint 是经典方案. 现代用 **Roaring Bitmap** -- 压缩率好且支持 AND/OR 快速合并 (跨 term 复合查询).
3. **"100 亿 entry 时索引怎么 shard? 时间分片 vs term hash 分片?"**
   → **时间分片**:老 segment 可独立 compact / 删除, 范围 query 友好; ES 默认 (按 index per day). **term hash 分片**:写均匀但查询要 scatter-gather. 大多数日志场景选时间分片.
4. **"小 segment 越来越多, 查询时打开几百个文件性能崩, 怎么办?"**
   → **LSM compaction**:后台周期把多个小 segment merge 成大 segment, 同时 dedupe / 排除过期 entry. 跟 Part 7 的归档天然衔接.

**4 种 sharding 策略对比**:

| 策略 | 加机器代价 | 范围 query | 全文 query | 写入热点 |
|------|----------|----------|-----------|---------|
| `hash(entry_id) % N` | 全 key 迁移 ❌ | 差 (全 shard scatter) | 差 | 低 |
| **时间分片** (按 day/hour) | 不影响老数据 | 好 (只查相关 shard) | 中 | 集中在最新 shard |
| 一致性哈希 + 虚拟节点 | ~1/N 迁移 | 差 | 差 | 低 |
| `hash(term) % N` | ~全迁移 | 差 | 单 term 快 / 跨 term 差 | 中 |

**自检题**:
- 我的 `searchPart8(term, [from,to])` 在 term 不存在 / postings 为 1M+ 时, 行为都合理吗?
- compaction 在跑的时候, 同时有 `indexPart8` 写入, 数据会丢吗?
- 冷层数据要不要也建倒排, 还是只留时间索引接受全文查冷数据 = 慢?

---

## 怎么练

```bash
cd src/02c-其他算法面经/Coinbase/Log-Parsing

javac LogParsing.java LogParsingTest.java

java LogParsingTest                # 跑所有 Part
java LogParsingTest part1          # 只跑 Part 1
java LogParsingTest part1 part2    # 跑指定的几个
```

输出格式:

```
Part 1 SKIPPED (not implemented)
Part 2 SKIPPED (not implemented)
Part 3 SKIPPED (not implemented)
Part 4 SKIPPED (not implemented)

Passed=0  Failed=0  Skipped=4
```

骨架在 [LogParsing.java](LogParsing.java), 测试在 [LogParsingTest.java](LogParsingTest.java). 每个 Part 在文件里是一段独立的 `public static class`, 后缀 `PartN`:

```
PART 1: LogParsing.LogParserPart1     [⚠ 你来写]
PART 2: LogParsing.LogParserPart2     [⚠ 你来写]
PART 3: LogParsing.LogParserPart3     [⚠ 你来写]
PART 4: LogParsing.LogParserPart4     [⚠ 你来写]
PART 5: LogParsing.LogParserPart5     [⚠ 超越面经 follow-up — 滑动窗口聚合]
PART 6: LogParsing.LogParserPart6     [⚠ 超越面经 follow-up — 并发]
PART 7: LogParsing.LogParserPart7     [⚠ 超越面经 follow-up — 冷热分层 + 归档]
PART 8: LogParsing.LogParserPart8     [⚠ 超越面经 follow-up — 倒排索引 + 分片 (偏设计讨论)]
```

`LogEntry` 是 `LogParsing` 的 public nested record, 4 个 Part 共用.

**为什么独立 class?** 真实面试是渐进的——做完 Part 1 才会让你看 Part 2. 这种切片结构让你能专注当前 Part 而不破坏已完成的部分; 各 Part 的 data structure 也可能不同(Part 4 要支持 out-of-order insert, 跟 Part 1 一次性 sort 的数据结构选择不一样).
