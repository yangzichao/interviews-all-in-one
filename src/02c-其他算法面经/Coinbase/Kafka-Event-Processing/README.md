# Coinbase — Kafka Event Processing

设计一个 Kafka consumer 处理交易事件,逐步加约束。面试时 CoderPad 上**一个 Part 给一个**,做完再放下一个。

来源:Coinbase VO 高频题 #8。**不在 LeetCode 上**——核心难点是 idempotent consumer + 乱序处理(LC 没有完全对应的题,最接近的是 LC 295 / LC 362 这种 sliding window 题,但只对应 Part 4)。

---

## API 全貌

```java
// 通用 record
public static record Event(String messageId, String userId,
                           long sequence, long eventTime, String payload) {}

// Part 1
new ProcessorPart1()
    .consume(Event e)
    .totalConsumed() -> int
    .consumedByUser(String userId) -> int

// Part 2
new ProcessorPart2()
    .consume(Event e)              // 同一 messageId 第二次起忽略
    .totalConsumed() -> int        // 只数 distinct
    .consumedByUser(String userId) -> int

// Part 3
new ProcessorPart3(String userId, long startSequence)
    .consume(Event e) -> List<Event>     // 返回这次能 emit 的有序 batch

// Part 4
new ProcessorPart4(long windowMillis)
    .consume(Event e)                            // dedupe by messageId
    .countInWindow(long nowEventTime) -> int
    .countByUserInWindow(long nowEventTime) -> Map<String, Integer>
```

---

## 8 个 Part 的核心

> Part 1–4 是面经原题。
> Part 5–8 是**超越面经**的 follow-up —— Coinbase 团队明确说重点考"并发 / 故障 / 持久化 / 观测",
> 这些就是面试官真要追问、而面经经常省略的部分。

| Part | 一句话 | 易踩的坑 / 讨论点 |
|------|--------|----------|
| 1 | 收下所有 event,按 user 计数 | 没什么坑,纯热身 |
| 2 | 按 `messageId` dedupe | 计数只反映**distinct** messageId |
| 3 | 乱序到达 → 按 `sequence` 重排序 emit | "expected = lastEmitted + 1";duplicate `sequence` 视为 dup 忽略 |
| 4 | 窗口 `[now - windowMillis, now]`(**闭区间**两端) | `eventTime`,不是 wall clock;dedupe 后再算 |
| 5 | **并发 consume + partition 路由** | 同 user 同 worker;rebalance 时 hash 不稳定;聚合计数线程安全 |
| 6 | **重试 + DLQ (poison pill)** | 重试不能死循环;DLQ 不阻塞主链路;同步 vs 异步重试 |
| 7 | **消费滞后 (lag) 监控** | 条数 lag vs 时间 lag;聚合开销;heat map / 单值 |
| 8 | **offset 持久化 + 重启恢复** | at-least-once vs exactly-once;dedupe 状态怎么持久化;checkpoint 频率 |

---

### Part 1 详解 — 纯热身

`consume(e)` 把 event 存起来。`totalConsumed()` 返回总条数。`consumedByUser("alice")` 返回 alice 的条数。

```
consume(e1, user=alice)
consume(e2, user=bob)
consume(e3, user=alice)
totalConsumed()           → 3
consumedByUser("alice")   → 2
consumedByUser("charlie") → 0
```

不需要去重,不需要排序。这一 Part 真的就是建立 baseline。

---

### Part 2 详解 — idempotent dedupe

Kafka **保证 at-least-once**,所以 consumer 端必须自己 dedupe。`messageId` 是 producer 端给每条逻辑消息打的唯一 id;同一 `messageId` 来 N 次,只算 1 次。

```
consume(messageId="m1", user=alice)
consume(messageId="m1", user=alice)  // 重复 — 忽略
consume(messageId="m2", user=alice)
totalConsumed()           → 2  (m1, m2)
consumedByUser("alice")   → 2
```

**实现思路**:`Set<String> seen` 记录见过的 `messageId`。`consume(e)` 时 `if (seen.add(e.messageId())) { ... }`,只在第一次见到时真正处理。

**面试加分点**:主动提一句 "in production we'd persist `seen` to a durable store with TTL so it survives consumer restarts but doesn't grow forever"。

---

### Part 3 详解 — ordered output by sequence

