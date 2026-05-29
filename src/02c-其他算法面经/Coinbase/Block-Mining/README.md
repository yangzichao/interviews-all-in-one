# Coinbase — Block Mining (Mempool Tx Selection)

模拟矿工从 mempool 选 transaction 打包进 block，逐步加约束。面试时 CoderPad 上**一个 Part 给一个**，做完再放下一个。

来源：Coinbase VO 高频题（见 [coinbase-vo-problem-origins.md](../coinbase-vo-problem-origins.md)）。**不在 LeetCode 上**，灵感来自 Bitcoin 真实 block 打包算法（fractional knapsack by fee/size），但 tx 不可分割所以是贪心**近似**——面试官明确禁止 DP（这是一道"工程题"，不是"算法题"）。

---

## API 全貌

```java
// Part 1 — 单 block, 无依赖
new MinerPart1()
    .selectTransactions(List<Transaction> txs, int blockSize) -> List<String>

// Part 2 — 加 parent-child 依赖
new MinerPart2()
    .selectTransactions(List<Transaction2> txs, int blockSize) -> List<String>

// Part 3 — 多 bundle 之间共享的 ancestor 去重
new MinerPart3()
    .selectTransactions(List<Transaction2> txs, int blockSize) -> List<String>

// Part 4 — 多 block
new MinerPart4()
    .selectBlocks(List<Transaction2> txs, int blockSize, int numBlocks) -> List<List<String>>

// 数据类型
record Transaction(String id, int size, long fee)
record Transaction2(String id, int size, long fee, List<String> parents)
```

---

## 4 个 Part 的题面

### Part 1 — Greedy Fee Maximization

给一堆 tx，每个有 `size` 和 `fee`，block 容量 `blockSize`，**最大化 fee**。tx 不可分割。

```
txs = [(a, size=30, fee=90), (b, size=40, fee=80), (c, size=30, fee=60)]
blockSize = 70

按 fee/size 比贪心:
  a: 90/30 = 3.0  ← 先选 (剩 40)
  c: 60/30 = 2.0  ← 再选 (剩 10)
  b: 80/40 = 2.0  ← 装不下 (40 > 10), 跳过

输出: ["a","c"]    总 fee 150, 总 size 60
```

**Clarifications**:
- 贪心按 `fee/size` 比从大到小排，**不可分割**，装不下就跳过看下一个。
- 这是**近似解**，不是最优——面试明确不让做 DP（fee 可能很大，0/1 knapsack DP 空间不可行）。
- 输出顺序：题面没要求，按贪心选中的顺序返回即可。
- 同分时怎么 tiebreak？面试不强制；测试里避开了 tie。

---

### Part 2 — Parent-Child Bundles

每个 tx 多了 `parents: List<String>`，**父 tx 必须和子 tx 在同一个 block** (UTXO 依赖)。选一个子 tx 等于选它和它所有祖先。

```
txs = [
  (p, size=10, fee=10, parents=[]),
  (c, size=10, fee=100, parents=[p])   // 单独看 c, ratio 极高; 但要带上 p
]
blockSize = 25

bundle({c}) = {p, c}, totalSize=20, totalFee=110, ratio=5.5
```

**Clarifications**:
- 父也可能有父，递归向上 (ancestor closure)。
- 选 bundle 的"得分"按 `bundle 的总 fee / 总 size` 排。
- 一旦选了一个 bundle，它的所有成员都进 block。
- 不考虑环（输入合法）。

---

### Part 3 — Dedupe Shared Ancestors

Part 2 的天真做法：选完 bundle A 后再选 bundle B，**B 里和 A 共享的 ancestor 会被重复计算 size/fee**。正确做法是去重——共享 ancestor 在 block 里只算一份 size 和一份 fee。

