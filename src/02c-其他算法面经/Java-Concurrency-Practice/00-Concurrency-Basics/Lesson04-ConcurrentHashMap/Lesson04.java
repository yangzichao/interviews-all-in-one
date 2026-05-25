import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lesson 04 — ConcurrentHashMap (复合原子操作)
 *
 * 看 ../README.md 里 Lesson 04 一节. 关键词: computeIfAbsent, merge, check-then-act race.
 *
 * 实现要点:
 *   - ConcurrentHashMap<String, AtomicLong> counts
 *   - ConcurrentHashMap<String, Object>    singletons
 *
 *   record(k):     counts.computeIfAbsent(k, x -> new AtomicLong()).incrementAndGet();
 *   count(k):      AtomicLong v = counts.get(k); return v == null ? 0 : v.get();
 *   getOrCreate(k): singletons.computeIfAbsent(k, x -> new Object());
 *
 *   ⚠️ 不要写成 `if (!singletons.containsKey(k)) singletons.put(k, new Object());`
 *      —— 两个线程同时进入, 各 new 一个 Object, 后写覆盖前写, getOrCreate 测试会挂.
 */
public class Lesson04 {

    // TODO: ConcurrentHashMap<String, AtomicLong> counts = new ConcurrentHashMap<>();
    // TODO: ConcurrentHashMap<String, Object>    singletons = new ConcurrentHashMap<>();

    public void record(String key) {
        throw new UnsupportedOperationException("TODO: computeIfAbsent + incrementAndGet");
    }

    public long count(String key) {
        throw new UnsupportedOperationException("TODO: 取出 AtomicLong, null 返回 0");
    }

    public Object getOrCreate(String key) {
        throw new UnsupportedOperationException("TODO: singletons.computeIfAbsent(key, k -> new Object())");
    }
}
