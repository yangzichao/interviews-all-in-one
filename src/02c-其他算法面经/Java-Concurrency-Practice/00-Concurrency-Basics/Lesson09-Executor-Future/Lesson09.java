import java.util.List;

/**
 * Lesson 09 — ExecutorService + Future (线程池 + 异步结果)
 *
 * 看 ../README.md 里 Lesson 09 一节. 关键词: thread pool, Callable, Future.get(), invokeAll, shutdown.
 *
 * 实现要点:
 *   sumInParallel(nums, workers):
 *     ExecutorService pool = Executors.newFixedThreadPool(workers);
 *     try {
 *         List<Callable<Long>> tasks = new ArrayList<>();
 *         // 把 nums 切成 workers 份, 每份一个 Callable<Long> 算 partial sum
 *         for (int w = 0; w < workers; w++) {
 *             final int wid = w;
 *             tasks.add(() -> {
 *                 long s = 0;
 *                 for (int i = wid; i < nums.size(); i += workers) s += nums.get(i);
 *                 return s;
 *             });
 *         }
 *         List<Future<Long>> futures = pool.invokeAll(tasks);
 *         long total = 0;
 *         for (Future<Long> f : futures) total += f.get();    // get 会抛 ExecutionException, signature 上声明
 *         return total;
 *     } finally {
 *         pool.shutdown();    // ⚠️ 必须 shutdown, 否则非 daemon 线程会让 JVM 不退出
 *     }
 */
public class Lesson09 {

    public long sumInParallel(List<Long> nums, int workers) throws Exception {
        throw new UnsupportedOperationException("TODO: 用 ExecutorService + Callable + invokeAll + Future.get 算 sum");
    }
}