```
共享 parent p:
  p (size=10, fee=10)
  c1 parents=[p] (size=20, fee=100)
  c2 parents=[p] (size=20, fee=80)
blockSize = 60

天真: bundle({c1})={p,c1} 30/110, bundle({c2})={p,c2} 30/90
      选了 c1 后剩 30, 算 c2 bundle 还是 30 → 刚好装下, 但 p 被算了两次的 size 是错的.
正确: 选 c1 后实际占用 {p,c1}=30; 加 c2 时 p 已在 block, 只新增 c2 自己 20 (90 - 10 = 80 新 fee).
      所以确实能放下, 但要按"增量 size / 增量 fee"思考.
```

**Clarifications**:
- 选下一个 bundle 时，只把**还没在 block 里**的 tx 算进 size / fee。
- "已经选了的 ancestor 已经付费"——重复 ancestor 是免费的 size、零 fee。
- 排序基准也得相应调整（按 incremental ratio）。
- 实现策略（hint, 不写在代码里）：每次重算剩余 candidate 的 incremental score，pick the best；或者更精细的 CPFP-style ancestor scoring。

---

### Part 4 — Multiple Blocks

填 N 个 block，按优先级顺序填。前一个 block 装满了就开下一个；同一个 tx 只能进一个 block。

```
selectBlocks(txs, blockSize=70, numBlocks=2)
→ [["a","c"], ["b","d"]]   // block 1 优先级最高, block 2 次之
```

**Clarifications**:
- 依赖 + 去重规则继承 Part 3。
- 同一个 tx 只能进一个 block。
- 跨 block **不**共享 ancestor 折扣——block N+1 的 bundle 不能假设 block N 里的 ancestor 还在；如果 ancestor 没和 child 在同一个 block 就违反依赖。所以每个 block 内部各自闭包。
- block 数量不够装完所有 tx 是允许的（返回的 inner list 可以不满）。
- 返回 list 长度恰好 = `numBlocks`（即使后面是空 list？面试可讨论；测试里我们 trim 掉空 block）。

---

## Part 5 — 并发挖矿（Coinbase 面试**最常追问**的方向）

**问题陈述**：mempool 是一个**共享**的可变集合，N 个 miner 线程同时从里面挑 tx 出块。一笔 tx 一旦被某个 block "确认"，其他 miner 就不该再选它。要求 `add()` 跟 `takeForBlock()` 在并发下：
- 同一笔 tx 不会被两个 miner 同时拿走
- miner 看到的 mempool 不会撕裂（一半旧一半新）
- 高频 `add` 不会饿死 `takeForBlock`，反之亦然

**面试官最常问的 4 个 follow-up**：

1. **"你的锁是 `synchronized this` 还是 `ReadWriteLock`？为什么？"**
   → mempool 是**写重**场景（每个 tx 进来都写、每次 mine 都改），ReadWriteLock 的读并发优势用不上。
   但要追问候选人有没有想清楚 "take 是读还是写"——它**读完一批马上要从 mempool 移走**，本质是 write。

2. **"两个 miner 同时调 `takeForBlock(50)`，会不会都拿到同一笔 tx？怎么保证不会？"**
   → 引出"原子地选 + 标记 + 移除"的讨论。最简单：take 时拿独占锁。
   进阶：每个 tx 一个 `AtomicBoolean claimed` 字段，CAS 抢；但要处理 "claim 了但 block 装不下" 的回滚。

3. **"mempool 里有 100 万笔 tx，take 一次要排序全部，锁会被持很久。怎么办？"**
   → 引出**始终维护一个按 fee/size 排序的结构**（`ConcurrentSkipListSet` / `PriorityBlockingQueue`）。
   缺点：依赖闭包让 "最优 tx" 不一定能直接挑——还要做 ancestor 检查。
   或者：snapshot 出锁——锁内只复制候选 id 列表，出锁后再算。

4. **"如果一个 miner 拿了一批 tx 但还没出块就崩了，这些 tx 会永远丢吗？"**
   → 引出 "lease / lock with timeout" —— take 出来的 tx 不立即移除，标记 reserved 一段时间；
   超时未确认就 release 回 mempool。这是 Bitcoin 节点真实做的（mempool acceptance vs block inclusion 分离）。

