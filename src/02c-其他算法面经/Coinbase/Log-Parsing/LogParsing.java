import java.util.*;
import java.util.function.Predicate;

/**
 * 4-part Coinbase interview practice — Log Parsing.
 *
 * 每个 Part 是独立的 class, 后缀 PartN. 先无脑独立写, 做完再讨论抽公共逻辑.
 *
 * 这不是产品代码, 是练习代码 —— 让你能专注当前 Part 而不破坏已完成的部分.
 *
 * Log line 格式:  "<timestamp> <thread_id> <level> <message>"
 * 其中 message 可以含空格; 解析时用 split(" ", 4).
 * malformed 行 (< 4 个 token / timestamp 非 long) 静默 skip.
 */
public class LogParsing {

    /** 4 个 Part 共用的 LogEntry record. */
    public static record LogEntry(long timestamp, String threadId, String level, String message) {}

    // ====================================================================
    // PART 1  —  Parse & Group                                      [⚠ TODO]
    // ====================================================================
    // parse 每行 -> 按 threadId 分组 -> 每组按 timestamp 升序.
    // malformed 行 (< 4 token 或 timestamp 不是 long) 静默丢弃, 不抛.
    //
    //   "1000 t1 INFO start"
    //   "1500 t2 WARN slow"
    //   "1200 t1 ERROR boom in core module"
    //   ↓
    //   { t1: [(1000,...,"start"), (1200,...,"boom in core module")],
    //     t2: [(1500,...,"slow")] }

    public static class LogParserPart1 {
        public LogParserPart1() {}

        public Map<String, List<LogEntry>> parseAndGroup(List<String> lines) {
            throw new UnsupportedOperationException("LogParserPart1.parseAndGroup: not implemented");
        }
    }

    // ====================================================================
    // PART 2  —  Filter by Predicate                                [⚠ TODO]
    // ====================================================================
    // 与 Part 1 比:
    //   同: parse 规则, 分组, 排序
    //   变: 无
    //   新: keep predicate, 只保留通过的 LogEntry
    //
    // **过滤后没条目的 thread 完全不出现在 map 里** (key 也不要).

    public static class LogParserPart2 {
        public LogParserPart2() {}

        public Map<String, List<LogEntry>> parseAndGroup(List<String> lines, Predicate<LogEntry> keep) {
            throw new UnsupportedOperationException("LogParserPart2.parseAndGroup: not implemented");
        }
    }

    // ====================================================================
    // PART 3  —  Time Window Query                                  [⚠ TODO]
    // ====================================================================
    // 构造时一次性 parse + 索引, 后续多次 window 查询.
    //   query(from, to, threadId)
    //     - [from, to) 半开
    //     - threadId == null  -> 所有 thread 合并, 按 timestamp 升序
    //     - 否则只看该 thread, 按 timestamp 升序
    //
    // 加分点: 按 thread 分桶 + 桶内升序, 二分 (Collections.binarySearch / lowerBound 写法)
    //         定位 window 范围, O(log n + output).

    public static class LogParserPart3 {
        public LogParserPart3(List<String> lines) {
            throw new UnsupportedOperationException("LogParserPart3.<init>: not implemented");
        }

        public List<LogEntry> query(long fromInclusive, long toExclusive, String threadId) {
            throw new UnsupportedOperationException("LogParserPart3.query: not implemented");
        }
    }

    // ====================================================================
    // PART 4  —  Streaming Append                                   [⚠ TODO]
    // ====================================================================
    // 与 Part 3 比:
    //   同: 解析规则, LogEntry schema
    //   变: 数据是流式 append 进来的, 不是一次性
    //   新: 支持 out-of-order 到达, 每个 thread 内部维持 timestamp 升序
    //       recent(threadId, n) 返回最近 n 条 (timestamp 最大的 n 条), 但 oldest-first
    //
    //   append("1000 t1 INFO a")
    //   append("3000 t1 INFO c")
    //   append("2000 t1 INFO b")   <- out-of-order
    //   recent("t1", 2)  ->  [(2000,...,"b"), (3000,...,"c")]
    //
    // n > 实际数 -> 返回所有.
    // thread 不存在 / 空 -> 返回 [].

    public static class LogParserPart4 {
        public LogParserPart4() {}

        public void append(String line) {
            throw new UnsupportedOperationException("LogParserPart4.append: not implemented");
        }

        public List<LogEntry> recent(String threadId, int n) {
            throw new UnsupportedOperationException("LogParserPart4.recent: not implemented");
        }
    }

