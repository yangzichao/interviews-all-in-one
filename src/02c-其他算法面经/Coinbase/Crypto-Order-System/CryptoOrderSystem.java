import java.util.*;

/**
 * 4-part Coinbase interview practice — Crypto Order System (OMS).
 *
 * 每个 Part 是独立的 class,后缀 PartN。先无脑独立写,做完再讨论抽公共逻辑。
 *
 * 这不是产品代码,是练习代码 —— 让你能专注当前 Part 而不破坏已完成的部分。
 *
 * 共享数据类型:
 *   OrderState  — enum: ACTIVE / PAUSED / CANCELLED / FILLED
 *   Order       — record(orderId, userId, symbol, quantity, state)
 *                 state 变更 = new Order(...) 替换, 不 mutate
 */
public class CryptoOrderSystem {

    // 共享数据类型 =========================================================

    public enum OrderState { ACTIVE, PAUSED, CANCELLED, FILLED }

    public static record Order(
            String orderId,
            String userId,
            String symbol,
            long quantity,
            OrderState state
    ) {}

    // ====================================================================
    // PART 1  —  Basic Place / Get                                  [⚠ TODO]
    // ====================================================================
    // 一个 Map<orderId, Order> 起步.
    //
    //   placeOrder("o1", "alice", "BTC-USD", 100)  → 进 ACTIVE 状态
    //   getOrder("o1")                              → Order(..., state=ACTIVE)
    //   getOrder("missing")                         → null  (不抛!)
    //   placeOrder("o1", ...)  再来一次               → IllegalArgumentException
    //
    // 注意:
    //   - 新订单一定是 ACTIVE 状态
    //   - duplicate orderId 抛 IllegalArgumentException
    //   - getOrder 找不到返 null, 不抛

    public static class OmsPart1 {
        public OmsPart1() {
            throw new UnsupportedOperationException("OmsPart1: not implemented");
        }

        public void placeOrder(String orderId, String userId, String symbol, long quantity) {
            throw new UnsupportedOperationException("OmsPart1.placeOrder: not implemented");
        }

