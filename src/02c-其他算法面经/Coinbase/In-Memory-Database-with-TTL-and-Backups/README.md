# Coinbase — In-Memory Database with TTL & Backups

设计一个内存 KV 数据库，逐步加功能。面试时 CoderPad 上**一个 Part 给一个**，做完再放下一个。

来源：PracHub（**不在 LeetCode 上**，最接近的是 LC 981 但方向不同）。

---

## API 全貌

```java
put(key, value, timestamp)                       // Part 1
put(key, value, timestamp, ttlSeconds)           // Part 3 重载
get(key, timestamp) -> String                    // null 表示不存在或已过期
scan(prefix, timestamp) -> String                // Part 2，"k1(v1), k2(v2)" 按字典序；没匹配返 ""
backup(timestamp) -> int                         // Part 4，返回从 1 开始递增的 backup_id
restore(backupId, timestamp)                     // Part 4
```

---

## 8 个 Part 的核心

> Part 1–4 是面经原题（PracHub 上的 4-part 渐进设计题）。
> Part 5–8 是**超越面经**的 follow-up —— Coinbase 团队明确说重点考"并发 / 数据增长 / 取舍"，
> 这些就是面试官真要追问、而面经经常省略的部分。

| Part | 一句话 | 易踩的坑 / 讨论点 |
|------|--------|----------|
| 1 | 普通 dict，后写覆盖前写 | timestamp 在 Part 1 不影响行为，只是为后面铺垫 |
| 2 | 前缀扫描，返回字符串 `"k1(v1), k2(v2)"` 按字典序 | 空前缀 = 全部；末尾**无** trailing comma；没匹配返 `""`；面试官会追问优化 |
| 3 | TTL 有效期是 **半开区间** `[t, t+ttl)` | `ttl=0` 当下就过期；不传 ttl = 永久；覆盖时新 TTL 替换旧 TTL；scan 也要过滤 |
| 4 | backup/restore | **restore 时 TTL 保留剩余量**，不是重置 |
| 5 | **并发安全** | 锁粒度选择 / 撕裂读 / scan 的快照语义 / backup 原子性 |
| 6 | **主动 TTL 清理** | 后台线程不能拖垮前台 / 抽样 vs PQ vs timing wheel |
| 7 | **持久化 / WAL** | fsync 时机 / log compaction / 重启 replay 顺序 |
| 8 | **分片 / 横向扩展** | 一致性哈希 / cross-shard scan / 分布式 backup |

### Part 2 的 trade-off 讨论（面试加分点）

**先用 HashMap 实现最快**（5 分钟搞定，每次 scan 是 O(n) 遍历 + O(k log k) 排序）。然后**主动跟面试官讲这段 trade-off**——比真去写优化方案更值钱：

| 方案 | scan | put / get | 代码量 | 何时选 |
|------|------|-----------|--------|--------|
| **HashMap + 遍历**（推荐起步） | O(n + k log k) | O(1) | ~5 行 | 默认；scan 不是 hot path |
| **TreeMap**（红黑树有序） | O(log n + k) | O(log n) | ~5 行（换个 Map + `tailMap(prefix)`） | scan 比较频繁但 put/get 还多 |
| **Trie** | O(prefix长度 + 输出大小) | O(key长度) | ~30–50 行 | scan / prefix 是真正的 hot path，且 key 空间大 |

**面试时怎么说（就一句话）：**

> "If we have a lot of prefix queries, I'd prioritize a Trie — it makes scan optimal at O(prefix + output). But if most of our usage is regular `get`, the Trie overhead isn't worth it; HashMap with linear filtering is simpler and `get` stays O(1). TreeMap is the middle ground."

**为什么 TreeMap 比全量 scan 快**：`map.tailMap(prefix)` 直接 O(log n) 切到 ≥ prefix 的子 map，往后遍历到 key 不再以 prefix 开头就 break——不用看 prefix 前面的 key。

---

### Part 3 详解：`get` 的 5 种情况

接口签名**不变**（仍是 `get(key, timestamp)`），但语义变了——内部要判断过期。调用方不知道也不关心这个 key 有没有 TTL。

| # | 场景 | 返回 |
|---|------|------|
| 1 | key 从来没 put 过 | `null` |
| 2 | put 过，**没传 TTL**（永久 entry） | value（永远） |
| 3 | put 过，传了 TTL，且 `timestamp ∈ [putTime, putTime + ttl)` | value |
| 4 | put 过，传了 TTL，且 `timestamp >= putTime + ttl`（已过期） | `null` |
| 5 | 被覆盖过 | 只看**最近一次** put 的 TTL，旧的完全丢弃 |

**易踩边界：**

