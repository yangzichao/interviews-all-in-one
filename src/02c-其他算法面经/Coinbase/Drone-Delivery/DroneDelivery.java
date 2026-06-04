import java.util.*;

/**
 * Coinbase interview practice — Drone Delivery (8 parts).
 *
 * ════════════════════════════════════════════════════════════════════════
 *  背景故事 (BACKGROUND) —— 读这里就够入手, 不需要任何配送/无人机背景
 * ════════════════════════════════════════════════════════════════════════
 *
 *  设想一条**笔直的送货路线**, 抽象成一根数轴: 仓库在原点 0, 收件人在 target.
 *  路上散落着若干**充电站 / 起降点 (station)**, 位置用坐标表示, 升序排列。
 *
 *      0 ────●──────────●────────────▶ target
 *      仓库  station    station       收件人
 *
 *  一名快递员从 0 出发往 target 走。两种移动方式:
 *    · walk (步行): 自己走, 1 单位距离算 1 单位 walk 成本 —— 我们要最小化的就是它。
 *    · fly  (无人机): 走到某个 station, 那里有无人机, 可以**免费**飞一段 (飞的距离
 *                     不计入 walk 成本), 但一次最多飞 range 这么远, 落地后接着走。
 *
 *  起点 0 本身**不是 station** (Part 1-2 没有无人机), 所以一开始必须步行到第一个
 *  station 才有飞机可用。核心目标: 把这趟配送的**步行总距离**降到最小。
 *
 *  题目逐 Part 加码: 单程 → 多包裹 → 每个 station 飞行距离不同 → 装箱 (背包) →
 *  多无人机并发抢单 → 在线动态重规划 → 故障接管 → 地理分片调度。后面几个 Part
 *  从"算几何距离"升级成"设计一个简化版 Uber/DoorDash 调度系统"。
 *
 *  术语 (后面注释会用到, 先混个脸熟):
 *    · target   : 收件人坐标, 即本趟要到达的终点 (整条路线长度)
 *    · station  : 路上的起降点坐标; 在这里可以让无人机带你飞一段
 *    · range    : 无人机一次飞行能覆盖的最大距离
 *    · walk     : 步行距离 = 成本, 所有 Part 求的都是"最小步行距离"
 *    · fly      : 无人机飞行段, 不计入 walk 成本 (相当于免费位移)
 *    · order    : 一个待配送的包裹 (Part 5 起, 多无人机抢的就是 order)
 *    · drone    : 无人机 / 也指 Part 5 起并发抢单的一个工作线程
 *    · dispatcher / planner : 把 order 分配给 drone 的调度器
 *
 * ════════════════════════════════════════════════════════════════════════
 *
 * 题面直接写在每个 Part 上方 —— 读代码就能读题, 不用切到别处。
 *
 * 每个 Part 是独立的 class, 后缀 PartN. 先无脑独立写, 做完再讨论抽公共逻辑.
 * 这不是产品代码, 是练习代码 —— 让你能专注当前 Part 而不破坏已完成的部分.
 *
 * 逐步加约束; 面试时一次只给一个 Part, 做完再放下一个。
 */
public class DroneDelivery {

    // ====================================================================
    // PART 1  —  Single Drone, Fixed Range                          [⚠ TODO]
    // ====================================================================
    // 场景: 一趟配送, 全程只有一种无人机, 飞行距离固定为 range. 求最小步行距离。
    //
    // 规则:
    //   - 起点 0 不是 station (那里没飞机), 所以 0→stations[0] 这一段只能 walk。
    //   - 到了某个 station 就可以起飞, 一次最多飞 range; 落地后继续往前 (走或再到
    //     下一个 station 起飞)。飞行段不计 walk 成本。
    //   - stations 升序给出。返回值是 long (步行总距离)。
    //
    // walkDistance(target, stations, range) -> long
    //
    // 例 (摘自测试):
    //   target=20, stations=[5,15], range=10
    //     → walk 0→5 (5), fly 5→15, walk 15→20 (5). 总 = 10
    //   target=20, stations=[],     range=10  → 没飞机, 全程步行 = 20
    //   target=10, stations=[5],    range=10  → walk 0→5 (5), fly 5→10. 总 = 5
    //   target=30, stations=[5,15], range=5
    //     → walk 0→5, fly 5→10, walk 10→15, fly 15→20, walk 20→30. 总 = 20
    //   target=0,  stations=[],     range=10  → 0 (终点即起点)
    //   target=100, stations=[50],  range=1000 → 必须先走到 50, 再一飞到底. 总 = 50