per-user `sequence` 严格递增(producer 端保证),但**网络可能乱序到达**。consumer 想按顺序处理 —— 拿到 seq=3 之前不能先处理 seq=4。

- 构造器:`new ProcessorPart3(userId, startSequence)`。**只 track 这一个 user**。
- 内部状态:`expected = startSequence`,从这个 seq 开始往后接。
- `consume(e)`:
  - 如果 `e.userId() != userId`:忽略(不属于本 processor)
  - 如果 `e.sequence() < expected`:已经处理过的旧消息,忽略
  - 如果 `e.sequence() == expected`:emit 这一条,然后看 buffer 里有没有 `expected+1, expected+2, ...`,能 emit 多少 emit 多少。返回这次 emit 的有序列表。
  - 如果 `e.sequence() > expected`:进 buffer,返回空列表
  - duplicate `sequence`(同一 seq 来两次,无论 messageId 是否相同):忽略后到的(本 Part 不要求按 messageId dedupe,只要求按 sequence 单调)。

```
processor = new ProcessorPart3("alice", 1)

consume(seq=2) → []          // ahead, buffer
consume(seq=3) → []          // ahead, buffer
consume(seq=1) → [1, 2, 3]   // gap-fill, drain
consume(seq=5) → []          // ahead again
consume(seq=4) → [4, 5]      // gap-fill
consume(seq=4) → []          // dup, ignored
```

**数据结构提示**:buffer 用 `HashMap<Long, Event>`(by seq)就够了。`TreeMap` 也行,但 `expected += 1` 后 `map.remove(expected)` 用 HashMap 一样 O(1)。

---

### Part 4 详解 — time-window aggregation

**event-time 滑动窗口**,不是 wall-clock。窗口 = `[now - windowMillis, now]`(**两端闭区间**)。

```
processor = new ProcessorPart4(windowMillis=10)

consume(t=0,  user=a, mid=m1)
consume(t=5,  user=a, mid=m2)
consume(t=5,  user=a, mid=m2)  // dup, ignored
consume(t=11, user=b, mid=m3)
consume(t=15, user=b, mid=m4)

countInWindow(now=15)
  → 窗口 [5, 15] → m2, m3, m4 = 3
countByUserInWindow(now=15)
  → {a: 1 (m2), b: 2 (m3, m4)}

countInWindow(now=20)
  → 窗口 [10, 20] → m3, m4 = 2
```

**实现思路(简版,面试够了)**:把所有 event 留下(已 dedupe),`countInWindow(now)` 时遍历全部 event 看是否落在 `[now - windowMillis, now]`。
**优化讨论(主动提)**:"if events are heavy and queries are frequent, I'd keep events in a `TreeMap<Long, List<Event>>` keyed by `eventTime` and use `subMap(lo, true, hi, true)`."

---

## Part 5 — 并发 consume + partition 路由(Coinbase 必问)

**问题**:单线程 consumer 撑不住吞吐。要把 consume 分发到 N 个 worker,但 Kafka 的核心保证是**单 partition 内有序**——同一 `userId` 的 event 必须被同一个 worker 处理,否则 Part 3 的 per-user 顺序就破了。线程安全地聚合 `totalConsumed` / `consumedByUser`。

**面试官最常问的 4 个 follow-up**:

1. **"为什么不直接 ConcurrentHashMap?"**
   → CHM 的 put/get 线程安全,但 `size()` 弱一致;计数会漂移。更要紧的是它不解决"同 user 同 worker"——你仍然需要在 dispatch 层路由。
2. **"加一个 worker (4 → 5),已有 user 怎么办?"**
   → `hash(userId) % N` 几乎所有 user 都换 worker,buffer / 顺序状态全乱。引出**一致性哈希**或**外部 coordinator (类似 Kafka group coordinator)** 的讨论。Kafka 现实是 rebalance 时所有 partition 短暂停顿。
3. **"99% read, 1% write 的 totalConsumed 怎么优化?"**
   → `LongAdder` 比 `AtomicLong` 在高并发写场景更快(分段累加)。读时聚合一遍。或者每 worker 自己 `int counter`,read 时遍历 (N 很小所以便宜)。
4. **"两个 worker 同时给同一 user 计数会不会错?"**
   → 设计上不会——同 user 同 worker。但 user 计数表是全局共享的 → CHM 或 per-worker shard + read-time 汇总。

**取舍表**:

