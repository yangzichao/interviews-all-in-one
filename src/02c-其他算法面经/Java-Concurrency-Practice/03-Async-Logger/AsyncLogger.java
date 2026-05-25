/**
 * 03 — Async Logger
 *
 * Producer/consumer logger. See README.md for the contract.
 * 关键决策点 (你来选):
 *   - 用哪种 BlockingQueue?
 *   - worker 怎么收到"停"的信号?
 *   - close() 怎么保证残留的行被 drain 出来?
 */
public class AsyncLogger implements AutoCloseable {

    // TODO: state — queue, worker thread, sink, lifecycle flag

    public AsyncLogger(int capacity, Appendable sink) {
        throw new UnsupportedOperationException("TODO: ctor — store sink, build queue, start worker");
    }

    public void log(String line) {
        throw new UnsupportedOperationException("TODO: enqueue with backpressure; silently drop after close");
    }

    @Override
    public void close() throws InterruptedException {
        throw new UnsupportedOperationException("TODO: drain queue, stop worker, idempotent");
    }
}
