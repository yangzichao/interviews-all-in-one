import java.util.*;
import java.util.function.Function;

/**
 * 04 — Parallel Fetcher
 *
 * See README.md.
 * 决策点:
 *   - 顺序怎么保住? (index 跟 Future 一起记?)
 *   - 整批 deadline 怎么实现? (invokeAll vs 一个个 get 的差别)
 *   - fetcher 抛异常时哪一层吃掉?
 */
public class ParallelFetcher implements AutoCloseable {

    // TODO: state — executor, closed flag

    public ParallelFetcher(int parallelism) {
        throw new UnsupportedOperationException("TODO: build a fixed thread pool of size `parallelism`");
    }

    public List<String> fetchAll(
            List<String> urls,
            Function<String, String> fetcher,
            long timeoutMillis) throws InterruptedException {
        throw new UnsupportedOperationException("TODO: dispatch in parallel; preserve order; null on failure/timeout");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("TODO: shutdown executor, idempotent");
    }
}