| 方案 | 吞吐 | 同 user 顺序 | rebalance 代价 |
|------|------|----|----|
| 单线程 | 低 | 天然保证 | 无 |
| N worker + `hash % N` 路由 | 高 | 保证 | 加机器 → 全 user rehash |
| N worker + 一致性哈希 | 高 | 保证 | 加机器 → ~1/N user 迁移 |
| Work-stealing 池 | 高 | **破坏** | 不适合需要顺序的 Kafka |

**自检题**:
- 多线程同时调 `consume(e)`,你的 `consumedByUser` 准吗?最坏漂移多少?
- 关 worker 时 in-flight event 怎么办?(drain vs drop)

---

## Part 6 — 重试 + DLQ(poison pill 不卡 partition)

**问题**:某条消息 payload 解析报错 → consumer 死循环重试 → offset 不前进 → 整个 partition lag 爆炸。生产事故 #1。

**面试官最常问的 4 个 follow-up**:

1. **"重试要不要 backoff?"**
   → 固定间隔太密集打挂下游;指数退避更友好;加 **jitter** 避免雷鸣群(N 个 consumer 同时重试同一个下游)。
2. **"同步重试 vs 异步 retry topic?"**
   → 同步阻塞 partition(简单,但 N 个 retry 全停);异步丢到 retry topic,主流继续。Coinbase 主链路一般异步,延迟队列。
3. **"DLQ 之后呢?"**
   → 另一个 Kafka topic,人工 review / 离线 replay。**不**回原 topic(会污染)。
4. **"瞬态错 vs 永久错怎么分?"**
   → Handler 抛子类异常(`TransientException` vs `PermanentException`)。永久错(schema 错)直接 DLQ 不重试;瞬态(5xx, timeout)才退避重试。

**取舍表**:

| 重试方式 | 复杂度 | 阻塞主流 | 适合场景 |
|---------|-------|---------|---------|
| 不重试,直接 DLQ | 低 | 否 | 下游极少出错 |
| 同步循环重试 | 低 | **是** | 单条不重要 + 下游通常稳 |
| 同步 + 上限 + DLQ(本 Part) | 中 | 短暂 | 大多数业务 |
| 异步 retry topic + delay | 高 | 否 | 高吞吐 / 长退避 |

**自检题**:
- 重试时会不会触发 Part 2 的 dedupe 导致永远跑不到 Handler?(在哪一层 dedupe 决定了答案)
- DLQ 满了怎么办?(报警 + 阻塞 vs 丢弃 + 计数)

---

## Part 7 — 消费滞后 (lag) 监控

**问题**:生产 SRE 看的 #1 指标。Producer 一直在生产,Consumer 跟上没?跟不上要扩容。

**面试官最常问的 4 个 follow-up**:

1. **"条数 lag 还是时间 lag?"**
   → 条数好算(`producerHwm - consumerOffset`);时间更直观(用户感知)但要把 event 的 `eventTime` 留住,且受时钟漂移影响。两个都要。
2. **"lag 为 0 也是异常吗?"**
   → 是。Producer 挂了 / topic 没流量都会让 lag 持续 0 → "心跳" metric 配合检测。
3. **"多 worker 怎么聚合 lag?"**
   → 不要全局锁。每 worker 维护自己的 (user → lastSeq),周期 sample 推到 metric backend。`maxLagUser` 用 `PriorityQueue` / heap。
4. **"P50 vs P99 vs max,怎么选?"**
   → 单值 max 容易被一个热点 user 拉爆。Histogram 看分布:大部分 user lag<100,少数 lag>10000 → 那是热点 partition,可能要拆 key。

**取舍表**:

| 指标 | 计算成本 | 用途 |
|------|---------|------|
| 总 lag (sum) | O(N user) | 整体扩容判断 |
| max lag | O(N) 或 heap | 找热点 user |
| P99 lag | histogram | SLA 报警 |
| 时间 lag (event_time delta) | 需存 eventTime | 用户体验 |

**自检题**:
- producer 发了 100,consumer 处理了 60,但其中第 70 条乱序还没到 —— lag 是 30 还是 40?(取决于 "处理到第几条" 怎么定义,这是 Part 3 / Part 8 也要回答的)
- 报警阈值用绝对值还是相对值?

---

## Part 8 — offset 持久化 + 重启恢复

