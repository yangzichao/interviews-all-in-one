/**
 * Lesson 02 — synchronized (互斥锁)
 *
 * 看 ../README.md 里 Lesson 02 一节. 关键词: synchronized, intrinsic lock, monitor, critical section.
 *
 * 实现要点:
 *   - private long count;  // 不要用 volatile, 不要用 Atomic
 *   - synchronized void increment(): count++
 *   - synchronized long get(): return count
 */
public class Lesson02 {

    // TODO: private long count;

    public void increment() {
        throw new UnsupportedOperationException("TODO: synchronized count++");
    }

    public long get() {
        throw new UnsupportedOperationException("TODO: synchronized return count");
    }
}
