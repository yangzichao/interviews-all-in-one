import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class TokenBucketTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) throws Exception {
        Map<String, RunnableEx> tests = new LinkedHashMap<>();
        tests.put("single_drain", TokenBucketTest::testSingleDrain);
        tests.put("refill_caps_at_capacity", TokenBucketTest::testRefillCaps);
        tests.put("all_or_nothing", TokenBucketTest::testAllOrNothing);
        tests.put("concurrent_drain", TokenBucketTest::testConcurrentDrain);
        tests.put("refill_under_load", TokenBucketTest::testRefillUnderLoad);

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
    static void testSingleDrain() {
        TokenBucket b = new TokenBucket(5);
        for (int i = 0; i < 5; i++) {
            if (!b.tryAcquire()) throw new AssertionError("acquire " + i + " should succeed");
        }
        if (b.tryAcquire()) throw new AssertionError("acquire 6 should fail (bucket empty)");
        assertEq(0, b.available(), "drained");
    }

    static void testRefillCaps() {
        TokenBucket b = new TokenBucket(5);
        assertEq(5, b.available(), "starts full");
        b.refill(10);
        assertEq(5, b.available(), "refill must not exceed capacity");

        for (int i = 0; i < 3; i++) b.tryAcquire();
        assertEq(2, b.available(), "after taking 3");
        b.refill(10);
        assertEq(5, b.available(), "refill back to capacity, no overflow");
    }

    static void testAllOrNothing() {
        TokenBucket b = new TokenBucket(5);
        for (int i = 0; i < 2; i++) b.tryAcquire();
        assertEq(3, b.available(), "down to 3");
        if (b.tryAcquire(5)) throw new AssertionError("tryAcquire(5) should fail (only 3 left)");
        assertEq(3, b.available(), "MUST not have consumed anything on failed all-or-nothing");
        if (!b.tryAcquire(3)) throw new AssertionError("tryAcquire(3) should succeed");
        assertEq(0, b.available(), "drained by batch");
    }

    static void testConcurrentDrain() throws Exception {
        final int capacity = 1000;
        final int threads = 16;
        final int perThread = 1000;
        final TokenBucket b = new TokenBucket(capacity);
        final AtomicInteger successes = new AtomicInteger();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        if (b.tryAcquire()) successes.incrementAndGet();
                    }
                } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                finally { done.countDown(); }
            });
        }
        start.countDown();
        done.await();
        pool.shutdown();
        assertEq(capacity, successes.get(), "EXACTLY capacity successes — no double-spending");
        assertEq(0, b.available(), "bucket fully drained");
    }

    static void testRefillUnderLoad() throws Exception {
        final int capacity = 100;
        final TokenBucket b = new TokenBucket(capacity);
        final AtomicBoolean stop = new AtomicBoolean(false);
        ExecutorService pool = Executors.newFixedThreadPool(9);

        // 8 consumers
        for (int t = 0; t < 8; t++) {
            pool.submit(() -> {
                while (!stop.get()) {
                    b.tryAcquire();
                }
            });
        }
        // 1 refiller
        pool.submit(() -> {
            while (!stop.get()) {
                b.refill(50);
                try { Thread.sleep(5); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
            }
        });

        Thread.sleep(200);
        stop.set(true);
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.SECONDS);

        int avail = b.available();
        if (avail < 0 || avail > capacity) {
            throw new AssertionError("available out of range: " + avail + " (capacity=" + capacity + ")");
        }
    }

    @FunctionalInterface interface RunnableEx { void run() throws Exception; }
}
