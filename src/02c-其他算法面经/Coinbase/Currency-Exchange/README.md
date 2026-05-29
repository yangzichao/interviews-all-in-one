# Coinbase — Currency Exchange

实现一组货币转换 API，逐步加需求。面试时 CoderPad 上**一个 Part 给一个**，做完再放下一个。

来源：Coinbase VO 高频题（见 [coinbase-vo-problem-origins.md](../coinbase-vo-problem-origins.md)）。最接近的 LeetCode 编号：**LC 399 Evaluate Division**，follow-ups 是 best-rate path + streaming updates + arbitrage detection。

---

## API 全貌

```java
// Part 1 — 基础转换
new ConverterPart1(List<String[]> rates)        // 每条 ["from","to","rateStr"]
    .convert(from, to, amount) -> double

// Part 2 — 最优路径
new BestRateConverterPart2(List<String[]> rates)
    .convertBest(from, to, amount) -> double

// Part 3 — 实时更新
new StreamingConverterPart3()                   // 没有初始 rates
    .update(from, to, rate) -> void
    .convert(from, to, amount) -> double

// Part 4 — 套利检测
new ArbitrageDetectorPart4(List<String[]> rates)
    .hasArbitrage() -> boolean

// Part 5 — 并发安全 + 实时汇率流式更新
new ConcurrentStreamingConverterPart5()
    .update(from, to, rate) -> void      // thread-safe
    .convert(from, to, amount) -> double // thread-safe

// Part 6 — 预计算 best rate (Floyd / Dijkstra / Bellman-Ford 三选一)
new PrecomputedBestRateConverterPart6(List<String[]> rates)
    .convertBest(from, to, amount) -> double   // O(1) 查表

// Part 7 — 多 source + TTL / staleness
new MultiSourceConverterPart7(long ttlMillis)
    .updateWithSource(from, to, rate, source, timestampMillis) -> void
    .convert(from, to, amount, nowMillis) -> double  // 任一 hop stale 抛 StaleRateException

// Part 8 — 历史汇率回查
new HistoricalConverterPart8()
    .update(from, to, rate, timestampMillis) -> void
    .rateAt(from, to, asOfMillis) -> double
    .convertAt(from, to, amount, asOfMillis) -> double
```

---

## 4 个 Part 的题面

### Part 1 — Basic Conversion

输入一组双向汇率，DFS 通过中间币种转换。

```
rates = [["USD","EUR","0.9"], ["EUR","GBP","0.85"]]
convert("USD", "GBP", 100)  →  100 * 0.9 * 0.85 = 76.5
convert("USD", "USD", 50)   →  50
convert("USD", "JPY", 1)    →  NoSuchElementException
```

**Clarifications**:
- Rate 是**双向**的：`USD→EUR=0.9` 隐含 `EUR→USD = 1/0.9`。
- `from == to` 直接返回 `amount`，不需要图里有这个币种。
- 未知币种或无路径 → 抛 `NoSuchElementException`。
- 不考虑负 rate / 零 rate（输入合法）。
- 不要求最优路径，**任意一条**可达路径都行。

---

### Part 2 — Best Rate

同样的图，但要找**乘积最大**的路径（对用户最有利的换算）。

```
rates = [
  ["USD","EUR","0.9"],
  ["EUR","GBP","0.85"],
  ["USD","GBP","0.7"]       // 直连一条差路径
]
convertBest("USD","GBP",100) → 100 * 0.9 * 0.85 = 76.5  (中转更好)
```

**Clarifications**:
- 还是双向，反向 rate = `1/rate`。
- 没有负权（rate > 0），所以 best path 可以用 DFS + memo 或者 Bellman-Ford 风格 relaxation。
- 不要求列出路径，只要 best amount。
- 图可能有环——别死循环。

---

### Part 3 — Streaming Updates

没有初始 rates；rates 边走边来，可以 add 也可以 replace。

```
c = new StreamingConverterPart3();
c.update("USD","EUR",0.9);
c.update("EUR","GBP",0.85);
c.convert("USD","GBP",100) → 76.5

c.update("USD","EUR",1.0);                 // replace, 现在 USD=EUR
c.convert("USD","GBP",100) → 85.0
```

**Clarifications**:
- `update` 既能新增边，也能覆盖已有边。
- `convert` 语义同 Part 1（任意路径即可，不要求最优）。
- 双向规则不变：`update("A","B",r)` 隐含 `B→A = 1/r`。
- 不需要 thread-safe（除非面试官追问）。

---

### Part 4 — Arbitrage Detection

判断是否存在一条**循环**，使得沿着这条循环走一圈后金额 > 起点（乘积 > 1）。

