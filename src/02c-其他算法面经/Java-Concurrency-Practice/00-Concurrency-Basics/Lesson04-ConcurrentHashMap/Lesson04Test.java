import java.util.*;
import java.util.concurrent.*;

public class Lesson04Test {
    public static void main(String[] args) throws Exception {
        try {
            Lesson04 l = new Lesson04();

            // part 1 — record same key from many threads
            final int threads = 32, perThread = 5000;
            CountDownLatch gate = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            for (int t = 0; t < threads; t++) {
                pool.submit(() -> {
                    try {
                        gate.await();
                        for (int i = 0; i < perThread; i++) l.record("homepage");
                    } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    finally { done.countDown(); }
                });
            }
            gate.countDown();
            done.await();

            long expected = (long) threads * perThread;
            if (l.count("homepage") != expected) {
                pool.shutdownNow();
                throw new AssertionError("record race: expected " + expected + " got " + l.count("homepage"));
            }
            if (l.count("nonexistent") != 0) {
                pool.shutdownNow();
                throw new AssertionError("missing key 应该返回 0, got " + l.count("nonexistent"));
            }

            // part 2 — getOrCreate identity under race
            final Set<Object> distinct = ConcurrentHashMap.newKeySet();
            CountDownLatch gate2 = new CountDownLatch(1);
            CountDownLatch done2 = new CountDownLatch(threads);
            for (int t = 0; t < threads; t++) {
                pool.submit(() -> {
                    try {
                        gate2.await();
                        distinct.add(l.getOrCreate("singleton"));
                    } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    finally { done2.countDown(); }
                });
            }
            gate2.countDown();
            done2.await();
            pool.shutdown();
            if (distinct.size() != 1) {
                throw new AssertionError("getOrCreate 应该对同一 key 返回同一实例, 实际看到 "
                        + distinct.size() + " 个不同实例 — 用了 putIfAbsent + new 而不是 computeIfAbsent?");
            }
            System.out.println("Lesson04 PASSED  (homepage=" + l.count("homepage") + ", singletons=" + distinct.size() + ")");
        } catch (UnsupportedOperationException e) {
            System.out.println("Lesson04 SKIPPED (not implemented)");
        } catch (AssertionError e) {
            System.out.println("Lesson04 FAILED: " + e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
}
