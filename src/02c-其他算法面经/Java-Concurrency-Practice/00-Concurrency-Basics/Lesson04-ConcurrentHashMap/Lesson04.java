/**
 * Lesson 04 — Concurrent Collections: ConcurrentHashMap as the main example
 *
 * 📖 请查阅 ../README.md 里 Lesson 04 一节。
 * 🎯 核心概念:
 *   - Concurrent data structures are the "concurrent world" versions of normal data structures.
 *   - Thread-safe container != every multi-step operation is automatically safe.
 *   - Compound Atomic Operation: a multi-step logic must be executed atomically.
 *   - Check-then-act race: `if missing, then put` is unsafe unless the whole operation is atomic.
 *
 * -----------------------------------------------------------------------------
 * 1. Mental map: normal world vs concurrent world
 * -----------------------------------------------------------------------------
 *
 * Primitive-like single values:
 *   int / long / boolean              -> AtomicInteger / AtomicLong / AtomicBoolean
 *   object reference                  -> AtomicReference<T>
 *
 * Common containers:
 *   HashMap<K,V>                      -> ConcurrentHashMap<K,V>
 *   HashSet<E>                        -> ConcurrentHashMap.newKeySet()
 *   ArrayList<E>                      -> CopyOnWriteArrayList<E>  (good for many reads, few writes)
 *   Queue<E> / Deque<E>               -> ConcurrentLinkedQueue<E> / ConcurrentLinkedDeque<E>
 *   producer-consumer queue           -> BlockingQueue<E>, e.g. LinkedBlockingQueue / ArrayBlockingQueue
 *
 * Big idea:
 *   - If the object is shared by multiple threads, ask: what is the concurrent-safe version?
 *   - If multiple operations must behave like one operation, ask: is there an atomic API for that?
 *
 * -----------------------------------------------------------------------------
 * 2. Why ConcurrentHashMap is not "magic"
 * -----------------------------------------------------------------------------
 *
 * ConcurrentHashMap makes individual map operations thread-safe:
 *   - get
 *   - put
 *   - remove
 *   - computeIfAbsent
 *   - merge
 *   - compute
 *
 * But this is still dangerous:
 *
 *   if (!map.containsKey(k)) {
 *       map.put(k, new Object());
 *   }
 *
 * Why? Because two threads can both pass the `containsKey` check before either one puts.
 * This is called a Check-then-act race condition.
 *
 * Correct solution:
 *
 *   map.computeIfAbsent(k, key -> new Object());
 *
 * This makes the whole "check missing, then create, then insert" logic one atomic operation.
 *
 * -----------------------------------------------------------------------------
 * 3. What this lesson implements
 * -----------------------------------------------------------------------------
 *
 * Implement these two concurrent data structures:
 *
 *   - ConcurrentHashMap<String, AtomicLong> counts
 *       Used as a thread-safe counter map.
 *       Each key has an AtomicLong counter.
 *
 *   - ConcurrentHashMap<String, Object> singletons
 *       Used as a thread-safe object factory.
 *       Each key should create exactly one Object instance.
 *
 * Required methods:
 *
 *   record(k):
 *       counts.computeIfAbsent(k, x -> new AtomicLong()).incrementAndGet();
 *
 *   count(k):
 *       AtomicLong v = counts.get(k);
 *       return v == null ? 0 : v.get();
 *
 *   getOrCreate(k):
 *       singletons.computeIfAbsent(k, x -> new Object());
 *
 * ⚠️ 致命雷区:
 *   Do NOT write:
 *
 *       if (!singletons.containsKey(k)) {
 *           singletons.put(k, new Object());
 *       }
 *
 *   Two threads may both cross the `if` boundary and create duplicate objects.
 *   The test engine is designed to catch this bug.
 */
public class Lesson04 {

    private final ConcurrentHashMap<String, AtomicLong> counts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> singletons = new ConcurrentHashMap<>();

    public void record(String key) {
        counts.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();
    }

    public long count(String key) {
        AtomicLong value = counts.get(key);
        return value == null ? 0 : value.get();
    }

    public Object getOrCreate(String key) {
        return singletons.computeIfAbsent(key, k -> new Object());
    }
}
