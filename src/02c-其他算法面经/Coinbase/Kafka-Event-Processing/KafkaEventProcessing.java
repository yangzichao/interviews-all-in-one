import java.util.*;

/**
 * Coinbase interview practice — Kafka Event Processing  (8 parts).
 *
 * ════════════════════════════════════════════════════════════════════════
 *  背景故事 (BACKGROUND) —— 读这里就够入手, 不需要懂 Kafka 内部
 * ════════════════════════════════════════════════════════════════════════
 *
 *  想象一个交易平台 (比如 Coinbase)。用户每下一单、每充值一笔, 系统就产生
 *  一条 "事件 (event)"。这些事件不是直接写进数据库, 而是先丢进一个叫
 *  Kafka 的消息队列里, 排成一条长长的流。
 *
 *      [下单服务] ──产生事件──▶  Kafka 队列  ──▶  [你的程序: consumer]
 *        producer                (一条事件流)        逐条读出来处理
 *
 *  你要写的就是右边那个 "consumer": 一个一个把事件读出来 (这个动作叫
 *  consume / 消费), 做点处理 —— 计数、去重、排序、统计…… 听起来简单,
 *  但真实世界的消息队列有几个 "不完美", 正是这道题层层加码要应对的:
 *
 *    ① 重复投递: Kafka 承诺 "至少投一次 (at-least-once)" —— 为了不丢消息,
 *       它宁可把同一条消息投给你两次。所以同一条事件你可能 consume 到多次,
 *       要自己识别并丢掉重复的 (Part 2)。
 *
 *    ② 乱序到达: 网络抖动会让事件不按产生顺序到。producer 先发的可能后到,
 *       你却必须按原始顺序处理 (Part 3)。
 *
 *    ③ 高吞吐 / 故障 / 重启: 流量大要多线程 (Part 5)、坏消息不能卡死整条流
 *       (Part 6)、要监控自己有没有处理得过来 (Part 7)、程序崩了重启得知道
 *       从哪接着读 (Part 8)。
 *
 *  几个术语 (后面注释会用到, 先混个脸熟):
 *    · producer  : 产生事件、往队列里写的一方
 *    · consumer  : 你, 从队列里把事件读出来处理的一方
 *    · consume   : "消费" 一条事件 = 把它读出来交给你的逻辑
 *    · messageId : producer 给每条逻辑消息打的唯一编号 (用来认出重复)
 *    · sequence  : 同一用户的事件被 producer 编的递增流水号 1,2,3… (用来排序)
 *    · offset    : consumer "已经读到第几条" 的进度标记 (Part 8 要持久化它)
 *
 * ════════════════════════════════════════════════════════════════════════
 *
 * 题面直接写在每个 Part 上方 —— 读代码就能读题, 不用切到别处。
 * 坑点 / 取舍 / follow-up 答案在 README.md (含剧透), 练的时候别看。
 *
 * 逐步加约束; 面试时一次只给一个 Part, 做完再放下一个。
 * 每个 Part 是独立的 class, 后缀 PartN —— 让你能专注当前 Part 而不破坏已完成的部分。
 */
public class KafkaEventProcessing {

    // ====================================================================
    // 通用 record  —  贯穿所有 Part 的 Event schema
    // ====================================================================
    //
    //   messageId : producer 端给每条逻辑消息的唯一 id (Part 2 起用于 dedupe)
    //   userId    : 事件属于哪个用户 (Part 1 起用于分组计数)
    //   sequence  : 该 user 下严格递增的逻辑序号 (Part 3 起用于重排序)
    //   eventTime : 事件自身的时间戳, ms (Part 4 用于窗口)
    //   payload   : 业务数据本体
    //
    // 用不到的字段在该 Part 可以忽略。

    public static record Event(String messageId, String userId,
                               long sequence, long eventTime, String payload) {}

    // ====================================================================
    // PART 1  —  basic consume + count                              [⚠ TODO]
    // ====================================================================
    // 场景: 队列里的事件一条条来, 你先做最简单的事 —— 收下来, 数一数:
    //       一共处理了多少条? 每个用户各多少条?
    //       (好比一个收银台先统计 "今天来了几位客人、每位下了几单"。)
    //
    // 这一 Part 不去重、不排序、不窗口, 纯热身, 建立 baseline。
    //
    //   consume(e1, alice); consume(e2, bob); consume(e3, alice);
    //   totalConsumed()          → 3
    //   consumedByUser("alice")  → 2
    //   consumedByUser("zz")     → 0   // 没见过的 user 返回 0

    public static class ProcessorPart1 {