    // ====================================================================
    // PART 5  —  Sliding Time-Window Aggregation                    [⚠ TODO]
    // ====================================================================
    // 与 Part 4 比:
    //   同: 解析规则, LogEntry schema, streaming append
    //   变: 不再返回原始 entries, 而是返回**聚合指标** (count / errorRate / topN)
    //       数据是持续涌入的, 老数据要逐渐被踢出窗口
    //   新: appendPart5(line)
    //       errorRatePart5(nowMs)         -> double in [0, 1]
    //       countByLevelPart5(nowMs)      -> Map<String, Integer>
    //       topNThreadsPart5(nowMs, n)    -> List<String>, by count desc
    //
    // 问题陈述 (超越面经的 follow-up):
    //   线上服务每秒涌入几十万条日志. 业务方要看:
    //     - 最近 5 分钟的 error rate (告警用)
    //     - 最近 1 小时按 level 的分布 (dashboard)
    //     - 最近 1 小时哪些 thread/service 报错最多 (排障)
    //   窗口大小固定 (构造时传入 windowMs), 但 query 时间点 nowMs 可以任意.
    //   关键: 老于 (nowMs - windowMs) 的数据要被踢出统计, 但不强求立刻物理删除.
    //
    // 面试要讨论的取舍:
    //   1. 每次 query 全扫窗口 vs 增量维护
    //      - 全扫: 简单, 但 query QPS 高时浪费
    //      - 增量: 维护一个 deque, append/expire 时更新累计指标 - O(1) query
    //   2. 时间桶 (bucketing):
    //      - 把窗口切成 K 个 sub-bucket (例如 5min 窗口 / 10s 一个桶 = 30 个桶)
    //      - 过期时整桶丢, 而不是一条一条踢 -> 摊销 O(1)
    //      - 代价: 边界精度损失 (≤ 一个桶宽度)
    //   3. topN:
    //      - 朴素: 每次 query 全扫窗口, 哈希计数, 排序 -> O(window_size * log n)
    //      - 维护一个 freq counter + max-heap, 但 heap 在 decrement 时不好处理
    //      - Count-Min Sketch (近似): 内存常数, 但 topN 不精确
    //   4. Out-of-order arrival: 一条 ts=旧的 entry 到达了, 它落在已结束的桶里, 怎么办?
    //      - 简单: 丢弃 (大多数 streaming 系统的默认)
    //      - 严谨: watermark + late record reassignment

    public static class LogParserPart5 {
        public LogParserPart5(long windowMs) {
            throw new UnsupportedOperationException("LogParserPart5.<init>: not implemented");
        }

        public void appendPart5(String line) {
            throw new UnsupportedOperationException("LogParserPart5.appendPart5: not implemented");
        }

        public double errorRatePart5(long nowMs) {
            throw new UnsupportedOperationException("LogParserPart5.errorRatePart5: not implemented");
        }

        public Map<String, Integer> countByLevelPart5(long nowMs) {
            throw new UnsupportedOperationException("LogParserPart5.countByLevelPart5: not implemented");
        }

        public List<String> topNThreadsPart5(long nowMs, int n) {
            throw new UnsupportedOperationException("LogParserPart5.topNThreadsPart5: not implemented");
        }
    }

    // ====================================================================
    // PART 6  —  Concurrent Streaming + Querying                    [⚠ TODO]
    // ====================================================================
    // 与 Part 5 比:
    //   同: append + 时间窗口聚合的接口形状
    //   变: 多个 ingestion 线程并发 append, 多个 dashboard 线程并发 query
    //   新: 没有新方法 —— 但所有方法必须线程安全
    //
    // 问题陈述 (Coinbase 必问的并发题):
    //   - 1000 个 worker thread 并发调 appendPart6 (典型 ingestion pipeline)
    //   - 10 个 reader thread 并发调 errorRatePart6 / topN... (dashboard 刷新)
    //   要求:
    //     (a) appendPart6 不能丢数据 (race condition 导致漏计)
    //     (b) errorRatePart6 看到的 (numerator, denominator) 必须配对自洽,
    //         不能出现 numerator 是新值/denominator 是旧值的撕裂
    //     (c) writer 之间不能严重 contention - 高并发 append 是 hot path
    //
    // 面试要讨论的取舍:
    //   1. 一把大锁 synchronized:
    //      - 简单, 正确; 但 1000 个 writer 排队 = 吞吐崩塌
    //   2. ConcurrentHashMap + LongAdder:
    //      - level/thread 计数用 ConcurrentHashMap<String, LongAdder>
    //      - append O(1) 无锁; query 多个 counter 读到的瞬时不一致 (撕裂)
    //   3. 每线程 thread-local buffer + 定时 merge:
    //      - 写完全无锁; query 需要遍历全部 thread-local 并 merge
    //      - 适合 write >> read
    //   4. Disruptor / ring buffer / single-writer principle:
    //      - 多生产者 → 单消费者; 单消费者更新所有聚合 -> 无锁单写
    //      - 高吞吐但实现复杂
    //   5. 跟 Part 5 的桶设计交互:
    //      - 桶过期是后台 job 还是 lazy expire on query? 后台 job 跟 append/query 怎么不打架?
    //
    // 面试官最常追问:
    //   "你这个 errorRate 在并发下能保证 numerator <= denominator 吗?"
    //   "如果 query QPS = 100, append QPS = 100k, 选哪个方案?"

