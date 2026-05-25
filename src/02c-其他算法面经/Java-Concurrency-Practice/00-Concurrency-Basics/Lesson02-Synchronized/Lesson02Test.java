import java.util.concurrent.*;

public class Lesson02Test {
    public static void main(String[] args) throws Exception {
        try {
            Lesson02 l = new Lesson02();
            final int threads = 8, perThread = 10_000;
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
            pool.shutdown();

            long expected = (long) threads * perThread;
            long actual = l.get();
            if (actual != expected) {
                throw new AssertionError("count race: expected " + expected + " got " + actual
                        + "  (差了 " + (expected - actual) + ", 说明 increment 不是原子的 — synchronized 漏了?)");
            }
            System.out.println("Lesson02 PASSED  (count=" + actual + ")");
        } catch (UnsupportedOperationException e) {
            System.out.println("Lesson02 SKIPPED (not implemented)");
        } catch (AssertionError e) {
            System.out.println("Lesson02 FAILED: " + e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
}