        private Map<String, Integer> userCount;
        public ProcessorPart1() {
            userCount = new HashMap<>();
        }

        public void consume(Event event) {
            String userId = event.userId();
            userCount.put(userId, userCount.getOrDefault(userId, 0) + 1);
        }

        public int totalConsumed() {
            int count = 0;
            for (String userId : userCount.keySet()) {
                count += userCount.get(userId);
            }
            return count;
        }

        public int consumedByUser(String userId) {
            return userCount.getOrDefault(userId, 0);
        }
    }

    // ====================================================================
    // PART 2  —  idempotent dedupe by messageId                     [⚠ TODO]
    // ====================================================================
    // 场景: 前面背景说过, Kafka 为了不丢消息会把同一条事件投给你多次
    //       (at-least-once)。如果这条事件是 "用户充值 $100", 你处理两遍就
    //       给人记了 $200 —— 灾难。所以 consumer 必须自己认出重复并丢掉。
    //
    //       怎么认? producer 给每条逻辑消息打了唯一的 messageId。同一个
    //       messageId 第二次及以后到达, 一律忽略。计数只反映 distinct messageId。
    //       ("idempotent / 幂等" 就是指: 同一条消息处理一次和处理多次效果相同。)
    //
    //   consume(mid=m1, user=a); consume(mid=m1, user=a);  // 第二个忽略
    //   consume(mid=m2, user=a);
    //   totalConsumed()         → 2   // 只数 distinct: m1, m2
    //   consumedByUser("a")     → 2

    public static class ProcessorPart2 {

        private Set<String> consumed;
        private Map<String, Integer> userCount;
        public ProcessorPart2() {
            this.consumed = new HashSet<>();
            this.userCount = new HashMap<>();
        }

        public void consume(Event event) {
            if (consumed.contains(event.messageId())) return;
            String messageId = event.messageId();
            String userId = event.userId();
            consumed.add(messageId);
            userCount.put(userId, userCount.getOrDefault(userId, 0) + 1);

        }

        public int totalConsumed() {
            int count = 0;
            for (String userId : userCount.keySet()) {
                count += userCount.get(userId);
            }
            return count;
        }

        public int consumedByUser(String userId) {
            return userCount.getOrDefault(userId, 0);
        }
    }

    // ====================================================================
    // PART 3  —  ordered output by sequence                         [⚠ TODO]
    // ====================================================================
    // 场景: producer 给同一用户的事件编了递增流水号 sequence = 1,2,3,…
    //       (比如一个用户的下单步骤必须按顺序处理)。但网络会让它们乱序到达:
    //       你可能先收到 3, 再收到 1。你不能一收到就处理 —— 必须按 1,2,3 的
    //       原始顺序往下游 "输出 (emit)"。还没轮到的, 先攒着, 等缺口补上再一起放。
    //
    //       (类比: 你在拼一套带编号的拼图, 必须从第 1 块开始按号摆出去; 手里
    //        拿到第 5 块但第 2 块还没来, 就只能先攥着第 5 块。)
    //
    // 规则 (构造时给定要 track 的 userId 和起始 startSequence, 只处理这一个 user):
    //   设 expected = 下一个该输出的 sequence (从 startSequence 起)。
    //   - userId 不匹配           → 不属于本 processor, 忽略
    //   - seq < expected         → 早就输出过的旧消息, 忽略
    //   - seq == expected        → 输出它; 再看后面 expected+1,+2… 连续到齐的一并输出
    //   - seq > expected         → 还没轮到, 先存着, 本次返回空
    //   - 同一个 seq 第二次到达     → 重复, 忽略
    //
    // consume() 返回 "本次调用能按序输出的有序列表" (可能空, 也可能一次输出很多条)。

    public static class ProcessorPart3 {
        private final Map<Long, Event> events;
        private final String userId;
        private long startSequence;
        public ProcessorPart3(String userId, long startSequence) {
            this.events = new HashMap<>();
            this.userId = userId;
            this.startSequence = startSequence;

        }

        public List<Event> consume(Event event) {
            String eventMessageId = event.messageId();
            String eventUserId = event.userId();
            long eventSequence = event.sequence();
            if (!eventUserId.equals(userId) 
                || eventSequence < startSequence) 
            {
                return new ArrayList<>();
            }
            
            List<Event> ans = new ArrayList<>();
            events.put(eventSequence, event);
            long index = this.startSequence;
            while (events.containsKey(index)) {
                ans.add(events.get(index));
                events.remove(index);
                index++;
            }
            this.startSequence = index;
            return ans;
        }
    }

