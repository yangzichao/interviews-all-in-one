import java.util.*;

/**
 * ============================================================================
 *  PROBLEM — In-Memory Key-Value Database (progressive design)
 * ============================================================================
 *
 *  We are going to design an in-memory key-value database. The database
 *  supports a set of operations that take a logical `timestamp` (an int)
 *  supplied by the caller — you do NOT read a real clock; time only moves
 *  forward because the caller passes larger timestamps. All values are
 *  strings; all keys are strings.
 *
 *  The problem is delivered in PARTS. In a real interview you only see the
 *  next part after finishing the current one. Each part adds one capability
 *  on top of the previous part's API.
 *
 *  ----------------------------------------------------------------------
 *   PART 1 — basic put / get
 *   ----------------------------------------------------------------------
 *   put(key, value, timestamp)
 *       Insert or overwrite the value for `key`. Later puts to the same key
 *       replace earlier ones.
 *
 *   get(key, timestamp) -> Optional<String>
 *       Return the current value for `key`, or Optional.empty() if the key
 *       was never put.
 *
 *  ----------------------------------------------------------------------
 *   PART 2 — prefix scan
 *   ----------------------------------------------------------------------
 *   scan(prefix, timestamp) -> String
 *       Return all keys that start with `prefix`, sorted lexicographically,
 *       formatted as:
 *           "k1(v1), k2(v2), k3(v3)"
 *       Rules:
 *         - separator is exactly ", " (comma + space); no trailing comma
 *         - empty prefix matches every key
 *         - if no key matches, return "" (empty string)
 *
 *  ----------------------------------------------------------------------
 *   PART 3 — TTL (time to live)
 *   ----------------------------------------------------------------------
 *   put(key, value, timestamp, ttlSeconds)   // overload
 *       Same as put, but the entry is valid only during the half-open
 *       interval [timestamp, timestamp + ttlSeconds). At or after
 *       timestamp + ttlSeconds the entry behaves as if it were never put.
 *
 *   Semantics:
 *     - put(...) without ttl  => entry never expires
 *     - ttl = 0               => expires immediately (empty interval)
 *     - overwriting a key replaces both value AND TTL; the old TTL is gone
 *     - get and scan must filter out expired entries
 *
 *   Example:
 *     put("a", "1", t=1, ttl=5)
 *     get("a", 5) == "1"     // 5 is inside [1, 6)
 *     get("a", 6) == null    // 6 is outside [1, 6)
 *
 *  ----------------------------------------------------------------------
 *   PART 4 — backup / restore
 *   ----------------------------------------------------------------------
 *   backup(timestamp) -> int
 *       Capture a snapshot of all currently-alive entries (i.e. not yet
 *       expired at `timestamp`). Return a backup id; ids start at 1 and
 *       increase by 1 with each backup call.
 *
 *   restore(backupId, timestamp)
 *       Replace the current database with the snapshot identified by
 *       `backupId`. Restored entries keep their REMAINING TTL — not the
 *       original TTL. Concretely, if at backup time an entry would have
 *       expired at originalExpireAt, the restored entry expires at
 *           timestamp + (originalExpireAt - backupTimestamp)
 *       Permanent entries (no ttl) remain permanent after restore.
 *
 *   Example:
 *     put("x", "v", t=1, ttl=10)   // would expire at 11
 *     id = backup(t=5)              // remaining ttl = 6
 *     restore(id, t=20)             // new expireAt = 26
 *     get("x", 25) == "v"           // alive
 *     get("x", 26) == null          // expired
 *
 * ============================================================================
 *  Follow-ups (Part 5–8) cover concurrency, active eviction, WAL persistence,
 *  and sharding. Each is described in the section header below where its
 *  stubs live.
 * ============================================================================
 *
 *  Practice-code conventions in this file (NOT part of the problem):
 *    - Methods are suffixed putPartN / getPartN / scanPartN / ... so each
 *      part is independent and earlier parts keep passing after you change
 *      direction.
 *    - Each part's banner lists [same / changed / new] vs. the previous
 *      part. A new EntryPartN / mapPartN is introduced ONLY when the
 *      schema actually changes — otherwise reuse the previous one.
 */
public class InMemoryDatabase {

    // ====================================================================
    // PART 1  —  基础 put / get                                    [✓ DONE]
    // ====================================================================
    // 起点 —— EntryPart1 只存 (value, timestamp), HashMap 装着.

    private record EntryPart1(String value, int timestamp) {}
    private final Map<String, EntryPart1> mapPart1 = new HashMap<>();

    public void putPart1(String key, String value, int timestamp) {
        mapPart1.put(key, new EntryPart1(value, timestamp));
    }