```
rates = [
  ["USD","EUR","0.9"],
  ["EUR","GBP","0.85"],
  ["GBP","USD","1.4"]
]
hasArbitrage() → true  (0.9 * 0.85 * 1.4 = 1.071 > 1)
```

**Clarifications**:
- 只要存在**任意一条** product > 1 的环就返 true。
- 双向边在套利里要小心：A→B→A 的乘积永远是 1，不是套利环。真正的套利环长度 ≥ 3。
- 经典做法（**hint，不写在代码里**）：把每条边的权重转成 `-log(rate)`，然后 Bellman-Ford 找负权环。

---

## 4 个 Part 的核心总览

> Part 1–4 是面经原题。Part 5+ 是超越面经的 follow-up（Coinbase 团队最常追问的方向）。

| Part | 一句话 | 易踩的坑 / 讨论点 |
|------|--------|----------|
| 1 | 双向汇率图任意路径 DFS | 反向边 1/rate；from==to 不查图；不可达 → throw |
| 2 | 乘积最大路径 | 不是 Dijkstra 直接套——边权 -log(rate) 可能为负；DFS+memo 或 relaxation |
| 3 | 流式 update + 覆盖 | replace 边的语义；空图同币种 identity |
| 4 | 套利检测 (cycle product > 1) | -log + Bellman-Ford 负权环；A↔B 反向边自身循环乘积=1 不算 |
| 5 | **并发安全 + 实时汇率流式更新** | 锁 / CHM / COW / single-writer 五选一；说清各自的吞吐 / 延迟 |
| 6 | **算法选型: Dijkstra vs Bellman-Ford vs Floyd** | rate>1 让 Dijkstra 失效；Floyd 预算 O(V³) 后 O(1) 查 |
| 7 | **多 source + TTL/staleness** | 多家报价怎么聚合；任一 hop stale 整条失败 |
| 8 | **持久化 + 历史汇率回查** | TreeMap<ts, rate> + floorEntry；冷热数据分离 |

---

## Part 5 — 并发安全 + 实时汇率流式更新（Coinbase 面试最常追问的方向）

**问题**：行情系统每秒 10k 条 rate update，同时 1k 个用户线程在 convert。要求所有操作线程安全，且 convert 不能看到"撕裂"的汇率组合（即一段用旧 rate、一段用新 rate 拼出一个历史上从未存在过的汇率）。

**5 种实现，各自的取舍**：

| 方案 | 读吞吐 | 写吞吐 | 撕裂风险 | Coinbase 场景适用 |
|------|-------|-------|---------|-------------------|
| `synchronized this` 一把大锁 | 低 | 低 | 无 | 不行——读写互斥 |
| `ReentrantReadWriteLock` | 高 | 中 | 无 | 读远多于写时好；行情场景写也多，退化 |
| `ConcurrentHashMap` 邻接表 | 极高 | 高 | **有**（多 hop 没快照） | 单 hop convert OK；多 hop 不行 |
| **Copy-on-Write** 整图 | 极高（无锁读） | 低（每次复制 O(V+E)） | 无 | 写少读多；行情写太多复制爆炸 |
| **Single-writer + volatile snapshot** | 极高 | 高（顺序化） | 无 | 行情场景最合适——单线程消费 update queue，多 reader 直接读 volatile 引用 |

**面试官最常问的 4 个 follow-up**：

1. **"10k update/s + 1k convert/s，你的方案吞吐多少？延迟 p99？"**
   → 写场景吃锁还是 lock-free？读路径有没有内存屏障？
2. **"convert 中途看到新 rate（USD→EUR 旧 + EUR→GBP 新），用户用了历史上不存在过的组合，能接受吗？"**
   → 这就是 single-writer + snapshot 的核心价值；CHM 单纯每边加锁解决不了。
3. **"两个线程同时 `update(USD,EUR,0.9)` 和 `update(USD,EUR,0.95)`，最终是哪个？"**
   → "未定义" 一般不被接受——金融场景要顺序化（FIFO queue / lock）。
4. **"update 顺序乱了，convert 看到一个老 rate 是不是 bug？"**
   → 引出 monotonic version / sequence number 讨论。

**自检题**（写完代码自己问自己）：
- 我的 convert 跨多条边时，能保证看到的是"某个时间点的整张图"吗？
- 用 `ConcurrentHashMap` 单纯包一下就够吗？为什么不够？
- 我的方案在 10k writer/1k reader 和 1k writer/10k reader 两种比例下，哪种更适合？

---

## Part 6 — 算法选型对比：Dijkstra / Bellman-Ford / Floyd-Warshall

**问题**：货币对总数 1000+，`convertBest` 每秒被调用 10k 次。Part 2 那种"每次 DFS+memo 从头算"撑不住。

**三种算法的取舍**：

