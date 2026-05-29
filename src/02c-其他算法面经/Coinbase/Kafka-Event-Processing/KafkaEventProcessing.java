import java.util.*;

/**
 * 4-part Coinbase interview practice — Kafka Event Processing.
 *
 * 每个 Part 是独立的 class,后缀 PartN。先无脑独立写,做完再讨论抽公共逻辑。
 *
 * 这不是产品代码,是练习代码 —— 让你能专注当前 Part 而不破坏已完成的部分。
 */
public class KafkaEventProcessing {

    // ====================================================================
    // 通用 record  —  贯穿 4 个 Part 的 Event schema
    // ====================================================================
    //
    //   messageId : producer 端的唯一 id (Part 2 起用于 dedupe)
    //   userId    : 哪个用户 (Part 1 起用于分组计数)
    //   sequence  : 该 user 严格递增的逻辑序号 (Part 3 起用于重排序)
    //   eventTime : event 自身的时间戳, ms (Part 4 用于窗口)
    //   payload   : 业务数据本体

    public static record Event(String messageId, String userId,
                               long sequence, long eventTime, String payload) {}

    // ====================================================================
    // PART 1  —  basic consume + count                              [⚠ TODO]
    // ====================================================================
    // 不去重、不排序、不窗口. 就是热身.
    //
    //   consume(e1, alice); consume(e2, bob); consume(e3, alice);
    //   totalConsumed()          → 3
    //   consumedByUser("alice")  → 2
    //   consumedByUser("zz")     → 0

    public static class ProcessorPart1 {

        public ProcessorPart1() {
            throw new UnsupportedOperationException("ProcessorPart1: not implemented");
        }

        public void consume(Event event) {
            throw new UnsupportedOperationException("ProcessorPart1.consume: not implemented");
        }

        public int totalConsumed() {
            throw new UnsupportedOperationException("ProcessorPart1.totalConsumed: not implemented");
        }

        public int consumedByUser(String userId) {
            throw new UnsupportedOperationException("ProcessorPart1.consumedByUser: not implemented");
        }
    }

    // ====================================================================
    // PART 2  —  idempotent dedupe by messageId                     [⚠ TODO]
    // ====================================================================
    // Kafka 是 at-least-once. 同 messageId 第二次来必须忽略.
    // 计数只反映 distinct messageId.
    //
    //   consume(mid=m1, user=a); consume(mid=m1, user=a);  // 第二个忽略
    //   consume(mid=m2, user=a);
    //   totalConsumed()         → 2
    //   consumedByUser("a")     → 2

    public static class ProcessorPart2 {

        public ProcessorPart2() {
            throw new UnsupportedOperationException("ProcessorPart2: not implemented");
        }

        public void consume(Event event) {
            throw new UnsupportedOperationException("ProcessorPart2.consume: not implemented");
        }

        public int totalConsumed() {
            throw new UnsupportedOperationException("ProcessorPart2.totalConsumed: not implemented");
        }

        public int consumedByUser(String userId) {
            throw new UnsupportedOperationException("ProcessorPart2.consumedByUser: not implemented");
        }
    }

    // ====================================================================
    // PART 3  —  ordered output by sequence                         [⚠ TODO]
    // ====================================================================
    // 只 track 一个 user. expected 从 startSequence 起.
    // 乱序来的先 buffer, 收到 expected 时一并 drain.
    //   - seq < expected         → 已处理过, 忽略
    //   - seq == expected        → emit, 再 drain buffer
    //   - seq > expected         → buffer 起来
    //   - 同 seq 来第二次          → 忽略 (dup)
    //   - userId 不匹配           → 忽略
    //
    // consume() 返回这次能 emit 的有序列表 (可能空, 可能很长).

    public static class ProcessorPart3 {

        public ProcessorPart3(String userId, long startSequence) {
            throw new UnsupportedOperationException("ProcessorPart3: not implemented");
        }

        public List<Event> consume(Event event) {
            throw new UnsupportedOperationException("ProcessorPart3.consume: not implemented");
        }
    }

    // ====================================================================
    // PART 4  —  time-window aggregation                            [⚠ TODO]
    // ====================================================================
    // 滑动 event-time 窗口 [now - windowMillis, now] (两端闭区间).
    // consume() 仍然按 messageId dedupe.
    //
    //   processor = new ProcessorPart4(10)
    //   consume(t=0,  user=a, mid=m1)
    //   consume(t=5,  user=a, mid=m2)
    //   consume(t=11, user=b, mid=m3)
    //   consume(t=15, user=b, mid=m4)
    //   countInWindow(15)        → 3  (window [5,15]: m2, m3, m4)
    //   countByUserInWindow(15)  → {a:1, b:2}
    //   countInWindow(20)        → 2  (window [10,20]: m3, m4)

