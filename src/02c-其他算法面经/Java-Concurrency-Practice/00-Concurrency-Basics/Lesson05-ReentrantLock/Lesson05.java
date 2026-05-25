import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lesson 05 — ReentrantLock (显式锁)
 *
 * 看 ../README.md 里 Lesson 05 一节. 关键词: explicit lock, try-finally, tryLock, reentrancy.
 *
 * 实现要点:
 *   - ReentrantLock lock = new ReentrantLock();
 *   - long count;
 *
 *   increment():
 *       lock.lock();
 *       try { count++; }
 *       finally { lock.unlock(); }       // 这个 try-finally idiom 是面试必背
 *
 *   get(): 同上, 但是 return count
 *
 *   tryIncrement(timeoutMillis):
 *       if (lock.tryLock(timeoutMillis, MILLISECONDS)) {
 *           try { count++; return true; }
 *           finally { lock.unlock(); }
 *       }
 *       return false;
 *
 *   ⚠️ tryLock 是 boolean, **拿到了**才能 unlock; 拿不到的话不要 unlock (会抛 IllegalMonitorStateException).
 */
public class Lesson05 {

    // TODO: ReentrantLock lock;
    // TODO: long count;

    public void increment() {
        throw new UnsupportedOperationException("TODO: lock + try-finally + count++");
    }

    public long get() {
        throw new UnsupportedOperationException("TODO: lock + try-finally + return count");
    }

    public boolean tryIncrement(long timeoutMillis) throws InterruptedException {
        throw new UnsupportedOperationException("TODO: tryLock(timeout) + count++ if got it");
    }
}
