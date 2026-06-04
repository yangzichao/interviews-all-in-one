import java.util.*;

public class PredictionMarketOrderSystemTest {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", PredictionMarketOrderSystemTest::testPart1);
        tests.put("part2", PredictionMarketOrderSystemTest::testPart2);
        tests.put("part3", PredictionMarketOrderSystemTest::testPart3);

        List<String> testsToRun = args.length == 0 ? new ArrayList<>(tests.keySet()) : Arrays.asList(args);
        for (String testName : testsToRun) {
            Runnable test = tests.get(testName);
            if (test == null) {
                System.out.println("unknown part: " + testName + ", available: " + tests.keySet());
                System.exit(2);
            }
            run(testName, test);
        }

        System.out.printf("%nPassed=%d  Failed=%d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void run(String testName, Runnable test) {
        try {
            test.run();
            System.out.println(testName + " PASSED");
            passed++;
        } catch (Throwable error) {
            System.out.println(testName + " FAILED: " + error.getMessage());
            error.printStackTrace(System.out);
            failed++;
        }
    }

    static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " -- expected: " + expected + ", actual: " + actual);
        }
    }

    static void assertThrows(Class<? extends Throwable> expectedType, Runnable action, String message) {
        try {
            action.run();
        } catch (Throwable error) {
            if (expectedType.isInstance(error)) {
                return;
            }
            throw new AssertionError(message + " -- wrong exception: " + error.getClass().getSimpleName());
        }
        throw new AssertionError(message + " -- expected " + expectedType.getSimpleName());
    }

    static void testPart1() {
        PredictionMarketOrderSystem.MarketPart1 system = new PredictionMarketOrderSystem.MarketPart1();

        system.placeOrder("o1", "alice", "btc-above-100k", PredictionMarketOrderSystem.Outcome.YES, 10);
        PredictionMarketOrderSystem.Order order = system.getOrder("o1");

        assertEquals("o1", order.orderId(), "orderId");
        assertEquals("alice", order.userId(), "userId");
        assertEquals("btc-above-100k", order.marketId(), "marketId");
        assertEquals(PredictionMarketOrderSystem.Outcome.YES, order.outcome(), "outcome");
        assertEquals(10L, order.quantity(), "quantity");
        assertEquals(PredictionMarketOrderSystem.OrderState.ACTIVE, order.state(), "new order is active");
        assertEquals(null, system.getOrder("missing"), "missing order returns null");

        assertThrows(IllegalArgumentException.class,
                () -> system.placeOrder("o1", "bob", "eth-above-10k",
                        PredictionMarketOrderSystem.Outcome.YES, 5),
                "duplicate orderId throws");
    }

    static void testPart2() {
        PredictionMarketOrderSystem.MarketPart2 system = new PredictionMarketOrderSystem.MarketPart2();
        system.placeOrder("o1", "alice", "btc-above-100k", PredictionMarketOrderSystem.Outcome.YES, 10);

        system.cancelOrder("o1");
        assertEquals(PredictionMarketOrderSystem.OrderState.CANCELLED,
                system.getOrder("o1").state(), "active can be cancelled");

        system.cancelOrder("o1");
        assertEquals(PredictionMarketOrderSystem.OrderState.CANCELLED,
                system.getOrder("o1").state(), "cancel is idempotent");

        system.activateOrder("o1");
        assertEquals(PredictionMarketOrderSystem.OrderState.ACTIVE,
                system.getOrder("o1").state(), "cancelled can be reactivated");

        system.fulfillOrder("o1");
        assertEquals(PredictionMarketOrderSystem.OrderState.FULFILLED,
                system.getOrder("o1").state(), "active can be fulfilled");

        assertThrows(IllegalStateException.class,
                () -> system.cancelOrder("o1"),
                "fulfilled is terminal");
        assertThrows(IllegalStateException.class,
                () -> system.activateOrder("o1"),
                "fulfilled cannot be reactivated");

        system.cancelOrder("missing");
        system.activateOrder("missing");
        system.fulfillOrder("missing");
    }

    static void testPart3() {
        PredictionMarketOrderSystem.MarketPart3 system = new PredictionMarketOrderSystem.MarketPart3();

        system.placeOrder("a1", "alice", "btc-above-100k", PredictionMarketOrderSystem.Outcome.YES, 10);
        system.placeOrder("a2", "alice", "btc-above-100k", PredictionMarketOrderSystem.Outcome.YES, 20);
        system.placeOrder("a3", "alice", "eth-above-10k", PredictionMarketOrderSystem.Outcome.YES, 30);
        system.placeOrder("b1", "bob", "btc-above-100k", PredictionMarketOrderSystem.Outcome.YES, 40);

        system.cancelOrder("a2");
        system.fulfillOrder("a3");

        int cancelled = system.cancelAllOrdersForUser("alice");
        assertEquals(1, cancelled, "cancel-all only counts active orders actually cancelled");
        assertEquals(PredictionMarketOrderSystem.OrderState.CANCELLED,
                system.getOrder("a1").state(), "active alice order cancelled");
        assertEquals(PredictionMarketOrderSystem.OrderState.CANCELLED,
                system.getOrder("a2").state(), "already cancelled alice order remains cancelled");
        assertEquals(PredictionMarketOrderSystem.OrderState.FULFILLED,
                system.getOrder("a3").state(), "fulfilled alice order is untouched");
        assertEquals(PredictionMarketOrderSystem.OrderState.ACTIVE,
                system.getOrder("b1").state(), "bob order is untouched");

        assertEquals(0, system.cancelAllOrdersForUser("alice"), "second cancel-all has no active orders");
        assertEquals(0, system.cancelAllOrdersForUser("ghost"), "unknown user returns zero");
    }
}