    public static class LogParserPart6 {
        public LogParserPart6(long windowMs) {
            throw new UnsupportedOperationException("LogParserPart6.<init>: not implemented");
        }

        public void appendPart6(String line) {
            throw new UnsupportedOperationException("LogParserPart6.appendPart6: not implemented");
        }

        public double errorRatePart6(long nowMs) {
            throw new UnsupportedOperationException("LogParserPart6.errorRatePart6: not implemented");
        }

        public Map<String, Integer> countByLevelPart6(long nowMs) {
            throw new UnsupportedOperationException("LogParserPart6.countByLevelPart6: not implemented");
        }
    }

    // ====================================================================
    // PART 7  —  Cold/Hot Tiering + Archival                        [⚠ TODO]
    // ====================================================================
    // 与 Part 6 比:
    //   同: append/query 接口, 并发模型可以沿用
    //   变: 日志不能无限留在内存 -> 老数据要刷到 "冷存储" (磁盘 / S3 / object store)
    //       压缩 (gzip / zstd) 以省成本
    //   新: rolloverPart7(nowMs)         -> 把 < cutoff 的数据从 hot tier 移到 cold tier (返回压缩文件 id)
    //       queryColdPart7(...)          -> 从冷存储里查 (慢, 但能查)
    //       queryUnifiedPart7(...)       -> 自动判断从 hot 还是 cold 查
    //
    // 问题陈述 (数据无限增长是 Coinbase 真实痛点):
    //   日志每天产生 TB 级. 必须分层:
    //     - hot (最近 1h): 内存 + 本地 SSD, 查询毫秒级
    //     - warm (最近 7d): 本地磁盘, 已压缩, 查询秒级
    //     - cold (> 7d): S3, gzip/zstd, 查询分钟级 (合规要求保留 90d)
    //   设计 rollover 流程, 以及 query 怎么自动路由.
    //
    // 面试要讨论的取舍:
    //   1. 压缩算法:
    //      - gzip: 兼容性好, CPU 中, 压缩率中
    //      - zstd: 压缩率高, 解压非常快; 现在的事实标准
    //      - lz4: 速度极快, 压缩率一般 (适合 hot→warm)
    //      - snappy: 类似 lz4
    //      面试官追问: "为什么不全用 zstd?" -> 解压 latency / library 可用性 / 历史包袱
    //   2. Rollover 触发:
    //      - 时间驱动 (每小时 rollover 一次)
    //      - 大小驱动 (hot tier 满了就 rollover)
    //      - 混合 (whichever first)
    //   3. Cold tier 的索引:
    //      - 文件名编码 [start_ts, end_ts] -> query 直接挑文件
    //      - 文件内部还要不要二级索引? (footer + sparse index, 类似 Parquet)
    //   4. 查询路由:
    //      - 跨 tier query: hot + cold 都查, 结果合并 (注意去重 - 边界 entry 可能在两边都有)
    //      - 怎么避免同一时间窗内出现 hot/cold 切换导致的数据丢失/重复?
    //   5. 删除 / 合规:
    //      - GDPR 要求删某用户所有日志 -> 冷存储里压缩文件怎么 "选择性" 删?
    //      - 通常做法: tombstone + 下次 rewrite 时排除
    //
    // 这道题更偏设计讨论; 给两个执行入口让你练 rollover 的核心.

    public static class LogParserPart7 {
        public LogParserPart7() {
            throw new UnsupportedOperationException("LogParserPart7.<init>: not implemented");
        }