    public Optional<String> getPart1(String key, int timestamp) {
        EntryPart1 e = mapPart1.get(key);
        return e == null ? Optional.empty() : Optional.of(e.value());
    }

    // ====================================================================
    // PART 2  —  加 scan (prefix, 按字典序, 字符串格式)              [✓ DONE]
    // ====================================================================
    // 与 Part 1 比:
    //   同: EntryPart1 / mapPart1  (schema 没变, 直接复用)
    //   变: 无
    //   新: scanPart2(prefix, timestamp) -> String
    //
    // putPart2 / getPart2 是套壳到 Part 1, 没有逻辑改变.

    public void putPart2(String key, String value, int timestamp) {
        putPart1(key, value, timestamp);
    }

    public Optional<String> getPart2(String key, int timestamp) {
        return getPart1(key, timestamp);
    }

    public String scanPart2(String prefix, int timestamp) {
        List<String> keyList = new ArrayList<>();
        for (String key : mapPart1.keySet()) {
            if (key.startsWith(prefix)) {
                keyList.add(key);
            }
        }
        Collections.sort(keyList);
        String res = "";
        for (String key : keyList) {
            if (res.length() > 0) res += ", ";
            res += key + '(' + mapPart1.get(key).value() + ')';
        }
        return res;
    }

    // ====================================================================
    // PART 3  —  加 TTL                                            [✓ DONE]
    // ====================================================================
    // 与 Part 2 比:
    //   同: 数据结构思路一样 (HashMap), scan 字符串格式一样
    //   变: Entry schema 加了 expireAt 字段 (null 表示永久)
    //       -> 新 EntryPart3, 新 mapPart3
    //       get / scan 都要按 expireAt 过滤
    //   新: putPart3(k,v,t,ttl) 重载 (带 TTL 的 put)

    private record EntryPart3(String value, Integer expireAt) {}
    private final Map<String, EntryPart3> mapPart3 = new HashMap<>();

    public void putPart3(String key, String value, int timestamp) {
        mapPart3.put(key, new EntryPart3(value, null));  // 没 TTL = 永久
    }

    public void putPart3(String key, String value, int timestamp, int ttlSeconds) {
        mapPart3.put(key, new EntryPart3(value, timestamp + ttlSeconds));
    }

    // expireAt == null  -> 永远不过期
    // expireAt != null  -> 半开区间 [putTime, expireAt), 所以 expireAt <= now 即过期
    private boolean isExpiredPart3(EntryPart3 entry, int timestamp) {
        return entry.expireAt() != null && entry.expireAt() <= timestamp;
    }

    public Optional<String> getPart3(String key, int timestamp) {
        EntryPart3 entry = mapPart3.get(key);
        if (entry == null || isExpiredPart3(entry, timestamp)) return Optional.empty();
        return Optional.of(entry.value());
    }

    public String scanPart3(String prefix, int timestamp) {
        List<String> keyList = new ArrayList<>();
        for (String key : mapPart3.keySet()) {
            if (key.startsWith(prefix) && !isExpiredPart3(mapPart3.get(key), timestamp)) {
                keyList.add(key);
            }
        }
        Collections.sort(keyList);
        String res = "";
        for (String key : keyList) {
            if (res.length() > 0) res += ", ";
            res += key + '(' + mapPart3.get(key).value() + ')';
        }
        return res;
    }

    // ====================================================================
    // PART 4  —  backup / restore                                  [⚠ TODO]
    // ====================================================================
    // 与 Part 3 比:
    //   同: EntryPart3 / mapPart3  (schema 没变, 不引入新 Entry)
    //   变: 无
    //   新: backupPart4(timestamp) -> int
    //       restorePart4(backupId, timestamp)
    //       内部要存历史快照 + 单调递增 backupId
    //       restore 时 TTL 保留剩余量, 不重置 (见 README 的 Part 4 陷阱)
    //
    // 现在 5 个方法全是 stub —— 等你完成 Part 3 后:
    //   putPart4 / getPart4 / scanPart4 会套壳到 Part 3
    //   你只需要写 backupPart4 / restorePart4

    private record EntryPart4(String value, Integer expireAt) {}
    private final Map<String, EntryPart4> mapPart4 = new HashMap<>();

    public void putPart4(String key, String value, int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 4 — 等 Part 3 done 后展开");
    }

    public void putPart4(String key, String value, int timestamp, int ttlSeconds) {
        throw new UnsupportedOperationException("TODO: Part 4 — 等 Part 3 done 后展开");
    }

    public Optional<String> getPart4(String key, int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 4 — 等 Part 3 done 后展开");
    }

    public String scanPart4(String prefix, int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 4 — 等 Part 3 done 后展开");
    }

    public int backupPart4(int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 4 — 等 Part 3 done 后展开");
    }

