import java.util.*;

/**
 * 4-part Coinbase interview practice — Transaction Filter + Pagination.
 *
 * 每个 Part 是独立的 class,后缀 PartN。先无脑独立写,做完再讨论抽公共逻辑。
 *
 * 这不是产品代码,是练习代码 —— 让你能专注当前 Part 而不破坏已完成的部分。
 */
public class TransactionPagination {

    // ====================================================================
    // 通用 record  —  贯穿 4 个 Part 的 Transaction schema
    // ====================================================================
    //
    //   type ∈ {"DEPOSIT", "WITHDRAWAL", "BUY", "SELL"}
    //   排序规则: timestamp DESC, 同 timestamp 按 id ASC.

    public static record Transaction(String id, String userId, String type,
                                     long amount, long timestamp) {}

    // ====================================================================
    // PART 1  —  add + offset-pagination                            [⚠ TODO]
    // ====================================================================
    // 排序键: (timestamp DESC, id ASC).
    // page(offset, limit): 排好后跳过 offset, 取最多 limit 条.
    // 重复 id 抛 IllegalArgumentException.
    //
    //   add: (b, t=10), (a, t=10), (c, t=20)
    //   page(0, 10) → [c(20), a(10), b(10)]
    //   page(1, 1)  → [a]
    //   page(10, 5) → []   (越界不抛)

    public static class StorePart1 {

        public StorePart1() {
            throw new UnsupportedOperationException("StorePart1: not implemented");
        }

        public void add(Transaction tx) {
            throw new UnsupportedOperationException("StorePart1.add: not implemented");
        }

        public List<Transaction> page(int offset, int limit) {
            throw new UnsupportedOperationException("StorePart1.page: not implemented");
        }
    }

    // ====================================================================
    // PART 2  —  filter by fields                                   [⚠ TODO]
    // ====================================================================
    // Filter: 任何 null 字段 = wildcard, 非 null 字段之间 AND.
    // amount 区间是闭区间 [minAmount, maxAmount].
    // 先 filter 再 sort 再 paginate.
    //
    //   Filter(userId="alice", type=null, min=100, max=1000)
    //     → alice 的 amount ∈ [100, 1000] 的交易
    //   Filter(null, null, null, null) → 全量

    public static record Filter(String userId, String type,
                                Long minAmount, Long maxAmount) {}

    public static class StorePart2 {

        public StorePart2() {
            throw new UnsupportedOperationException("StorePart2: not implemented");
        }

        public void add(Transaction tx) {
            throw new UnsupportedOperationException("StorePart2.add: not implemented");
        }

        public List<Transaction> page(Filter filter, int offset, int limit) {
            throw new UnsupportedOperationException("StorePart2.page: not implemented");
        }
    }

    // ====================================================================
    // PART 3  —  cursor pagination                                  [⚠ TODO]
    // ====================================================================
    // cursor = base64 编码的 "(timestamp, id) of last returned item",
    //          调用方应当当作 opaque string.
    // cursor == null  → 从头开始 (最新一条).
    // nextCursor == null → 没有下一页了.
    //
    // 关键不变量: 插入更老的数据 (timestamp 比已发 cursor 还小) 不应该让
    // 已发 cursor 错位 —— cursor 只依赖排序键, 不依赖位置 offset.
    //
    //   排好序: [c(20), a(10), b(10), d(5)]
    //   page(∅, null, 2)              → items=[c,a], next=C1(t=10,id="a")
    //   page(∅, C1,   2)              → items=[b,d], next=null

    public static record Page<T>(List<T> items, String nextCursor) {}

    public static class StorePart3 {

        public StorePart3() {
            throw new UnsupportedOperationException("StorePart3: not implemented");
        }

        public void add(Transaction tx) {
            throw new UnsupportedOperationException("StorePart3.add: not implemented");
        }

        public Page<Transaction> page(Filter filter, String cursor, int limit) {
            throw new UnsupportedOperationException("StorePart3.page: not implemented");
        }
    }