**方案对比表**：

| 方案 | 并发吞吐 | 实现复杂度 | take 一致性 |
|------|---------|----------|------------|
| 一把大锁 (`synchronized`) | 低 | ★ | 强 |
| `ConcurrentHashMap` + take 时拿全局锁 | 中 | ★★ | take 内部强一致 |
| `PriorityBlockingQueue` + drainTo | 高 | ★★★ | 弱（drain 出来的可能不是当下最优） |
| CAS / AtomicBoolean claimed 标记 | 高 | ★★★★ | 需手动处理回滚 |

**自检题**：
- 我能解释为什么 `Transaction2` 是 immutable record 让并发实现简单吗？（引用换原子，内容不会被改坏）
- 我的 `takeForBlock` 在拿锁期间会不会做 O(n log n) 排序？这是问题吗？
- 如果 take 出来的 tx 后来发现装不下，我会不会"少删"或"多删"？

---

## Part 6 — Mempool 内存管理（数据增长 + 淘汰）

**问题陈述**：mempool 不能无限大——Bitcoin Core 默认 300MB 上限。达到 `maxBytes` 时，新 tx 进来要么被拒，要么挤掉旧 tx。要求：在并发下也维持容量不变式。

**面试官最常问的 4 个 follow-up**：

1. **"淘汰策略按什么排？fee 还是 fee/size？"**
   → fee/size（feerate），因为 mempool 容量约束的是 **size**，不是数量。
   面试要主动说出 "fee 大但 size 也大的 tx 可能不如 fee 小但 size 更小的 tx"。

2. **"新来的 tx 比所有现有都差怎么办？淘汰一通然后还不收？"**
   → **不能**淘汰之后又拒绝——这是 DoS 向量：攻击者发垃圾 tx 把好 tx 全挤走，自己又不进。
   正确做法：先看 "如果淘汰最差的 K 个，新 tx 装得下吗 + 新 tx 比这 K 个的平均 feerate 高吗"，
   两个都满足才执行淘汰；否则**直接拒绝**。

3. **"有依赖的 tx 怎么淘汰？淘汰一个 parent 但留着 child 会怎样？"**
   → child 会变成"孤儿"（orphan tx）—— 实际 Bitcoin 节点会把它们也一起淘汰。
   引出 ancestor/descendant package 的概念：淘汰要按 package 算，不能拆开。

4. **"高频 add 时, eviction 在锁里做会不会成为瓶颈？"**
   → 不在 add 路径里 sync 算 —— 后台线程定期看一眼水位, 超过 90% 才触发批量淘汰。
   或者: 维护一个 minHeap by feerate, eviction 是 O(log n) 而不是 O(n).

**方案对比表**：

| 策略 | 公平性 | 抗 DoS | 实现复杂度 |
|------|-------|--------|----------|
| FIFO（最早进入的踢） | 差（高 fee 也可能被踢） | 差 | ★ |
| LRU | 差 | 差 | ★★ |
| **按 feerate 淘汰最低** | 好（保留最赚钱的） | 好（需配合 "新 tx 必须高于淘汰阈值"） | ★★★ |
| Package-aware 淘汰 | 最好（不破坏依赖） | 好 | ★★★★ |

**自检题**：
- 我的 `add` 在容量满时, 会不会先扣 size 再发现新 tx 比所有都差然后还得回滚?
- 我能不能给出 evicted/accepted 的统计, 方便面试官追问 "线上怎么 debug"?

---

## Part 7 — 链分叉 / Re-org（longest chain rule）

**问题陈述**：实现中本聪共识的核心—— **最长链规则**。两个 miner 几乎同时出块就分叉了，节点要维护一棵区块树，选出 `height` 最大的链为主链。主链切换（re-org）时，老主链上的 tx 要"退回" mempool，新主链的 tx 离开 mempool。

**面试官最常问的 4 个 follow-up**：