    public static class DronePart1 {
        public static long walkDistance(int target, int[] stations, int range) {
            throw new UnsupportedOperationException("DronePart1.walkDistance: not implemented");
        }
    }

    // ====================================================================
    // PART 2  —  Multiple Packages                                  [⚠ TODO]
    // ====================================================================
    // 场景: 同一组 station / 同一种无人机, 现在要送多个包裹 (多个 target)。
    //       每个包裹都是一趟独立的、从仓库 0 重新出发的配送 —— 没有"顺路捎带"
    //       这种优化。求所有配送的步行距离之和。
    //
    // 与 Part 1 比: stations / range / 单趟规则都不变; 只是把单个 target 换成数组。
    //
    // totalWalkDistance(targets, stations, range) -> long
    //
    // 例 (摘自测试):
    //   targets=[10,20], stations=[5,15], range=10
    //     → 送 10: walk 5 + fly 5→10        = 5
    //       送 20: walk 5 + fly 5→15 + walk 5 = 10
    //       合计 = 15
    //   targets=[],   stations=[5,15], range=10 → 0
    //   targets=[20], stations=[5,15], range=10 → 10 (单个 target 等同 Part 1)

    public static class DronePart2 {
        public static long totalWalkDistance(int[] targets, int[] stations, int range) {
            throw new UnsupportedOperationException("DronePart2.totalWalkDistance: not implemented");
        }
    }

    // ====================================================================
    // PART 3  —  Per-Drone Range                                    [⚠ TODO]
    // ====================================================================
    // 场景: 现在每个 station 自带的无人机飞行距离各不相同, 而且起点也可能自带
    //       一架无人机。你要在"哪几个 station 起飞、哪些直接走过去"之间做选择,
    //       使总步行距离最小。
    //
    // 规则:
    //   - ranges[i] = 在 stations[i] 起飞能飞的距离 (与 stations 一一对应)。
    //   - globalStartRange = 起点 0 自带无人机的飞行距离; 没有就传 0。
    //   - 不强制每个 station 都起飞 —— 可以跳过某个 station 直接步行经过它。
    //   - 落地点不必正好是某个 station (飞到半路落地继续走也行)。
    //
    // walkDistance(target, stations, ranges, globalStartRange) -> long
    //
    // 例 (摘自测试):
    //   target=10, stations=[5],  ranges=[5], globalStartRange=0
    //     → walk 0→5 (5), fly 5→10. 总 = 5
    //   target=10, stations=[],   ranges=[],  globalStartRange=10
    //     → 起点直接飞 0→10. 总 = 0
    //   target=15, stations=[],   ranges=[],  globalStartRange=0
    //     → 哪儿都没飞机, 全程步行 = 15
    //   target=30, stations=[5,10,20], ranges=[20,1,2], globalStartRange=0
    //     → walk 0→5 (5), 在 5 起飞 (range=20) 飞到 25, walk 25→30 (5). 总 = 10
    //       (中途 range=1 / range=2 的两个 station 直接跳过不起飞)

    public static class DronePart3 {
        public static long walkDistance(int target, int[] stations, int[] ranges, int globalStartRange) {
            throw new UnsupportedOperationException("DronePart3.walkDistance: not implemented");
        }
    }

