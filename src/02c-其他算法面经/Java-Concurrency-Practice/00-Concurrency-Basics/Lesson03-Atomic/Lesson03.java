/**
 * Lesson 03 — AtomicLong / CAS (无锁原子)
 *
 * 看 ../README.md 里 Lesson 03 一节. 关键词: CAS, compare-and-swap, lock-free, retry loop, ABA.
 *
 * 实现要点:
 *   - AtomicLong count, max
 *   - increment(): count.incrementAndGet()
 *   - get(): count.get()
 *   - updateMax(candidate):
 *       for (;;) {
 *           long cur = max.get();
 *           if (candidate <= cur) return;
 *           if (max.compareAndSet(cur, candidate)) return;   // 失败就重试
 *       }
 *   - max(): max.get()
 */
public class Lesson03 {

    // TODO: AtomicLong count = new AtomicLong();
    // TODO: AtomicLong max = new AtomicLong(Long.MIN_VALUE);

    public void increment() {
        throw new UnsupportedOperationException("TODO: count.incrementAndGet()");
    }

    public long get() {
        throw new UnsupportedOperationException("TODO: count.get()");
    }

    public void updateMax(long candidate) {
        throw new UnsupportedOperationException("TODO: CAS retry loop on max");
    }

    public long max() {
        throw new UnsupportedOperationException("TODO: max.get()");
    }
}
