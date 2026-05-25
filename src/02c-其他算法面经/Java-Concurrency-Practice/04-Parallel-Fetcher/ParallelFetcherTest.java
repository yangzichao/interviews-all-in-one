import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class ParallelFetcherTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) throws Exception {
        Map<String, RunnableEx> tests = new LinkedHashMap<>();
        tests.put("single_thread_fast", ParallelFetcherTest::testSingleThreadFast);
        tests.put("parallel_speedup", ParallelFetcherTest::testParallelSpeedup);
        tests.put("failure_isolation", ParallelFetcherTest::testFailureIsolation);
        tests.put("batch_timeout", ParallelFetcherTest::testBatchTimeout);

        List<String> toRun = args.length == 0 ? new ArrayList<>(tests.keySet()) : Arrays.asList(args);
        for (String name : toRun) {
            RunnableEx t = tests.get(name);
            if (t == null) { System.out.println("unknown case: " + name); System.exit(2); }
            run(name, t);
        }
        System.out.printf("%nPassed=%d  Failed=%d  Skipped=%d%n", passed, failed, skipped);
        if (failed > 0) System.exit(1);
    }

    static void run(String name, RunnableEx test) {
        try { test.run(); System.out.println(name + " PASSED"); passed++; }
        catch (UnsupportedOperationException e) { System.out.println(name + " SKIPPED (not implemented)"); skipped++; }
        catch (AssertionError e) { System.out.println(name + " FAILED: " + e.getMessage()); failed++; }
        catch (Throwable e) { System.out.println(name + " ERROR: " + e); e.printStackTrace(System.out); failed++; }
    }

    static void assertEq(Object expected, Object actual, String msg) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(msg + " — expected: " + expected + ", actual: " + actual);
        }
    }

    // ========================================================================
    // single_thread_fast
    // ========================================================================
    static void testSingleThreadFast() throws Exception {
        try (ParallelFetcher pf = new ParallelFetcher(4)) {
            List<String> urls = Arrays.asList("a", "b", "c", "d", "e");
            Function<String, String> fetcher = String::toUpperCase;
            List<String> out = pf.fetchAll(urls, fetcher, 1_000);
            assertEq(Arrays.asList("A", "B", "C", "D", "E"), out, "order preserved, all fetched");
        }
    }

    // ========================================================================
    // parallel_speedup — 8 x 200ms must finish well under 1600ms (sequential time)
    // ========================================================================
    static void testParallelSpeedup() throws Exception {
        try (ParallelFetcher pf = new ParallelFetcher(8)) {
            List<String> urls = new ArrayList<>();
            for (int i = 0; i < 8; i++) urls.add("url-" + i);
            Function<String, String> fetcher = u -> {
                try { Thread.sleep(200); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                return u + "-ok";
            };
            long t0 = System.currentTimeMillis();
            List<String> out = pf.fetchAll(urls, fetcher, 2_000);
            long elapsed = System.currentTimeMillis() - t0;
            assertEq(8, out.size(), "result size");
            for (int i = 0; i < 8; i++) assertEq("url-" + i + "-ok", out.get(i), "result " + i);
            if (elapsed > 500) {
                throw new AssertionError("not actually parallel: elapsed=" + elapsed + "ms (expected < 500)");
            }
        }
    }

    // ========================================================================
    // failure_isolation
    // ========================================================================
    static void testFailureIsolation() throws Exception {
        try (ParallelFetcher pf = new ParallelFetcher(4)) {
            List<String> urls = Arrays.asList("a", "b", "BOOM", "d", "e");
            Function<String, String> fetcher = u -> {
                if ("BOOM".equals(u)) throw new RuntimeException("simulated downstream failure");
                return u.toUpperCase();
            };
            List<String> out = pf.fetchAll(urls, fetcher, 1_000);
            assertEq(5, out.size(), "size");
            assertEq("A", out.get(0), "0");
            assertEq("B", out.get(1), "1");
            assertEq(null, out.get(2), "failure slot is null");
            assertEq("D", out.get(3), "3");
            assertEq("E", out.get(4), "4");
        }
    }

    // ========================================================================
    // batch_timeout — slow half must be null, total time bounded
    // ========================================================================
    static void testBatchTimeout() throws Exception {
        try (ParallelFetcher pf = new ParallelFetcher(10)) {
            List<String> urls = new ArrayList<>();
            for (int i = 0; i < 10; i++) urls.add("u" + i);
            Function<String, String> fetcher = u -> {
                int idx = Integer.parseInt(u.substring(1));
                long sleep = (idx < 5) ? 100 : 5_000;
                try { Thread.sleep(sleep); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                return u + "-ok";
            };
            long t0 = System.currentTimeMillis();
            List<String> out = pf.fetchAll(urls, fetcher, 300);
            long elapsed = System.currentTimeMillis() - t0;
            assertEq(10, out.size(), "size matches input");
            for (int i = 0; i < 5; i++) assertEq("u" + i + "-ok", out.get(i), "fast " + i);
            for (int i = 5; i < 10; i++) assertEq(null, out.get(i), "slow " + i + " timed out");
            if (elapsed > 600) {
                throw new AssertionError("batch deadline not enforced: elapsed=" + elapsed + "ms (expected < 600)");
            }
        }
    }

    @FunctionalInterface interface RunnableEx { void run() throws Exception; }
}