- `ttl=0`：put 当下就过期（`[t, t)` 是空区间）
- 区间末尾：`put(k,v,1,ttl=5)` 后 `get(k,5)` ✓ value；`get(k,6)` ✗ null（半开 `[1,6)`）
- 覆盖替换 TTL：旧 `ttl=100` 被新 `ttl=1` 覆盖，旧的 100 秒**完全作废**

**重载关系：**

- `put(k,v,t)` 和 `put(k,v,t,ttl)` 是**两个 Java 方法**（重载）
- `get` 和 `scan` 都只有**一个**方法，原签名不变
- `scan` 也要按同样规则过滤已过期 entry

**面试加分点 — 主动提懒删除：**

> "Expired entries linger in memory until they're get'd or scanned. We could lazily delete in get/scan, or run a periodic cleanup. For this scope I'll keep it simple."

---

### Part 4 的关键陷阱（最难一点）

backup 时存的是 **剩余 TTL**，不是绝对过期时间：

```
put("temp", "v", 1, ttl=10)   →  原过期时间 = 11
backup(t=5)                    →  剩余 = 11 - 5 = 6
restore(bid, t=20)             →  新过期时间 = 20 + 6 = 26
get("temp", 25) == "v"   ✓
get("temp", 26) == null  ✗ (半开)
```

- 永久 entry（没 ttl）→ 剩余 TTL 记为 `null`，restore 后仍永久。
- 已经过期的 entry → 不进 backup。

---

## Part 5 — 并发访问（这是 Coinbase 面试**最常追问**的方向）

**问题**：多个线程同时 `put / get / scan / backup / restore`，要求所有操作线程安全。

**面试官最常问的 4 个 follow-up**：

1. **"你这把锁会不会让 get 等 scan？"**
   → 引出 ReadWriteLock 讨论。但 scan 是读还是写？它要拿读锁、但要看到一致快照——
   两个不同 get 看到的 value 可以不同时刻，但同一个 scan 输出里的 entry 必须是同一时刻。

2. **"99% 是 get、1% 是 put 的话呢？"**
   → 引出 Copy-on-Write（读完全无锁，写时复制整个 map）。代价：写 O(n) 复制 + 写延迟高。
   反过来 99% 写 1% 读 → CHM + 细粒度锁更合适。

3. **"backup 怎么保证原子？put 跟 backup 同时跑会怎样？"**
   → 简单方案：backup 拿写锁，全停。
   进阶：MVCC 思路——backup 拿当前版本号快照，put 写新版本，老版本被 backup 读完才回收。

4. **"scan 输出 1MB 字符串，全程持锁会卡死写操作。怎么办？"**
   → snapshot iteration：先在锁内把候选 key 列表拷出来，出锁后再拼字符串。
   或者用 ConcurrentSkipListMap，迭代器本身就是 weakly consistent。

**自检题**（写完代码自己问自己）：

- 我的锁 / CHM 能保证 entry 不被"撕裂读"吗？（提示：`Entry` 是 immutable record，引用换原子，这帮了大忙）
- 两个线程同时 `put(k,v1,t)` 和 `put(k,v2,t)` 是 t 时刻同时发生，最后存的是谁？面试官会接受"未定义"吗？
- 我的 backup 输出能复现吗？

---

## Part 6 — 主动 TTL 清理

**问题**：Part 3 的 lazy delete 让过期 entry 一直占内存。加后台线程定期清理。

**4 种实现，各自的取舍**：

| 方案 | 复杂度 | 优点 | 缺点 |
|------|-------|------|------|
| 定时全扫 | O(n) 每轮 | 简单，无额外结构 | n 大时一轮卡顿 |
| **抽样清理**（Redis 用） | O(k) 每轮 | 摊销均匀 | 短期内可能漏掉一些 |
| PriorityQueue 按 expireAt 排序 | put O(log n)，清理 O(log n) | 总拿最早过期的 | 被覆盖的 entry 留下"幽灵"要懒清理 |
| Timing wheel | 实现复杂 | 大量短 TTL 场景最优 | 长 TTL 难处理（需分级时间轮） |

**跟 Part 5 的交互**：
- 清理线程拿什么锁？跟 put 的锁是同一把吗？
- 清理预算：每轮最多删 N 个，避免长尾延迟。
- 时钟来源：用 `System.currentTimeMillis()` 还是接收外部 timestamp？测试时怎么 mock？

---

## Part 7 — 持久化 / WAL（金融场景重点）

**问题**：进程被 kill 后数据要能恢复。加 write-ahead log。

**最关键的 trade-off：fsync 策略**

| 策略 | 吞吐 | 崩溃丢失 | Coinbase 能接受吗？ |
|------|------|---------|---------------------|
| 每次 put 都 fsync | 极低（~几百 ops/s） | 0 | 订单写入这种级别？可能要 |
| 每 N 笔批量 fsync | 中 | 最近 N 笔 | 多数业务场景 OK |
| 每 100ms fsync | 高 | 最近 100ms | 缓存/会话类 |
| 完全异步 | 极高 | 看进程崩多快 | 一般不行 |

