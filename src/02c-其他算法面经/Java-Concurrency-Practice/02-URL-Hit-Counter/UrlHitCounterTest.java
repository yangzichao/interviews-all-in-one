import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class UrlHitCounterTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) throws Exception {
        Map<String, RunnableEx> tests = new LinkedHashMap<>();
        tests.put("single", UrlHitCounterTest::testSingle);
        tests.put("concurrent", UrlHitCounterTest::testConcurrent);
        tests.put("new_url_under_load", UrlHitCounterTest::testNewUrlUnderLoad);

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
    // single
    // ========================================================================
    static void testSingle() {
        UrlHitCounter c = new UrlHitCounter();
        c.hit("/home");
        c.hit("/home");
        c.hit("/about");
        assertEq(2L, c.countFor("/home"), "/home count");
        assertEq(1L, c.countFor("/about"), "/about count");
        assertEq(0L, c.countFor("/missing"), "missing returns 0");

        Map<String, Long> snap = c.snapshot();
        assertEq(2, snap.size(), "snapshot size");

        // snapshot must be immutable (or at least: mutating it must not affect the store).
        try {
            snap.put("/home", 999L);
            // If we got here, snapshot is mutable — verify it didn't leak back into the store.
            assertEq(2L, c.countFor("/home"), "snapshot mutation must not affect store");
        } catch (UnsupportedOperationException ignore) {
            // truly immutable — also acceptable
        }
    }

    // ========================================================================
    // concurrent — fixed total checks no lost updates
    // ========================================================================
    static void testConcurrent() throws Exception {
        final int threads = 16;
        final int perThread = 1_000;
        final int distinctUrls = 50;
        final UrlHitCounter c = new UrlHitCounter();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int t = 0; t < threads; t++) {
            final int seed = t;
            pool.submit(() -> {
                Random r = new Random(seed);
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        c.hit("/url-" + r.nextInt(distinctUrls));
                    }
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
        long total = c.snapshot().values().stream().mapToLong(Long::longValue).sum();
        assertEq((long) threads * perThread, total, "no lost updates across all urls");
    }

    // ========================================================================
    // new_url_under_load — all threads hammer the SAME first-time url
    // ========================================================================
    static void testNewUrlUnderLoad() throws Exception {
        final int threads = 32;
        final int perThread = 5_000;
        final UrlHitCounter c = new UrlHitCounter();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) c.hit("/contended");
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
        assertEq((long) threads * perThread, c.countFor("/contended"), "no lost updates on hot key");
    }

    @FunctionalInterface interface RunnableEx { void run() throws Exception; }
}
