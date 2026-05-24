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

## 4 个 Part 的核心

| Part | 一句话 | 易踩的坑 |
|------|--------|----------|
| 1 | 普通 dict，后写覆盖前写 | timestamp 在 Part 1 不影响行为，只是为后面铺垫 |
| 2 | 前缀扫描，返回字符串 `"k1(v1), k2(v2)"` 按字典序 | 空前缀 = 全部；末尾**无** trailing comma；没匹配返 `""`；面试官会追问优化 |
| 3 | TTL 有效期是 **半开区间** `[t, t+ttl)` | `ttl=0` 当下就过期；不传 ttl = 永久；覆盖时新 TTL 替换旧 TTL；scan 也要过滤 |
| 4 | backup/restore | **restore 时 TTL 保留剩余量**，不是重置 |

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
PART 3: putPart3 (×2 重载) / getPart3 / scanPart3         [⚠ 你来写]
PART 4: putPart4 / getPart4 / scanPart4 / backupPart4
        / restorePart4                                   [⚠ Part 3 done 后展开]
```

**为什么这样切？** 真实面试是渐进的——做完 Part 1 才会让你看 Part 2，期间要演化 Entry 的 schema。在**练习**时如果你为了 Part 3 改了 `Entry`，可能把 Part 1+2 弄坏；这种切片结构让你能专注当前 Part 而不破坏已完成的部分。每个 Part 的代码也保留了演化轨迹，对比 `EntryPart2` 和 `EntryPart3` 可以看 schema 怎么变。
