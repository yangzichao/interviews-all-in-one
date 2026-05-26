/**
 * 🛠️ Lesson 01 — volatile (内存可见性 Memory Visibility)
 *
 * ❓ 现实痛点：
 * 线程 A 在死循环检测 `running == true`，线程 B 将其改为 `false`。但在多核 CPU 架构下，
 * 线程 A 可能永远在读取自己 CPU Cache 里的“脏数据 (Stale Read)”，导致死循环永远无法退出。
 * 这就是经典的可见性 (Visibility) 故障。
 *
 * 💡 核心机制：
 * 给变量增加 `volatile` 修饰符。它利用底层内存屏障 (Memory Barrier)，强制每次读取绕过
 * 缓存直达主存，每次写入立刻刷新到主存，从而在 JMM (Java Memory Model) 中确立了严格的 
 * Happens-Before 关系。
 *
 * ⚠️ 专业避坑：
 * `volatile` 仅保证可见性和禁止指令重排 (有序性)，绝不保证原子性 (Atomicity)！
 * 写出 `volatile int x; x++;` 在高并发下依然会引发严重的数据覆盖 (Data Race)。
 *
 * 🎯 你的任务：
 * 实现一个可控启停的 Worker。完成之后，建议强行删掉 `volatile` 再次运行测试，
 * 亲眼见证 JIT 编译器优化下的程序卡死故障。
 */
public class Lesson01 {

    // TODO: 声明一个 volatile boolean running 作为线程间通信的标志位
    // TODO: 声明一个 long ticks 用于计数 (注意: 仅由 worker 线程写入，因此无需 volatile)
    // TODO: 声明一个 Thread worker 实例

    volatile boolean running;
    long ticks;
    Thread worker;

    public void start() {
        running = true;
        this.worker = new Thread(() -> {
            while (running == true) {
                ticks++;
            }
        });
        worker.start();
    }

    public void stop() throws InterruptedException {
        running = false;
        worker.join();
    }

    public long ticks() {
        return ticks;
    }
}

/*
 * KEY TAKEAWAYS
 *
 * Thread: A Thread represents an independent path of execution inside a JVM process. Creating a raw
 * Thread is relatively expensive — it allocates OS-level resources — so in production you almost
 * always hand tasks to a thread pool (ExecutorService) instead of spawning threads directly. The
 * two essential lifecycle calls are start() to launch the thread and join() to block the caller
 * until that thread finishes.
 *
 * volatile: On a multi-core CPU each core caches memory locally, so a write by thread B may sit
 * invisible in B's cache while thread A keeps reading a stale value from its own cache. Marking a
 * field volatile forces every write to flush immediately to main memory and every read to fetch
 * directly from main memory, bypassing the cache. In JMM terms this establishes a happens-before
 * edge: a volatile write by any thread is guaranteed to be visible to any subsequent volatile read
 * of the same field. volatile does NOT make compound operations (e.g. x++) atomic — it only
 * guarantees single-read / single-write visibility and prevents instruction reordering around
 * the field.
 */