| 算法 | 复杂度 | 支持负权 | 最适合的场景 | 易踩的坑 |
|------|-------|---------|------------|---------|
| **Dijkstra** | O((V+E) log V) per source | ❌ | V 中等、convert 不极端高频 | rate > 1 时 -log(rate) < 0，**Dijkstra 失效**；要么验证 rate ≤ 1 用反向规范化，要么直接做"最长路径"DAG 风格 |
| **Bellman-Ford** | O(V·E) per source | ✓ | 边少、需要顺便检测套利环 | 慢，但 Part 4 已经有半成品 |
| **Floyd-Warshall** | O(V³) 一次性，之后 O(1) 查 | ✓ | V ≤ 几百、convertBest 高频 | rate update 后要么全表重算 O(V³)，要么增量维护（很难） |

**对 Coinbase 场景的判断**：货币种类 ~200，convertBest 高频 → **Floyd 一次预算 + O(1) 查最划算**。但要想清楚 update 来了怎么办。

**面试官最常问的 4 个 follow-up**：

1. **"如果加了套利环（rate > 1 的反向），Dijkstra 还能跑吗？"**
   → -log(rate) 变负 → 不能直接用。要么改 Bellman-Ford，要么先用 Part 4 拒掉套利环再 Dijkstra。
2. **"Floyd 预算了，现在 update 一条边 USD→EUR，怎么办？"**
   → 全表重算 O(V³)；或增量：只对包含 USD 或 EUR 的 (i,j) pair 重 relax，但其实复杂度跟全表差不多。
3. **"1000 货币 × 1M 历史对，内存放得下 1000² 的矩阵吗？"**
   → 1M doubles = 8MB，OK。但 1M 种货币就不行了。
4. **"如果 convertBest 一秒 1 次但 update 一秒 10k 次呢？反向情况怎么选？"**
   → 这时维护 Floyd 矩阵代价远大于查询；改回 Bellman-Ford 或 DFS+memo 即时算。

**自检题**：
- 我选 Floyd / Dijkstra / BF，能口头算出"V=200、E=10000、update 1/s、query 100/s"场景的成本对比吗？
- 我的算法对"反向边 1/rate"怎么处理？（隐式 vs 显式建反向）

---

## Part 7 — 多 source 汇率聚合 + TTL / staleness

**问题**：Binance、Coinbase、Kraken 同时报 BTC→USD，价格不同。行情可能断线——5 秒没新报价就是 stale，不能再用。要求 convert 时：每条 hop 必须有 fresh rate；多 source 怎么合一份用。

**多 source 聚合策略对比**：

| 策略 | 抗异常 | 延迟 | 实现 | 业务语义 |
|------|-------|------|------|---------|
| Last-write-wins | 差（被抖动主导） | 低 | 简单 | 不严肃的场景 |
| 平均 / median | 好 | 中（要凑多个 source） | 中 | 报表 / 索引 |
| **VWAP**（成交量加权） | 好 | 中 | 需要 volume 数据 | 真实交易场景 |
| 最优（买高卖低） | 一般 | 低 | 简单 | 给用户最有利的 |

**TTL 实现对比**：

| 方式 | 内存 | 实现 | 适合 |
|------|------|------|------|
| Lazy（convert 时检查 timestamp） | 高（垃圾堆积） | 极简 | low traffic |
| 后台清理线程 | 低 | 中（要考虑跟 Part 5 锁配合） | 大量短 TTL |
| `PriorityQueue<expireAt>` | 中（幽灵条目） | 中 | 知道下次过期点 |

**面试官最常问的 4 个 follow-up**：

1. **"5 秒 TTL，convert 请求 t=4.99 来，rate t=5.00 过期，怎么判定？"**
   → "rate 在 convert 发起时是 fresh" 就行；不要求整个 convert 全程 fresh。或者引入 "freshness lock" 把 rate 钉到当前 convert。
2. **"Binance 报 BTC=50000，Kraken 报 49000，给用户用哪个？"**
   → 看业务：买家用低价（对用户最有利），卖家相反；或者用 median 抗 outlier。
3. **"convert(BTC, EUR) 中转 USD：BTC→USD 用 Binance、USD→EUR 用 Coinbase，OK 吗？"**
   → 引出"跨 source 拼接是否合理"——理论上 OK（每段都是 fresh + 你能成交），但要承认存在执行时差风险。
4. **"某 source 突然狂推报价，把别的 source 挤掉了怎么办？"**
   → 单 source 限流；多 source 聚合时按 source 维度算权重；不能让一个 source 主导。

**自检题**：
- 我的 staleness 是 per-edge 还是 per-source？per-(edge,source) 更准但更贵。
- TTL 用 lazy 还是 active？如果 lazy，convert 时怎么避免 O(n) 扫所有 source？

---

## Part 8 — 持久化 + 历史汇率回查（监管 / audit 场景）