1. **"两条链一样长怎么 tiebreak？"**
   → Bitcoin 真实策略：**先收到的赢**（first-seen）—— 不切换。
   也可以按 block hash 字典序，但 first-seen 更自然。
   面试要说出"这是收敛性问题——没有 tiebreak 规则就可能两个节点永远不同步"。

2. **"re-org 深度可以无限吗？6 个块以上的 re-org 怎么处理？"**
   → 工业实践有 "finality depth" —— 超过 N 个 confirmations 的 block 视为终态，不允许 re-org。
   Coinbase 自己的入金规则就是 "N confirmations 后才认账"。
   PoS 链（如 Ethereum 2）有 explicit finality (Casper FFG)；PoW 是经济 finality（攻击成本）。

3. **"re-org 时, 老链的 tx 全部退回 mempool 吗？如果它们已经在新主链上呢？"**
   → 要做差集：`reverted = oldChainTxs - newChainTxs`。
   如果某笔 tx 两条链都有（rare but possible），就不退回。
   引出 "替代交易（RBF / Replace-By-Fee）" 的讨论。

4. **"双花攻击在这个模型里怎么发生？"**
   → 攻击者私下挖一条更长的链, 包含一笔把钱转回自己的 tx, 然后公开链触发 re-org.
   防御就是 confirmation depth + 51% attack 假设.

**方案对比表**：

| 主链选择规则 | 收敛性 | 抗攻击 | 复杂度 |
|------------|-------|--------|--------|
| Longest chain (block count) | ★★ | 弱（不考虑 PoW 难度） | ★ |
| **Heaviest chain (累计 PoW)** | ★★★ | 好（Bitcoin 用） | ★★ |
| GHOST (含 uncle blocks) | ★★★ | 好（Ethereum PoW 期用） | ★★★★ |
| Finality gadget (Casper) | ★★★★ | 最好 | ★★★★★ |

**自检题**：
- 我的 `addBlock` 收到一个 parent 还没收到的 block 怎么办？(orphan pool)
- 我能 detect 出 "提交了一个不接在任何已知 block 上的 block" 这种异常吗？
- re-org 时 mempool 操作是不是 atomic 的？re-org 一半被中断会怎样？

---

## Part 8 — PoW vs PoS / 持久化与重启恢复（设计讨论）

**问题陈述**：两个偏设计的话题，**不强求写代码**。
- (a) chain 本身是 append-only，进程崩了如何恢复？mempool 这种易失状态丢了影响多大？
- (b) 切换到 PoS 后，Part 5-7 的哪些假设变了？

**面试官最常问的 4 个 follow-up**：

1. **"chain 怎么持久化？每个 block 都 fsync 还是批量？"**
   → block 级别 fsync 是必须的（chain 是金融数据，丢一个 block 是灾难）。
   但 block 本身已经是 "几秒级" 的 rate，fsync 不会成为瓶颈。
   mempool 不一样——tx 来得快，全 fsync 撑不住。

2. **"mempool 丢了有什么影响？要不要持久化？"**
   → mempool 丢了 = 一批 unconfirmed tx 失踪。用户会重发，但用户体验差。
   实践：周期性 snapshot（不是 WAL），重启时加载最新 snapshot + 等 peer gossip 重新填充。
   Bitcoin Core 就是这么做的——`mempool.dat`。

3. **"PoW 改成 PoS 后，Part 5 的并发挖矿模型还成立吗？"**
   → 不太一样：PoS 是 "validator 被选中出块"，不是 "所有人抢"。
   并发竞争从 "争 tx" 变成 "validator 内部的 tx 选择算法"（基本退化成单线程 + Part 4 的算法）。
   分叉问题反而更突出，因为 PoS 无能耗代价让 "nothing-at-stake" 攻击成立——需要 slashing 机制。

4. **"如果让你为 Coinbase 重新设计一个轻节点（SPV），mempool 还要吗？"**
   → 轻节点通常**不维护 mempool**，只验证 block headers + Merkle proof 自己关心的 tx。
   引出 SPV / Merkle proof 的讨论——这是另一个常见 follow-up。