    public static class ProcessorPart4 {

        public ProcessorPart4(long windowMillis) {
            throw new UnsupportedOperationException("ProcessorPart4: not implemented");
        }

        public void consume(Event event) {
            throw new UnsupportedOperationException("ProcessorPart4.consume: not implemented");
        }

        public int countInWindow(long nowEventTime) {
            throw new UnsupportedOperationException("ProcessorPart4.countInWindow: not implemented");
        }

        public Map<String, Integer> countByUserInWindow(long nowEventTime) {
            throw new UnsupportedOperationException("ProcessorPart4.countByUserInWindow: not implemented");
        }
    }

    // ====================================================================
    // PART 5  —  并发 consume + partition 分配                       [⚠ TODO]
    // ====================================================================
    // 与 Part 4 比:
    //   同: Event schema 不变, 仍然按 messageId dedupe
    //   变: 多个 worker 线程同时 consume; 同一 user 的 event 必须由同一个
    //       worker 处理 (才能保留 Part 3 的 per-user 顺序)
    //   新: ProcessorPart5(int numWorkers); 内部用 hash(userId) % numWorkers
    //       路由到 worker. 所有 read API (totalConsumed / consumedByUser)
    //       必须是线程安全的.
    //
    // 问题陈述:
    //   单线程 consumer 撑不住吞吐 —— 上 N 个 worker. 但 Kafka 的保证是
    //   "单 partition 内有序", 跨 partition 不保证. Producer 端用 key
    //   (这里是 userId) 决定 partition, 让同 user 的 event 落到同一 partition.
    //   你的 consumer 端也要保持这个不变量: 同 user → 同 worker.
    //
    // 面试要讨论的取舍 (Coinbase 必问的方向):
    //   1. 一把大锁 vs 每 worker 一把锁: 后者吞吐线性增长, 但 totalConsumed
    //      要遍历所有 worker (或者用 LongAdder).
    //   2. ConcurrentHashMap 看似简单, 但 size() 不是强一致, 计数会漂移.
    //   3. 同 user 同 worker 怎么实现: 入站 dispatch queue (ArrayBlockingQueue) 还是
    //      直接 hash 到 worker 的本地 map (然后调用方自己保证不跨线程)?
    //   4. Rebalance: 加一个 worker 时, hash(userId) % N 变化, 几乎所有 user 都要
    //      搬家 —— 跟 In-Memory-Database Part 8 的分片是同一个问题. 这里要不要
    //      上一致性哈希? (面试官最爱追问这个.)
    //
    // 你要写的: 构造 N 个 worker (内部状态), 路由 dispatch, 线程安全聚合.

    public static class ProcessorPart5 {

        public ProcessorPart5(int numWorkers) {
            throw new UnsupportedOperationException("ProcessorPart5: not implemented");
        }

        // 由调用线程直接调; 内部决定路由到哪个 worker.
        public void consume(Event event) {
            throw new UnsupportedOperationException("ProcessorPart5.consume: not implemented");
        }

        // 跨 worker 聚合; 必须线程安全.
        public int totalConsumed() {
            throw new UnsupportedOperationException("ProcessorPart5.totalConsumed: not implemented");
        }

        public int consumedByUser(String userId) {
            throw new UnsupportedOperationException("ProcessorPart5.consumedByUser: not implemented");
        }

        // 优雅关闭 (drain in-flight 之后退出).
        public void shutdown() {
            throw new UnsupportedOperationException("ProcessorPart5.shutdown: not implemented");
        }
    }

