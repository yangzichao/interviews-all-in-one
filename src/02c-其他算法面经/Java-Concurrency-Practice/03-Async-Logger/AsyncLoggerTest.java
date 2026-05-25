import java.util.*;
import java.util.concurrent.*;

public class AsyncLoggerTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) throws Exception {
        Map<String, RunnableEx> tests = new LinkedHashMap<>();
        tests.put("single", AsyncLoggerTest::testSingle);
        tests.put("concurrent_no_loss", AsyncLoggerTest::testConcurrentNoLoss);
        tests.put("per_thread_order", AsyncLoggerTest::testPerThreadOrder);
        tests.put("close_idempotent", AsyncLoggerTest::testCloseIdempotent);

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

    /** Thread-safe sink. Logger may call append from worker; tests call toString from main. */
    static class CollectingSink implements Appendable {
        final List<String> lines = Collections.synchronizedList(new ArrayList<>());
        @Override public Appendable append(CharSequence csq) { lines.add(csq.toString()); return this; }
        @Override public Appendable append(CharSequence csq, int start, int end) { return append(csq.subSequence(start, end)); }
        @Override public Appendable append(char c) { return append(String.valueOf(c)); }
    }

    // ========================================================================
    // single
    // ========================================================================
    static void testSingle() throws Exception {
        CollectingSink sink = new CollectingSink();
        AsyncLogger log = new AsyncLogger(16, sink);
        for (int i = 0; i < 100; i++) log.log("line-" + i);
        log.close();
        assertEq(100, sink.lines.size(), "all lines written");
        for (int i = 0; i < 100; i++) {
            assertEq("line-" + i, sink.lines.get(i), "order at index " + i);
        }
    }

    // ========================================================================
    // concurrent_no_loss
    // ========================================================================
    static void testConcurrentNoLoss() throws Exception {
        final int threads = 8;
        final int perThread = 1_000;
        CollectingSink sink = new CollectingSink();
        AsyncLogger log = new AsyncLogger(64, sink);

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int t = 0; t < threads; t++) {
            final int tid = t;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) log.log("T" + tid + "-" + i);
                } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                finally { done.countDown(); }
            });
        }
        start.countDown();
        done.await();
        log.close();
        pool.shutdown();
        assertEq(threads * perThread, sink.lines.size(), "no lost lines across producers");
    }

    // ========================================================================
    // per_thread_order — each thread's own lines must remain in order
    // ========================================================================
    static void testPerThreadOrder() throws Exception {
        final int threads = 6;
        final int perThread = 500;
        CollectingSink sink = new CollectingSink();
        AsyncLogger log = new AsyncLogger(32, sink);

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int t = 0; t < threads; t++) {
            final int tid = t;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) log.log("T" + tid + "-" + i);
                } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                finally { done.countDown(); }
            });
        }
        start.countDown();
        done.await();
        log.close();
        pool.shutdown();

        // Group lines by thread, verify ascending sequence within each thread.
        Map<Integer, List<Integer>> perThreadSeq = new HashMap<>();
        for (String line : sink.lines) {
            int dash = line.indexOf('-');
            int tid = Integer.parseInt(line.substring(1, dash));
            int seq = Integer.parseInt(line.substring(dash + 1));
            perThreadSeq.computeIfAbsent(tid, k -> new ArrayList<>()).add(seq);
        }
        for (Map.Entry<Integer, List<Integer>> e : perThreadSeq.entrySet()) {
            List<Integer> seq = e.getValue();
            assertEq(perThread, seq.size(), "thread " + e.getKey() + " line count");
            for (int i = 0; i < seq.size(); i++) {
                if (seq.get(i) != i) {
                    throw new AssertionError("thread " + e.getKey() + " out of order at index " + i + ": " + seq.get(i));
                }
            }
        }
    }

    // ========================================================================
    // close_idempotent
    // ========================================================================
    static void testCloseIdempotent() throws Exception {
        CollectingSink sink = new CollectingSink();
        AsyncLogger log = new AsyncLogger(8, sink);
        log.log("hello");
        log.close();
        log.close(); // must not throw or hang
        log.log("after-close"); // should be silently dropped
        assertEq(1, sink.lines.size(), "post-close log dropped");
        assertEq("hello", sink.lines.get(0), "pre-close line preserved");
    }

    @FunctionalInterface interface RunnableEx { void run() throws Exception; }
}