        public void appendPart7(String line) {
            throw new UnsupportedOperationException("LogParserPart7.appendPart7: not implemented");
        }

        // 把 timestamp < cutoffMs 的数据从 hot 搬到 cold; 返回新生成的 cold-tier 文件/segment id.
        public String rolloverPart7(long cutoffMs) {
            throw new UnsupportedOperationException("LogParserPart7.rolloverPart7: not implemented");
        }

        // 跨 hot+cold 统一查询; 实现要小心边界条目重复.
        public List<LogEntry> queryUnifiedPart7(long fromInclusive, long toExclusive, String threadId) {
            throw new UnsupportedOperationException("LogParserPart7.queryUnifiedPart7: not implemented");
        }
    }

    // ====================================================================
    // PART 8  —  Inverted Index + Sharding + Compaction              [⚠ TODO]
    // ====================================================================
    // 与 Part 7 比:
    //   同: 数据还是逐 entry 进来, 仍然有冷热分层概念
    //   变: 查询从 "时间窗口" 升级到 "全文检索 / 按 level / 按 service / 按 message keyword"
    //       要求建倒排索引 (term -> postings list of entry_ids)
    //       索引本身会随时间无限增长 -> 必须 sharding + compaction
    //   新: indexPart8(line)                              -> ingest + index
    //       searchPart8(term, fromMs, toMs)              -> matching entries
    //       compactPart8()                                -> merge small segments into big ones
    //
    // 问题陈述 (这道题做完就是设计简化版 Elasticsearch / Loki):
    //   - 用户查询: "找过去 7 天 message 包含 'timeout' 的所有日志"
    //   - 朴素方案: 全扫 7 天数据, 用 String.contains -> O(N * |query|) 不可接受
    //   - 必须建倒排: tokenize message -> 每个 term 对应一个 postings list
    //   - 索引大小最终远超原始数据 -> 必须 shard
    //
    // 面试要讨论的取舍 (这是最深的 follow-up):
    //   1. Tokenization:
    //      - 朴素 split(" "): 简单, 但 "ERROR:" 跟 "ERROR" 不匹配
    //      - 加 lowercase / stem / stop-words: 提高召回, 降低精度
    //      - 全文 vs 字段化 (level:ERROR, service:auth, message:"timeout")
    //   2. Postings list 编码:
    //      - 列表 of (entry_id, ts) 排序 -> delta encoding + varint
    //      - Roaring bitmap (大压缩比, 支持 AND/OR 快速合并)
    //   3. Sharding 策略 (跟 IMDB Part 8 一样的讨论):
    //      - 按 time-range shard (推荐: 时间局部性强, 老数据可以独立 compact / 删除)
    //      - 按 term hash shard (查询时要跨多 shard scatter-gather)
    //      - 按 thread/service shard (多租户场景常用)
    //   4. Segment + Compaction (LSM-tree 思路):
    //      - 写入新 segment (immutable, sorted)
    //      - 后台周期性把多个小 segment merge 成大 segment, 同时去重过期 entry
    //      - 跟 Part 6 的并发 + Part 7 的 rollover 怎么配合?
    //   5. 查询路径:
    //      - searchPart8(term, [from,to]): 先按 time shard 定位候选 segment, 每个 segment 查 postings,
    //        bitmap AND 出最终 entry_ids, 再 fetch entry 内容
    //      - 边界: term 不存在? postings 极大 (返回 1M entries 怎么分页?)
    //   6. 跟 cold tier 配合:
    //      - 冷数据需要倒排吗? 还是只保留时间索引, 全文查冷数据就是慢 (acceptable)?
    //      - Elasticsearch 用 "frozen tier" 思路: 倒排留着, 但 segment 卸到 S3
    //
    // 这道题答到这步基本就是设计 Loki / Elasticsearch 了. 不强求写代码,
    // 把上面 5 条权衡说清楚就够了; 真要写就实现最小可用版.

    public static class LogParserPart8 {
        public LogParserPart8(int numShards) {
            throw new UnsupportedOperationException("LogParserPart8.<init>: not implemented");
        }

        public void indexPart8(String line) {
            throw new UnsupportedOperationException("LogParserPart8.indexPart8: not implemented");
        }

        public List<LogEntry> searchPart8(String term, long fromMs, long toMs) {
            throw new UnsupportedOperationException("LogParserPart8.searchPart8: not implemented");
        }

        public void compactPart8() {
            throw new UnsupportedOperationException("LogParserPart8.compactPart8: not implemented");
        }
    }
}
