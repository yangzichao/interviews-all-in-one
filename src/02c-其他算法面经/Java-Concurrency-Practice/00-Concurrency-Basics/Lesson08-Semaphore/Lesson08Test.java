import java.util.concurrent.*;

public class Lesson08Test {
    public static void main(String[] args) throws Exception {
        try {
            final int permits = 3;
            Lesson08 l = new Lesson08(permits);
            final int threads = 12;
            CountDownLatch gate = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            for (int t = 0; t < threads; t++) {
                pool.submit(() -> {
                    try {
                        gate.await();
                        l.access();
                    } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    finally { done.countDown(); }
                });
            }
            gate.countDown();
            if (!done.await(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                throw new AssertionError("5 秒没跑完 — 是不是没 release, permit 漏光了?");
            }
            pool.shutdown();

            int peak = l.peak();
            if (peak > permits) {
                throw new AssertionError("peak in-flight = " + peak + ", 但 permits = " + permits
                        + "  — semaphore 没限制住, acquire 漏调了?");
            }
            if (peak < 2) {
                throw new AssertionError("peak in-flight = " + peak
                        + ", 太低了, 应该至少有 2-3 个同时在跑 (12 线程 × 30ms). 你是不是 serial 化了?");
            }
            System.out.println("Lesson08 PASSED  (peak in-flight = " + peak + " / limit " + permits + ")");
        } catch (UnsupportedOperationException e) {
            System.out.println("Lesson08 SKIPPED (not implemented)");
        } catch (AssertionError e) {
            System.out.println("Lesson08 FAILED: " + e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
}