    // ====================================================================
    // PART 4  —  0/1 Knapsack (Packing)                             [⚠ TODO]
    // ====================================================================
    // 场景: 给无人机装货箱。有 n 件待装物品, 每件有重量 weight 和价值 value,
    //       货箱总承重上限是 capacity。每件物品**最多装一次** (0/1, 不能拆、不能
    //       装两份)。在不超重的前提下, 求能装走的最大总价值。
    //
    //       这是个跟 Part 1-3 互相独立的子问题 (面试里也常单独出)。
    //
    // maxValue(weights, values, capacity) -> long
    //   weights[i] 与 values[i] 一一对应; capacity 为承重上限。
    //
    // 例 (摘自测试):
    //   weights=[1,2,3], values=[6,10,12], capacity=5 → 22 (选第 2、3 件: 重 2+3=5, 值 10+12)
    //   weights=[1,2,3], values=[6,10,12], capacity=0 → 0   (装不下任何东西)
    //   weights=[],      values=[],        capacity=10 → 0  (没有物品)
    //   weights=[5],     values=[10],      capacity=5 → 10  (恰好装下)
    //   weights=[6],     values=[10],      capacity=5 → 0   (唯一物品超重)
    //   weights=[2],     values=[5],       capacity=4 → 5   (0/1: 同一件不能拿两次, 不是 10)

    public static class PackingPart4 {
        public static long maxValue(int[] weights, int[] values, int capacity) {
            throw new UnsupportedOperationException("PackingPart4.maxValue: not implemented");
        }
    }

    // ====================================================================
    // PART 5  —  多 Drone 并发调度 (concurrent dispatch)            [⚠ TODO]
    // ====================================================================
    // 与 Part 1-4 比:
    //   同: 单次配送的 walk/fly 物理规则不变 (复用 Part 3 的几何)
    //   变: 现在有 N 个 drone (或 N 个 dispatcher 线程) 同时拉订单池里的 order
    //   新: DispatcherPart5.submitOrder(orderId, target) / assignNext(droneId) -> Long
    //       多个线程同时调 assignNext, 同一个 orderId 不能被分给两个 drone
    //
    // 问题陈述:
    //   订单池里有 M 个待送的 target. 系统启动 N 个 drone, 每个 drone 在自己线程上
    //   不停地 "拿一个未分配的订单 -> 送 -> 拿下一个".
    //   要求:
    //     (a) 同一个订单只能被一个 drone 拿到 (no double-spend)
    //     (b) 拿不到订单时不能 busy-spin 烧 CPU
    //     (c) 高 throughput: 100 个 drone 抢同一个池子也不应该退化成串行
    //     (d) 优雅 shutdown: 主线程喊停时, 在飞的 drone 完成当前订单后退出
    //
    // 面试常追问 (Coinbase 真考):
    //   1. 用什么数据结构当订单池? ArrayDeque + synchronized? BlockingQueue? CHM?
    //      → 引出 "MPMC queue" 讨论. LinkedBlockingQueue 简单, 但锁竞争; ConcurrentLinkedQueue
    //        无锁但要自己处理 "空" wait/notify.
    //   2. 如果订单有优先级 (VIP 加急) 怎么办? PriorityBlockingQueue 的代价?
    //   3. drone 数 >> 订单数 时, 多数 drone 在 await, 怎么避免惊群 (thundering herd)?
    //   4. 怎么测? (生成 1000 订单, 跑 8 drone, 跑完所有 order 都被处理 exactly once,
    //      用 ConcurrentHashMap 记 orderId -> droneId 校验)
    //
    // 设计 trade-off (面试加分):
    //   单全局锁 / BlockingQueue / 分片队列 (per-zone) / work-stealing 各自优劣.

    public static class DispatcherPart5 {
        public DispatcherPart5(int[] stations, int range) {
            throw new UnsupportedOperationException("TODO: Part 5 — 多 drone 并发调度");
        }

        public void submitOrder(long orderId, int target) {
            throw new UnsupportedOperationException("TODO: Part 5 — 多 drone 并发调度");
        }

        // 返回这架 drone 这次配送走的 walk 距离; 没单可拿时阻塞或返回 null.
        public Long assignNext(long droneId) throws InterruptedException {
            throw new UnsupportedOperationException("TODO: Part 5 — 多 drone 并发调度");
        }

