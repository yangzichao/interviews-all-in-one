import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lesson 08 — Semaphore (限并发数)
 *
 * 看 ../README.md 里 Lesson 08 一节. 关键词: permit, bounded concurrency, acquire/release, try-finally.
 *
 * 实现要点:
 *   - Semaphore sem = new Semaphore(permits);
 *   - AtomicInteger inFlight, peakInFlight;
 *
 *   access():
 *     sem.acquire();
 *     try {
 *         int now = inFlight.incrementAndGet();
 *         peakInFlight.updateAndGet(p -> Math.max(p, now));  // 记最高水位
 *         Thread.sleep(30);
 *         inFlight.decrementAndGet();
 *     } finally {
 *         sem.release();    // ⚠️ 必须 finally; sleep 被中断也得 release, 不然 permit 泄漏
 *     }
 *
 *   peak(): peakInFlight.get()
 */
public class Lesson08 {

    // TODO: Semaphore sem;
    // TODO: AtomicInteger inFlight = new AtomicInteger();
    // TODO: AtomicInteger peakInFlight = new AtomicInteger();

    public Lesson08(int permits) {
        throw new UnsupportedOperationException("TODO: new Semaphore(permits)");
    }

    public void access() throws InterruptedException {
        throw new UnsupportedOperationException("TODO: acquire → 计 in-flight 峰值 → sleep 30ms → release (try-finally)");
    }

    public int peak() {
        throw new UnsupportedOperationException("TODO: 返回 in-flight 峰值");
    }
}
