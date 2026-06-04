import java.util.*;
import java.util.concurrent.*;

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
 *   术语 (GLOSSARY) —— 先混个脸熟, 后面注释会用到
 *   ----------------------------------------------------------------------
 *   · key / value : 键 / 值, 都是字符串
 *   · timestamp   : 调用方传进来的逻辑时间 (int), 不读真实时钟; 时间只靠
 *                   调用方传更大的值往前走
 *   · entry       : 一个 key 当前对应的记录 (value + 它的过期信息)
 *   · TTL         : time-to-live, 存活时长 (秒); 见 Part 3
 *   · expireAt    : 一条 entry 的到期时刻; 半开区间 [写入时刻, expireAt),
 *                   到了 expireAt 就视为不存在; null 表示永不过期
 *   · 半开区间     : [start, end) —— 含 start 不含 end, 所以 now == expireAt 即过期
 *   · scan        : 按 key 前缀查询, 结果按 key 字典序拼成字符串
 *   · backup      : 给当前 "还活着" 的所有 entry 拍一张快照, 返回一个 backupId
 *   · restore     : 用某个 backupId 的快照覆盖当前数据库
 *   · backupId    : 快照编号, 从 1 开始每次 backup 递增 1
 *
 *  注: 题面用英文写, 术语表给中文对照; 两边说的是同一件事。
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
 *  Practice-code conventions in this file:
 *    - Each Part is a public static class DbPartN with clean method names
 *      (put / get / scan / backup / restore) — no suffix weirdness.
 *    - Parts whose schema is unchanged copy the same Entry + db fields;
 *      only a schema change introduces a new Entry type.
 *    - Stubs throw UnsupportedOperationException — the test runner treats
 *      that as SKIPPED.
 */
public class InMemoryDatabase {

    // ====================================================================
    // PART 1  —  基础 put / get                                    [⚠ TODO]
    // ====================================================================
    // 场景: 最朴素的 KV 存储 —— put 写入/覆盖一个 key, get 读回来; 没见过的 key
    //       返回 Optional.empty()。完整契约见文件顶部 PART 1。
    //
    //   put("a","x",t=1); get("a",t=2) → Optional.of("x")
    //   put("a","y",t=3); get("a",t=4) → Optional.of("y")   (后写覆盖前写)
    //   get("b",t=5)                    → Optional.empty()   (没写过)
    //
    // Entry schema: plain Map<String, String> — no Entry record needed here.

    public static class DbPart1 {
        final Map<String, String> db = new HashMap<>();

        public void put(String key, String value, int timestamp) {
            db.put(key, value);
        }

        public Optional<String> get(String key, int timestamp) {
            return Optional.ofNullable(db.get(key));
        }
    }

    // ====================================================================
    // PART 2  —  加 scan (prefix, 按字典序, 字符串格式)              [⚠ TODO]
    // ====================================================================
    // 场景: 加一个前缀查询 —— 给一个 prefix, 把所有以它开头的 key 按字典序列出,
    //       拼成 "k1(v1), k2(v2)" 这样的字符串 (分隔符正好是 ", ", 无尾逗号)。
    //       空 prefix 匹配所有 key; 无匹配返回 ""。
    //
    //   put apple=1, app=2, banana=3
    //   scan("app",t)  → "app(2), apple(1)"            (按 key 字典序)
    //   scan("",t)     → "app(2), apple(1), banana(3)" (空前缀=全部)
    //   scan("z",t)    → ""                            (无匹配)
    //
    // 与 Part 1 比:
    //   同: Entry schema 不变 (直接复用)
    //   变: 无
    //   新: scan(prefix, timestamp) -> String
    //
    // ★ 复用: put / get 跟 Part 1 完全一样, 直接搬。

    public static class DbPart2 {
        record Entry(String value) {}
        final Map<String, Entry> db = new HashMap<>();

        public void put(String key, String value, int timestamp) {
            db.put(key, new Entry(value));
        }

        public Optional<String> get(String key, int timestamp) {
            Entry e = db.get(key);
            return e == null ? Optional.empty() : Optional.of(e.value());
        }

