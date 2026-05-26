import java.util.concurrent.atomic.AtomicLong;

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

    AtomicLong count = new AtomicLong();
    AtomicLong max = new AtomicLong(Long.MIN_VALUE);

    public void increment() {
        count.incrementAndGet();
    }

    public long get() {
        return count.get();
    }

    public void updateMax(long candidate) {
        while (true) {
            long cur = max.get();
            if (candidate <= cur) return;
            if (max.compareAndSet(cur, candidate)) {
                return;
            }
        }
    }

    public long max() {
        return max.get();
    }
}

/*
 * KEY TAKEAWAYS
 *
 * AtomicLong wraps a volatile long and exposes atomic read-modify-write operations backed by CAS
 * (compare-and-swap), a CPU instruction (CMPXCHG on x86) that atomically performs "if memory == expected,
 * write newValue and return true; else return false". The volatile field gives visibility for free —
 * get() and set() always touch main memory — while CAS gives single-step atomicity for composite updates
 * like incrementAndGet().
 *
 * The retry-loop pattern (read current → compute new → CAS → on failure re-read) is how you build any
 * lock-free read-modify-write on top of CAS. Unlike synchronized, losers don't block; they simply re-read
 * the latest value and try again. Under low/moderate contention this is dramatically faster than locking
 * because there are no OS-level context switches. Under extreme contention many threads waste CPU spinning,
 * and a coarse lock may actually win.
 *
 * Use Atomic* for single-variable atomic updates (counters, flags, accumulators). For multi-variable
 * invariants (e.g. moving money between two accounts) CAS on a single field is not enough — fall back to
 * synchronized, ReentrantLock, or a database transaction.
 *
 * ABA: CAS compares values, not history. If a field goes A → B → A while a thread is mid-loop, its CAS
 * still succeeds even though the world changed underneath it. Harmless for numeric counters, dangerous
 * for lock-free pointer structures (linked lists, stacks) where node identity matters. Java's
 * AtomicStampedReference attaches a version stamp to defeat ABA in those cases.
 */