**问题**:进程崩了重启,从哪一条接着消费?Kafka 自身的 `__consumer_offsets` 决定了语义——**何时 commit** 决定是 at-least-once 还是 at-most-once。

**面试官最常问的 4 个 follow-up**:

1. **"处理前 commit 还是处理后 commit?"**
   → 处理后(at-least-once)+ 下游 idempotent(Part 2 dedupe)是 Coinbase 财务场景的事实标准。处理前(at-most-once)金融业务不接受。
2. **"那 dedupe set 也要持久化吗?"**
   → 是。否则重启后 Part 2 失效,所有 in-flight 消息会被重复处理。常见做法:把 dedupe 集合也写到 OffsetStore,或者改用 **per-user lastProcessedSequence 单调推进**(Part 3 思路)代替 messageId 集合 —— 后者状态空间小,天然持久化友好。
3. **"exactly-once 怎么实现?"**
   → Kafka 0.11+ transactional producer/consumer 把 "业务写 + offset commit" 包成事务。或者业务写 RDBMS,把 offset 和业务记录在同一事务里。本质都是把两个动作原子化。
4. **"checkpoint 多久一次?"**
   → 每条:语义最强,但 IOPS 撑不住;按时间(5s):Kafka 默认 auto-commit,但崩溃丢 5s 内的 commit;按批(N 条):省 IO。Coinbase 真实订单链路接近每条。

**取舍表**:

| Checkpoint 策略 | 吞吐 | 崩溃重复消费量 | 实现复杂度 |
|---|---|---|---|
| 每条 manual commit | 低 | 0–1 条 | 低 |
| 每批 (N 条) commit | 中 | 0–N 条 | 中 |
| 定时 (5s auto-commit) | 高 | 0–5s 之内的 | 极低 |
| Transactional (exactly-once) | 中 | 0 | 高 |

**自检题**:
- 你的 OffsetStore 在 `save()` 中途崩溃,加载时拿到的是不是一致 snapshot?(原子写 / 双 buffer / rename)
- 重启后第一条 event 是 `lastSequence + 1` 还是从 `lastSequence` 开始?(决定了重复消费窗口大小)
- Rebalance 时 worker A → worker B 接手,A 的 checkpoint 没 flush 怎么办?

---

## 怎么练

```bash
cd src/02c-其他算法面经/Coinbase/Kafka-Event-Processing

javac KafkaEventProcessing.java KafkaEventProcessingTest.java

java KafkaEventProcessingTest                # 跑所有 Part
java KafkaEventProcessingTest part1          # 只跑 Part 1
java KafkaEventProcessingTest part1 part3    # 跑指定的几个
```

输出格式:

```
Part 1 SKIPPED (not implemented)
Part 2 SKIPPED (not implemented)
Part 3 SKIPPED (not implemented)
Part 4 SKIPPED (not implemented)

Passed=0  Failed=0  Skipped=4
```

骨架在 [KafkaEventProcessing.java](KafkaEventProcessing.java),测试在 [KafkaEventProcessingTest.java](KafkaEventProcessingTest.java)。每个 Part 在文件里是一段独立的 `public static class`,后缀 `PartN`:

```
PART 1: KafkaEventProcessing.ProcessorPart1   [⚠ 你来写]
PART 2: KafkaEventProcessing.ProcessorPart2   [⚠ 你来写]
PART 3: KafkaEventProcessing.ProcessorPart3   [⚠ 你来写]
PART 4: KafkaEventProcessing.ProcessorPart4   [⚠ 你来写]
PART 5: KafkaEventProcessing.ProcessorPart5            [⚠ 超越面经 follow-up]
PART 6: KafkaEventProcessing.ProcessorPart6 + Handler  [⚠ 超越面经 follow-up]
PART 7: KafkaEventProcessing.ProcessorPart7            [⚠ 超越面经 follow-up]
PART 8: KafkaEventProcessing.ProcessorPart8 + Store    [⚠ 超越面经 follow-up]
```

**为什么独立 class 而不是继承同一个基类?** 真实面试是渐进的——做完 Part 1 才会让你看 Part 2。这种切片结构让你能专注当前 Part 而不破坏已完成的部分;做完之后还能讨论 "if I had an `IdempotentEventStore` base, parts 2/3/4 could all extend it"——这种 trade-off 讨论是面试加分点,但不是必做项。