**方案对比表（PoW vs PoS）**：

| 维度 | PoW | PoS |
|------|-----|-----|
| 出块决定 | 算力竞争（抢） | 按 stake 加权抽签 |
| 并发模型 | Part 5 的 "多 miner 抢 tx" 成立 | validator 内部单线程即可 |
| 分叉频率 | 高（自然分叉） | 低（理论上） |
| Finality | 概率（6 个 conf） | 显式（Casper / Tendermint） |
| 能耗 | 高 | 低 |
| Nothing-at-stake | 不存在 | 存在，需 slashing |
| 51% 攻击成本 | 算力 | 持币 |

**Mempool 持久化方案对比**：

| 方案 | 数据丢失风险 | 重启速度 | 实现复杂度 |
|------|------------|---------|----------|
| 完全不持久化 | 全丢，靠 peer gossip 重填 | 快 | ★ |
| **周期性 snapshot** (Bitcoin Core) | 最近一次 snapshot 之后的 tx | 中 | ★★ |
| WAL（每笔 tx append） | 几乎 0 | 慢（要 replay） | ★★★★ |

**自检题**：
- 我能不能解释 chain 持久化和 mempool 持久化为什么策略不同？(频率 + 重要性)
- PoS 下 Part 5 的 "并发竞争 tx" 还有意义吗？
- "轻节点不维护 mempool" 这句话我能展开讲清楚原因吗？

---

## 怎么练

```bash
cd src/02c-其他算法面经/Coinbase/Block-Mining

javac BlockMining.java BlockMiningTest.java

java BlockMiningTest                # 跑所有 Part
java BlockMiningTest part1          # 只跑 Part 1
java BlockMiningTest part1 part2    # 跑指定的几个
```

输出格式：

```
Part 1 SKIPPED (not implemented)
Part 2 SKIPPED (not implemented)
Part 3 SKIPPED (not implemented)
Part 4 SKIPPED (not implemented)

Passed=0  Failed=0  Skipped=4
```

骨架在 [BlockMining.java](BlockMining.java)，测试在 [BlockMiningTest.java](BlockMiningTest.java)。每个 Part 在文件里是一段独立的 `public static class`，后缀 `PartN`：

```
PART 1: BlockMining.MinerPart1   selectTransactions(List<Transaction>,...)    [⚠ 你来写]
PART 2: BlockMining.MinerPart2   selectTransactions(List<Transaction2>,...)   [⚠ 你来写]
PART 3: BlockMining.MinerPart3   selectTransactions(List<Transaction2>,...)   [⚠ 你来写]
PART 4: BlockMining.MinerPart4   selectBlocks(...)                            [⚠ 你来写]
PART 5: SharedMempoolPart5 + MinerPart5  并发 mempool + 多 miner            [⚠ 超越面经 follow-up]
PART 6: BoundedMempoolPart6      mempool 容量上限 + 淘汰                    [⚠ 超越面经 follow-up]
PART 7: BlockChainPart7          区块树 + longest chain + re-org             [⚠ 超越面经 follow-up]
PART 8: ChainPersistencePart8    持久化 + PoW/PoS 设计讨论                  [⚠ 超越面经 follow-up，偏设计]
```

**为什么不让做 DP？** 真实 Bitcoin mempool 里 tx 数能上万，fee 是 sat 单位（数十亿量级），0/1 knapsack DP 的 `O(n * W)` 完全跑不动。Bitcoin Core 用的就是 ancestor-feerate greedy。面试看你能不能在拿到"贪心"这个方向后**主动说出**这是 trade-off：

> "Greedy by fee-rate is approximate—pathological inputs can be far from optimal. But fee values and tx counts make DP infeasible at production scale, so we accept the approximation. If we needed tighter bounds we'd look at LP relaxation or branch-and-bound, not full DP."

这种 trade-off 讨论是面试加分点。
