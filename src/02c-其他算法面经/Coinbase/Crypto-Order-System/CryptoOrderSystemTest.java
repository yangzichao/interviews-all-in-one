import java.util.*;

import static java.util.Objects.requireNonNull;

public class CryptoOrderSystemTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", CryptoOrderSystemTest::testPart1);
        tests.put("part2", CryptoOrderSystemTest::testPart2);
        tests.put("part3", CryptoOrderSystemTest::testPart3);
        tests.put("part4", CryptoOrderSystemTest::testPart4);
        tests.put("part5", CryptoOrderSystemTest::testPart5);
        tests.put("part6", CryptoOrderSystemTest::testPart6);
        tests.put("part7", CryptoOrderSystemTest::testPart7);
        tests.put("part8", CryptoOrderSystemTest::testPart8);

        List<String> toRun = args.length == 0 ? new ArrayList<>(tests.keySet()) : Arrays.asList(args);
        for (String name : toRun) {
            Runnable t = tests.get(name);
            if (t == null) {
                System.out.println("unknown part: " + name + ", available: " + tests.keySet());
                System.exit(2);
            }
            run(name, t);
        }
        System.out.printf("%nPassed=%d  Failed=%d  Skipped=%d%n", passed, failed, skipped);
        if (failed > 0) System.exit(1);
    }

    static void run(String name, Runnable test) {
        String label = "Part " + name.substring(4);
        try {
            test.run();
            System.out.println(label + " PASSED");
            passed++;
        } catch (UnsupportedOperationException e) {
            System.out.println(label + " SKIPPED (not implemented)");
            skipped++;
        } catch (AssertionError e) {
            System.out.println(label + " FAILED: " + e.getMessage());
            failed++;
        } catch (Throwable e) {
            System.out.println(label + " ERROR: " + e);
            e.printStackTrace(System.out);
            failed++;
        }
    }

    static void assertEq(Object expected, Object actual, String msg) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(msg + " — expected: " + expected + ", actual: " + actual);
        }
    }

    static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    static void assertThrows(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) return;
            throw new AssertionError(msg + " — wrong exception: " + t.getClass().getSimpleName() + " (" + t.getMessage() + ")");
        }
        throw new AssertionError(msg + " — expected " + ex.getSimpleName() + ", nothing thrown");
    }

    // ===== Part 1: Basic Place / Get ====================================

    static void testPart1() {
        CryptoOrderSystem.OmsPart1 oms = new CryptoOrderSystem.OmsPart1();

        oms.placeOrder("o1", "alice", "BTC-USD", 100);
        var o = oms.getOrder("o1");
        assertTrue(o != null, "placed order should be retrievable");
        assertEq("o1", o.orderId(), "orderId");
        assertEq("alice", o.userId(), "userId");
        assertEq("BTC-USD", o.symbol(), "symbol");
        assertEq(100L, o.quantity(), "quantity");
        assertEq(CryptoOrderSystem.OrderState.ACTIVE, o.state(), "new order is ACTIVE");

        // missing → null
        assertEq(null, oms.getOrder("nope"), "missing returns null, not throw");

        // duplicate → throw
        assertThrows(IllegalArgumentException.class,
                () -> oms.placeOrder("o1", "bob", "ETH-USD", 50),
                "duplicate orderId should throw");
    }

    // ===== Part 2: Pause / Resume / Cancel ==============================

    static void testPart2() {
        CryptoOrderSystem.OmsPart2 oms = new CryptoOrderSystem.OmsPart2();
        oms.placeOrder("o1", "alice", "BTC-USD", 100);
        assertEq(CryptoOrderSystem.OrderState.ACTIVE, oms.getOrder("o1").state(), "starts ACTIVE");

        // ACTIVE → PAUSED
        oms.pauseOrder("o1");
        assertEq(CryptoOrderSystem.OrderState.PAUSED, oms.getOrder("o1").state(), "after pause");

        // PAUSED → PAUSED 非法
        assertThrows(IllegalStateException.class,
                () -> oms.pauseOrder("o1"),
                "double pause should throw");

        // PAUSED → ACTIVE
        oms.resumeOrder("o1");
        assertEq(CryptoOrderSystem.OrderState.ACTIVE, oms.getOrder("o1").state(), "after resume");

        // ACTIVE → ACTIVE 非法
        assertThrows(IllegalStateException.class,
                () -> oms.resumeOrder("o1"),
                "resume while ACTIVE should throw");

        // ACTIVE → CANCELLED
        oms.cancelOrder("o1");
        assertEq(CryptoOrderSystem.OrderState.CANCELLED, oms.getOrder("o1").state(), "after cancel");

        // CANCELLED → * 全非法
        assertThrows(IllegalStateException.class,
                () -> oms.pauseOrder("o1"),
                "pause on CANCELLED should throw");
        assertThrows(IllegalStateException.class,
                () -> oms.resumeOrder("o1"),
                "resume on CANCELLED should throw");
        assertThrows(IllegalStateException.class,
                () -> oms.cancelOrder("o1"),
                "double cancel should throw");

        // PAUSED → CANCELLED 直接合法
        oms.placeOrder("o2", "bob", "ETH-USD", 50);
        oms.pauseOrder("o2");
        oms.cancelOrder("o2");
        assertEq(CryptoOrderSystem.OrderState.CANCELLED, oms.getOrder("o2").state(), "paused → cancelled");
    }

    // ===== Part 3: Cancel All from User =================================

    static void testPart3() {
        CryptoOrderSystem.OmsPart3 oms = new CryptoOrderSystem.OmsPart3();

        oms.placeOrder("a1", "alice", "BTC-USD", 10);
        oms.placeOrder("a2", "alice", "BTC-USD", 20);
        oms.placeOrder("a3", "alice", "BTC-USD", 30);
        oms.placeOrder("a4", "alice", "BTC-USD", 40);
        oms.placeOrder("b1", "bob", "ETH-USD", 100);

        oms.pauseOrder("a2");
        oms.cancelOrder("a3");  // 已 CANCELLED, 不该被算

        int n = oms.cancelAllOrdersForUser("alice");
        assertEq(3, n, "cancelled count (a1 ACTIVE + a2 PAUSED + a4 ACTIVE = 3)");

        assertEq(CryptoOrderSystem.OrderState.CANCELLED, oms.getOrder("a1").state(), "a1 cancelled");
        assertEq(CryptoOrderSystem.OrderState.CANCELLED, oms.getOrder("a2").state(), "a2 cancelled");
        assertEq(CryptoOrderSystem.OrderState.CANCELLED, oms.getOrder("a3").state(), "a3 still cancelled");
        assertEq(CryptoOrderSystem.OrderState.CANCELLED, oms.getOrder("a4").state(), "a4 cancelled");
        assertEq(CryptoOrderSystem.OrderState.ACTIVE, oms.getOrder("b1").state(), "bob untouched");

        // 再来一次 → 应该是 0
        assertEq(0, oms.cancelAllOrdersForUser("alice"), "second call returns 0");

        // 不存在的 user → 0
        assertEq(0, oms.cancelAllOrdersForUser("ghost"), "unknown user returns 0");
    }

    // ===== Part 4: Query by State =======================================

    static void testPart4() {
        CryptoOrderSystem.OmsPart4 oms = new CryptoOrderSystem.OmsPart4();

        oms.placeOrder("a1", "alice", "BTC-USD", 10);
        oms.placeOrder("a2", "alice", "BTC-USD", 20);
        oms.placeOrder("a3", "alice", "BTC-USD", 30);
        oms.placeOrder("b1", "bob", "ETH-USD", 100);
        oms.placeOrder("b2", "bob", "ETH-USD", 200);

        oms.pauseOrder("a2");
        oms.cancelOrder("a3");

        // ordersInState(ACTIVE) → a1, b1, b2
        var active = new HashSet<>(idsOf(oms.ordersInState(CryptoOrderSystem.OrderState.ACTIVE)));
        assertEq(Set.of("a1", "b1", "b2"), active, "all ACTIVE");

        var paused = new HashSet<>(idsOf(oms.ordersInState(CryptoOrderSystem.OrderState.PAUSED)));
        assertEq(Set.of("a2"), paused, "all PAUSED");

        var cancelled = new HashSet<>(idsOf(oms.ordersInState(CryptoOrderSystem.OrderState.CANCELLED)));
        assertEq(Set.of("a3"), cancelled, "all CANCELLED");

        var filled = new HashSet<>(idsOf(oms.ordersInState(CryptoOrderSystem.OrderState.FILLED)));
        assertEq(Set.of(), filled, "no FILLED");

        // per-user
        var aliceActive = new HashSet<>(idsOf(
                oms.ordersInStateForUser(CryptoOrderSystem.OrderState.ACTIVE, "alice")));
        assertEq(Set.of("a1"), aliceActive, "alice ACTIVE");

        var bobActive = new HashSet<>(idsOf(
                oms.ordersInStateForUser(CryptoOrderSystem.OrderState.ACTIVE, "bob")));
        assertEq(Set.of("b1", "b2"), bobActive, "bob ACTIVE");

        var aliceCancelled = new HashSet<>(idsOf(
                oms.ordersInStateForUser(CryptoOrderSystem.OrderState.CANCELLED, "alice")));
        assertEq(Set.of("a3"), aliceCancelled, "alice CANCELLED");

        // 不存在的 user → 空
        var ghost = new HashSet<>(idsOf(
                oms.ordersInStateForUser(CryptoOrderSystem.OrderState.ACTIVE, "ghost")));
        assertEq(Set.of(), ghost, "unknown user returns empty");

        // state 变更后 index 同步
        oms.pauseOrder("a1");
        var aliceActive2 = new HashSet<>(idsOf(
                oms.ordersInStateForUser(CryptoOrderSystem.OrderState.ACTIVE, "alice")));
        assertEq(Set.of(), aliceActive2, "alice no longer has ACTIVE after pause");
        var alicePaused = new HashSet<>(idsOf(
                oms.ordersInStateForUser(CryptoOrderSystem.OrderState.PAUSED, "alice")));
        assertEq(Set.of("a1", "a2"), alicePaused, "alice now has 2 PAUSED");
    }

    static List<String> idsOf(List<CryptoOrderSystem.Order> orders) {
        requireNonNull(orders, "ordersInState* must not return null");
        List<String> out = new ArrayList<>();
        for (var o : orders) out.add(o.orderId());
        return out;
    }

    // ===== Part 5: 并发安全 =============================================
    // Part 5 在接口形状上跟 Part 4 一致 —— 这里跑一遍 Part 4 的功能用例 + 一个轻量并发用例.
    // 真实"线程安全"的压测请自己用 ExecutorService 多跑几万次.

    static void testPart5() {
        CryptoOrderSystem.OmsPart5 oms = new CryptoOrderSystem.OmsPart5();

        oms.placeOrder("a1", "alice", "BTC-USD", 10);
        oms.placeOrder("a2", "alice", "BTC-USD", 20);
        oms.placeOrder("b1", "bob", "ETH-USD", 100);

        oms.pauseOrder("a2");
        assertEq(CryptoOrderSystem.OrderState.PAUSED, oms.getOrder("a2").state(), "single-thread pause works");

        int cancelled = oms.cancelAllOrdersForUser("alice");
        assertEq(2, cancelled, "cancelAllOrdersForUser counts");
        assertEq(CryptoOrderSystem.OrderState.ACTIVE, oms.getOrder("b1").state(), "bob untouched");

        var bobActive = new HashSet<>(idsOf(
                oms.ordersInStateForUser(CryptoOrderSystem.OrderState.ACTIVE, "bob")));
        assertEq(Set.of("b1"), bobActive, "bob ACTIVE list");

        // 多线程小压: N 个线程各 place 1 个 order, 不能丢
        int N = 32;
        Thread[] threads = new Thread[N];
        for (int i = 0; i < N; i++) {
            final int idx = i;
            threads[i] = new Thread(() -> oms.placeOrder("t" + idx, "user" + (idx % 4), "BTC-USD", idx + 1));
        }
        for (var t : threads) t.start();
        try {
            for (var t : threads) t.join();
        } catch (InterruptedException e) {
            throw new AssertionError("interrupted");
        }
        for (int i = 0; i < N; i++) {
            assertTrue(oms.getOrder("t" + i) != null, "concurrent place: t" + i + " present");
        }
    }

    // ===== Part 6: 撮合引擎 + 订单簿 ====================================

    static void testPart6() {
        CryptoOrderSystem.OmsPart6 oms = new CryptoOrderSystem.OmsPart6();

        // 单边挂簿: 没对手就停留
        oms.submitLimitOrder("b1", "alice", "BTC-USD", CryptoOrderSystem.Side.BUY, 100L, 5L);
        assertEq(Long.valueOf(100L), oms.bestBid("BTC-USD"), "bestBid after lone buy");
        assertEq(null, oms.bestAsk("BTC-USD"), "no ask yet");
        assertEq(5L, oms.depthAtPrice("BTC-USD", CryptoOrderSystem.Side.BUY, 100L), "depth @100");

        // 加另一个买单, 价高优先
        oms.submitLimitOrder("b2", "alice", "BTC-USD", CryptoOrderSystem.Side.BUY, 101L, 3L);
        assertEq(Long.valueOf(101L), oms.bestBid("BTC-USD"), "bestBid moved up");

        // 卖单价高于最优买价: 挂簿不撮合
        oms.submitLimitOrder("s1", "bob", "BTC-USD", CryptoOrderSystem.Side.SELL, 105L, 4L);
        assertEq(Long.valueOf(105L), oms.bestAsk("BTC-USD"), "bestAsk");

        // cancel 摘单
        oms.cancelLimitOrder("b1");
        assertEq(0L, oms.depthAtPrice("BTC-USD", CryptoOrderSystem.Side.BUY, 100L), "depth @100 after cancel");
        assertEq(CryptoOrderSystem.OrderState.CANCELLED, oms.getLimitOrder("b1").state(), "b1 cancelled");
    }

    // ===== Part 7: 订单类型扩展 =========================================

    static void testPart7() {
        CryptoOrderSystem.OmsPart7 oms = new CryptoOrderSystem.OmsPart7();

        // 先用 LIMIT GTC 建一些挂单
        oms.submitOrder("s1", "bob", "BTC-USD",
                CryptoOrderSystem.Side.SELL, CryptoOrderSystem.OrderType.LIMIT,
                CryptoOrderSystem.TimeInForce.GTC, 100L, null, 5L);
        oms.submitOrder("s2", "bob", "BTC-USD",
                CryptoOrderSystem.Side.SELL, CryptoOrderSystem.OrderType.LIMIT,
                CryptoOrderSystem.TimeInForce.GTC, 101L, null, 3L);
        assertEq(Long.valueOf(100L), oms.bestAsk("BTC-USD"), "ask side built");

        // MARKET BUY 5 → 吃 s1 全部
        oms.submitOrder("m1", "alice", "BTC-USD",
                CryptoOrderSystem.Side.BUY, CryptoOrderSystem.OrderType.MARKET,
                CryptoOrderSystem.TimeInForce.IOC, null, null, 5L);
        assertEq(100L, oms.lastTradePrice("BTC-USD"), "last trade @100");
        assertEq(Long.valueOf(101L), oms.bestAsk("BTC-USD"), "ask moved up after s1 filled");

        // FOK BUY 10 @101: 只有 3 单, 应该全拒
        oms.submitOrder("f1", "alice", "BTC-USD",
                CryptoOrderSystem.Side.BUY, CryptoOrderSystem.OrderType.LIMIT,
                CryptoOrderSystem.TimeInForce.FOK, 101L, null, 10L);
        assertEq(Long.valueOf(101L), oms.bestAsk("BTC-USD"), "FOK rejected, ask untouched");
    }

    // ===== Part 8: WAL + 市场数据广播 ===================================

    static void testPart8() {
        CryptoOrderSystem.OmsPart8 oms = new CryptoOrderSystem.OmsPart8("/tmp/coinbase-oms.wal");

        // 收集广播
        List<String> trades = Collections.synchronizedList(new ArrayList<>());
        CryptoOrderSystem.MarketDataListener listener = new CryptoOrderSystem.MarketDataListener() {
            @Override
            public void onTrade(String symbol, long price, long quantity) {
                trades.add(symbol + "@" + price + "x" + quantity);
            }
            @Override
            public void onBookDelta(String symbol, CryptoOrderSystem.Side side, long price, long newDepth) {
                // ignore for this test
            }
        };
        String subId = oms.subscribe("BTC-USD", listener);
        assertTrue(subId != null && !subId.isEmpty(), "subscribe returns id");

        oms.submitOrder("s1", "bob", "BTC-USD",
                CryptoOrderSystem.Side.SELL, CryptoOrderSystem.OrderType.LIMIT,
                CryptoOrderSystem.TimeInForce.GTC, 100L, null, 5L);
        oms.submitOrder("b1", "alice", "BTC-USD",
                CryptoOrderSystem.Side.BUY, CryptoOrderSystem.OrderType.MARKET,
                CryptoOrderSystem.TimeInForce.IOC, null, null, 5L);

        oms.flush();  // fsync WAL

        assertTrue(trades.size() >= 1, "listener should have received at least 1 trade");
        oms.unsubscribe(subId);
    }
}
