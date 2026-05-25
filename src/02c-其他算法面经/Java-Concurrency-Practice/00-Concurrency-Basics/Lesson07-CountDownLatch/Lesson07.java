/**
 * Lesson 07 — CountDownLatch (一次性等待)
 *
 * 看 ../README.md 里 Lesson 07 一节. 关键词: latch, one-shot, start gate / finish line.
 *
 * 实现要点:
 *   runAll(n, task):
 *     CountDownLatch done = new CountDownLatch(n);
 *     for (int i = 0; i < n; i++) {
 *         new Thread(() -> {
 *             try { task.run(); }
 *             finally { done.countDown(); }   // ⚠️ 必须放 finally, 否则 task 抛异常就永远等不到
 *         }).start();
 *     }
 *     done.await();   // 阻塞直到所有 n 个都 countDown
 *
 *   注意: latch 用完不能 reset. 要循环用就上 CyclicBarrier.
 */
public class Lesson07 {

    public void runAll(int n, Runnable task) throws InterruptedException {
        throw new UnsupportedOperationException("TODO: 启动 n 个线程, 每个跑一次 task; 全部跑完才返回");
    }
}