        public String scan(String prefix, int timestamp) {
            List<String> keys = new ArrayList<>();
            for (String key : db.keySet()) {
                if (key.startsWith(prefix)) keys.add(key);
            }
            Collections.sort(keys);
            StringBuilder sb = new StringBuilder();
            for (String key : keys) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(key).append("(").append(db.get(key).value()).append(")");
            }
            return sb.toString();
        }
    }

    // ====================================================================
    // PART 3  —  加 TTL                                            [⚠ TODO]
    // ====================================================================
    // 场景: 给 put 加一个带存活时长的重载 put(k,v,t,ttl)。这条 entry 只在半开
    //       区间 [t, t+ttl) 内有效, 到了 t+ttl 就当它不存在 (get 返 empty、scan
    //       不列)。覆盖一个 key 时, 新的 value 和新的 TTL 一起生效, 旧 TTL 作废。
    //
    //   put("a","x",t=1,ttl=5)  → 有效区间 [1,6)
    //     get("a",1)=of("x"); get("a",5)=of("x"); get("a",6)=empty (右端开)
    //   put("b","y",t=10,ttl=0) → 空区间, 立刻过期, get("b",10)=empty
    //   put("c","z",t=1)        → 无 ttl 重载 = 永久, get("c",1000000)=of("z")
    //   覆盖会换 TTL: put("d",.,1,100) 后 put("d",.,2,1) → get("d",3)=empty
    //   scan 同样过滤掉已过期的 entry。
    //
    // 与 Part 2 比:
    //   同: scan 字符串格式一样
    //   变: Entry schema 加了 expireAt 字段 (null 表示永久) → 新 Entry
    //       get / scan 都要按 expireAt 过滤
    //   新: put(k,v,t,ttl) 重载 (带 TTL 的 put)

    public static class DbPart3 {
        record Entry(String value, Integer expireAt) {}
        final Map<String, Entry> db = new HashMap<>();

        public void put(String key, String value, int timestamp) {
            db.put(key, new Entry(value, null));
        }

        public void put(String key, String value, int timestamp, int ttlSeconds) {
            db.put(key, new Entry(value, timestamp + ttlSeconds));
        }

        public Optional<String> get(String key, int timestamp) {
            Entry e = db.get(key);
            if (e == null || (e.expireAt() != null && timestamp >= e.expireAt())) return Optional.empty();
            return Optional.of(e.value());
        }

        public String scan(String prefix, int timestamp) {
            List<String> keys = new ArrayList<>();
            for (Map.Entry<String, Entry> entry : db.entrySet()) {
                String key = entry.getKey();
                Entry e = entry.getValue();
                if (key.startsWith(prefix) && (e.expireAt() == null || timestamp < e.expireAt())) {
                    keys.add(key);
                }
            }
            Collections.sort(keys);
            StringBuilder sb = new StringBuilder();
            for (String key : keys) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(key).append("(").append(db.get(key).value()).append(")");
            }
            return sb.toString();
        }
    }

    // ====================================================================
    // PART 4  —  backup / restore                                  [⚠ TODO]
    // ====================================================================
    // 场景: 支持给数据库 "拍快照" 再 "回滚"。backup(t) 把当前所有还活着 (在 t
    //       未过期) 的 entry 存成一张快照, 返回一个 backupId (从 1 开始递增)。
    //       restore(id, t) 用那张快照覆盖当前数据库。关键陷阱: 恢复出来的带 TTL
    //       entry 保留的是 *剩余* 存活时长, 不是原始 TTL —— 即新到期时刻 =
    //       restore 时刻 + (快照时它本来还能活多久)。永久 entry 恢复后仍永久。
    //       backup 时已经过期的 entry 不进快照。
    //
    //   put("temp","v",t=1,ttl=10)   // 原本 [1,11)
    //   bid = backup(t=5)            // bid=1; 此刻还剩 11-5=6 的寿命
    //   put("temp","overwritten",100) // backup 之后再改, 不影响已存的快照
    //   restore(bid, t=20)           // 新到期 = 20+6 = 26
    //   get("temp",25)=of("v"); get("temp",26)=empty   (半开区间右端)
    //
    //   永久 entry: put("p","forever",1); backup; restore(1,100)
    //     → get("p",1_000_000_000)=of("forever")   (恢复后仍永久)
    //   第二次 backup 的 id 是 2 (单调递增)。
    //
    // 与 Part 3 比:
    //   同: Entry schema 不变 (直接复用)
    //   变: 无
    //   新: backup(timestamp) -> int
    //       restore(backupId, timestamp)
    //       内部要存历史快照 + 单调递增 backupId
    //       restore 时 TTL 保留剩余量, 不重置 (见 README 的 Part 4 陷阱)
    //
    // ★ 复用: put / get / scan 跟 Part 3 完全一样, 直接搬。

    public static class DbPart4 {
        record Entry(String value, Integer expireAt) {}
        record BackupEntry(Integer timestamp, Map<String, Entry> db){};
        Map<String, Entry> db = new HashMap<>();
        List<BackupEntry> list = new ArrayList<>();

        public void put(String key, String value, int timestamp) {
            db.put(key, new Entry(value, null));
        }

        public void put(String key, String value, int timestamp, int ttlSeconds) {
            db.put(key, new Entry(value, timestamp + ttlSeconds));
        }

        public Optional<String> get(String key, int timestamp) {
            if (db.get(key) == null || (db.get(key).expireAt() != null && db.get(key).expireAt() <= timestamp) ) return Optional.empty();
            return Optional.of(db.get(key).value());
        }

        public String scan(String prefix, int timestamp) {
            List<String> keys = new ArrayList<>();
            for (Map.Entry<String, Entry> entry : db.entrySet()) {
                String key = entry.getKey();
                Entry e = entry.getValue();
                if (key.startsWith(prefix) && (e.expireAt() == null || timestamp < e.expireAt())) {
                    keys.add(key);
                }
            }
            Collections.sort(keys);
            StringBuilder sb = new StringBuilder();
            for (String key : keys) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(key).append("(").append(db.get(key).value()).append(")");
            }
            return sb.toString();
        }

        public int backup(int timestamp) {

            Map<String, Entry> dbCopy = new HashMap<>();
            for (Map.Entry<String, Entry> entry : db.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue().value();
                Integer expireAt = entry.getValue().expireAt();
                if (expireAt != null && expireAt <= timestamp ) continue;
                dbCopy.put(key, new Entry(val, expireAt));
            }

            list.add(new BackupEntry(timestamp, dbCopy));
            return list.size() - 1;
        }

        public void restore(int backupId, int timestamp) {
            BackupEntry backup = list.get(backupId);
            int timeDiff = timestamp - backup.timestamp(); // we need to deduct this much for each entry in backup
            Map<String, Entry> backupDb = backup.db();
            Map<String, Entry> newDb = new HashMap<>();

            for (Map.Entry<String, Entry> entry : backupDb.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue().value();
                Integer expireAt = entry.getValue().expireAt();
                if (expireAt == null) {
                    newDb.put(key, new Entry(val, null));
                } else {
                    newDb.put(key, new Entry(val, expireAt + timeDiff));
                }
            }
            this.db = newDb;
        }
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
    // 一个实用的 hybrid 方案:
    //   - db 用 ConcurrentHashMap, 保证单 key 的 get/put 是线程安全的。
    //   - Entry 是 immutable record, 所以单个 key 不会出现 value/expireAt 撕裂。
    //   - get 直接读 ConcurrentHashMap, 不拿全局锁, 最大化读可用性。
    //   - put 拿 snapshotLock.readLock(), 然后写 ConcurrentHashMap:
    //       多个 put 可以并发; 但 backup/restore 想做全局快照时能挡住 put。
    //   - backup 拿 snapshotLock.writeLock(), 阻止 put/restore, 然后 copy db:
    //       这样 backup 是某个时间点的一致快照。
    //   - restore 也拿 snapshotLock.writeLock(), 先构造新 map, 再一次性替换 db 引用。
    //       如果用引用替换, db 不能是 final; 应该是 volatile ConcurrentHashMap<String, Entry>。
    //   - scan 有两种选择:
    //       (a) 接受 weakly-consistent scan: 不拿全局锁, 直接扫 ConcurrentHashMap。
    //       (b) 要强一致 scan: 像 backup 一样拿 writeLock 或短锁内 copy snapshot。
    //
    // 这套方案的 trade-off:
    //   - 比所有方法都用 ReadWriteLock 更高并发: get 不被 put 阻塞, 多个 put 也可并发。
    //   - 比纯 ConcurrentHashMap 更强: backup/restore 有全局一致性。
    //   - 复杂度更高: 锁的语义不是 "read method 拿 readLock", 而是把 readLock 当作
    //     "normal mutation allowed" 的共享门闩, writeLock 当作 "global snapshot barrier"。
    //
    // ★ 复用: 业务逻辑全部跟 Part 4 一样 —— 本 Part 只换并发策略。

    public static class DbPart5 {
        record Entry(String value, Integer expireAt) {}
        final Map<String, Entry> db = new ConcurrentHashMap<>();

        public void put(String key, String value, int timestamp) {
            
        }

        public void put(String key, String value, int timestamp, int ttlSeconds) {
            throw new UnsupportedOperationException("TODO: Part 5 — 加并发安全");
        }

        public Optional<String> get(String key, int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 5 — 加并发安全");
        }

        public String scan(String prefix, int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 5 — 加并发安全");
        }

        public int backup(int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 5 — 加并发安全");
        }

        public void restore(int backupId, int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 5 — 加并发安全");
        }
    }

    // ====================================================================
    // PART 6  —  主动 TTL 清理 (active eviction)                    [⚠ TODO]
    // ====================================================================
    // 与 Part 5 比:
    //   同: 对外接口不变 (get/scan 仍然按 expireAt 过滤)
    //   变: 加一个后台线程, 主动删除已过期的 entry —— 不再依赖 lazy delete
    //   新: startEvictor() / stopEvictor() 控制后台线程
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

    public static class DbPart6 {
        record Entry(String value, Integer expireAt) {}
        final Map<String, Entry> db = new ConcurrentHashMap<>();

        public void put(String key, String value, int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 6");
        }

        public void put(String key, String value, int timestamp, int ttlSeconds) {
            throw new UnsupportedOperationException("TODO: Part 6");
        }

        public Optional<String> get(String key, int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 6");
        }

        public String scan(String prefix, int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 6");
        }

        public int backup(int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 6");
        }

        public void restore(int backupId, int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 6");
        }

        public void startEvictor() {
            throw new UnsupportedOperationException("TODO: Part 6 — 启动后台清理线程");
        }

        public void stopEvictor() {
            throw new UnsupportedOperationException("TODO: Part 6 — 停止后台清理线程");
        }
    }

    // ====================================================================
    // PART 7  —  持久化 / WAL (write-ahead log)                     [⚠ TODO]
    // ====================================================================
    // 与 Part 6 比:
    //   同: 内存数据结构和并发模型基本一致
    //   变: 每次写操作 (put / restore) 在改内存前先 append 到日志文件
    //       进程重启时 replay 日志, 重建内存状态
    //   新: 构造函数接收一个 log 文件路径; flush() 强制 fsync
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
    //   2. Log 压缩: 老的 put 被覆盖后日志无限增长 —— 需要定期 compaction
    //   3. 跟 Part 5 并发结合: 多线程 put 写 log 的顺序怎么定?

    public static class DbPart7 {
        record Entry(String value, Integer expireAt) {}
        final Map<String, Entry> db = new ConcurrentHashMap<>();

        public DbPart7(String walPath) {
            throw new UnsupportedOperationException("TODO: Part 7 — 加 WAL 持久化");
        }

        public void put(String key, String value, int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 7 — 加 WAL 持久化");
        }

        public void put(String key, String value, int timestamp, int ttlSeconds) {
            throw new UnsupportedOperationException("TODO: Part 7 — 加 WAL 持久化");
        }

        public Optional<String> get(String key, int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 7 — 加 WAL 持久化");
        }

        public String scan(String prefix, int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 7 — 加 WAL 持久化");
        }

        public int backup(int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 7 — 加 WAL 持久化");
        }

        public void restore(int backupId, int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 7 — 加 WAL 持久化");
        }

        public void flush() {
            throw new UnsupportedOperationException("TODO: Part 7 — fsync");
        }
    }

    // ====================================================================
    // PART 8  —  分片 / 横向扩展 (sharding)                         [⚠ TODO]
    // ====================================================================
    // 与 Part 7 比:
    //   同: 每个 shard 内部还是 Part 1-7 那一套
    //   变: 单机内存装不下了, key 空间按 hash 分到 N 个 shard
    //   新: ShardedDb 包装 N 个 DbPart4 实例; 按 hash(key) % N 路由
    //
    // 问题陈述:
    //   假设 10 亿 key, 单机内存只有 64GB. 怎么分到 16 台机器上?
    //
    // 面试要讨论的取舍:
    //   1. 哈希分片 (hash(key) % N): 简单均匀, 但 N 变化时几乎所有 key 都要搬
    //   2. 一致性哈希: 节点上下线只搬 1/N 的 key; 要解决 hot spot (虚拟节点)
    //   3. 范围分片: scan 友好, 但容易热点
    //   4. 跨 shard scan: scatter 到所有可能 shard, gather 排序
    //   5. 跨 shard backup: 全局一致快照需要协调 (类似 Chandy-Lamport)
    //
    // 这道题答完了基本就是设计一个简化版 Redis Cluster.
    // 不强求写代码 —— 跟面试官讨论清楚就够; 真写就写一个 router。

    public static class ShardedDb {
        private final DbPart4[] shards;

        public ShardedDb(int n) {
            throw new UnsupportedOperationException("TODO: Part 8 — 初始化 N 个 shard");
        }

        private DbPart4 shardFor(String key) {
            throw new UnsupportedOperationException("TODO: Part 8 — hash(key) % N 路由");
        }

        public void put(String key, String value, int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 8");
        }

        public Optional<String> get(String key, int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 8");
        }

        public String scan(String prefix, int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 8 — scatter / gather across shards");
        }

        public int backup(int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 8 — coordinate cross-shard snapshot");
        }
    }
}
