import java.util.*;
import java.util.concurrent.*;

public class Lesson03Test {
    public static void main(String[] args) throws Exception {
        try {
            Lesson03 l = new Lesson03();

            // part 1 — count under contention
            final int threads = 16, perThread = 10_000;
            CountDownLatch gate = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            for (int t = 0; t < threads; t++) {
                pool.submit(() -> {
                    try {
                        gate.await();
                        for (int i = 0; i < perThread; i++) l.increment();
                    } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    finally { done.countDown(); }
                });
            }
            gate.countDown();
            done.await();
            long expected = (long) threads * perThread;
            if (l.get() != expected) {
                pool.shutdownNow();
                throw new AssertionError("count race: expected " + expected + " got " + l.get());
            }

            // part 2 — updateMax under contention
            final int n = 5000;
            final long[] candidates = new long[n];
            Random rnd = new Random(42);
            long realMax = Long.MIN_VALUE;
            for (int i = 0; i < n; i++) { candidates[i] = rnd.nextLong(); realMax = Math.max(realMax, candidates[i]); }
            CountDownLatch gate2 = new CountDownLatch(1);
            CountDownLatch done2 = new CountDownLatch(threads);
            for (int t = 0; t < threads; t++) {
                final int tid = t;
                pool.submit(() -> {
                    try {
                        gate2.await();
                        for (int i = tid; i < n; i += threads) l.updateMax(candidates[i]);
                    } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    finally { done2.countDown(); }
                });
            }
            gate2.countDown();
            done2.await();
            pool.shutdown();
            if (l.max() != realMax) {
                throw new AssertionError("max race: expected " + realMax + " got " + l.max()
                        + "  (CAS retry loop 漏了一次比较?)");
            }
            System.out.println("Lesson03 PASSED  (count=" + l.get() + ", max=" + l.max() + ")");
        } catch (UnsupportedOperationException e) {
            System.out.println("Lesson03 SKIPPED (not implemented)");
        } catch (AssertionError e) {
            System.out.println("Lesson03 FAILED: " + e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
}