    // ====================================================================
    // PART 4  —  bidirectional traversal                            [⚠ TODO]
    // ====================================================================
    // Direction.FORWARD  = 往更老 (跟 Part 3 一致, "下一页")
    // Direction.BACKWARD = 往更新 ("上一页")
    //
    // 不变量: forward 翻到 cursor C 之后, 用 (someCursor, BACKWARD) 应能拿
    // 回上一页 (顺序仍是 DESC 呈现, 不是反过来).
    //
    // 返回的 Page 仍然按 (timestamp DESC, id ASC) 排序; 只是 cursor 的"指向
    // 方向"反了过来.

    public enum Direction { FORWARD, BACKWARD }

    public static class StorePart4 {

        public StorePart4() {
            throw new UnsupportedOperationException("StorePart4: not implemented");
        }

        public void add(Transaction tx) {
            throw new UnsupportedOperationException("StorePart4.add: not implemented");
        }

        public Page<Transaction> page(Filter filter, String cursor, int limit,
                                      Direction direction) {
            throw new UnsupportedOperationException("StorePart4.page: not implemented");
        }
    }

    // ====================================================================
    // PART 5  —  并发安全 + 分页快照语义 (thread-safety)              [⚠ TODO]
    // ====================================================================
    // 与 Part 4 比:
    //   同: 接口形状不变 (add / page / cursor / Direction)
    //   变: 多线程同时 add 和 page; 同一次 page 调用必须看到一致快照
    //   新: 没有新方法 —— 但内部要加并发控制
    //
    // 问题陈述:
    //   N 个线程持续 add 新交易, 另一些线程同时翻页. 要求:
    //     (a) page() 不能枚举到一半被 add 干扰 (排序中途 list 变了 → ConcurrentModificationException)
    //     (b) 已发出的 cursor 必须稳定 —— 跟 Part 3 一样: 翻页途中 add 更老的数据不能让
    //         调用方"漏一条"或"重一条". 这个不变量在并发下更难保证.
    //     (c) add 不应该被 page 长时间阻塞 (page 可能扫几千条然后排序, 别全程独占)
    //     (d) 不能撕裂读: Transaction 是 immutable record, 这帮了大忙 —— 但你怎么放进 store?
    //
    // 面试要讨论的取舍 (Coinbase 团队明确说重点考):
    //   1. synchronized 一把大锁                  —— 简单, page 扫全表时阻塞所有 add
    //   2. ReadWriteLock                          —— page 拿读锁, add 拿写锁; 但 page 内排序+拼装慢
    //   3. CopyOnWriteArrayList                   —— add O(n), 但 read 完全无锁; 写少读多场景
    //   4. ConcurrentSkipListMap<SortKey, Tx>     —— add O(log n), iterator weakly-consistent;
    //                                                cursor pagination 天然契合 (按 key 切片)
    //   5. MVCC / 版本号 snapshot                  —— page 锁定版本号, 看到的是某一刻的状态;
    //                                                add 写新版本, 老版本被 GC 之前不变
    //   面试官最常追问: "如果 add 频率 10K QPS, page 1K QPS, 你选哪个? 反过来呢?"
    //
    // 你要写的: 重新组织存储, 让 add 和 page 能安全并发. cursor 语义跟 Part 3/4 一致.

    public static class StorePart5 {

        public StorePart5() {
            throw new UnsupportedOperationException("StorePart5: not implemented");
        }

        public void add(Transaction tx) {
            throw new UnsupportedOperationException("StorePart5.add: not implemented");
        }

        public Page<Transaction> page(Filter filter, String cursor, int limit,
                                      Direction direction) {
            throw new UnsupportedOperationException("StorePart5.page: not implemented");
        }
    }

