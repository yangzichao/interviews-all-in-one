import java.util.*;
import java.util.concurrent.*;

public class EventCounterTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) throws Exception {
        Map<String, RunnableEx> tests = new LinkedHashMap<>();
        tests.put("single", EventCounterTest::testSingleThread);
        tests.put("concurrent", EventCounterTest::testConcurrent);
        tests.put("reset_under_load", EventCounterTest::testResetUnderLoad);

        List<String> toRun = args.length == 0 ? new ArrayList<>(tests.keySet()) : Arrays.asList(args);
        for (String name : toRun) {
            RunnableEx t = tests.get(name);
            if (t == null) {
                System.out.println("unknown case: " + name + ", available: " + tests.keySet());
                System.exit(2);
            }
            run(name, t);
        }
        System.out.printf("%nPassed=%d  Failed=%d  Skipped=%d%n", passed, failed, skipped);
        if (failed > 0) System.exit(1);
    }

    static void run(String name, RunnableEx test) {
        try {
            test.run();
            System.out.println(name + " PASSED");
            passed++;
        } catch (UnsupportedOperationException e) {
            System.out.println(name + " SKIPPED (not implemented)");
            skipped++;
        } catch (AssertionError e) {
            System.out.println(name + " FAILED: " + e.getMessage());
            failed++;
        } catch (Throwable e) {
            System.out.println(name + " ERROR: " + e);
            e.printStackTrace(System.out);
            failed++;
        }
    }

    static void assertEq(long expected, long actual, String msg) {
        if (expected != actual) {
            throw new AssertionError(msg + " — expected: " + expected + ", actual: " + actual);
        }
    }

    // ========================================================================
    // single — sanity check on a single thread
    // ========================================================================
    static void testSingleThread() {
        EventCounter c = new EventCounter();
        for (int i = 0; i < 10_000; i++) c.record();
        assertEq(10_000, c.total(), "single-thread total");
        c.reset();
        assertEq(0, c.total(), "after reset");
    }

    // ========================================================================
    // concurrent — 16 threads x 100k records, no lost updates
    // ========================================================================
    static void testConcurrent() throws Exception {
        final int threads = 16;
        final int perThread = 100_000;
        final EventCounter c = new EventCounter();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) c.record();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        pool.shutdown();
        assertEq((long) threads * perThread, c.total(), "no lost updates under contention");
    }

    // ========================================================================
    // reset_under_load — reset called while records still flowing
    // ========================================================================
    static void testResetUnderLoad() throws Exception {
        final int threads = 8;
        final int perThread = 50_000;
        final EventCounter c = new EventCounter();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads + 1);
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) c.record();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        // Trigger a reset midway through.
        pool.submit(() -> {
            try {
                start.await();
                Thread.sleep(2);
                c.reset();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });
        start.countDown();
        done.await();
        pool.shutdown();
        long after = c.total();
        // We don't know the exact count (reset happens midway), but we DO know:
        //   - total must be >= 0 and <= threads * perThread
        if (after < 0 || after > (long) threads * perThread) {
            throw new AssertionError("total out of range after reset under load: " + after);
        }
    }

    @FunctionalInterface interface RunnableEx { void run() throws Exception; }
}
