import java.lang.reflect.Field;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Lesson05Test {
    public static void main(String[] args) throws Exception {
        try {
            Lesson05 l = new Lesson05();

            // part 1 — basic counter correctness under contention
            final int threads = 16, perThread = 5000;
            CountDownLatch gate = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            ExecutorService pool = Executors.newFixedThreadPool(threads + 2);
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

            // part 2 — tryIncrement should mostly fail when lock is held
            ReentrantLock victimLock = findReentrantLockField(l);
            if (victimLock == null) {
                pool.shutdown();
                System.out.println("Lesson05 PASSED  (count=" + l.get() + ", tryIncrement check skipped — 找不到 ReentrantLock 字段)");
                return;
            }

            final AtomicInteger trueCount = new AtomicInteger();
            final AtomicInteger falseCount = new AtomicInteger();
            final CountDownLatch holdReady = new CountDownLatch(1);
            final CountDownLatch holdRelease = new CountDownLatch(1);
            pool.submit(() -> {
                victimLock.lock();
                try {
                    holdReady.countDown();
                    holdRelease.await();
                } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                finally { victimLock.unlock(); }
            });
            holdReady.await();

            final int probes = 50;
            CountDownLatch doneProbes = new CountDownLatch(probes);
            for (int i = 0; i < probes; i++) {
                pool.submit(() -> {
                    try {
                        if (l.tryIncrement(5)) trueCount.incrementAndGet();
                        else falseCount.incrementAndGet();
                    } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    finally { doneProbes.countDown(); }
                });
            }
            doneProbes.await();
            holdRelease.countDown();
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.SECONDS);

            if (falseCount.get() < probes * 0.7) {
                throw new AssertionError("tryIncrement(5ms) 在锁被占着的时候应该绝大多数失败, 实际 "
                        + falseCount.get() + "/" + probes + " 失败 — 没真正用 tryLock(timeout)?");
            }
            System.out.println("Lesson05 PASSED  (count=" + l.get()
                    + ", tryIncrement: " + falseCount.get() + " false / " + trueCount.get() + " true)");
        } catch (UnsupportedOperationException e) {
            System.out.println("Lesson05 SKIPPED (not implemented)");
        } catch (AssertionError e) {
            System.out.println("Lesson05 FAILED: " + e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }

    static ReentrantLock findReentrantLockField(Object o) {
        for (Field f : o.getClass().getDeclaredFields()) {
            if (ReentrantLock.class.isAssignableFrom(f.getType())) {
                try { f.setAccessible(true); return (ReentrantLock) f.get(o); }
                catch (IllegalAccessException ignored) {}
            }
        }
        return null;
    }
}