    // ====================================================================
    // PART 6  —  poison-pill 重试 + DLQ                              [⚠ TODO]
    // ====================================================================
    // 与 Part 5 比:
    //   同: dedupe by messageId, multi-worker 不变
    //   变: consume(e) 的真正业务处理交给一个外部 Handler, 它可能抛异常 (恶意 payload
    //       / 下游服务挂了). 不能因为一条坏消息卡死整个 partition.
    //   新: 构造时传入 Handler + maxRetries; 失败到顶就进 DLQ (dead-letter queue).
    //
    // 问题陈述:
    //   生产里最常见的事故: 某个 messageId 的 payload 解析报错, consumer 死循环
    //   重试, offset 不前进, lag 爆炸. 解决方案:
    //     - 重试有限次 (带 backoff)
    //     - 顶到上限后写入 DLQ, 主流 offset 继续前进
    //     - DLQ 由人 (或离线任务) 后审, 不阻塞主链路.
    //
    // 面试要讨论的取舍:
    //   1. 重试在同步路径 (consume() 阻塞重试) vs 异步路径 (失败后丢到 retry topic
    //      / delay queue): 同步简单但阻塞 partition; 异步要额外存储和调度.
    //   2. Backoff 策略: 固定间隔 vs 指数退避 vs 抖动 (jitter, 避免雷鸣群).
    //   3. DLQ 是另一个 Kafka topic 还是本地存储? 真线上几乎都是另一个 topic, 因为
    //      要复用 Kafka 的持久化和重新消费能力.
    //   4. 区分错误类型: 永久性 (payload schema 错) 直接进 DLQ, 不重试;
    //      瞬态 (下游 5xx) 重试. 怎么区分? Handler 抛不同子类异常.
    //   5. Idempotency: 重试时不能再触发 Part 2 的 dedupe (否则重试根本不起作用).
    //      在哪一层 dedupe? messageId 进入时 dedupe, 还是处理成功后 dedupe?
    //
    // 你要写的: ProcessorPart6 — 包装 Handler, 重试到上限, 失败入 DLQ.

    public interface EventHandlerPart6 {
        // 抛任何异常都算失败, 触发重试.
        void handle(Event event) throws Exception;
    }

    public static class ProcessorPart6 {

        public ProcessorPart6(EventHandlerPart6 handler, int maxRetries) {
            throw new UnsupportedOperationException("ProcessorPart6: not implemented");
        }

        public void consume(Event event) {
            throw new UnsupportedOperationException("ProcessorPart6.consume: not implemented");
        }

        // 成功处理过的 (Handler 没抛) 计数.
        public int successCount() {
            throw new UnsupportedOperationException("ProcessorPart6.successCount: not implemented");
        }

        // 重试到顶仍失败, 进 DLQ 的 event 列表.
        public List<Event> deadLetterQueue() {
            throw new UnsupportedOperationException("ProcessorPart6.deadLetterQueue: not implemented");
        }

        // 累计重试次数 (跨所有 event).
        public int totalRetries() {
            throw new UnsupportedOperationException("ProcessorPart6.totalRetries: not implemented");
        }
    }

    // ====================================================================
    // PART 7  —  消费滞后监控 (lag)                                   [⚠ TODO]
    // ====================================================================
    // 与 Part 6 比:
    //   同: dedupe + 重试 + DLQ 的语义可继续沿用
    //   变: 在 producer 端会持续打"已生产到 sequence=X" 的 high-water mark,
    //       consumer 要 track "已处理到 sequence=Y", 实时报 lag = X - Y.
    //   新: recordProducerHighWatermark(userId, seq); lag(userId); totalLag().
    //
    // 问题陈述:
    //   线上 SRE 最关心的 Kafka 指标就是 "consumer lag". 它告诉你 consumer 是不是
    //   跟上了生产速度, 是不是要扩容. 假设我们有一个 admin channel 持续告诉我们
    //   每个 user 的 latest produced sequence (high-water mark), consumer 要
    //   随时报出 lag.
    //
    // 面试要讨论的取舍 (Coinbase 真实运营场景):
    //   1. Lag 的定义: 条数 lag (seq 差值) 还是时间 lag (event_time 差值)?
    //      条数好算, 时间更直观 (用户能感受到的延迟); 时间 lag 受时钟漂移影响.
    //   2. 报警阈值: 绝对值 (lag > 10000) 还是相对值 (lag > 1min)? 大流量场景
    //      用绝对值, 报警风暴.
    //   3. Lag 的反向用途: lag 持续为 0 也可能是异常 (producer 挂了).
    //   4. 多 worker 下怎么聚合 lag? 全局锁会拖垮路径, 用 LongAdder + 周期 sample.
    //   5. Histogram vs 单值: P50/P99 的 lag 更有价值 (大部分 user lag 低,
    //      少数 user lag 高就是热点 partition).
    //
    // 这个 Part 偏 "添加观测性", 不强求改业务逻辑.

    public static class ProcessorPart7 {

        public ProcessorPart7() {
            throw new UnsupportedOperationException("ProcessorPart7: not implemented");
        }

        public void consume(Event event) {
            throw new UnsupportedOperationException("ProcessorPart7.consume: not implemented");
        }