面试官追问："如果一笔订单 ACK 用户了但还没 fsync 就崩了，怎么办？"
→ 答案是：**ACK 必须在 fsync 后**。这是 durability 的本质。

**Log compaction**：老的 put 被覆盖后还在日志里，日志会无限增长。
- 周期性快照：把当前内存 dump 出来，删早于快照的 log。
- 跟 Part 4 的 backup 天然契合：backup 就是 compaction checkpoint。

**Restart 流程**：
1. 加载最新快照（来自 Part 4 backup 或 Part 7 自己的 snapshot）
2. Replay 快照之后的 log
3. 用 CRC 检测 log 尾部撕裂（崩溃时最后一条可能写一半）

---

## Part 8 — 分片 / 横向扩展

**问题**：单机内存装不下。10 亿 key、64GB 单机 → 16 台机器。

**4 种分片策略**：

| 策略 | 加机器代价 | scan 友好度 | 热点风险 |
|------|----------|-----------|---------|
| `hash(key) % N` | 几乎全 key 迁移 ❌ | 差（要 scatter-gather） | 低 |
| **一致性哈希** | ~1/N 迁移 ✓ | 差 | 用虚拟节点缓解 |
| 范围分片（按 key 范围切） | 中 | **好** | 高（写热点） |
| Hybrid（范围 + hash） | 中 | 中 | 中 |

**Cross-shard 操作的硬骨头**：

1. **`scan(prefix)`**：prefix 可能跨多个 shard
   - Hash 分片：必须广播到所有 shard，gather + 全局排序。还能 O(prefix + output)? 不行了。
   - 范围分片：可能只命中 1-2 个 shard，效率高。
   → 这就是为什么 Bigtable / HBase 用范围分片。

2. **`backup`**：全局一致快照
   - 简化版：每个 shard 各自 backup，时间点略有偏差（**最终一致**）。
   - 严格版：协调一个全局 timestamp T，所有 shard 同时 backup 在 T 时刻。
     → 需要分布式快照算法（Chandy-Lamport）或者外部协调器（如 ZooKeeper）。

3. **TTL 清理**：各 shard 各自跑后台线程，没问题。

**这道题答到一半就是设计简化版 Redis Cluster 了。**真实面试**不需要写代码**，跟面试官把上面这些权衡讨论清楚就够了。

---

## 怎么练

```bash
cd src/02c-其他算法面经/Coinbase/In-Memory-Database-with-TTL-and-Backups

javac InMemoryDatabase.java InMemoryDatabaseTest.java

java InMemoryDatabaseTest                # 跑所有 Part
java InMemoryDatabaseTest part1          # 只跑 Part 1
java InMemoryDatabaseTest part1 part2    # 跑指定的几个
```

输出格式：

```
Part 1 PASSED
Part 2 SKIPPED (not implemented)
Part 3 SKIPPED (not implemented)
Part 4 SKIPPED (not implemented)

Passed=1  Failed=0  Skipped=3
```

骨架在 [InMemoryDatabase.java](InMemoryDatabase.java)，测试在 [InMemoryDatabaseTest.java](InMemoryDatabaseTest.java)。每个 Part 的方法对应骨架里的一段，照着填就行。

---

## 代码结构（练习用，不是产品代码）

每个 Part 在 `InMemoryDatabase.java` 里**完全独立**——自己的 `EntryPartN` record、自己的 `mapPartN` 字段、自己的 `putPartN` / `getPartN` / `scanPartN` 方法。

```
PART 1: putPart1 / getPart1                              [✓ done]
PART 2: putPart2 / getPart2 / scanPart2                  [✓ done]
PART 3: putPart3 (×2 重载) / getPart3 / scanPart3         [✓ done]
PART 4: putPart4 / getPart4 / scanPart4 / backupPart4
        / restorePart4                                   [⚠ 你来写]
PART 5: 同 Part 4 接口, 加并发安全                        [⚠ 超越面经 follow-up]
PART 6: startEvictorPart6 / stopEvictorPart6              [⚠ 超越面经 follow-up]
PART 7: putPart7 / getPart7 / flushPart7                  [⚠ 超越面经 follow-up]
PART 8: ShardedDatabasePart8 (建议入口)                   [⚠ 超越面经 follow-up，偏设计讨论]
```

**为什么这样切？** 真实面试是渐进的——做完 Part 1 才会让你看 Part 2，期间要演化 Entry 的 schema。在**练习**时如果你为了 Part 3 改了 `Entry`，可能把 Part 1+2 弄坏；这种切片结构让你能专注当前 Part 而不破坏已完成的部分。每个 Part 的代码也保留了演化轨迹，对比 `EntryPart2` 和 `EntryPart3` 可以看 schema 怎么变。