**问题**：Coinbase 是金融公司——用户 5 分钟前下单用的汇率必须能复现。监管要求 7 年完整 audit log。需要 `rateAt(from, to, T)` 和 `convertAt(from, to, amount, T)`。

**存储模型对比**：

| 方案 | 写复杂度 | 查 rateAt 复杂度 | 内存 | 适合 |
|------|---------|----------------|------|------|
| `Map<Pair, TreeMap<ts, rate>>` | O(log n) | O(log n) `floorEntry` | 全在内存，1 年 10k/s 会爆 | 短时间窗口 |
| Append-only WAL + 索引 | O(1) 写 | O(log n) 走索引 | 磁盘友好 | 长时间 audit |
| LSM-tree (Bloom + 分级合并) | 写优化 | O(log n) | 磁盘 + cache | 真实生产 |
| 冷热分层（近 1h 内存 + 历史 S3 parquet 按天） | 中 | 热 O(log n)、冷 O(扫一天文件) | 可控 | Coinbase 实战 |

**convertAt 的复杂度**：T 时刻的整图 = 每对 `(a,b)` 用 `rateAt(a,b,T)`；直接套 Part 2 best-path，每边查 O(log n)，总复杂度 O((V+E) log n)。

**面试官最常问的 4 个 follow-up**：

1. **"10k update/s，1 年的数据约 3·10¹¹ 条，怎么存得下查得快？"**
   → 冷热分层 + parquet 列存 + Bloom filter；近 1h 内存，老数据 S3。
2. **"asOf 时间点在第一次 update 之前查不到 rate，return null 还是 throw？"**
   → 我的答案：throw `NoSuchElementException`，跟 Part 1 一致。null 给 caller 太多负担。
3. **"convertAt 拼了不同 update 时刻的 rate（每段都是 asOf 之前最新），这种拼出来的汇率有意义吗？"**
   → 业务语义：snapshot semantics——"那个时刻你能拿到的最新 rate 的组合"。可接受。
4. **"两个并发 update 时间戳一样，谁赢？"**
   → 同 ts 要么后到者覆盖（last-write-wins），要么引入 (ts, sequence) 排序保证全序。

**自检题**：
- 我的 `TreeMap<ts, rate>` 在并发 update / rateAt 下需要换成 `ConcurrentSkipListMap` 吗？
- `convertAt` 跨多个 (from,to) 的 TreeMap，需要"全局 snapshot 一致"吗？（提示：asOf 固定，每对独立查就一致）
- 7 年 audit log 怎么 GC？哪些数据能压缩 / 抽样？

---

## 怎么练

```bash
cd src/02c-其他算法面经/Coinbase/Currency-Exchange

javac CurrencyExchange.java CurrencyExchangeTest.java

java CurrencyExchangeTest                # 跑所有 Part
java CurrencyExchangeTest part1          # 只跑 Part 1
java CurrencyExchangeTest part1 part2    # 跑指定的几个
```

输出格式：

```
Part 1 SKIPPED (not implemented)
Part 2 SKIPPED (not implemented)
Part 3 SKIPPED (not implemented)
Part 4 SKIPPED (not implemented)

Passed=0  Failed=0  Skipped=4
```

骨架在 [CurrencyExchange.java](CurrencyExchange.java)，测试在 [CurrencyExchangeTest.java](CurrencyExchangeTest.java)。每个 Part 在文件里是一段独立的 `public static class`，后缀 `PartN`：

```
PART 1: CurrencyExchange.ConverterPart1                       [⚠ 你来写]
PART 2: CurrencyExchange.BestRateConverterPart2               [⚠ 你来写]
PART 3: CurrencyExchange.StreamingConverterPart3              [⚠ 你来写]
PART 4: CurrencyExchange.ArbitrageDetectorPart4               [⚠ 你来写]
PART 5: CurrencyExchange.ConcurrentStreamingConverterPart5    [⚠ 超越面经 follow-up: 并发]
PART 6: CurrencyExchange.PrecomputedBestRateConverterPart6    [⚠ 超越面经 follow-up: 算法选型]
PART 7: CurrencyExchange.MultiSourceConverterPart7            [⚠ 超越面经 follow-up: 多 source + TTL]
PART 8: CurrencyExchange.HistoricalConverterPart8             [⚠ 超越面经 follow-up: 历史回查 + 持久化]
```

**为什么独立 class 而不是继承同一个基类？** 真实面试是渐进的——做完 Part 1 才会让你看 Part 2。这种切片结构让你能专注当前 Part 而不破坏已完成的部分；做完之后还能讨论 "如果生产环境会复用 graph 表示，我会抽个 `RateGraph`"——这种 trade-off 讨论是面试加分点，但不是必做项。