    // ====================================================================
    // PART 4  —  time-window aggregation                            [⚠ TODO]
    // ====================================================================
    // 场景: 风控 / 监控常问 "最近 N 毫秒内发生了多少笔?" —— 这就是滑动窗口。
    //       注意时间用的是事件自带的 eventTime (它真实发生的时刻), 不是你处理
    //       它的墙上时钟 —— 因为事件会迟到, 用墙上时钟会算错。
    //
    //       给定窗口宽度 windowMillis, 查询某个时刻 now 时, 统计落在
    //       [now - windowMillis, now] (两端都闭) 内的事件。事件仍按 messageId 去重。
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

        private List<Event> list;
        private long windowMillis;
        public ProcessorPart4(long windowMillis) {
            this.windowMillis = windowMillis;
            this.list = new ArrayList<>();
        }

        public void consume(Event event) {
            
        }

        public int countInWindow(long nowEventTime) {
            throw new UnsupportedOperationException("ProcessorPart4.countInWindow: not implemented");
        }

        public Map<String, Integer> countByUserInWindow(long nowEventTime) {
            throw new UnsupportedOperationException("ProcessorPart4.countByUserInWindow: not implemented");
        }
    }

    // ====================================================================
    // PART 5  —  并发 consume + partition 路由                       [⚠ TODO]
    // ====================================================================
    // 场景: 流量上来了, 一个线程一条条读太慢。开 numWorkers 个 worker 线程
    //       并行处理。难点是 Part 3 要求 "同一用户按序", 所以同一个用户的事件
    //       不能被两个 worker 抢着处理 —— 必须始终落到同一个 worker。
    //
    // 约束:
    //   - 同一个 userId 的 event 必须由同一个 worker 处理 (否则 Part 3 的
    //     per-user 顺序会被破坏)。
    //   - 仍然按 messageId 去重。
    //   - 所有读 API (totalConsumed / consumedByUser) 必须线程安全。
    //
    // (坑点 / rebalance / 一致性哈希等 follow-up 讨论见 README。)

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
    // 场景: 偶尔会有一条 "毒丸 (poison pill)" 消息 —— payload 解析报错, 或它
    //       依赖的下游服务正好挂了, 处理它就抛异常。如果你死磕重试, 整条流就被
    //       它堵死, 后面的好消息全卡住。要做到: 坏消息重试几次还不行, 就把它
    //       挪到一个 "死信队列 (DLQ)" 单独存着, 主流继续往下走。
    //
    // 真正的业务处理委托给一个外部 Handler, 它可能抛异常。要求:
    //   - 失败要有限次重试。
    //   - 重试到上限仍失败 → 写入 DLQ (dead-letter queue), 主流继续前进。
    //   - 仍按 messageId 去重。
    //
    // (backoff / 同步 vs 异步重试 / 错误分类等 follow-up 讨论见 README。)

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
    // 场景: 运维最关心一个问题 —— "consumer 追得上 producer 吗?" 如果 producer
    //       每秒产 1000 条、你只处理 800 条, 积压 (lag) 会越滚越大, 该报警 / 扩容了。
    //       "lag" 就是 producer 已经产到第几条、减去你已经处理到第几条的差。
    //
    // 外部 admin channel 会持续告诉你每个 user 在 producer 端已生产到第几条
    // (high-water mark / 高水位线)。consumer 要随时报出:
    //     lag = producer 已生产到的 sequence  −  consumer 已处理到的 sequence
    //
    // 接口: recordProducerHighWatermark(userId, seq); lag(userId); totalLag();
    //       maxLagUser()。
    //
    // (条数 lag vs 时间 lag / 聚合开销 / P99 等 follow-up 讨论见 README。)

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
    // 场景: 你的程序总会重启 (发版、崩溃、机器换)。重启后你怎么知道 "上次读到
    //       哪条了"? 如果忘了, 要么从头重读 (大量重复), 要么从队尾开始 (丢消息)。
    //       所以要把进度 (每个用户处理到第几条 = offset) 时不时存到外部 store;
    //       重启时读回来, 从下一条接着读。
    //
    // 关键状态 (每个 user 处理到的 lastProcessedSequence) 要周期性 checkpoint
    // 到 OffsetStore。进程重启时从 store 加载, 从 lastProcessedSequence + 1
    // 继续消费。
    //
    // 接口: OffsetStorePart8 (save / load); ProcessorPart8(store) 构造;
    //       checkpoint() 触发持久化; lastSequenceFor(userId) 查恢复后的进度。
    //
    // (at-least-once vs exactly-once / checkpoint 频率 / dedupe 状态持久化等
    //  follow-up 讨论见 README。)

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