    public void restorePart4(int backupId, int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 4 — 等 Part 3 done 后展开");
    }

    // ====================================================================
    // PART 5  —  并发访问 (thread-safety)                           [⚠ TODO]
    // ====================================================================
    // 与 Part 4 比:
    //   同: 接口形状不变 (put/get/scan/backup/restore), Entry schema 不变
    //   变: 多个线程同时调用; 所有方法必须线程安全
    //   新: 没有新方法 —— 但要在内部加锁 / 用 concurrent 集合
    //
    // 问题陈述:
    //   假设有 N 个线程同时调 put/get/scan, 另一个线程调 backup/restore.
    //   要求:
    //     (a) get 不能看到 "撕裂" 的 entry (value 来自旧 put, expireAt 来自新 put)
    //     (b) scan 必须是某个一致的时间点的快照 (不能枚举到一半被别的线程改)
    //     (c) backup 时正在发生的 put 要么全进 backup, 要么全不进 —— 不能有半个
    //     (d) 高频 get 不应该被低频 put 严重阻塞
    //
    // 面试要讨论的取舍 (这是 Coinbase 团队明确说重点考的):
    //   1. 一把大锁 (synchronized this) —— 简单, 但读写互斥, 吞吐低
    //   2. ReadWriteLock —— 读并发, 写独占; scan/backup 也要拿写锁吗?
    //   3. ConcurrentHashMap + 细粒度 —— put/get 快, 但 scan/backup 没原子语义
    //   4. 分段锁 / 按 key hash 取锁 —— 折中, 但 scan 跨段难做
    //   5. Copy-on-write —— 读完全无锁, 写代价高; 适合 read-heavy
    //   面试官最常追问: "如果 99% 是 get, 1% 是 put, 选哪个? 反过来呢?"
    //
    // 你要写的: 把 Part 4 的所有方法套一遍, 加上你选的并发策略.
    // (Entry 是不可变 record, 这是优势 —— 引用换原子换, 内容不会被改坏)

    public void putPart5(String key, String value, int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 5 — 加并发安全");
    }

    public void putPart5(String key, String value, int timestamp, int ttlSeconds) {
        throw new UnsupportedOperationException("TODO: Part 5 — 加并发安全");
    }

    public Optional<String> getPart5(String key, int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 5 — 加并发安全");
    }

    public String scanPart5(String prefix, int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 5 — 加并发安全");
    }

    public int backupPart5(int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 5 — 加并发安全");
    }

    public void restorePart5(int backupId, int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 5 — 加并发安全");
    }

    // ====================================================================
    // PART 6  —  主动 TTL 清理 (active eviction)                    [⚠ TODO]
    // ====================================================================
    // 与 Part 5 比:
    //   同: 对外接口不变 (get/scan 仍然按 expireAt 过滤)
    //   变: 加一个后台线程, 主动删除已过期的 entry —— 不再依赖 lazy delete
    //   新: startEvictorPart6() / stopEvictorPart6() 控制后台线程
    //
    // 问题陈述:
    //   Part 3 的 lazy delete: 过期 entry 一直占着内存, 除非有人 get/scan 撞上.
    //   如果 100 万个 key 全设了 ttl=5 然后再也没人访问, 它们永远不会被回收.
    //   要求: 后台主动清理, 但不能严重影响前台 put/get/scan 的延迟.
    //
    // 面试要讨论的取舍:
    //   1. 定时全表扫描 —— O(n) 每轮, 简单, 但 n 大时长尾延迟差
    //   2. 抽样清理 (Redis 用的) —— 随机抽 k 个, 过期的删, 过期率高就再抽一轮
    //   3. PriorityQueue (按 expireAt 排序) —— 总能拿最早过期的; 但 put 时插入 O(log n),
    //      被覆盖/删除的 entry 会留在 PQ 里成为 "幽灵", 需要懒清理
    //   4. Timing wheel (时间轮) —— 适合大量短 TTL, 实现复杂
    //   5. 跟 Part 5 并发结合: 清理线程拿什么锁? 跟 put 怎么不打架?
    //
    // 还要讨论:
    //   - 清理频率 (每 100ms 一轮? 每 1s? 自适应?)
    //   - 清理预算 (每轮最多删 N 个, 避免阻塞)
    //   - 如何测试? (mock 时钟? 或者 sleep + assert?)

    public void startEvictorPart6() {
        throw new UnsupportedOperationException("TODO: Part 6 — 启动后台清理线程");
    }

    public void stopEvictorPart6() {
        throw new UnsupportedOperationException("TODO: Part 6 — 停止后台清理线程");
    }