        public Order getOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart1.getOrder: not implemented");
        }
    }

    // ====================================================================
    // PART 2  —  Pause / Resume / Cancel by ID                      [⚠ TODO]
    // ====================================================================
    // 状态机:
    //   ACTIVE  → PAUSED      (pauseOrder)
    //   PAUSED  → ACTIVE      (resumeOrder)
    //   ACTIVE/PAUSED → CANCELLED  (cancelOrder)
    //
    // 非法转移 → IllegalStateException:
    //   PAUSED → PAUSED   (重复 pause)
    //   ACTIVE → ACTIVE   (重复 resume)
    //   CANCELLED → *     (terminal)
    //   FILLED → *        (terminal — 没接口能进入, 但要假设它是 terminal)
    //
    // record 是 immutable, 改 state = new Order(...) 替换.

    public static class OmsPart2 {
        public OmsPart2() {
            throw new UnsupportedOperationException("OmsPart2: not implemented");
        }

        public void placeOrder(String orderId, String userId, String symbol, long quantity) {
            throw new UnsupportedOperationException("OmsPart2.placeOrder: not implemented");
        }

        public Order getOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart2.getOrder: not implemented");
        }

        public void pauseOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart2.pauseOrder: not implemented");
        }

        public void resumeOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart2.resumeOrder: not implemented");
        }

        public void cancelOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart2.cancelOrder: not implemented");
        }
    }

    // ====================================================================
    // PART 3  —  Cancel All from User                               [⚠ TODO]
    // ====================================================================
    // 取消该 user 所有 ACTIVE+PAUSED 订单, 返回**实际取消数量**.
    //
    //   userA: o1(ACTIVE), o2(PAUSED), o3(CANCELLED), o4(ACTIVE)
    //   cancelAllOrdersForUser("userA")  → 返回 3  (o3 跳过)
    //
    // 注意:
    //   - 不要把已 CANCELLED/FILLED 的算进取消数
    //   - 如果想 O(1) 找到 user 的订单, 需要 Map<userId, Set<orderId>> 副索引
    //     naive O(n) 全表扫也能过, 看你想怎么演化

    public static class OmsPart3 {
        public OmsPart3() {
            throw new UnsupportedOperationException("OmsPart3: not implemented");
        }

        public void placeOrder(String orderId, String userId, String symbol, long quantity) {
            throw new UnsupportedOperationException("OmsPart3.placeOrder: not implemented");
        }

        public Order getOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart3.getOrder: not implemented");
        }

        public void pauseOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart3.pauseOrder: not implemented");
        }

        public void resumeOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart3.resumeOrder: not implemented");
        }

        public void cancelOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart3.cancelOrder: not implemented");
        }

        public int cancelAllOrdersForUser(String userId) {
            throw new UnsupportedOperationException("OmsPart3.cancelAllOrdersForUser: not implemented");
        }
    }

    // ====================================================================
    // PART 4  —  Query by State                                     [⚠ TODO]
    // ====================================================================
    // ordersInState(state) → 所有处于该 state 的订单 (顺序不指定)
    // ordersInStateForUser(state, userId) → 同上 + 按 user 过滤
    //
    // 面试加分点 — 数据结构:
    //   - naive: Map<id, Order>, query 时全表扫 → O(n)
    //   - 加副索引: Map<State, Map<id, Order>> → query O(k)
    //   - 再加一层: Map<State, Map<userId, Set<id>>> → per-user 更快
    //
    //   每次 state 变更要同步更新所有索引 —— 真正的代码量在这里.

    public static class OmsPart4 {
        public OmsPart4() {
            throw new UnsupportedOperationException("OmsPart4: not implemented");
        }

        public void placeOrder(String orderId, String userId, String symbol, long quantity) {
            throw new UnsupportedOperationException("OmsPart4.placeOrder: not implemented");
        }

        public Order getOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart4.getOrder: not implemented");
        }

        public void pauseOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart4.pauseOrder: not implemented");
        }

        public void resumeOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart4.resumeOrder: not implemented");
        }

        public void cancelOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart4.cancelOrder: not implemented");
        }

        public int cancelAllOrdersForUser(String userId) {
            throw new UnsupportedOperationException("OmsPart4.cancelAllOrdersForUser: not implemented");
        }

        public List<Order> ordersInState(OrderState state) {
            throw new UnsupportedOperationException("OmsPart4.ordersInState: not implemented");
        }

        public List<Order> ordersInStateForUser(OrderState state, String userId) {
            throw new UnsupportedOperationException("OmsPart4.ordersInStateForUser: not implemented");
        }
    }

    // ====================================================================
    // PART 5  —  并发安全 (thread-safety)                            [⚠ TODO]
    // ====================================================================
    // 与 Part 4 比:
    //   同: 对外接口形状不变 (placeOrder / getOrder / pause / resume / cancel /
    //       cancelAllOrdersForUser / ordersInState / ordersInStateForUser)
    //   同: Order 仍是 immutable record, state 变更 = new Order(...) 替换
    //   变: N 个线程并发调用; 所有方法必须线程安全
    //       state transition 必须原子化 (read-modify-write 不能撕裂)
    //       ordersInState* 必须返回某个一致快照 (不能迭代到一半看到 index 半更新)
    //   新: 没有新方法 —— 重点在内部如何选锁 / concurrent 集合 / CAS
    //
    // 问题陈述:
    //   Part 4 的多层 index (Map<State, Map<userId, Set<id>>>) 在并发下最危险 ——
    //   一次 cancel 要同时改: (a) 主 map 的 Order 引用; (b) 旧 state 的 user-bucket;
    //   (c) 新 state 的 user-bucket. 这三步不原子, 就可能出现:
    //     - 一个 order 同时出现在 ACTIVE 和 CANCELLED 两个桶里
    //     - cancel 和 pause 同时跑 → 最后状态可能是 PAUSED (cancel 丢了!)
    //     - cancelAllOrdersForUser 跟 placeOrder 并发, 漏掉刚 place 的 order
    //
    // 面试官最常追问的方向 (这是 Coinbase OMS 面试**核心**考点):
    //   1. 锁粒度: per-order lock? per-user lock? 一把大锁? Map<id, Lock>?
    //   2. cancel 跟 fill 同时发生 (用户点取消, 撮合引擎正好成交) 谁赢? —— "race to terminal"
    //   3. cancelAllOrdersForUser 要一次性原子吗? 还是允许"逐个 cancel, 期间有新单进来"?
    //   4. ordersInState 返回的 List 在调用方迭代时, 内部 state 变了怎么办? (snapshot vs live)
    //   5. CAS / compare-and-swap state transition: 用 AtomicReference<Order> 而不是锁?
    //
    // 你要写的: 重新实现 Part 4 的 8 个方法, 加上你选的并发策略.

    public static class OmsPart5 {
        public OmsPart5() {
            throw new UnsupportedOperationException("OmsPart5: not implemented");
        }

        public void placeOrder(String orderId, String userId, String symbol, long quantity) {
            throw new UnsupportedOperationException("OmsPart5.placeOrder: not implemented");
        }

        public Order getOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart5.getOrder: not implemented");
        }

        public void pauseOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart5.pauseOrder: not implemented");
        }

        public void resumeOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart5.resumeOrder: not implemented");
        }

        public void cancelOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart5.cancelOrder: not implemented");
        }

        public int cancelAllOrdersForUser(String userId) {
            throw new UnsupportedOperationException("OmsPart5.cancelAllOrdersForUser: not implemented");
        }

        public List<Order> ordersInState(OrderState state) {
            throw new UnsupportedOperationException("OmsPart5.ordersInState: not implemented");
        }

        public List<Order> ordersInStateForUser(OrderState state, String userId) {
            throw new UnsupportedOperationException("OmsPart5.ordersInStateForUser: not implemented");
        }
    }

    // ====================================================================
    // PART 6  —  撮合引擎 + 订单簿 (matching engine + order book)    [⚠ TODO]
    // ====================================================================
    // 与 Part 5 比:
    //   同: OrderState / Order 共享类型还在用
    //   变: Order schema 扩展 —— 真正的 limit order 有 side (BUY/SELL) 和 price
    //       -> 新 enum Side, 新 record LimitOrder
    //       state 多了 PARTIALLY_FILLED (虽然这一 Part 没暴露所有过渡)
    //   新: submitLimitOrder(orderId, userId, symbol, side, price, quantity)
    //       bestBid(symbol) / bestAsk(symbol) -> Long  (没单返 null)
    //       depthAtPrice(symbol, side, price) -> long  (该档累计 quantity)
    //       cancelLimitOrder(orderId)  (从订单簿摘掉, 状态变 CANCELLED)
    //
    // 问题陈述:
    //   Part 1-5 是 OMS (订单生命周期管理). 现在加撮合 ——
    //   按 price-time priority: 同 symbol 内 BUY 价高优先 / SELL 价低优先, 同价先到先成交.
    //   新单进来时: 如果对手盘有可成交价位, 立刻吃单; 否则挂在订单簿上.
    //
    // 面试官最常追问的 4 个 follow-up:
    //   1. **价格档位用什么数据结构?**
    //      → TreeMap<Price, Queue<Order>>: bestBid/bestAsk 是 firstKey/lastKey 都 O(log n);
    //        SkipListMap: 类似 TreeMap 但天然 weakly-consistent, 适合并发读;
    //        Array-indexed levels (固定 tick): O(1) 查最优价, 但 price range 大时浪费内存;
    //        Heap of heaps: 拿 best 是 O(1), 但 cancel 中间的 order 是 O(n).
    //      → 真实交易所多数用 array + linked-list-per-level (price 离散化), 速度极致.
    //
    //   2. **price-time priority 怎么保证?**
    //      → 每个 price level 是一个 FIFO queue. cancel 时要从 queue 中间删 ——
    //        ArrayDeque 不支持, 用 LinkedHashMap 或双向链表 + Map<id, Node>.
    //
    //   3. **partial fill 谁算赢? 残量怎么处理?**
    //      → 撮合时新单 quantity 大于对手盘头部 → 吃完一个 level head, 继续往下吃;
    //        新单残量 > 0 → 挂上去 (limit 单) 或丢掉 (Part 7 的 IOC/FOK).
    //        被吃的对手盘头部如果只吃掉一部分 → state 变 PARTIALLY_FILLED, quantity 减.
    //
    //   4. **并发: 撮合引擎是单线程还是多线程?**
    //      → 业界几乎都是 **per-symbol 单线程** (LMAX Disruptor 风格) —— BTC-USD 一个线程,
    //        ETH-USD 另一个; 没有跨 symbol 撮合就没有跨线程同步. 简单又快.
    //        多线程撮合需要复杂的 lock-free queue 或乐观 CAS, 难且容易错.
    //
    // 取舍表 (主要考点):
    //   | 价格档位 DS         | bestBid | 同档插入 | 中间 cancel | 内存 |
    //   |--------------------|---------|----------|-------------|------|
    //   | TreeMap + ArrayDeque | O(log n) | O(1)   | O(level)    | 紧凑 |
    //   | SkipListMap          | O(log n) | O(1)   | O(level)    | 紧凑 |
    //   | Array of levels      | O(1)     | O(1)   | O(level)    | 浪费 |
    //   | LinkedHashMap-per-level | O(log n) | O(1) | O(1) (有 ref) | 中等 |
    //
    // 你要写的: LimitOrder 的存储 + best price 查询 + 最简撮合.

    public enum Side { BUY, SELL }

    public static record LimitOrder(
            String orderId,
            String userId,
            String symbol,
            Side side,
            long price,
            long quantity,
            OrderState state
    ) {}

    public static class OmsPart6 {
        public OmsPart6() {
            throw new UnsupportedOperationException("OmsPart6: not implemented");
        }

        public void submitLimitOrder(String orderId, String userId, String symbol,
                                     Side side, long price, long quantity) {
            throw new UnsupportedOperationException("OmsPart6.submitLimitOrder: not implemented");
        }

        public LimitOrder getLimitOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart6.getLimitOrder: not implemented");
        }

        public Long bestBid(String symbol) {
            throw new UnsupportedOperationException("OmsPart6.bestBid: not implemented");
        }

        public Long bestAsk(String symbol) {
            throw new UnsupportedOperationException("OmsPart6.bestAsk: not implemented");
        }

        public long depthAtPrice(String symbol, Side side, long price) {
            throw new UnsupportedOperationException("OmsPart6.depthAtPrice: not implemented");
        }

        public void cancelLimitOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart6.cancelLimitOrder: not implemented");
        }
    }

    // ====================================================================
    // PART 7  —  订单类型扩展 (market / IOC / FOK / stop)            [⚠ TODO]
    // ====================================================================
    // 与 Part 6 比:
    //   同: 订单簿数据结构和撮合主循环一样, LimitOrder schema 复用
    //   变: 新增 TimeInForce 和 OrderType, submit 时附带
    //   新: submitOrder(spec...) 接收 type + TIF;
    //       submitStopOrder(...) 触发条件挂在另一张表上
    //
    // 问题陈述:
    //   现在所有单都是 GTC limit (Good-Till-Cancel limit). 加这几种:
    //     - MARKET:        无价格, 用对手盘最优价吃, 吃不完直接丢
    //     - LIMIT + IOC:   能吃多少吃多少, 剩余立刻取消 (不挂簿)
    //     - LIMIT + FOK:   要么全成交要么全不成交 (fill-or-kill, 残量 1 都不行就拒)
    //     - STOP:          市场价穿过 trigger 时, 转成 market/limit 单进簿
    //
    // 面试官最常追问的 4 个 follow-up:
    //   1. **FOK 怎么"预检"全成交?**
    //      → 在不实际吃单的前提下扫一遍对手盘价格档位, 累加 quantity 直到够,
    //        够了再真正 match. 这要求订单簿支持"只读 walk"; 不能边 walk 边吃.
    //   2. **STOP 的触发条件存在哪? 怎么 O(log n) 判触发?**
    //      → 维护两个 TreeMap<TriggerPrice, List<StopOrder>>:
    //          BUY STOP 当 last_trade_price >= trigger 触发 (向上突破)
    //          SELL STOP 当 last_trade_price <= trigger 触发 (向下穿)
    //        每次成交后查 ceiling/floor key 看有没有要触发的.
    //   3. **MARKET 单价格滑点保护?**
    //      → 实际交易所会给 market 单一个"最坏可接受价" (slippage limit),
    //        否则恶意大单可以把订单簿吃穿. Coinbase 实际上不允许纯 MARKET,
    //        而是 market with price limit.
    //   4. **IOC 跟 FOK 在并发下的"虚假拒单"?**
    //      → FOK 预检通过, 真撮合时对手盘被另一个线程吃了 —— 这就是为什么
    //        撮合必须 per-symbol 单线程串行化. 不然 FOK 语义无法保证.
    //
    // 取舍表 (TimeInForce 实现):
    //   | TIF | 撮合后残量处理 | 实现难度 | 注意 |
    //   |-----|---------------|---------|------|
    //   | GTC | 挂簿            | 简单     | (Part 6 默认) |
    //   | IOC | 丢残量          | 简单     | 撮合完不挂簿 |
    //   | FOK | 不够则全拒      | 中等     | 需"预检" 全成交 |
    //   | GTD | 挂簿 + 过期时间 | 中等     | 跟 TTL 类似, 后台清理 |
    //
    // 你要写的: 一个统一的 submitOrder, 根据 type + tif 走不同路径.

    public enum OrderType { LIMIT, MARKET, STOP_LIMIT, STOP_MARKET }
    public enum TimeInForce { GTC, IOC, FOK, GTD }

    public static class OmsPart7 {
        public OmsPart7() {
            throw new UnsupportedOperationException("OmsPart7: not implemented");
        }

        public void submitOrder(String orderId, String userId, String symbol,
                                Side side, OrderType type, TimeInForce tif,
                                Long limitPrice, Long triggerPrice, long quantity) {
            throw new UnsupportedOperationException("OmsPart7.submitOrder: not implemented");
        }

        public LimitOrder getLimitOrder(String orderId) {
            throw new UnsupportedOperationException("OmsPart7.getLimitOrder: not implemented");
        }

        public long lastTradePrice(String symbol) {
            throw new UnsupportedOperationException("OmsPart7.lastTradePrice: not implemented");
        }

        public Long bestBid(String symbol) {
            throw new UnsupportedOperationException("OmsPart7.bestBid: not implemented");
        }

        public Long bestAsk(String symbol) {
            throw new UnsupportedOperationException("OmsPart7.bestAsk: not implemented");
        }
    }

    // ====================================================================
    // PART 8  —  持久化 / 恢复 + 市场数据广播 (WAL + market data)    [⚠ TODO]
    // ====================================================================
    // 与 Part 7 比:
    //   同: 撮合 / 订单簿 / TIF 全部继承, 接口形状不变
    //   变: 每次写操作 (submit / cancel / state transition) 先 append WAL;
    //       撮合产生的 trade tick 通过 subscriber 接口 fanout 给订阅者
    //   新: subscribe(symbol, listener), unsubscribe(id);
    //       replay(walPath) 在构造时从磁盘恢复;
    //       flush() 强制 fsync.
    //
    // 问题陈述:
    //   Coinbase 是金融系统, crash 后未成交挂单必须能恢复. 同时, 撮合后产生的
    //   trade tick / order book delta 要广播给所有客户 (WebSocket / pub-sub).
    //
    // 面试官最常追问的 4 个 follow-up:
    //   1. **WAL 写在撮合**之前**还是之后**?**
    //      → 必须**之前** (write-ahead). 顺序: 验单 → append WAL → fsync → 实际撮合 → ack.
    //        否则进程在 "撮合完但没写 WAL" 时崩 → 重启状态比 ACK 给用户的少, 灾难.
    //
    //   2. **恢复时 trade tick 要不要重新广播?**
    //      → 不要. WAL replay 只重建内存状态, 不重发市场数据. 否则订阅者会收到重复 tick.
    //        实现上区分 "live mode" 和 "replay mode", replay 时静默 listener.
    //
    //   3. **市场数据广播: push vs pull, 同步 vs 异步?**
    //      → 同步 fanout (撮合线程直接 listener.onTrade) → 简单, 但慢的 listener 拖垮撮合;
    //        异步队列 (per-listener 一个 ring buffer) → 撮合线程只 enqueue, 慢 listener 自己 drop;
    //        snapshot + delta 协议: 新订阅者先收一份 full 订单簿, 再收 delta.
    //
    //   4. **listener 在订阅期间订单簿变化, snapshot + delta 怎么保证不丢不重?**
    //      → 撮合线程在拿快照时记下 sequence number S; 广播每个 delta 都带 seq;
    //        新订阅者收到 snapshot@S 后, 把 seq <= S 的 delta 丢掉, seq > S 的从队列里 replay.
    //        这就是 Coinbase 真实 Pro WebSocket 的协议设计.
    //
    // 取舍表 (持久化 + 广播):
    //   | 维度 | 选项 A | 选项 B | Coinbase 真实 |
    //   |------|--------|--------|---------------|
    //   | WAL fsync | 每笔 fsync | 批量 fsync | 每笔 (订单 ACK 前) |
    //   | 广播模型 | 同步 fanout | 异步 ring buffer | 异步 + per-listener slow-detect |
    //   | 订阅协议 | live-only | snapshot+delta | snapshot+delta + seq number |
    //   | replay 范围 | 全部 log | snapshot + tail log | snapshot + tail log |
    //
    // 这道 Part 偏设计讨论, 写代码量大 —— 留几个 stub 起头就够.

    public interface MarketDataListener {
        void onTrade(String symbol, long price, long quantity);
        void onBookDelta(String symbol, Side side, long price, long newDepth);
    }

    public static class OmsPart8 {
        public OmsPart8(String walPath) {
            throw new UnsupportedOperationException("OmsPart8: not implemented");
        }

        public void submitOrder(String orderId, String userId, String symbol,
                                Side side, OrderType type, TimeInForce tif,
                                Long limitPrice, Long triggerPrice, long quantity) {
            throw new UnsupportedOperationException("OmsPart8.submitOrder: not implemented");
        }

        public String subscribe(String symbol, MarketDataListener listener) {
            throw new UnsupportedOperationException("OmsPart8.subscribe: not implemented");
        }

        public void unsubscribe(String subscriptionId) {
            throw new UnsupportedOperationException("OmsPart8.unsubscribe: not implemented");
        }

        public void flush() {  // 强制 fsync WAL
            throw new UnsupportedOperationException("OmsPart8.flush: not implemented");
        }
    }
}