        public void shutdown() {
            throw new UnsupportedOperationException("TODO: Part 5 — 多 drone 并发调度");
        }
    }

    // ====================================================================
    // PART 6  —  动态重规划 (online re-planning)                    [⚠ TODO]
    // ====================================================================
    // 与 Part 5 比:
    //   同: 多 drone, 订单池
    //   变: drone 已经在飞了 (位置介于 station 之间), 中途新订单进来要决定:
    //        - 当前 drone 继续完成手头的 order, 然后接新单?
    //        - 还是 abort 当前飞行, 改飞新单 (cancellation cost)?
    //        - 还是分给其他空闲 drone?
    //   新: PlannerPart6.tick(now) 推进世界一步; addOrderAt(now, target)
    //       getDronePosition(droneId, now) -> int
    //
    // 问题陈述:
    //   订单流是 online 的 —— 时刻 t 才知道时刻 t 的订单. 之前的 "全知全能" 不存在了.
    //   要求一个在线策略, 同时考虑:
    //     (a) 已分配但未完成的 order 不能简单丢 (会让用户等无限久)
    //     (b) 新 order 来时如果有空闲 drone, 立刻分配; 否则进队列
    //     (c) 怎么衡量好坏: 平均送达时延? Tail 时延 (p99)? 总 walk?
    //
    // 面试常追问:
    //   1. "greedy assign 给最近 drone vs 等下一轮 batch 一起规划, 哪个好?"
    //      → 引出 online vs batch 的 trade-off; competitive ratio 概念.
    //   2. "怎么处理 'preemption': 当前 drone 正在送 order A, 来了个超急 order B
    //       在它隔壁, 要不要拦截?" → 引出 cancellation 成本和 SLA 讨论.
    //   3. "怎么避免 starvation? (低优先级 order 永远抢不到 drone)"
    //      → aging / 优先级提升 / 双队列.
    //   4. "如果 drone 移动需要时间, 怎么模拟 / 测试? 时间步长怎么选?"
    //      → 离散事件仿真 vs 真时钟; mock clock 注入.
    //
    // 这个 Part 偏设计讨论, 代码可以写一个简化版 stub.

    public static class PlannerPart6 {
        public PlannerPart6(int numDrones, int[] stations, int range) {
            throw new UnsupportedOperationException("TODO: Part 6 — 动态重规划");
        }

        public void addOrderAt(long now, long orderId, int target) {
            throw new UnsupportedOperationException("TODO: Part 6 — 动态重规划");
        }

        public void tick(long now) {  // 推进仿真时钟到 now
            throw new UnsupportedOperationException("TODO: Part 6 — 动态重规划");
        }

        public int getDronePosition(long droneId, long now) {
            throw new UnsupportedOperationException("TODO: Part 6 — 动态重规划");
        }

        // 返回到 now 为止已送达订单的平均完成时延.
        public double averageLatency(long now) {
            throw new UnsupportedOperationException("TODO: Part 6 — 动态重规划");
        }
    }

    // ====================================================================
    // PART 7  —  故障 drone 接管 + 持久化 (failover & recovery)     [⚠ TODO]
    // ====================================================================
    // 与 Part 6 比:
    //   同: 在线订单流, 多 drone
    //   变: drone 可能 "失联" (心跳超时); dispatcher 进程也可能 crash 重启
    //   新: heartbeatPart7(droneId, now) / markFailedPart7(droneId)
    //       checkpointPart7() / recoverPart7(snapshot)
    //
    // 问题陈述:
    //   现实中 drone 会掉电 / 信号丢失. 订单分给它了, 它没回话, 该把这个 order 转给
    //   别的 drone. 但要小心 split-brain: 万一原 drone 其实没死, 只是慢, 这单不能送两次.
    //   另外 dispatcher 进程崩了, 重启后未送达的 order 不能丢, 飞在路上的 drone 状态要恢复.
    //
    // 面试常追问 (Coinbase 真实场景, 因为他们玩钱):
    //   1. "drone 失联多久才算死? 30s? 5min? 失误代价?"
    //      → 引出 lease / heartbeat / failure detector (Phi accrual) 讨论.
    //   2. "原 drone 复活了, 它手上的 order 已经被转给别人了, 怎么办?"
    //      → 引出 fencing token (单调递增 epoch) 防止旧主写入.
    //   3. "dispatcher 自己挂了怎么办?" → WAL + checkpoint (复用 InMemoryDatabase Part 7 的思路).
    //   4. "怎么保证 'order 已送达' 这个状态是 exactly-once?"
    //      → 引出幂等性 + ack 协议.
    //
    // 跟 In-Memory-Database 的 Part 7 (WAL) 是一个内核, 这里换了业务场景.