    // ====================================================================
    // PART 6  —  二级索引 + keyset pagination (按 amount 排序)        [⚠ TODO]
    // ====================================================================
    // 与 Part 5 比:
    //   同: Transaction schema 不变
    //   变: 排序键不再是 (timestamp DESC, id ASC), 而是调用方指定 SortKey
    //   新: SortKey 枚举 (TIMESTAMP_DESC / AMOUNT_DESC / AMOUNT_ASC)
    //       cursor 编码相应字段, 不是固定 (timestamp, id)
    //
    // 问题陈述:
    //   产品要求支持 "按交易金额从大到小翻页". 数据量 100 亿条.
    //   - offset 分页 (Part 1) 在 offset=100000 时数据库 OFFSET 扫描会扫掉前 100K 行, 灾难.
    //   - cursor (keyset) 分页用 (amount, id) 作为游标, WHERE (amount, id) < (?, ?) ORDER BY ...
    //     永远只扫 limit 行.
    //   - 但内存里没有"按 amount 排序的索引", 每次扫全表排序也是 O(n).
    //   → 加二级索引: ConcurrentSkipListMap<AmountKey, TxId> 或 TreeMap.
    //
    // 面试要讨论的取舍:
    //   1. 维护几个二级索引? 每加一个索引, add 的写代价多一倍.
    //   2. 索引膨胀: 100 亿条 × N 个索引 = 内存爆炸. 哪些字段值得索引?
    //   3. 部分索引 (partial index): 只索引 status=ACTIVE 的, 减小索引大小.
    //   4. Filter + SortKey 组合: 按 userId 过滤 + 按 amount 排序, 要 (userId, amount) 复合索引?
    //   5. keyset pagination vs offset pagination 的硬数据 (面试官常追问):
    //      - 数据库的 OFFSET 100000 LIMIT 20: 扫 100020 行
    //      - keyset (amount, id) < (?, ?) LIMIT 20: 扫 20 行 (走索引)
    //
    // 你要写的: 加 SortKey 参数, 对应的 cursor 编码方式, 多个二级索引.

    public enum SortKey {
        TIMESTAMP_DESC,   // 默认, 跟 Part 1-5 一致
        AMOUNT_DESC,      // 按 amount 大到小
        AMOUNT_ASC        // 按 amount 小到大
    }

    public static class StorePart6 {

        public StorePart6() {
            throw new UnsupportedOperationException("StorePart6: not implemented");
        }

        public void add(Transaction tx) {
            throw new UnsupportedOperationException("StorePart6.add: not implemented");
        }

        public Page<Transaction> page(Filter filter, SortKey sortKey,
                                      String cursor, int limit) {
            throw new UnsupportedOperationException("StorePart6.page: not implemented");
        }
    }

    // ====================================================================
    // PART 7  —  跨分片分页 (sharded merge / scatter-gather)          [⚠ TODO]
    // ====================================================================
    // 与 Part 6 比:
    //   同: Transaction schema / SortKey 不变
    //   变: 数据按 userId 分到 N 个 shard, 每个 shard 是一个 StorePart5/6 实例.
    //       现在要 "list 所有用户最新 N 条交易" —— 跨 shard 翻页.
    //   新: ShardedStorePart7 包装 N 个 shard; cursor 必须编码每个 shard 的子游标.
    //
    // 问题陈述:
    //   产品: 内部 dashboard 显示"全 Coinbase 最近 100 条交易". 数据按 userId hash 分 16 shard.
    //   - 单 shard 内 page 很快 (Part 6 的索引)
    //   - 跨 shard 怎么取 top N?
    //     朴素: 每 shard 取 N 条 → gather → 全局排序 → 切前 N. 网络 N*16, 内存 N*16.
    //     优化: heap-based k-way merge —— 每 shard 维持一个 iterator, 用 min-heap (按排序键)
    //          挑下一条; 取够 N 就停. 网络 ≈ N + 一点 overflow.
    //   - cursor 要存什么? 每个 shard 一个子 cursor, 总 cursor 是它们的组合.
    //     新增的 shard (扩容) 怎么处理?
    //
    // 面试要讨论的取舍:
    //   1. Scatter-gather (每 shard 取 limit 条然后合并) vs k-way merge cursor:
    //      - scatter-gather 简单, 但读放大 N 倍 (N=16 时 limit=10 → 实际读 160 条)
    //      - k-way merge 复杂, 但精确 limit
    //   2. cursor 大小: 16 个子 cursor 编码后可能几 KB, base64 后给客户端 OK 吗?
    //   3. Shard 增减:
    //      - 加新 shard 时, 老 cursor 里没有这个 shard 的子 cursor —— 当作 "从头开始" 还是 "跳过"?
    //      - shard 暂时不可用 (网络分区): 阻塞? 返 partial result? 标记 "incomplete"?
    //   4. 一致性: 各 shard 各自有时钟 / 写入延迟, 跨 shard 排序得到的"最近 100 条"是不是真的最近?
    //   5. 跟 Part 5 并发结合: scatter 时 N 个 shard 并行查 (CompletableFuture / RxJava)?
    //
    // 这是设计讨论题, 不强求完整实现. 给一个建议入口:

