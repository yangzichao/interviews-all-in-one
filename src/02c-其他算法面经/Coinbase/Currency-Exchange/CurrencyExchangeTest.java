import java.util.*;

public class CurrencyExchangeTest {

    static int passed = 0, failed = 0, skipped = 0;
    static final double EPS = 1e-9;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", CurrencyExchangeTest::testPart1);
        tests.put("part2", CurrencyExchangeTest::testPart2);
        tests.put("part3", CurrencyExchangeTest::testPart3);
        tests.put("part4", CurrencyExchangeTest::testPart4);
        tests.put("part5", CurrencyExchangeTest::testPart5);
        tests.put("part6", CurrencyExchangeTest::testPart6);
        tests.put("part7", CurrencyExchangeTest::testPart7);
        tests.put("part8", CurrencyExchangeTest::testPart8);

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

    static void assertClose(double expected, double actual, String msg) {
        if (Math.abs(expected - actual) > EPS) {
            throw new AssertionError(msg + " — expected: " + expected + ", actual: " + actual);
        }
    }

    static List<String[]> rates(String[]... rs) {
        return Arrays.asList(rs);
    }

    // ===== Part 1: Basic Conversion =====================================

    static void testPart1() {
        CurrencyExchange.ConverterPart1 c = new CurrencyExchange.ConverterPart1(rates(
                new String[]{"USD", "EUR", "0.9"},
                new String[]{"EUR", "GBP", "0.85"}
        ));

        assertClose(76.5, c.convert("USD", "GBP", 100), "USD→EUR→GBP chain");
        assertClose(50.0, c.convert("USD", "USD", 50), "same currency identity");
        assertClose(90.0, c.convert("USD", "EUR", 100), "direct edge");
        assertClose(100.0 / 0.9, c.convert("EUR", "USD", 100), "reverse edge = 1/rate");

        // unknown currency
        boolean threw = false;
        try { c.convert("USD", "JPY", 1); } catch (NoSuchElementException e) { threw = true; }
        assertTrue(threw, "unknown 'to' throws NoSuchElementException");

        threw = false;
        try { c.convert("JPY", "USD", 1); } catch (NoSuchElementException e) { threw = true; }
        assertTrue(threw, "unknown 'from' throws NoSuchElementException");

        // disconnected components
        CurrencyExchange.ConverterPart1 c2 = new CurrencyExchange.ConverterPart1(rates(
                new String[]{"USD", "EUR", "0.9"},
                new String[]{"JPY", "KRW", "10.0"}
        ));
        threw = false;
        try { c2.convert("USD", "JPY", 1); } catch (NoSuchElementException e) { threw = true; }
        assertTrue(threw, "unreachable path throws NoSuchElementException");
    }

    // ===== Part 2: Best Rate ============================================

    static void testPart2() {
        // direct edge is worse than the chain → pick the chain
        CurrencyExchange.BestRateConverterPart2 c = new CurrencyExchange.BestRateConverterPart2(rates(
                new String[]{"USD", "EUR", "0.9"},
                new String[]{"EUR", "GBP", "0.85"},
                new String[]{"USD", "GBP", "0.7"}
        ));
        assertClose(76.5, c.convertBest("USD", "GBP", 100), "best path beats direct edge");

        // direct edge is better than the chain → pick the direct edge
        CurrencyExchange.BestRateConverterPart2 c2 = new CurrencyExchange.BestRateConverterPart2(rates(
                new String[]{"USD", "EUR", "0.5"},
                new String[]{"EUR", "GBP", "0.5"},
                new String[]{"USD", "GBP", "0.9"}
        ));
        assertClose(90.0, c2.convertBest("USD", "GBP", 100), "direct edge beats chain");

        // same currency
        assertClose(42.0, c.convertBest("USD", "USD", 42), "same currency identity");

        // unreachable
        boolean threw = false;
        try { c.convertBest("USD", "JPY", 1); } catch (NoSuchElementException e) { threw = true; }
        assertTrue(threw, "unreachable throws NoSuchElementException");
    }

    // ===== Part 3: Streaming Updates ====================================

