# Coinbase — Transaction Filter + Pagination

设计一个交易存储,逐步加 query 能力。面试时 CoderPad 上**一个 Part 给一个**,做完再放下一个。

来源:Coinbase VO 高频题 #3。镜像 Coinbase Prime REST API —— 真实业务里 paginate 交易列表的常见模式。**不在 LeetCode 上**,最接近的是 LC 1352(只是普及题),核心难点是 cursor pagination + 多字段过滤。

---

## API 全貌

```java
// 通用 record
public static record Transaction(String id, String userId, String type,
                                 long amount, long timestamp) {}
//   type ∈ {"DEPOSIT", "WITHDRAWAL", "BUY", "SELL"}

// Part 1
new StorePart1()
    .add(Transaction tx)                     // 重复 id 抛 IllegalArgumentException
    .page(int offset, int limit) -> List<Transaction>

// Part 2
new StorePart2()
    .add(Transaction tx)
    .page(Filter f, int offset, int limit) -> List<Transaction>
// Filter(userId, type, minAmount, maxAmount), 字段 null 表示 wildcard.

// Part 3
new StorePart3()
    .add(Transaction tx)
    .page(Filter f, String cursor, int limit) -> Page<Transaction>
// Page<T>(List<T> items, String nextCursor); nextCursor==null 表示没有下一页.

// Part 4
new StorePart4()
    .add(Transaction tx)
    .page(Filter f, String cursor, int limit, Direction dir) -> Page<Transaction>
// Direction { FORWARD, BACKWARD }.  FORWARD = 翻往更老; BACKWARD = 翻往更新.
```

---

## 8 个 Part 的核心