        // 外部 admin channel 告诉 consumer 该 user 在 producer 端已经写到第几条.
        public void recordProducerHighWatermark(String userId, long latestSequence) {
            throw new UnsupportedOperationException("ProcessorPart7.recordProducerHighWatermark: not implemented");
        }

        // 单个 user 的 lag = producer high-water - consumer 已处理到的 sequence.
        public long lag(String userId) {
            throw new UnsupportedOperationException("ProcessorPart7.lag: not implemented");
        }

        // 所有 user 的 lag 之和.
        public long totalLag() {
            throw new UnsupportedOperationException("ProcessorPart7.totalLag: not implemented");
        }

        // 当前 lag 最大的 user (热点 partition 排障用).
        public String maxLagUser() {
            throw new UnsupportedOperationException("ProcessorPart7.maxLagUser: not implemented");
        }
    }

    // ====================================================================
    // PART 8  —  offset 持久化 + 重启恢复                             [⚠ TODO]
    // ====================================================================
    // 与 Part 7 比:
    //   同: 内存中的 dedupe / per-user sequence track 不变
    //   变: 关键状态 (per-user lastProcessedSequence + seen messageIds 的最近窗口)
    //       周期性 checkpoint 到 OffsetStore. 进程重启时从 OffsetStore 加载,
    //       从 lastProcessedSequence + 1 继续消费.
    //   新: OffsetStore 接口; ProcessorPart8(store) 构造; checkpoint() 触发持久化;
    //       静态恢复方法 restoreFrom(store).
    //
    // 问题陈述:
    //   Kafka 自己提供 __consumer_offsets, 但语义上"消费完成"在哪一点 commit 决定了
    //   at-least-once vs at-most-once:
    //     - 处理前 commit: 处理中崩溃 → 丢消息 (at-most-once)
    //     - 处理后 commit: commit 前崩溃 → 重复消费 (at-least-once, 配合 Part 2 dedupe)
    //   Coinbase 财务场景必须是后者. 但 "dedupe set" 也得持久化, 否则重启后
    //   Part 2 的 dedupe 失效, 重复消息会被重新处理.
    //
    // 面试要讨论的取舍:
    //   1. Checkpoint 频率: 每条都 commit (慢) vs 批量 (省 IO 但崩溃丢更多) vs
    //      按时间 (固定开销). Kafka client 默认 5s auto-commit, 但很多生产环境
    //      改成 manual.
    //   2. Exactly-once 怎么做? Kafka 0.11+ 的 transactional producer/consumer
    //      把 "处理 + offset commit" 包成事务. 没事务的话, idempotent handler
    //      (Part 6 + Part 2 dedupe) 是务实方案.
    //   3. Dedupe set 持续增长 → 用 TTL bloom filter, 或者只保留最近 N 个
    //      messageId, 或者按 sequence 单调推进 (Part 3 思路) 替代 messageId 集合.
    //   4. OffsetStore 选型: Kafka 自身 / 外部 KV (Redis, etcd) / RDBMS.
    //      和业务写在同一事务里 (RDBMS) 是 exactly-once 的本质.
    //   5. Rebalance + checkpoint: 一个 partition 从 worker A 迁到 worker B,
    //      A 要先 flush checkpoint, B 才能安全接管.
    //
    // 不强求方法完整实现, 主要是设计讨论. 给一个最小可演示的入口:

    public interface OffsetStorePart8 {
        // 把 (userId -> lastProcessedSequence) 全表写下去 (原子语义由实现保证).
        void save(Map<String, Long> snapshot);
        // 重启时加载; 没有就返回空 map.
        Map<String, Long> load();
    }

    public static class ProcessorPart8 {

        public ProcessorPart8(OffsetStorePart8 store) {
            throw new UnsupportedOperationException("ProcessorPart8: not implemented");
        }

        public void consume(Event event) {
            throw new UnsupportedOperationException("ProcessorPart8.consume: not implemented");
        }

        // 把当前 per-user lastProcessedSequence flush 到 store. 真线上由后台
        // 调度器周期触发, 这里手动调便于测试.
        public void checkpoint() {
            throw new UnsupportedOperationException("ProcessorPart8.checkpoint: not implemented");
        }

        // 重启后从 store 恢复. 调用方负责调 checkpoint() 然后再 new ProcessorPart8(store).
        public long lastSequenceFor(String userId) {
            throw new UnsupportedOperationException("ProcessorPart8.lastSequenceFor: not implemented");
        }
    }
}