    static void testPart3() {
        CurrencyExchange.StreamingConverterPart3 c = new CurrencyExchange.StreamingConverterPart3();
        c.update("USD", "EUR", 0.9);
        c.update("EUR", "GBP", 0.85);
        assertClose(76.5, c.convert("USD", "GBP", 100), "after two updates");

        // replace an existing edge
        c.update("USD", "EUR", 1.0);
        assertClose(85.0, c.convert("USD", "GBP", 100), "replaced edge takes effect");

        // reverse still derived from latest rate
        assertClose(100.0, c.convert("EUR", "USD", 100), "reverse uses 1/new_rate");

        // unknown currency before any update touching it
        boolean threw = false;
        try { c.convert("USD", "JPY", 1); } catch (NoSuchElementException e) { threw = true; }
        assertTrue(threw, "unknown currency throws");

        // adding a new edge connects components
        c.update("GBP", "JPY", 150.0);
        assertClose(100 * 1.0 * 0.85 * 150.0, c.convert("USD", "JPY", 100), "new edge connects components");

        // same currency identity even on empty graph
        CurrencyExchange.StreamingConverterPart3 empty = new CurrencyExchange.StreamingConverterPart3();
        assertClose(7.0, empty.convert("USD", "USD", 7), "identity on empty graph");
    }

    // ===== Part 4: Arbitrage Detection ==================================

    static void testPart4() {
        // 3-cycle with product > 1 → arbitrage
        CurrencyExchange.ArbitrageDetectorPart4 d1 = new CurrencyExchange.ArbitrageDetectorPart4(rates(
                new String[]{"USD", "EUR", "0.9"},
                new String[]{"EUR", "GBP", "0.85"},
                new String[]{"GBP", "USD", "1.4"}
        ));
        assertEq(true, d1.hasArbitrage(), "3-cycle product 1.071 > 1");

        // 3-cycle with product = 1 exactly → no arbitrage
        CurrencyExchange.ArbitrageDetectorPart4 d2 = new CurrencyExchange.ArbitrageDetectorPart4(rates(
                new String[]{"A", "B", "2.0"},
                new String[]{"B", "C", "3.0"},
                new String[]{"C", "A", String.valueOf(1.0 / 6.0)}
        ));
        assertEq(false, d2.hasArbitrage(), "3-cycle product = 1 is not arbitrage");

        // no cycle at all → no arbitrage
        CurrencyExchange.ArbitrageDetectorPart4 d3 = new CurrencyExchange.ArbitrageDetectorPart4(rates(
                new String[]{"USD", "EUR", "0.9"},
                new String[]{"EUR", "GBP", "0.85"}
        ));
        assertEq(false, d3.hasArbitrage(), "tree (using forward edges only) has no >1 cycle");

        // empty graph
        CurrencyExchange.ArbitrageDetectorPart4 d4 = new CurrencyExchange.ArbitrageDetectorPart4(rates());
        assertEq(false, d4.hasArbitrage(), "empty graph: no arbitrage");
    }

    // ===== Part 5: 并发安全 + 实时汇率流式更新 ============================

