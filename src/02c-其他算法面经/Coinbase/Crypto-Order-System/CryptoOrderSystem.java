import java.util.*;

/**
 * Coinbase interview practice — Crypto Order System (OMS)  (8 parts).
 *
 * ════════════════════════════════════════════════════════════════════════
 *  背景故事 (BACKGROUND) —— 读这里就够入手, 不需要懂交易所内部
 * ════════════════════════════════════════════════════════════════════════
 *
 *  在一个加密货币交易所 (比如 Coinbase) 里, 用户会下"订单 (order)": 比如
 *  "我要买 100 个 BTC-USD"。交易所内部得有一个系统负责**管理这些订单的一生**:
 *  下单、暂停、恢复、取消、查询、最后成交……这个系统叫 OMS
 *  (Order Management System, 订单管理系统)。你要写的就是它。
 *
 *      [用户 App]  ──下单/改单/撤单──▶   OMS (你写的)   ──▶  撮合成交
 *                  ◀──查询订单状态───
 *
 *  一开始 (Part 1-5) 你只做"**管理**": 把订单存起来, 维护它的状态, 支持各种
 *  查询和批量操作。到 Part 6 才升级成真正的"**交易所**": 加入撮合引擎
 *  (matching engine), 让买单和卖单按价格撮合成交。
 *
 *  一个订单的状态会变 (这是全题的核心):
 *
 *      下单 ──▶ ACTIVE ──暂停──▶ PAUSED
 *                 │   ◀─恢复──     │
 *                 └──取消──┬───取消─┘
 *                          ▼
 *                      CANCELLED  (终态, 不能再变)
 *                 撮合成交 ──▶ FILLED  (终态)
 *
 *  几个术语 (后面注释会用到, 先混个脸熟):
 *    · order      : 一笔订单 (谁、买卖什么、多少量、什么状态)
 *    · orderId    : 订单的唯一编号
 *    · symbol     : 交易对, 如 "BTC-USD" (拿什么换什么)
 *    · state      : 订单当前处于生命周期的哪一步 (ACTIVE/PAUSED/...)
 *    · terminal   : 终态 (CANCELLED / FILLED), 到了就不能再转移
 *    · OMS        : 管理订单生命周期的系统 = 你写的东西
 *    · 撮合 (match): 把买单和卖单配对成交 (Part 6 才登场)
 *    · 订单簿 (book): 所有未成交挂单, 按价格组织 (Part 6 才登场)
 *
 *  贯穿全题的一个约定: Order 是**不可变 (immutable) record** —— 要改状态,
 *  不是去改原对象, 而是 new 一个新 Order 替换掉旧的 (函数式风格, 避免并发踩坑)。
 *
 * ════════════════════════════════════════════════════════════════════════
 *
 * 题面直接写在每个 Part 上方 —— 读代码就能读题, 不用切到别处。
 * 坑点 / 取舍 / follow-up 答案在 README.md (含剧透), 练的时候别看。
 *
 * 逐步加约束; 面试时一次只给一个 Part, 做完再放下一个。
 * 每个 Part 是独立的 class, 后缀 PartN —— 让你能专注当前 Part 而不破坏已完成的部分。
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
    // 场景: 最朴素的一步 —— 用户下单, 系统记下来; 用户拿 orderId 来查, 系统返回。
    //       (像柜台先把每张单子收下、编号、能凭号取回。)
    //       新下的单一律是 ACTIVE 状态。
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
        Map<String, Order> orders;
        public OmsPart1() {
            this.orders = new HashMap<>();
        }

        public void placeOrder(String orderId, String userId, String symbol, long quantity) {
            if (orders.containsKey(orderId)) throw new IllegalArgumentException();
            Order order = new Order(orderId, userId, symbol, quantity, OrderState.ACTIVE);
            orders.put(orderId, order);
        }

        public Order getOrder(String orderId) {
            return orders.get(orderId);
        }
    }

    // ====================================================================
    // PART 2  —  Pause / Resume / Cancel by ID                      [⚠ TODO]
    // ====================================================================
    // 场景: 订单不是一锤子买卖 —— 用户能临时暂停 (pause)、再恢复 (resume)、或彻底
    //       取消 (cancel)。但不是任意状态都能随便跳: 已取消的单不能再恢复, 重复
    //       暂停也不合法。这就是一个"状态机", 你要挡住所有非法转移。
    //
    // ★ 复用: placeOrder / getOrder 跟 Part 1 完全一样, 直接搬。
    //         本 Part 只需新写 pauseOrder / resumeOrder / cancelOrder。
    //
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
        Map<String, Order> orders;                          // ← 复用自 Part 1
        public OmsPart2() {
            this.orders = new HashMap<>();
        }

        public void placeOrder(String orderId, String userId, String symbol, long quantity) {
            if (orders.containsKey(orderId)) throw new IllegalArgumentException();
            Order order = new Order(orderId, userId, symbol, quantity, OrderState.ACTIVE);
            orders.put(orderId, order);
        }

        public Order getOrder(String orderId) {
            return orders.get(orderId);
        }

        public void pauseOrder(String orderId) {
            transformOrder(orderId, OrderState.PAUSED, OrderState.ACTIVE);
        }

        public void resumeOrder(String orderId) {
            transformOrder(orderId, OrderState.ACTIVE, OrderState.PAUSED);
        }

        public void cancelOrder(String orderId) {
            transformOrder(orderId, OrderState.CANCELLED,OrderState.ACTIVE, OrderState.PAUSED);
        }

        private void transformOrder(String orderId, OrderState toState, OrderState... allowedStates) {
            Order order = getOrder(orderId);
            if (order == null) return;
            boolean isAllowed = false;
            for (OrderState allowedState : allowedStates) {
                if (allowedState.equals(order.state)) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) throw new IllegalStateException();
            Order newOrder = new Order(order.orderId, order.userId, order.symbol, order.quantity, toState);
            orders.put(orderId, newOrder);
        }
    }

    // ====================================================================
    // PART 3  —  Cancel All from User                               [⚠ TODO]
    // ====================================================================
    // 场景: 风控/用户操作常见需求 —— "把这个用户的所有挂单一键撤掉" (比如账号
    //       异常、用户点'全部取消')。难点是怎么快速找到某用户的全部订单, 并且
    //       只取消还活着的 (ACTIVE/PAUSED), 已经 CANCELLED/FILLED 的跳过。
    //
    // ★ 复用: placeOrder/getOrder/pauseOrder/resumeOrder/cancelOrder 全部跟 Part 2 一样, 直接搬。
    //         本 Part 只需新写 cancelAllOrdersForUser。
    //
    // 取消该 user 所有 ACTIVE+PAUSED 订单, 返回**实际取消数量**.
    //
    //   userA: o1(ACTIVE), o2(PAUSED), o3(CANCELLED), o4(ACTIVE)
    //   cancelAllOrdersForUser("userA")  → 返回 3  (o3 跳过)
    //
    // 注意:
    //   - 不要把已 CANCELLED/FILLED 的算进取消数

    public static class OmsPart3 {
        Map<String, Order> orders;                          // ← 复用自 Part 2
        Map<String, Set<String>> ordersByUid;
        public OmsPart3() {
            this.orders = new HashMap<>();
            this.ordersByUid = new HashMap<>();
        }

        public void placeOrder(String orderId, String userId, String symbol, long quantity) {
            if (orders.containsKey(orderId)) throw new IllegalArgumentException();
            Order order = new Order(orderId, userId, symbol, quantity, OrderState.ACTIVE);
            orders.put(orderId, order);
            ordersByUid.putIfAbsent(userId, new HashSet<>());
            ordersByUid.get(userId).add(orderId);
        }

        public Order getOrder(String orderId) {
            return orders.get(orderId);
        }

        public void pauseOrder(String orderId) {
            transformOrder(orderId, OrderState.PAUSED, OrderState.ACTIVE);
        }

        public void resumeOrder(String orderId) {
            transformOrder(orderId, OrderState.ACTIVE, OrderState.PAUSED);
        }

        public void cancelOrder(String orderId) {
            transformOrder(orderId, OrderState.CANCELLED, OrderState.ACTIVE, OrderState.PAUSED);
        }

        private void transformOrder(String orderId, OrderState toState, OrderState... allowedStates) {
            Order order = getOrder(orderId);
            if (order == null) return;
            boolean isAllowed = false;
            for (OrderState allowedState : allowedStates) {
                if (allowedState == order.state()) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) throw new IllegalStateException();
            orders.put(orderId, new Order(order.orderId(), order.userId(), order.symbol(), order.quantity(), toState));
        }

        public int cancelAllOrdersForUser(String userId) {
            int cancelledCount = 0;
            for (String orderId : ordersByUid.getOrDefault(userId, new HashSet<>())) {
                Order order = getOrder(orderId);
                if (order.state == OrderState.ACTIVE || order.state == OrderState.PAUSED) {
                    cancelOrder(orderId);
                    cancelledCount++;
                }
            }
            return cancelledCount;
        }
    }

    // ====================================================================
    // PART 4  —  Query by State                                     [⚠ TODO]
    // ====================================================================
    // 场景: 运营面板要问 "现在所有 ACTIVE 的单有哪些?"、"这个用户 PAUSED 的单有哪些?"。
    //
    // ★ 复用: 前面 6 个方法 (place/get/pause/resume/cancel/cancelAllOrdersForUser) 全部跟 Part 3 一样, 直接搬。
    //         本 Part 只需新写 ordersInState / ordersInStateForUser。
    //
    // ordersInState(state) → 所有处于该 state 的订单 (顺序不指定)
    // ordersInStateForUser(state, userId) → 同上 + 按 user 过滤

    public static class OmsPart4 {
        Map<String, Order> orders;                          // ← 复用自 Part 3
        Map<String, Set<String>> ordersByUid;
        public OmsPart4() {
            this.orders = new HashMap<>();
            this.ordersByUid = new HashMap<>();
        }

        public void placeOrder(String orderId, String userId, String symbol, long quantity) {
            if (orders.containsKey(orderId)) throw new IllegalArgumentException();
            Order order = new Order(orderId, userId, symbol, quantity, OrderState.ACTIVE);
            orders.put(orderId, order);
            ordersByUid.computeIfAbsent(userId, k -> new HashSet<>()).add(orderId);
        }

        public Order getOrder(String orderId) {
            return orders.get(orderId);
        }

        public void pauseOrder(String orderId) {
            transformOrder(orderId, OrderState.PAUSED, OrderState.ACTIVE);
        }

        public void resumeOrder(String orderId) {
            transformOrder(orderId, OrderState.ACTIVE, OrderState.PAUSED);
        }

        public void cancelOrder(String orderId) {
            transformOrder(orderId, OrderState.CANCELLED, OrderState.ACTIVE, OrderState.PAUSED);
        }

        private void transformOrder(String orderId, OrderState toState, OrderState... allowedStates) {
            Order order = getOrder(orderId);
            if (order == null) return;
            boolean isAllowed = false;
            for (OrderState allowedState : allowedStates) {
                if (allowedState == order.state()) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) throw new IllegalStateException();
            orders.put(orderId, new Order(order.orderId(), order.userId(), order.symbol(), order.quantity(), toState));
        }

        public int cancelAllOrdersForUser(String userId) {
            int cancelledCount = 0;
            for (String orderId : ordersByUid.getOrDefault(userId, new HashSet<>())) {
                Order order = getOrder(orderId);
                if (order.state() == OrderState.ACTIVE || order.state() == OrderState.PAUSED) {
                    cancelOrder(orderId);
                    cancelledCount++;
                }
            }
            return cancelledCount;
        }

        public List<Order> ordersInState(OrderState state) {
            List<Order> ordersInState = new ArrayList<>();
            for (String orderId : orders.keySet()) {
                Order order = getOrder(orderId);
                if (order.state() == state) {
                    ordersInState.add(orders.get(orderId));
                }
            }
            return ordersInState;
        }

        public List<Order> ordersInStateForUser(OrderState state, String userId) {
            List<Order> ordersInState = new ArrayList<>();
            for (String orderId : ordersByUid.getOrDefault(userId, new HashSet<>())) {
                Order order = getOrder(orderId);
                if (order.state() == state) {
                    ordersInState.add(orders.get(orderId));
                }
            }
            return ordersInState;
        }
    }

    // ====================================================================
    // PART 5  —  并发安全 (thread-safety)                            [⚠ TODO]
    // ====================================================================
    // 场景: 真实交易所里成千上万个请求同时打进来 —— 用户在取消、撮合引擎在成交、
    //       运营在查询, 全是并发的。单线程能跑通的逻辑, 多个线程同时调就可能出错。
    //
    // ★ 复用: 8 个方法的"业务逻辑"全部跟 Part 4 一样 —— 本 Part 不写新逻辑,
    //         而是给同样的逻辑加上线程安全 (选锁 / 并发集合 / CAS)。
    //
    // 要求:
    //   - 对外接口形状跟 Part 4 完全一样 (没有新方法)。
    //   - N 个线程并发调用, 所有方法必须线程安全。
    //   - 一次状态转移必须原子, 不能被打断到"改了一半"。
    //   - ordersInState* 要返回一个一致的快照。
    //
    // (并发策略的取舍见 README。)

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
    // 场景: 这是从"订单管理"跳到"真交易所"的一步。前面只是 track 订单状态, 现在
    //       要让买单和卖单真正**撮合成交**。真正的限价单 (limit order) 带方向
    //       (BUY/SELL) 和价格 (price), 所有没成交的挂单组织成一个"订单簿"。
    //
    //       撮合规则 = price-time priority (价格优先, 同价时间优先):
    //         同一个 symbol 内, 买单价高的先成交、卖单价低的先成交; 同价的先到先成交。
    //       新单进来: 如果对手盘有能成交的价位, 立刻吃单; 吃不完(或没对手盘)就挂上簿。
    //
    // 你要写的接口:
    //   submitLimitOrder(orderId, userId, symbol, side, price, quantity)
    //   bestBid(symbol) / bestAsk(symbol) -> Long      (没单返 null)
    //   depthAtPrice(symbol, side, price) -> long       (该价位档上累计 quantity)
    //   cancelLimitOrder(orderId)                       (从订单簿摘掉, 状态变 CANCELLED)
    //
    // (撮合相关的 follow-up 讨论见 README。)

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

        // ----- 数据结构选择 (operations decide structure) ---------------------
        //
        // 两套存储, 服务两种不同的访问模式 —— 谁是 single source of truth?
        //
        //   ordersById : Map<orderId, LimitOrder>
        //       按 id 取 / 改状态 / 取消. 这是【真相源】—— 订单的最新状态以这里为准.
        //
        //   booksBySymbol : Map<symbol, OrderBook>
        //       按价格组织, 服务撮合 / bestBid / bestAsk / depthAtPrice.
        //       它只存"还挂在簿上的剩余量", 是为读速度做的【反范式冗余】,
        //       必须和 ordersById 保持同步 (这是本题最容易出 bug 的不变量).
        //
        // OrderBook 内部: 买卖两侧各一棵 TreeMap<price, PriceLevel>.
        //   bids 用 lastKey() 取最高买价, asks 用 firstKey() 取最低卖价.
        //   同一价位内用 PriceLevel 维护 FIFO (price-time priority 的 time 部分).

        /** 单个价位上的所有挂单, 按到达顺序排队 (FIFO), 并缓存该价位总量. */
        private static final class PriceLevel {
            // LinkedHashMap 保留插入顺序 => 先到的先成交.
            final LinkedHashMap<String, Long> restingQtyByOrderId = new LinkedHashMap<>();
            long totalQty = 0;                 // = sum(restingQtyByOrderId.values()), 冗余缓存

            void add(String orderId, long qty) {
                restingQtyByOrderId.merge(orderId, qty, Long::sum);
                totalQty += qty;
            }

            void remove(String orderId) {
                Long qty = restingQtyByOrderId.remove(orderId);
                if (qty != null) totalQty -= qty;
            }

            boolean isEmpty() { return restingQtyByOrderId.isEmpty(); }
        }

        /** 一个交易对的订单簿: 买侧 + 卖侧两棵价格树. */
        private static final class OrderBook {
            final TreeMap<Long, PriceLevel> bids = new TreeMap<>();   // 买: 价高者优先 -> lastKey
            final TreeMap<Long, PriceLevel> asks = new TreeMap<>();   // 卖: 价低者优先 -> firstKey

            TreeMap<Long, PriceLevel> sameSide(Side side) { return side == Side.BUY ? bids : asks; }
            TreeMap<Long, PriceLevel> oppositeSide(Side side) { return side == Side.BUY ? asks : bids; }
        }

        private final Map<String, LimitOrder> ordersById = new HashMap<>();
        private final Map<String, OrderBook> booksBySymbol = new HashMap<>();

        public OmsPart6() {}

        public void submitLimitOrder(String orderId, String userId, String symbol,
                                     Side side, long price, long quantity) {
            if (ordersById.containsKey(orderId)) throw new IllegalArgumentException("dup orderId");

            OrderBook book = booksBySymbol.computeIfAbsent(symbol, k -> new OrderBook());

            // 先登记真相源 (初始 ACTIVE), 撮合过程中再回写状态/剩余量.
            ordersById.put(orderId, new LimitOrder(orderId, userId, symbol, side, price, quantity, OrderState.ACTIVE));

            // ---- 撮合循环: 吃掉对手盘里所有能成交的价位 ----
            long remaining = matchAgainstBook(book, side, price, quantity);

            // ---- 剩余量挂上本侧簿 ----
            if (remaining > 0) {
                book.sameSide(side).computeIfAbsent(price, p -> new PriceLevel()).add(orderId, remaining);
                // 状态保持 ACTIVE; 剩余量写回真相源
                ordersById.put(orderId, withQtyAndState(orderId, remaining, OrderState.ACTIVE));
            } else {
                // 全部吃完, 不进簿, 标 FILLED
                ordersById.put(orderId, withQtyAndState(orderId, 0, OrderState.FILLED));
            }
        }

        /**
         * 用一笔 incoming 订单去吃对手盘, 返回未成交的剩余量.
         * BUY 吃 asks(从最低价开始), 只要 incomingPrice >= 卖价就成交;
         * SELL 吃 bids(从最高价开始), 只要 incomingPrice <= 买价就成交.
         * BUY/SELL 对称: 只有"取哪一侧 / 取哪一端 / 价格比较方向"不同.
         */
        private long matchAgainstBook(OrderBook book, Side side, long price, long quantity) {
            TreeMap<Long, PriceLevel> opposite = book.oppositeSide(side);
            long remaining = quantity;

            while (remaining > 0 && !opposite.isEmpty()) {
                long bestPrice = (side == Side.BUY) ? opposite.firstKey() : opposite.lastKey();
                boolean crosses = (side == Side.BUY) ? price >= bestPrice : price <= bestPrice;
                if (!crosses) break;

                PriceLevel level = opposite.get(bestPrice);
                // 在该价位内按 FIFO 逐单成交
                Iterator<Map.Entry<String, Long>> it = level.restingQtyByOrderId.entrySet().iterator();
                while (remaining > 0 && it.hasNext()) {
                    Map.Entry<String, Long> resting = it.next();
                    String restingId = resting.getKey();
                    long restingQty = resting.getValue();
                    long traded = Math.min(remaining, restingQty);

                    remaining -= traded;
                    long restingLeft = restingQty - traded;
                    level.totalQty -= traded;

                    if (restingLeft == 0) {
                        it.remove();                                          // 从价位摘掉
                        ordersById.put(restingId, withQtyAndState(restingId, 0, OrderState.FILLED));
                    } else {
                        resting.setValue(restingLeft);
                        ordersById.put(restingId, withQtyAndState(restingId, restingLeft, OrderState.ACTIVE));
                    }
                }
                if (level.isEmpty()) opposite.remove(bestPrice);             // 价位空了就删, 维持 bestBid/Ask 正确
            }
            return remaining;
        }

        public LimitOrder getLimitOrder(String orderId) {
            return ordersById.get(orderId);
        }

        public Long bestBid(String symbol) {
            OrderBook book = booksBySymbol.get(symbol);
            if (book == null || book.bids.isEmpty()) return null;
            return book.bids.lastKey();                  // 最高买价
        }

        public Long bestAsk(String symbol) {
            OrderBook book = booksBySymbol.get(symbol);
            if (book == null || book.asks.isEmpty()) return null;
            return book.asks.firstKey();                 // 最低卖价
        }

        public long depthAtPrice(String symbol, Side side, long price) {
            OrderBook book = booksBySymbol.get(symbol);
            if (book == null) return 0;
            PriceLevel level = book.sameSide(side).get(price);
            return level == null ? 0 : level.totalQty;   // O(1) 靠缓存的 totalQty
        }

        public void cancelLimitOrder(String orderId) {
            LimitOrder order = ordersById.get(orderId);
            if (order == null) return;
            if (order.state() != OrderState.ACTIVE) return;   // FILLED/已取消不可再取消

            // 从簿上摘掉剩余量, 再改状态 —— 两套存储一起更新, 保持同步.
            OrderBook book = booksBySymbol.get(order.symbol());
            if (book != null) {
                TreeMap<Long, PriceLevel> sameSide = book.sameSide(order.side());
                PriceLevel level = sameSide.get(order.price());
                if (level != null) {
                    level.remove(orderId);
                    if (level.isEmpty()) sameSide.remove(order.price());
                }
            }
            ordersById.put(orderId, withQtyAndState(orderId, order.quantity(), OrderState.CANCELLED));
        }

        /** 拿当前订单, 换掉剩余量和状态, 其余字段不变 (record 不可变, 只能新建). */
        private LimitOrder withQtyAndState(String orderId, long quantity, OrderState state) {
            LimitOrder o = ordersById.get(orderId);
            return new LimitOrder(o.orderId(), o.userId(), o.symbol(), o.side(), o.price(), quantity, state);
        }
    }

    // ====================================================================
    // PART 7  —  订单类型扩展 (market / IOC / FOK / stop)            [⚠ TODO]
    // ====================================================================
    // 场景: Part 6 只有最普通的"挂着等成交"的限价单 (GTC limit)。真实交易所还有
    //       好几种订单类型, 它们的区别在于"撮合完剩下的量怎么办"和"什么时候才激活":
    //
    //         - MARKET:      不指定价格, 直接按对手盘最优价吃, 吃不完的直接丢弃。
    //         - LIMIT + IOC: (Immediate-Or-Cancel) 能吃多少吃多少, 剩余立刻取消, 不挂簿。
    //         - LIMIT + FOK: (Fill-Or-Kill) 要么一次全部成交, 要么整单拒绝 (差 1 都不行)。
    //         - STOP:        平时不激活, 等市场价穿过某个 trigger 价位才转成普通单进场。
    //
    //       这一 Part 复用 Part 6 的订单簿和撮合, 新增 OrderType + TimeInForce 两个维度,
    //       下单时附带, submitOrder 要按类型给出对应行为。
    //
    // (各订单类型的实现 follow-up 讨论见 README。)

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
    // 场景: 交易所是金融系统, 进程崩了重启后, 那些还没成交的挂单**必须能恢复**,
    //       不能凭空消失。做法是每次写操作先记一条日志到磁盘 (WAL, write-ahead log),
    //       重启时把日志重放一遍重建内存里的订单簿。
    //
    //       同时, 每当撮合产生成交, 要把"成交了 / 某价位深度变了"这些市场数据
    //       广播 (broadcast) 给所有订阅的客户端 (就是你在交易 App 上看到的实时跳动)。
    //
    // 新接口: 构造时 replay(walPath) 恢复; subscribe/unsubscribe 订阅市场数据;
    //         flush() 强制把 WAL 落盘 (fsync)。撮合/订单簿/TIF 全部继承前面。
    //
    // (持久化与广播的 follow-up 讨论见 README。这道 Part 偏设计讨论, 代码量大, 起几个 stub 即可。)

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
