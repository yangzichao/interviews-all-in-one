# Coinbase — In-Memory File System

LC 588 (Hard) baseline + 一系列 follow-up，是 Coinbase Tech Execution 轮"LC Hard + 分布式追问"的典型样本。

来源：
- [LC 588 Design In-Memory File System](https://leetcode.com/problems/design-in-memory-file-system/)
- 面经 follow-up：[LC Discuss 1028666 (Coinbase Phone, In-memory File System)](https://leetcode.com/discuss/interview-question/1028666/Coinbase-or-Phone-or-In-memory-File-System/)、[Glassdoor 3854467 "Build an in memory file system"](https://www.glassdoor.com/Interview/Build-an-in-memory-file-system-QTN_3854467.htm)、[LC Discuss 1061521 (Coinbase Bay Area 2020)](https://leetcode.com/discuss/interview-question/1061521/coinbase-bay-area-2020-reject/)

---

## API 全貌

```java
ls(path)              -> List<String>      // Part 1：dir 返子项字典序；file 返 [filename]
mkdir(path)                                 // Part 1：递归建中间目录
addContentToFile(path, content)             // Part 1：不存在创建；存在追加
readContentFromFile(path) -> String         // Part 1
mv(src, dest)                               // Part 2：移动（挪 reference，O(1)）
rm(path)                                     // Part 2：删除（摘 reference，O(1)）
cp(src, dest)                               // Part 2：拷贝（deep copy，O(子树)）
// Part 3：上面方法在一把锁下变 thread-safe
// Part 4：分布式设计（无代码）
```

---

## 4 个 Part 的核心

> Part 1 是 LC 588 原题。
> Part 2–4 是 Coinbase 面经里反复出现的渐进 follow-up。

| Part | 一句话 | 易踩的坑 / 讨论点 |
|------|--------|----------|
| 1 | LC 588 原题：4 个方法 + 树结构 | `ls` 在文件上要返 `[filename]`；`mkdir` 要递归建；`addContentToFile` 不存在自动建（含父目录） |
| 2 | `mv` / `rm` / `cp`（简化版） | 移动 vs 删除 vs 拷贝三连：mv/rm 挪 reference O(1)，cp 必须 deep copy O(N)；overwrite/cycle 等边界放 README 讨论 |
| 3 | **简单线程安全**：一把 RWLock | 读 lock / 写 lock；不展开 per-node 锁 |
| 4 | **分布式设计**（白板讨论） | metadata vs data 分离；按 path prefix 还是 inode shard；cross-shard mv |

---

## Part 1 — 数据结构选型（trade-off 讨论）

最自然的表示：**一种 tree node**，可以同时承担 file / dir 两种角色（用一个 `isFile` 字段、或 `content == null` 标记目录）。

`children` 用什么 map？

| 选择 | `ls` | `mkdir / add / read` | 何时选 |
|------|------|----------------------|--------|
| `HashMap` + `ls` 时 sort | O(k) + O(k log k) sort | O(1) | **默认**；匹配真实 fs（ext4 htree、HDFS namenode）的做法 |
| `TreeMap`（红黑树有序） | O(k)（天然有序） | O(log k) | `ls` 是 hot path 且目录很大；写多时反而变慢 |
| `Trie`（按路径段共享前缀） | O(prefix + 输出) | O(path 长度) | 路径深、prefix-style 查询多（如 `find /a -name '*.log'`） |

**关键事实**：POSIX `readdir(2)` **不保证顺序**——shell 里 `ls` 看到的字典序是 `ls` 这个**命令在用户态自己 sort 出来的**，kernel 把无序 entries 丢过来。所以 "HashMap + 在 `ls` API 里 sort" = 生产文件系统的真实分工。

**面试加分口播**：

> "I'll use HashMap for children — O(1) lookup matches what real filesystems do internally (ext4's htree, HDFS's inode map). POSIX `readdir` doesn't guarantee order anyway; the sort in `ls` is a userspace concern, so I'll sort on the API call. If you push me on 'what about a directory with millions of entries' — that's when you'd switch to either ext4's htree (a B-tree variant) or paginated listing like S3's `ListObjects`. TreeMap is the in-memory toy version of that escape hatch, not the default."

### 边界 checklist

- `ls("/")` 在空 fs 上返 `[]`
- `ls` 在 file 上返 `[filename]`，**不**返 content
- `mkdir` 已存在 → no-op，**不**抛异常
- `addContentToFile` 不存在 → 创建（含所有缺失父目录）；存在 → 追加（不是覆盖）
- 题目保证 path 合法，不用处理 `..` / 重复斜线

---

## Part 2 — `mv` / `rm` / `cp`（文件系统题经典 follow-up 三连）

三个操作考的是**同一个核心：引用语义的区别**。一句话记牢：

| 操作 | 语义 | 实现 | 复杂度 |
|------|------|------|--------|
| `mv(src, dest)` | 移动 | 子树 reference 从旧 parent 摘下、挂到新 parent | **O(1)** |
| `rm(path)` | 删除 | 子树 reference 从 parent 摘下，GC 自动回收 | **O(1)** |
| `cp(src, dest)` | 拷贝 | **递归 deep copy 整棵子树**（新建每个 Node） | **O(子树大小)** |

**面试官就想听这个对比**：mv/rm 只动一个引用；cp 要造一整棵新树。

**题面（练习时假设）**：
- `mv` / `cp`：`src` 存在；`dest.parent` 存在；`dest` 不存在（不考虑 overwrite）；`src` 不是 `dest` 祖先；`src != dest`
- `rm`：`path` 存在，且不是 root

**思路提示**（不展开代码）：
- `mv`：子树**重接 reference**，不要 deep copy；rename 时记得更新 `node.name`
- `rm`：`parent.children.remove(name)` 一行就够；不必递归清子节点（GC 接手）
- `cp`：**必须递归新建**——`copy(node)` 造一个新 Node，再对每个 child 递归 `copy` 挂上去。
  千万别像 mv 一样挪 reference，否则改副本会改到原件（test 专门抓这个）

### 面试官常追问的"硬"边界（口播即可）

**`mv` 的边界**

1. **"如果 `dest` 已存在怎么办？"**
   → POSIX `mv` 的语义是 overwrite。文件 overwrite 文件可以；文件 overwrite 非空目录要拒绝；目录 overwrite 非空目录要拒绝。
2. **"如果把目录 mv 进它自己的子树？"**
   → 必须拒绝（会形成游离环）。检测方式：从 `dest.parent` 向上走 parent 链，看是否撞到 `src`。代价 O(depth)。
3. **"大子树 mv 应该是 O(1) 还是 O(N)？"**
   → 应该 O(1)（只改两个 parent 的 children map）。如果你 deep-copy 就是 O(N)——错误答案。
4. **"跨文件系统 mv？"**
   → 那就退化成 `cp -r && rm -rf`，不是原子的。Linux `EXDEV` 错误。

**`rm` 的边界**

5. **"`rm` 一个非空目录要报错吗？"**
   → POSIX `rmdir` 拒绝非空目录；`rm -r` 才递归删。练习版直接摘 reference = `rm -rf` 语义。
6. **"`rm` 大子树是 O(1) 还是 O(N)？"**
   → 摘 reference 是 O(1)，剩下交给 GC。但若要返回"删了多少文件"或释放外部资源（fd / 磁盘块），就得 O(N) 遍历。
7. **"有别的引用指向被删子树会怎样？"**
   → Java 里 GC 不回收（还有人引用）；类比 Unix 的 hard link / inode refcount——`unlink` 只减引用计数，到 0 才真删。

**`cp` 的边界**

8. **"`cp` 为什么不能像 mv 那样挪 reference？"**
   → 因为 cp 后 src 和 dest 是**两个独立实体**，改一个不能影响另一个。共享 reference 会让它们纠缠。
9. **"`cp` 软链接 vs 硬链接 vs 深拷贝？"**
   → 深拷贝 = 整棵子树复制（我们练的）；硬链接 = 共享 inode（省空间，改一个都变）；软链接 = 存一个路径字符串。
10. **"巨大子树 cp 怎么优化？"**
    → copy-on-write：先共享，等真正写某个节点时才复制那一条路径（btrfs / ZFS 的 `cp --reflink` 就是这么干的）。

---

## Part 3 — 简单线程安全（"day-1 上线版本"）

**问题陈述**：多线程并发 `ls / mkdir / addContent / read / mv`，怎么保证不撕裂？

**最简方案**（这一 Part 实现的就是它）：
- 整个 fs **一把** `ReentrantReadWriteLock`
- **读**操作（`ls`, `readContentFromFile`）：`lock.readLock()`
- **写**操作（`mkdir`, `addContentToFile`, `mv`）：`lock.writeLock()`

**为什么从这里起步**：
- 正确性最容易论证：write 独占，read 之间共享，没有死锁可能（只有一把锁）
- 代码量 5 行：每个方法套 `lock.xxx().lock() / try {...} finally { unlock() }`
- 面试官真要听的就是"你先选最稳的，然后讲清楚什么时候才需要升级"

### 面试官最常追问的 4 个 follow-up（不在代码里展开）

1. **"全局锁的瓶颈在哪？"**
   → 大量并发只读还行（read lock 共享）；只要一个 writer 进来，所有 reader 阻塞。`addContentToFile` 是写——一台机器写很多文件时所有读都被卡。
2. **"怎么改成 per-directory 锁？"**
   → 每个 node 自带锁；`ls` 拿该 dir 的 read lock；`mkdir/add` 拿目标 parent 的 write lock。**但 `mv` 同时改两个 parent → 需要一致顺序加锁**（按 path 字典序 / inode id 大小），否则死锁。
3. **"`addContentToFile` 是 read-modify-write，read lock 够吗？"**
   → 不够。这操作要写 content（即使节点已存在），必须 write lock。**典型陷阱**：以为"节点存在就是 read 路径"——错。
4. **"能不能让 read lock-free？"**
   → 可以：copy-on-write tree（写时复制路径上的 node + bump root atomic reference）；或者整个 fs 是 immutable，每次写返回新 root（持久化数据结构）。读完全不加锁。代价：写放大 O(depth)，GC 压力。

### 自检题（写完代码自己问自己）

- 我的 `mv` 拿的是 read 还是 write lock？为什么？
- 如果一个线程在 `readContentFromFile`，另一个线程在 `mv` 这个文件，谁先 / 谁后是 well-defined 的吗？（在 RWLock 下，是的——一个会等另一个完成）
- 如果换成 per-node 锁，`mv("/a/x", "/b/y")` 要拿几把锁？顺序？

---

## Part 4 — 分布式（白板讨论，无代码）

面经里 follow-up chain 大致这样推进。**不写代码**，对着 README 自答即可。

### Q1: "How would you make this distributed?"

参考 GFS / HDFS：
- **Namenode** 存 metadata（inode tree、permission、path → chunk handle 的映射）。inode 树通常 ~GB 量级，可以全放内存。
- **Datanode** 存实际 content。文件按 chunk（64 MB / 128 MB）切，每个 chunk 3 副本散到不同 datanode。
- Client 先问 namenode 拿 chunk handle + datanode 列表，再直连 datanode 读写。

### Q2: "How would you shard the metadata when one namenode isn't enough?"

| 方案 | 优点 | 缺点 |
|------|------|------|
| **按 path prefix shard** (`/a/*` → shard A, `/b/*` → shard B) | `ls`, `mkdir` 大概率 single-shard；locality 好 | 热点目录失衡（`/users/`）；`mv /a/x → /b/y` 跨 shard 难做原子 |
| **按 inode id 一致性哈希** | 负载均匀；`mv` 不改 inode id 所以是 metadata-only 操作 | `ls` 要 fan-out 到所有 shard 收集 children；rebalance 麻烦 |
| **federation（每个 shard 是一棵完整子树挂载点）** | 简单；HDFS Federation 实际用 | `mv` 跨挂载点退化为 cp + rm |

业界更常用的是 prefix shard + 一层 **mount table**（client 端缓存"/a → shard A"）。

### Q3: "How would cross-shard `mv` stay atomic?"

朴素思路 ❌：先在 shard B 创建 dest，再在 shard A 删 src。中间崩了 → 两份 / 零份。

正确思路：
- **两阶段提交（2PC）**：coordinator 让 A、B 都 prepare（A 标记 src 为 "pending unlink"，B 标记 dest 为 "pending link"），全 prepared 才 commit；任何一方 abort 都全回滚。
- **Saga**：拆成 `link(dest)` + `unlink(src)`，两步都 idempotent；失败可重试，最终一致。期间外部可能看到两份——文件系统语义里大多不接受，所以 2PC 更常见。
- **借鉴单 metadata raft log**：所有 metadata 操作走一条共识 log，`mv` 是一条原子 log entry。问题：metadata 写吞吐被 raft 限死，namenode 不能水平扩展。

### Q4: "What's the consistency model?"

- **Metadata：strong (linearizable)**——用户对"我刚 `mkdir`，下一秒 `ls` 看不到"非常敏感。
- **Content：eventually consistent OK**——`addContentToFile` 之后，read 容忍几十 ms 滞后是可以的（HDFS 就是这样：write 完成后只保证"最终"被 replica 看到）。
- 这就是 GFS 的核心取舍。

### 自检题

- 单台 namenode 的吞吐瓶颈在哪？（metadata 写的 raft / 持久化 / 锁）
- 一个文件 100 GB 怎么存？（chunked + replicated；client 并行读多个 chunk）
- 怎么做 snapshot？（写时复制 inode 树根，content chunk 共享不动）
- 一个 datanode 挂了多久能恢复？（namenode 监测心跳超时 → 触发 re-replicate；典型分钟级）

---

## 怎么练

```bash
cd src/02c-其他算法面经/Coinbase/In-Memory-File-System
javac InMemoryFileSystem.java InMemoryFileSystemTest.java
java InMemoryFileSystemTest                    # 跑全部
java InMemoryFileSystemTest part1 part2        # 只跑指定
```

Part 4 没有 runnable test —— 它是白板讨论，对着 README 的 Q1-Q4 自答即可。

---

## 代码结构（练习用，不是产品代码）

```
PART 1: 4 methods on a tree (ls / mkdir / addContent / read)         [⚠ TODO]
PART 2: + mv (reuse Part 1 tree, thin wrapper for others)            [⚠ TODO]
PART 3: + 单 ReentrantReadWriteLock 包装 Part 2                       [⚠ TODO]
PART 4: 分布式设计（README §"Part 4"，无代码）                         [💬 DISCUSS]
```