    public static class FailoverDispatcherPart7 {
        public FailoverDispatcherPart7(int[] stations, int range, long leaseTimeoutMs) {
            throw new UnsupportedOperationException("TODO: Part 7 — failover & recovery");
        }

        public void heartbeatPart7(long droneId, long now) {
            throw new UnsupportedOperationException("TODO: Part 7 — failover & recovery");
        }

        public void markFailedPart7(long droneId) {
            throw new UnsupportedOperationException("TODO: Part 7 — failover & recovery");
        }

        // 落一个 snapshot (内存 byte[] 占位; 真实场景写文件 / WAL)
        public byte[] checkpointPart7() {
            throw new UnsupportedOperationException("TODO: Part 7 — failover & recovery");
        }

        public void recoverPart7(byte[] snapshot) {
            throw new UnsupportedOperationException("TODO: Part 7 — failover & recovery");
        }
    }

    // ====================================================================
    // PART 8  —  地理分片 / 跨区调度 (geo sharding)                 [⚠ TODO]
    // ====================================================================
    // 与 Part 7 比:
    //   同: 单 zone 内部还是 Part 5-7 那一套
    //   变: 城市 / 大区被切成 K 个 dispatch zone, 每个 zone 自己的 station 集合和 drone 池
    //   新: GeoRouterPart8.routeOrder(orderId, target) -> zoneId
    //       handoffPart8(orderId, fromZone, toZone) 跨区切换
    //
    // 问题陈述:
    //   target 落在 zone A 的范围里, 由 zone A 的 drone 送, 没问题.
    //   但 (a) target 在 zone 边界附近, 哪个 zone 送更省? (b) target 在 zone A 但 A
    //   的 drone 都忙了, 能不能借 zone B 的? (c) zone A 整个挂了, 它的订单转哪儿?
    //
    // 面试常追问 (跟 In-Memory-Database Part 8 同思路, 但换了 routing key):
    //   1. "用什么做 sharding key? 经纬度? 邮编? 哈希?"
    //      → 地理区块 (geohash / S2 cells) vs 邮编 vs 一致性哈希.
    //   2. "跨区 handoff 怎么保证 exactly-once?"
    //      → 借 Part 7 的 fencing token.
    //   3. "全局 SLA (95% 30 分钟内送达) 在分片下怎么监控?"
    //      → 各 zone 上报 metrics, 中心化聚合.
    //   4. "drone 跨区飞需要协调 (空域)? 数据面 vs 控制面分开?"
    //      → 引出 zone leader / lease + 中心 coordinator (etcd / Zookeeper) 讨论.
    //
    // 这道题答完基本是 "设计一个简化版 Uber/DoorDash dispatch", 真面试不强求写代码,
    // 跟面试官把上面这些权衡讲清楚就够; 真写就写一个 router stub.

    // 建议入口 (写不写都可以):
    //
    //   static class GeoRouterPart8 {
    //       private final DispatcherPart5[] zones;
    //       GeoRouterPart8(int k, ...) { ... }
    //       int routeOrder(long orderId, int target) { ... }     // -> zoneId
    //       void handoffPart8(long orderId, int fromZone, int toZone) { ... }
    //   }
}
