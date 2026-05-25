/**
 * Lesson 01 — volatile (memory visibility)
 *
 * 看 ../README.md 里 Lesson 01 一节. 关键词: volatile, visibility, JMM happens-before.
 *
 * 实现要点:
 *   - volatile boolean running
 *   - long ticks (普通 long, 只在 worker 线程里写)
 *   - Thread worker
 *   - start(): 启动 worker, 它 while (running) { ticks++; }
 *   - stop(): running = false; worker.join()
 *   - ticks(): 返回 ticks
 */
public class Lesson01 {

    // TODO: volatile boolean running;
    // TODO: long ticks;
    // TODO: Thread worker;

    public void start() {
        throw new UnsupportedOperationException("TODO: 启动一个 while(running) ticks++ 的 worker");
    }

    public void stop() throws InterruptedException {
        throw new UnsupportedOperationException("TODO: running = false; worker.join();");
    }

    public long ticks() {
        throw new UnsupportedOperationException("TODO: return ticks");
    }
}