    // put/get/scan 继续沿用 Part 5 的并发安全版本 —— 这里就不再重复列 stub,
    // 因为 Part 6 的核心是后台线程, 不是新 API.

    // ====================================================================
    // PART 7  —  持久化 / WAL (write-ahead log)                     [⚠ TODO]
    // ====================================================================
    // 与 Part 6 比:
    //   同: 内存数据结构和并发模型基本一致
    //   变: 每次写操作 (put / restore) 在改内存前先 append 到日志文件
    //       进程重启时 replay 日志, 重建内存状态
    //   新: 构造函数 (或 init) 接收一个 log 文件路径; flushPart7() 强制 fsync
    //
    // 问题陈述:
    //   现在 backup 只是内存快照, 进程崩了什么都没了.
    //   要支持 durability: 进程崩溃 / kill -9 后重启, 数据要能恢复到崩前最后一次写之前.
    //
    // 面试要讨论的取舍 (Coinbase 真实场景, 因为他们是金融数据):
    //   1. WAL 同步策略:
    //      - 每次 put 都 fsync —— 最安全, 吞吐量崩塌 (磁盘 IOPS 限制)
    //      - 批量 fsync (每 100ms 一次) —— 折中, 但崩溃可能丢最近 100ms 的写
    //      - 完全异步 —— 最快, 崩溃可能丢更多
    //      面试官常追问: "Coinbase 一笔订单写入丢了能接受吗? 那 100ms 呢?"
    //   2. Log 压缩:
    //      - 老的 put 被新的 put 覆盖后, 老的记录还在日志里 —— 日志会无限增长
    //      - 周期性 compaction: 把当前快照 dump 出来, 删掉之前的 log
    //      - 跟 Part 4 的 backup 怎么结合? backup 可以充当 compaction checkpoint?
    //   3. Restart 时:
    //      - 从最新 backup 加载, 再 replay backup 之后的 log
    //      - 如何判断 log 文件本身没坏? (checksum / CRC)
    //   4. 跟 Part 5 并发结合:
    //      - 多个线程同时 put, 写 log 的顺序怎么定? (单写线程串行化, 还是各自加 sequence number?)
    //
    // 这是开放设计题, 不强制方法签名. 给两个建议入口:

    public void putPart7(String key, String value, int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 7 — 加 WAL 持久化");
    }

    public Optional<String> getPart7(String key, int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 7 — 加 WAL 持久化");
    }

    public void flushPart7() {  // 强制把 buffer 中的 log 落盘
        throw new UnsupportedOperationException("TODO: Part 7 — fsync");
    }

    // ====================================================================
    // PART 8  —  分片 / 横向扩展 (sharding)                         [⚠ TODO]
    // ====================================================================
    // 与 Part 7 比:
    //   同: 每个 shard 内部还是 Part 1-7 那一套
    //   变: 单机内存装不下了, key 空间按 hash 分到 N 个 shard
    //   新: ShardedDatabasePart8 包装 N 个 InMemoryDatabase 实例; 按 hash(key) % N 路由
    //
    // 问题陈述:
    //   假设 10 亿 key, 单机内存只有 64GB. 怎么分到 16 台机器上?
    //
    // 面试要讨论的取舍:
    //   1. 哈希分片 (hash(key) % N):
    //      - 简单, 负载均匀
    //      - 但 N 变化时 (加机器/坏机器), 几乎所有 key 都要搬 —— 灾难
    //   2. 一致性哈希 (consistent hashing):
    //      - 节点上下线只搬 1/N 的 key
    //      - 但要解决 hot spot (虚拟节点)
    //   3. 范围分片 (range sharding):
    //      - scan 友好 (prefix scan 可能只命中 1-2 个 shard)
    //      - 但容易热点 (新写都集中在一个 range)
    //   4. 跨 shard 操作怎么办:
    //      - scan(prefix): 要 scatter 到所有可能 shard, gather 排序 —— 还能保持 O(prefix+output)?
    //      - backup: 全局一致快照需要协调 (类似 distributed snapshot, Chandy-Lamport)
    //              还是接受 "每个 shard 各自 backup, 时间点略有差异"?
    //   5. 跨 shard 的 TTL 后台清理: 各 shard 各自跑, 还是中心化协调?
    //
    // 这道题答完了基本就是设计一个简化版 Redis Cluster.
    // 不强求写代码 —— 跟面试官讨论清楚就够; 真写就写一个 1 文件的 router.

    // 建议入口 (写不写都可以):
    //
    //   static class ShardedDatabasePart8 {
    //       private final InMemoryDatabase[] shards;
    //       ShardedDatabasePart8(int n) { ... }
    //       private InMemoryDatabase shardFor(String key) { ... }
    //       // 路由 put/get; 实现 cross-shard scan/backup
    //   }
}