    public static class ShardedStorePart7 {

        public ShardedStorePart7(int shardCount) {
            throw new UnsupportedOperationException("ShardedStorePart7: not implemented");
        }

        public void add(Transaction tx) {
            throw new UnsupportedOperationException("ShardedStorePart7.add: not implemented");
        }

        public Page<Transaction> page(Filter filter, String cursor, int limit) {
            throw new UnsupportedOperationException("ShardedStorePart7.page: not implemented");
        }
    }

    // ====================================================================
    // PART 8  —  缓存 + Stateful vs Stateless cursor                  [⚠ TODO]
    // ====================================================================
    // 与 Part 7 比:
    //   同: 接口形状跟 Part 5 类似 (filter + cursor + limit + direction)
    //   变: 加一层 cache 应付热门用户; cursor 形态可选 stateful 或 stateless
    //   新: cursor 设计的两条路线对比 + 失效策略
    //
    // 问题陈述:
    //   80% 的查询命中 1% 的"热门用户"(交易所做市商 / 网红账户). 每次翻第一页全去打 DB 太浪费.
    //   而且, cursor 本身有两种实现:
    //     A. Stateless cursor (Part 3 的做法): cursor 直接编码排序键值,
    //        服务端无状态; 但 cursor 字符串包含数据 (timestamp, id), 客户端能解码看到内部值.
    //     B. Stateful cursor: cursor 只是个 opaque ID (例如 UUID), 服务端在 Redis / 内存里存
    //        cursor → query 状态的映射. 客户端拿到的 token 完全不透明, 但服务端要维护状态.
    //
    // 面试要讨论的取舍:
    //   1. Stateful vs Stateless cursor:
    //      | 维度        | Stateless                  | Stateful                       |
    //      |-------------|----------------------------|--------------------------------|
    //      | 服务端状态  | 无                         | 有 (Redis/memory)              |
    //      | 客户端透明  | cursor 暴露内部字段        | 完全 opaque                    |
    //      | 容量        | cursor 几十字节 (排序键)   | cursor UUID + 服务端状态可大   |
    //      | 失效        | 永不失效 (除非数据被删)    | TTL / LRU 驱逐, 翻太慢就失效   |
    //      | 复杂查询    | 难 (cursor 编不进去复杂 join) | 可以存任意中间状态        |
    //      面试官最常问: "Coinbase 的 REST API cursor 你会选哪个?"
    //   2. Cache 失效策略:
    //      - 热门用户第一页结果 cache 30 秒, 但用户新 add 一条交易立刻看不到 → "stale read" 用户能接受吗?
    //      - Write-through: add 时主动更新 cache —— 但 cache 是 sorted list, 插入位置要算.
    //      - Write-invalidate: add 时把该用户 cache 清掉 —— 下次第一页查询 cold miss.
    //   3. 哪些查询适合 cache?
    //      - 第一页 (cursor=null) 命中率高, 适合 cache
    //      - 深翻页 (cursor 非 null) 各种各样, cache hit ratio 低, 别 cache
    //   4. Cache key 怎么算? (userId, filter hash, limit) —— filter 字段太多时 hash 碰撞?
    //
    // 这道题答完基本就是"如何为 Coinbase Prime API 设计可扩展的分页层". 不强求写代码.

    public static class CachedStorePart8 {

        public CachedStorePart8() {
            throw new UnsupportedOperationException("CachedStorePart8: not implemented");
        }

        public void add(Transaction tx) {
            throw new UnsupportedOperationException("CachedStorePart8.add: not implemented");
        }

        public Page<Transaction> page(Filter filter, String cursor, int limit) {
            throw new UnsupportedOperationException("CachedStorePart8.page: not implemented");
        }
    }
}
