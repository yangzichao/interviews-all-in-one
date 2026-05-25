import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Lesson06Test {
    public static void main(String[] args) throws Exception {
        try {
            Lesson06 l = new Lesson06();
            final int n = 100;
            final AtomicLong consumedSum = new AtomicLong();
            ExecutorService pool = Executors.newFixedThreadPool(2);

            CountDownLatch consumerDone = new CountDownLatch(1);
            pool.submit(() -> {
                try {
                    for (int i = 0; i < n; i++) {
                        consumedSum.addAndGet(l.consume());
                    }
                } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                finally { consumerDone.countDown(); }
            });

            // producer 故意快, 让队列满到 capacity, 验证 put 会 block 而不是丢
            pool.submit(() -> {
                try {
                    for (int i = 1; i <= n; i++) l.produce(i);
                } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            });

            if (!consumerDone.await(3, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                throw new AssertionError("3 秒内没消费完 100 个 — 可能用了 offer 但满了直接 false 没 block? 或者根本没消费?");
            }
            pool.shutdown();

            long expected = (long) n * (n + 1) / 2; // 1+2+...+100 = 5050
            if (consumedSum.get() != expected) {
                throw new AssertionError("consumed sum = " + consumedSum.get() + ", expected " + expected
                        + "  (丢消息 or 多/少消费?)");
            }
            System.out.println("Lesson06 PASSED  (consumed sum = " + consumedSum.get() + ")");
        } catch (UnsupportedOperationException e) {
            System.out.println("Lesson06 SKIPPED (not implemented)");
        } catch (AssertionError e) {
            System.out.println("Lesson06 FAILED: " + e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
}