> Part 1–4 是面经原题(Coinbase VO #3 4-part 渐进设计题)。
> Part 5–8 是**超越面经**的 follow-up —— Coinbase 团队明确说重点考"并发 / 数据增长 / 取舍",
> 这些就是面试官真要追问、而面经经常省略的部分。

| Part | 一句话 | 易踩的坑 / 讨论点 |
|------|--------|----------|
| 1 | 存 list,按 (timestamp DESC, id ASC) 排序,offset + limit 切片 | 重复 id 必须抛;timestamp 相同时 id ASC 是稳定 tiebreaker |
| 2 | 过滤后再排序再切片;`Filter` 字段 null = wildcard | 4 个字段全 null = "全量" |
| 3 | cursor 是 `(timestamp, id)` 的 base64 编码,opaque | 插入更老的数据**不能**让已发的 cursor 错位;到末尾 `nextCursor=null` |
| 4 | 加 BACKWARD 方向 | 翻完一页后用返回的 cursor + 反方向能拿回上一页 |
| 5 | **并发安全 + 分页快照** | page 扫描中途 add 不能抛 / 不能撕裂;锁粒度选择 |
| 6 | **二级索引 + keyset pagination(按 amount 排序)** | offset 100K 灾难 vs keyset O(limit);维护几个索引 |
| 7 | **跨分片分页(scatter-gather / k-way merge)** | shard 加减 / cursor 怎么编码 16 个子游标 |
| 8 | **缓存 + Stateful vs Stateless cursor** | 热门用户 cache 失效;cursor 形态的取舍 |

---

### Part 1 详解 — sort order

排序键:**timestamp DESC**(新的在前),timestamp 相同时 **id ASC**(稳定 tiebreaker)。

```
add: (id="b", t=10), (id="a", t=10), (id="c", t=20)
page(0, 10) → [c(t=20), a(t=10), b(t=10)]
```

`offset` 跳过前面 N 条,`limit` 最多取 N 条。`offset` 越界 → 返回空 list,不抛。

**为什么 id 是 ASC 而 timestamp 是 DESC?** UI 里最新的在最上,但同一秒里要给一个稳定确定的顺序,id 字典序最简单。

---

### Part 2 详解 — Filter 是 AND 组合

```java
new Filter(userId, type, minAmount, maxAmount)
```

- 字段 null = 该字段不限制(wildcard)
- 非 null 字段之间是 **AND**
- `minAmount`/`maxAmount` 是闭区间 `[min, max]`

```
new Filter("alice", null, 100L, 1000L)
  → alice 的交易里 amount ∈ [100, 1000] 的
new Filter(null, "BUY", null, null)
  → 所有人的 BUY 交易
new Filter(null, null, null, null)
  → 全量
```

**实现思路**:先过滤再排序再切片。面试可以提一句 "如果过滤命中率低,可以为 userId/type 建二级索引",但**别真去写**——面试官想看的是你说出来,不是 30 行 index 代码。

---

### Part 3 详解 — cursor pagination

为什么不用 offset?因为数据会变。在 offset=20 时新插入一条更新的交易,你下一次 offset=40 就会**重复**或**跳过**一条。

cursor 解决方式:cursor 编码"上一页最后一条的排序键",下一页 = "排序键严格小于这个 cursor 的"。

**cursor 内容**:`(timestamp, id)`,base64 编码后给调用方。调用方只该把它当不透明 string 传回来。

```
排好序的全集: [c(t=20), a(t=10), b(t=10), d(t=5)]
page(filter=∅, cursor=null, limit=2)
  → items=[c, a], nextCursor=encode(t=10, id="a")

page(filter=∅, cursor=encode(t=10,"a"), limit=2)
  → items=[b, d], nextCursor=null   // 末尾
```

**稳定性要求**:在两次 page 调用之间往里 `add` 一条 `t=3` 的更老交易,**已经发出的 cursor** 仍然指向"a",下一页仍然是 `[b, d, 新条]` 或 `[b, d]`(取决于 limit)——绝不会让 cursor 失效。

**判断"下一条排序键 < cursor"** 的比较函数:`(t1, id1) < (t2, id2)` 等价于 `t1 < t2 || (t1 == t2 && id1 > id2)`(因为 id 是 ASC,timestamp 是 DESC)。

**`nextCursor == null` 的判定**:取 limit+1 条;如果实际只剩 ≤ limit 条,说明已经到末尾,`nextCursor = null`。

---

### Part 4 详解 — 双向翻页

加一个 `Direction`:
- `FORWARD`(默认行为,跟 Part 3 一样):往更老翻
- `BACKWARD`:往更新翻(回到前一页)

```
全集: [c(t=20), a(t=10), b(t=10), d(t=5)]

forward 翻第一页 (cursor=null, limit=2)
  → items=[c, a], nextCursor=C1=encode(10,"a")

forward 第二页 (cursor=C1, limit=2)
  → items=[b, d], nextCursor=null

backward 从 b 开始 (cursor=encode(10,"b"), limit=2, BACKWARD)
  → items=[c, a]   // 回到第一页
```

**实现 hint**:BACKWARD 时,你要找"排序键**严格大于** cursor"的元素,取前 limit 个(注意结果**仍然按 DESC 顺序**呈现给调用方,不是反过来)。

**面试加分点**:主动提一句 "if backward needs to be efficient on huge data, we'd index by sorted key — `TreeSet<(timestamp, id)>` lets you `headSet(cursor)` for backward in O(log n)"。

---

## Part 5 — 并发安全 + 分页快照(Coinbase 面试**最常追问**的方向)

**问题**:N 个线程持续 `add` 新交易,另一些线程同时 `page`。要求:
- `page()` 中途 `add` 不能让排序枚举抛 `ConcurrentModificationException`
- 已发出的 cursor 在并发下仍然稳定(不重不漏)
- `add` 不应该被慢 `page` 长时间阻塞

**面试官最常问的 4 个 follow-up**:

1. **"你这把锁会不会让所有 add 等 page 排序完?"**
   → 引出 ReadWriteLock vs CopyOnWrite vs ConcurrentSkipListMap 的对比。如果 page 要扫几千条然后排序,
   全程独占锁就是灾难。常见解法:**snapshot iteration** —— 锁内只 copy 候选 id 列表,出锁后再排序+拼装。

2. **"99% 是 page、1% 是 add 怎么办? 反过来呢?"**
   → 读多写少 → CopyOnWrite,`add` O(n) 但 `page` 完全无锁。
   → 写多读少 → ConcurrentSkipListMap<(timestamp,id), Tx>,`add` O(log n),迭代器 weakly-consistent
   就够用了。

3. **"并发下 cursor 的稳定性怎么保证?"**
   → cursor 编的是排序键,不是位置 —— 这本身就帮你解耦了"并发位置漂移"问题。
   关键是 page 的 "从 cursor 之后取 limit 条" 要走索引(ConcurrentSkipListMap 的 `tailMap(cursor)`)
   而不是全表过滤,否则中途有 add 还是会让 read 看到不一致的中间状态。

4. **"如果一个 page 调用返回了 100MB 字符串,会不会卡死所有 add?"**
   → snapshot-then-release:在锁内拿到 id 快照(几 KB),释放锁,后续拼装在锁外做。
   或者用 `ConcurrentSkipListMap`,iterator 本身就是 weakly consistent,不阻塞写。

**方案取舍表**:

| 方案 | add | page | 一致性 | 适用场景 |
|------|-----|------|--------|----------|
| synchronized 一把锁 | O(log n) blocked | O(n log n) blocking | strong | 简单场景,QPS < 100 |
| ReadWriteLock | 写独占 | 多读并发 | strong | 读 >> 写,但 page 不能太慢 |
| CopyOnWriteArrayList | O(n) | 无锁 | strong | 读 99%, 数据量 < 万级 |
| ConcurrentSkipListMap | O(log n) | weakly consistent | eventual | 写多读多,接受弱快照 |
| MVCC 版本号 | O(log n) | snapshot read | strong + 高并发 | 高并发金融场景,实现复杂 |

**自检题**(写完代码自己问自己):
- 我的 `page` 在锁内做了多少事? 如果数据有 100K 条会卡多久?
- cursor 编码的是排序键, 不是 list index —— 我有没有不小心退化成 offset?
- 两个线程同时 `add` 同 `id` 的 Transaction(重复), `IllegalArgumentException` 在哪个线程抛?

---

## Part 6 — 二级索引 + keyset pagination(按 amount 排序)

**问题**:产品要支持"按 amount 从大到小翻页"。数据量 100 亿。
- offset 分页在 offset=100000 时 DB 要扫前 100020 行 —— **灾难**
- keyset (amount, id) < (?, ?) LIMIT 20 永远只扫 20 行 —— **关键优化**
- 但内存没"按 amount 排序的索引",每次扫全表排序也 O(n) —— **加二级索引**

**面试官最常问的 4 个 follow-up**:

1. **"OFFSET 100000 LIMIT 20 到底慢在哪? 数字给我?"**
   → PostgreSQL 文档明说: `OFFSET` 不能跳, 必须扫到那一行再丢弃前 100K 行. P99 延迟从毫秒到秒级.
   keyset pagination 走索引: WHERE (amount, id) < ('100', 'xyz') ORDER BY amount DESC, id ASC LIMIT 20.

2. **"你要为哪些字段建二级索引? 索引膨胀怎么办?"**
   → trade-off: 每加一个索引 add 写代价 ×2, 内存 +N. 100 亿 × 4 个索引 = 几百 GB.
   → 反着问: **业务真的需要这么多排序维度吗**? Coinbase 实际可能只支持 (timestamp, amount).
   → 进阶: partial index (WHERE type='BUY' 才进索引), 减小索引大小.

3. **"Filter + SortKey 组合怎么用索引?"**
   → 复合索引 (userId, amount, id). filter userId='alice' + sort by amount → 索引能走.
   filter type='BUY' + sort by amount → 索引走不了 (前缀错了). 这就是为什么 Coinbase API 限定可过滤字段.

4. **"cursor 怎么编码? 如果中途数据被删了会怎么样?"**
   → cursor = base64(sortKey_value, id). 解码后 keyset query.
   → 数据被删: cursor 还指向那个被删的 (amount, id), 下次翻页 keyset query 仍然 OK (取的是 "严格小于" 这个点之后的, 不依赖那一行还存在).

**方案取舍表**:

| 方案 | scan 复杂度 | 内存 | 何时选 |
|------|------------|------|--------|
| 每次全表 sort | O(n log n) | 1 份数据 | 小数据 < 10K 行 |
| 主排序 + offset | O(offset + limit) | 1 份数据 + 1 索引 | offset 始终很小 |
| keyset + 复合索引 | O(limit log n) | 1 份 + K 个索引 | 100 亿行的真实场景 |
| 物化视图 (precomputed) | O(limit) | 物化结果 | 排序维度极少且固定 |

**自检题**:
- 我有几个二级索引? 每个 add 触发几次写?
- 删了一条交易, 索引怎么更新? 留下 "dangling cursor" 怎么办?
- 用户请求 "amount > 1000 排序按 timestamp DESC", 我有这个复合索引吗?

---

## Part 7 — 跨分片分页(scatter-gather / k-way merge)

**问题**:数据按 `userId` hash 分到 N 个 shard. 内部 dashboard 要 "全 Coinbase 最近 100 条交易".

**面试官最常问的 4 个 follow-up**:

1. **"朴素 scatter-gather 每 shard 取 limit 条然后合并 —— 有什么问题?"**
   → 读放大: limit=10, N=16 shards → 实际读 160 条网络 IO. 翻深页时更糟.
   → 进阶: k-way merge with cursor —— 每 shard 维持一个 iterator, 全局 min-heap 挑下一条.
   → 但 cursor 要编码 N 个子游标 (每 shard 一个), cursor 字符串变大.

2. **"shard 加机器时,老的 cursor 怎么办?"**
   → cursor 里没有新 shard 的子游标: 两种处理
     (a) 新 shard 当作 "从头开始" —— 可能重复 (老页早就跨过新 shard 数据)
     (b) 新 shard 当作 "已经翻完" —— 可能漏 (永远看不到新 shard 数据)
   → 实务: 把 cursor 加版本号, 版本不匹配就让客户端从第一页重翻.

3. **"某个 shard 暂时挂了, 你要返 partial 还是阻塞?"**
   → Coinbase 真实选择: 标记 `incomplete=true` 返回 partial result, 让前端 retry 该 shard.
   → 全阻塞会让 dashboard 整体不可用 (1 个 shard 拖 16 个).

4. **"跨 shard 的 ordering 严格吗? 各 shard 时钟可能不一致."**
   → 答: 不严格. 答完顺便提 "Coinbase 的 timestamp 是哪一端打的? client / API gateway / db?
       Coinbase 实际用 db 的 commit time, 单 shard 内严格, 跨 shard 接受 ms 级偏差."

**方案取舍表**:

| 方案 | 读放大 | cursor 大小 | shard 加减容错 | 复杂度 |
|------|-------|------------|---------------|--------|
| Scatter-gather (每 shard 取 limit) | N× | 1 个 cursor | 好 | 低 |
| k-way merge (heap) | 1× + tail | N 个子 cursor | 中 | 中 |
| 中心化排序节点 | 1× | 1 个 token | 好 | 高 (单点) |
| Pre-sorted global index | 1× | 1 个 | 重新建索引代价大 | 高 |

**自检题**:
- 我 cursor 里编了 N 个子 cursor —— base64 后多大? 客户端 URL 长度有限制吗?
- N=1 时我的 sharded store 行为跟单机的 StorePart5 一致吗?
- 一个 shard 慢 (network blip), 我整个 page 调用慢成什么样? 超时怎么处理?

---

## Part 8 — 缓存 + Stateful vs Stateless cursor

**问题**:80% 查询命中 1% "热门用户". 同时, cursor 形态本身有两种实现路线.

**Stateful vs Stateless cursor 对比**(面试官真要追问的):

| 维度 | Stateless(Part 3 用的) | Stateful |
|------|----------------------|----------|
| 服务端状态 | 无 | Redis / memory 存 cursor → query state |
| 客户端透明性 | cursor 含排序键 (能解码看) | 完全 opaque (UUID) |
| cursor 大小 | 几十字节 (排序键编码) | UUID 字符串 + 服务端状态可任意大 |
| 失效机制 | 永不失效 (除非那条数据被删) | TTL / LRU 驱逐, 翻太慢就 expired |
| 复杂查询 | 难 (cursor 编不进 join 中间结果) | 可以 (存任意中间状态) |
| 服务端水平扩展 | 完美 (无状态) | 需要 sticky session 或共享 Redis |

**面试官最常问的 4 个 follow-up**:

1. **"Coinbase REST API 公开 cursor, 你会选 stateful 还是 stateless?"**
   → Stateless. 公开 API 的 cursor 不能存服务端 (用户翻页可能间隔 1 小时, state 早就被驱逐了).
   → Stateful 适合**内部** dashboard, 单会话短时间内连续翻.

2. **"热门用户第一页 cache 30 秒, 用户 add 一条新交易立刻看不到, OK 吗?"**
   → 业务取舍. Coinbase 实际: 自己交易自己看 → 必须立刻看到 (不能 cache 自己的 list).
       别人交易 → 可以 cache 30 秒.
   → 实现: cache key 包含 userId, write-invalidate on add(同 userId).

3. **"哪些查询适合 cache?"**
   → 第一页 (cursor=null) 命中率高 → 适合 cache.
   → 深翻页 (cursor 非空) 各种各样 → cache hit ratio 低, 别 cache.
   → cache key = (userId, filterHash, limit). filter 字段多 → 用稳定 hash.

4. **"cache 失效策略选 write-through 还是 write-invalidate?"**
   → write-through: add 时把新 tx 插到 cache list 正确位置 —— 但 cache 是 top-N 截断, 插入位置要算.
   → write-invalidate: add 时把该用户 cache 清掉 —— 简单, 下次 cold miss.
   → 实务: invalidate 是默认选择, 简单且正确; through 只在 hot path 优化阶段才上.

**方案取舍表**:

| Cache 策略 | 一致性 | 实现复杂度 | 适合场景 |
|------------|-------|-----------|----------|
| 不 cache | strong | 0 | QPS 低 |
| Read-through + TTL | eventual (TTL 内) | 低 | 别人看, 容忍 stale |
| Read-through + invalidate | strong (近实时) | 中 | 自己看自己, 必须新鲜 |
| Write-through | strong | 高 (要算插入位置) | hot path, 命中率极高 |

**自检题**:
- cache key 含 filter, filter 是 record —— 我用了 `equals` 还是序列化 hash 做 key? hash 碰撞概率?
- 用户深翻 10 页, 每页都打 DB —— cache 完全没帮上, 我有 metric 监控这个吗?
- Stateful cursor 服务器重启, 老 cursor 全失效, 用户体验是? (前端应该 catch 这个 error code, 显示 "session expired")

---

## 怎么练

```bash
cd src/02c-其他算法面经/Coinbase/Transaction-Pagination

javac TransactionPagination.java TransactionPaginationTest.java

java TransactionPaginationTest                # 跑所有 Part
java TransactionPaginationTest part1          # 只跑 Part 1
java TransactionPaginationTest part1 part3    # 跑指定的几个
```

输出格式:

```
Part 1 SKIPPED (not implemented)
Part 2 SKIPPED (not implemented)
Part 3 SKIPPED (not implemented)
Part 4 SKIPPED (not implemented)

Passed=0  Failed=0  Skipped=4
```

骨架在 [TransactionPagination.java](TransactionPagination.java),测试在 [TransactionPaginationTest.java](TransactionPaginationTest.java)。每个 Part 在文件里是一段独立的 `public static class`,后缀 `PartN`:

```
PART 1: TransactionPagination.StorePart1         [⚠ 你来写]
PART 2: TransactionPagination.StorePart2         [⚠ 你来写]
PART 3: TransactionPagination.StorePart3         [⚠ 你来写]
PART 4: TransactionPagination.StorePart4         [⚠ 你来写]
PART 5: TransactionPagination.StorePart5         [⚠ 超越面经 follow-up — 并发]
PART 6: TransactionPagination.StorePart6         [⚠ 超越面经 follow-up — 二级索引]
PART 7: TransactionPagination.ShardedStorePart7  [⚠ 超越面经 follow-up — 分片 / 偏设计讨论]
PART 8: TransactionPagination.CachedStorePart8   [⚠ 超越面经 follow-up — cache / cursor 取舍]
```

**为什么独立 class 而不是继承同一个基类?** 真实面试是渐进的——做完 Part 1 才会让你看 Part 2。这种切片结构让你能专注当前 Part 而不破坏已完成的部分;做完之后还能讨论 "if I extract a `SortedTransactionIndex` shared by all parts, here's how it'd look"——这种 trade-off 讨论是面试加分点,但不是必做项。