    static void testPart5() {
        // Single-threaded smoke: 行为应该跟 Part 3 streaming converter 等价
        CurrencyExchange.ConcurrentStreamingConverterPart5 c =
                new CurrencyExchange.ConcurrentStreamingConverterPart5();
        c.update("USD", "EUR", 0.9);
        c.update("EUR", "GBP", 0.85);
        assertClose(76.5, c.convert("USD", "GBP", 100), "single-thread baseline");

        // 真并发 smoke: N 个 reader 配 1 个 writer, 不抛异常 / 结果落在合法 rate 区间
        final CurrencyExchange.ConcurrentStreamingConverterPart5 cc =
                new CurrencyExchange.ConcurrentStreamingConverterPart5();
        cc.update("USD", "EUR", 1.0);  // 给一个初始 rate
        final int readers = 8;
        final int iterationsPerReader = 200;
        Thread writer = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                cc.update("USD", "EUR", 0.5 + (i % 10) * 0.1);
            }
        });
        Thread[] rs = new Thread[readers];
        final boolean[] anyError = {false};
        for (int i = 0; i < readers; i++) {
            rs[i] = new Thread(() -> {
                try {
                    for (int k = 0; k < iterationsPerReader; k++) {
                        double v = cc.convert("USD", "EUR", 100);
                        // 任何 0.5..1.5 之间的合法 rate * 100 都行
                        if (v < 40 || v > 160) anyError[0] = true;
                    }
                } catch (Throwable t) {
                    anyError[0] = true;
                }
            });
        }
        writer.start();
        for (Thread t : rs) t.start();
        try {
            writer.join();
            for (Thread t : rs) t.join();
        } catch (InterruptedException ignored) {}
        assertTrue(!anyError[0], "concurrent reader/writer must not throw or produce wild values");
    }

    // ===== Part 6: 预计算 best rate (算法选型) ============================

    static void testPart6() {
        CurrencyExchange.PrecomputedBestRateConverterPart6 c =
                new CurrencyExchange.PrecomputedBestRateConverterPart6(rates(
                        new String[]{"USD", "EUR", "0.9"},
                        new String[]{"EUR", "GBP", "0.85"},
                        new String[]{"USD", "GBP", "0.7"}
                ));
        assertClose(76.5, c.convertBest("USD", "GBP", 100), "precomputed: chain beats direct");
        assertClose(42.0, c.convertBest("USD", "USD", 42), "same currency");

        boolean threw = false;
        try { c.convertBest("USD", "JPY", 1); } catch (NoSuchElementException e) { threw = true; }
        assertTrue(threw, "unreachable throws NoSuchElementException");
    }

    // ===== Part 7: 多 source + TTL / staleness ============================

    static void testPart7() {
        long ttl = 5_000L; // 5 秒
        CurrencyExchange.MultiSourceConverterPart7 c =
                new CurrencyExchange.MultiSourceConverterPart7(ttl);

        c.updateWithSource("USD", "EUR", 0.9, "binance", 1000L);
        c.updateWithSource("EUR", "GBP", 0.85, "coinbase", 1000L);

        // 在 TTL 内: 正常 convert
        assertClose(76.5, c.convert("USD", "GBP", 100, 2000L), "fresh rates");

        // 超过 TTL: 抛 stale
        boolean threw = false;
        try { c.convert("USD", "GBP", 100, 10_000L); }
        catch (CurrencyExchange.StaleRateException e) { threw = true; }
        assertTrue(threw, "all rates stale → StaleRateException");

        // 一段 fresh 一段 stale 也算 stale
        c.updateWithSource("USD", "EUR", 0.9, "binance", 9000L); // 刷新这段
        // EUR→GBP 还停在 t=1000, 在 t=10000 时已 stale (差 9000ms > 5000ms TTL)
        threw = false;
        try { c.convert("USD", "GBP", 100, 10_000L); }
        catch (CurrencyExchange.StaleRateException e) { threw = true; }
        assertTrue(threw, "any hop stale → StaleRateException");
    }

    // ===== Part 8: 历史汇率回查 ==========================================

    static void testPart8() {
        CurrencyExchange.HistoricalConverterPart8 c = new CurrencyExchange.HistoricalConverterPart8();
        c.update("USD", "EUR", 0.9, 1000L);
        c.update("USD", "EUR", 1.0, 2000L);
        c.update("EUR", "GBP", 0.85, 1500L);

        // T=1500: USD→EUR=0.9 (latest at or before 1500)
        assertClose(0.9, c.rateAt("USD", "EUR", 1500L), "rateAt T=1500 picks 0.9");
        // T=2500: USD→EUR=1.0
        assertClose(1.0, c.rateAt("USD", "EUR", 2500L), "rateAt T=2500 picks 1.0");

        // convertAt T=2500: USD→EUR=1.0, EUR→GBP=0.85 → 100 * 1.0 * 0.85 = 85
        assertClose(85.0, c.convertAt("USD", "GBP", 100, 2500L), "convertAt T=2500");

        // convertAt T=1500: USD→EUR=0.9, EUR→GBP=0.85 → 100 * 0.9 * 0.85 = 76.5
        assertClose(76.5, c.convertAt("USD", "GBP", 100, 1500L), "convertAt T=1500");

        // 太早 (没数据) 应该 throw
        boolean threw = false;
        try { c.rateAt("USD", "EUR", 500L); } catch (NoSuchElementException e) { threw = true; }
        assertTrue(threw, "rateAt before any update throws");
    }
}
