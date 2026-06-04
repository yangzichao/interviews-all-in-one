import java.util.*;

/**
 * Coinbase interview practice — Block Mining (mempool tx selection)  (8 parts).
 *
 * ════════════════════════════════════════════════════════════════════════
 *  背景故事 (BACKGROUND) —— 读这里就够入手, 不需要懂区块链内部
 * ════════════════════════════════════════════════════════════════════════
 *
 *  在一条区块链 (比如比特币) 里, 用户发起的每一笔转账叫一笔 "交易
 *  (transaction, 简称 tx)"。这些 tx 不会立刻上链, 而是先漂在一个叫
 *  "内存池 (mempool)" 的待处理池子里排队。
 *
 *      [用户发 tx] ──▶  mempool (一池子待打包的 tx)  ──▶  [矿工 miner: 打包出块]
 *
 *  矿工 (miner) 要做的是: 从池子里挑一批 tx, 塞进一个 "区块 (block)"。
 *  但区块容量有限 (blockSize, 单位是字节), 装不下所有 tx。每笔 tx 都附带一笔
 *  "手续费 (fee)" 给矿工当报酬。所以矿工的核心问题是:
 *
 *      "在区块装得下的前提下, 挑哪几笔 tx 能让我收到的手续费总额最大?"
 *
 *  这本质是一个背包问题 (容量=blockSize, 价值=fee, 重量=size)。真实矿工不做
 *  最优 DP, 而是用 "按性价比 fee/size 从高到低贪心选" 的近似解 —— 这就是 Part 1。
 *
 *  后面层层加码, 贴近真实世界的复杂度:
 *    · tx 之间有依赖: 子 tx 必须和它的父 tx 一起进同一个块 (Part 2)。
 *    · 多个子共享同一个父: 父在块里只占一份 size / fee, 不能重复算 (Part 3)。
 *    · 一次要填好几个块 (Part 4); 多个矿工抢同一个 mempool (Part 5);
 *      池子有容量上限要淘汰 (Part 6); 两个矿工同时出块导致链分叉 (Part 7);
 *      共识机制与崩溃恢复 (Part 8, 偏设计讨论)。
 *
 *  几个术语 (后面注释会用到, 先混个脸熟):
 *    · transaction / tx : 一笔交易; 带 id、size (占多少字节)、fee (手续费)
 *    · fee              : 这笔 tx 付给矿工的报酬; 矿工想让选中 tx 的 fee 总和最大
 *    · size             : 这笔 tx 占用的字节数 (背包里的 "重量")
 *    · fee/size 比率     : "性价比" —— 每字节带来多少手续费, 贪心排序的依据
 *    · blockSize        : 一个区块的容量上限 (字节); 选中 tx 的 size 总和不能超过它
 *    · block            : 一个区块 = 矿工选中的一批 tx
 *    · mempool          : 待打包 tx 的内存池 (Part 5 起是多线程共享的可变集合)
 *    · parent / ancestor: tx 依赖的父 tx / 祖先链 (Part 2 起); 子进块, 祖先必须也进
 *    · bundle           : 一个子 tx + 它的全部祖先闭包, 作为一个整体被选中 (Part 2)
 *    · re-org           : 链重组 —— 出现更长的兄弟链时切换主链 (Part 7)
 *
 * ════════════════════════════════════════════════════════════════════════
 *
 * 题面直接写在每个 Part 上方 —— 读代码就能读题, 不用切到别处。
 * 坑点 / 取舍 / follow-up 答案在 README.md (含剧透), 练的时候别看。
 *
 * 逐步加约束; 面试时一次只给一个 Part, 做完再放下一个。
 * 每个 Part 是独立的 class, 后缀 PartN —— 让你能专注当前 Part 而不破坏已完成的部分。
 */
public class BlockMining {

    // 简单 tx (Part 1 用)
    public static record Transaction(String id, int size, long fee) {}

    // 带父依赖的 tx (Part 2/3/4 用)
    public static record Transaction2(String id, int size, long fee, List<String> parents) {}

    // ====================================================================
    // PART 1  —  Greedy Fee Maximization                            [⚠ TODO]
    // ====================================================================
    // 场景: 矿工面对一池子 tx (这里直接给一个 List), 区块容量 blockSize 有限,
    //       每笔 tx 占 size 字节、付 fee 手续费。挑哪几笔, 在不超容量的前提下
    //       让手续费总和尽量大? tx 不可分割 (要么整笔进, 要么不进)。
    //
    //   selectTransactions(txs, blockSize) -> List<String>
    //     输入: txs = Transaction(id, size, fee) 列表; blockSize = 容量上限
    //     输出: 选中 tx 的 id 列表 (顺序不强制, 测试按 set 比较)
    //
    //   txs = [(a,30,90),(b,40,80),(c,30,60)]   blockSize=70
    //     a: 90/30=3.0 → 选 (剩 40)
    //     c: 60/30=2.0 → 选 (剩 10)
    //     b: 80/40=2.0 → 装不下 (40 > 10), 跳过
    //   → {"a","c"}    选中 total fee=150, size=60
    //
    // 这是按 fee/size 比从高到低的贪心近似解, 不是最优 —— 面试不让做 DP。
    //
    // 边界 (测试覆盖):
    //   - 空输入            → []
    //   - blockSize=0       → []   (什么都装不下)
    //   - 单 tx 比 block 大  → 跳过它 (oversize tx skipped)
    //   - 全部装得下        → 全选

    public static class MinerPart1 {
        public List<String> selectTransactions(List<Transaction> txs, int blockSize) {
            throw new UnsupportedOperationException("MinerPart1.selectTransactions: not implemented");
        }
    }

    // ====================================================================
    // PART 2  —  Parent-Child Bundles                               [⚠ TODO]
    // ====================================================================
    // 场景: 真实 tx 之间有依赖 —— 一笔 tx 可能花的是另一笔 tx 的产出, 所以
    //       "子 tx" 必须和它的 "父 tx" (以及父的父…整条祖先链) 在同一个 block 里,
    //       否则区块自相矛盾。于是选一个子 tx, 实际上等于选它的整个 "祖先闭包"。
    //
    //   selectTransactions(txs, blockSize) -> List<String>
    //     输入: txs = Transaction2(id, size, fee, parents) 列表 (parents 是父 id 列表)
    //     把一个子 + 它全部祖先打包成一个 "bundle", 整体进或整体不进。
    //     排序依据从 "单 tx 的 fee/size" 升级成 "bundle 总 fee / bundle 总 size"。
    //
    //   p(10,10, parents=[]), c(10,100, parents=[p])
    //     bundle({c}) = {p,c}: totalSize=20, totalFee=110, ratio=110/20=5.5
    //     blockSize=25 → 选, {"p","c"}        (子把父一起拉进来)
    //     blockSize=15 → bundle 装不下 → 整个 bundle 都不进, []
    //
    //   多层祖先 gp ← p ← c: 选 c 要把 gp、p 都带上 → {"gp","p","c"}
    //   没有依赖的 tx → 退化成 Part 1 的行为。

    public static class MinerPart2 {
        public List<String> selectTransactions(List<Transaction2> txs, int blockSize) {
            throw new UnsupportedOperationException("MinerPart2.selectTransactions: not implemented");
        }
    }

    // ====================================================================
    // PART 3  —  Dedupe Shared Ancestors                            [⚠ TODO]
    // ====================================================================
    // 场景: Part 2 把每个子的祖先闭包当独立 bundle, 但如果两个子共享同一个父,
    //       那个父在区块里只该占 "一份" size、只该付 "一份" fee —— 不能因为
    //       两个子都引用它就重复计两遍。所以当一笔祖先已经因为前一个 bundle
    //       进了 block, 后面再选别的 bundle 时, 这笔已在 block 里的 tx 对新
    //       bundle 来说 "免费" (size 不再占、fee 也不再加), 这叫增量 (incremental) 计算。
    //
    //   selectTransactions(txs, blockSize) -> List<String>   (同 Part 2 签名)
    //     区别只在: 评估 "再加一个 bundle" 时, 只算它里面 *尚未在 block* 的那些 tx。
    //
    //   p(10,10), c1(20,100,parents=[p]), c2(20,80,parents=[p])   blockSize=50
    //     选 c1 → block={p,c1}, used=10+20=30
    //     再看 c2: 它的闭包是 {p,c2}, 但 p 已在 block → 增量只有 c2 (+size=20)
    //             30+20=50 ≤ 50 装得下 → block={p,c1,c2}, used=50, fee=10+100+80=190
    //   → {"p","c1","c2"}   (若不去重会以为要 10+20+10+20=60, 装不下 c2)
    //
    //   blockSize=30 → 装得下 {p,c1}=30, 但再加 c2 会到 50 → 只 {"p","c1"}
    //   没有共享祖先时 → 退化成 Part 2。空输入 → []。

    public static class MinerPart3 {
        public List<String> selectTransactions(List<Transaction2> txs, int blockSize) {
            throw new UnsupportedOperationException("MinerPart3.selectTransactions: not implemented");
        }
    }

    // ====================================================================
    // PART 4  —  Multiple Blocks                                    [⚠ TODO]
    // ====================================================================
    // 场景: 矿工不止填一个块, 而是按优先级连填 N 个块: 先把最赚钱的 tx 塞满
    //       block1, 剩下的填 block2, 依此类推。一笔 tx 只能进一个块 (进了
    //       block1 就不能再进 block2)。块内的依赖闭包 + 共享祖先去重规则
    //       继承 Part 3, 但 *折扣只在块内有效* —— 跨块不共享: 每个块必须
    //       自包含它用到的全部祖先 (block2 不能 "借用" block1 里的父 tx)。
    //
    //   selectBlocks(txs, blockSize, numBlocks) -> List<List<String>>
    //     返回恰好 numBlocks 个列表 (即使后面的块是空的也要占位返 [])。
    //
    //   txs=[(a,30,90),(b,40,80),(c,30,60)]  blockSize=70  numBlocks=2
    //     block1 (最高优先): a+c = size 60 ≤ 70 → {"a","c"}
    //     block2 (剩 b):      b = 40           → {"b"}
    //   → [{"a","c"}, {"b"}]   (同一 tx 不跨块重复)
    //
    //   块数多于需求: blockSize=100, numBlocks=3 → [{"a","b","c"}, [], []]
    //   依赖必须块内自包含: p(50,1) ← c(50,1000), blockSize=60
    //     bundle {p,c}=100 > 60, 任何单块都装不下 → [[], []]

    public static class MinerPart4 {
        public List<List<String>> selectBlocks(List<Transaction2> txs, int blockSize, int numBlocks) {
            throw new UnsupportedOperationException("MinerPart4.selectBlocks: not implemented");
        }
    }

    // ====================================================================
    // PART 5  —  并发挖矿 (concurrent miners on shared mempool)     [⚠ TODO]
    // ====================================================================
    // 与 Part 4 比:
    //   同: Transaction2 / 选 tx 的规则 (依赖闭包 + 去重) 不变
    //   变: mempool 是一个共享的可变集合; N 个 miner 线程同时从里面挑 tx 出块
    //       miner A 选中并 "确认" 一笔 tx 后, miner B 不能再用它
    //   新: SharedMempoolPart5 (add / size / 内部线程安全)
    //       MinerPart5.mine(...) 返回这一轮挑到的 tx (并把它们从 mempool 移走)
    //
    // 场景: 把 Part 1-4 里 "一个 List 当输入" 升级成一个真实存在的共享池子,
    //       多个矿工线程同时往里挑 tx 出块。一笔 tx 被某个矿工 "确认" 拿走后,
    //       就从池子里消失, 别的矿工再也拿不到它。
    //
    // 接口形状:
    //   SharedMempoolPart5: add(tx) 加一笔; size() 当前还剩几笔;
    //       takeForBlock(blockSize) 原子地挑一批不冲突的 tx 出来并从池中移走。
    //   MinerPart5.mine(mempool, blockSize) -> List<String>: 挖一轮 = 调 take, 返回拿到的 id。
    //
    // 例 (单线程验证基本行为):
    //   add a(30,90), b(40,80), c(30,60); size()=3
    //   takeForBlock(70) → 拿到 {a,c} (按 ratio 贪心, 同 Part 1); 之后 size()=1
    //
    // 例 (并发不变量): 100 笔 tx, 两个 miner 各 mine 5 轮, 同一笔 tx 绝不会
    //   被两个 miner 同时拿到 (no tx mined twice)。
    //
    // 问题陈述:
    //   真实区块链里, 多个 miner 同时在抢同一笔 tx 进自己的下一个 block.
    //   一个 tx 一旦被某个 block 确认, 其他 miner 就不该再选它.
    //   要求 mine() 是线程安全的, 多 miner 并发跑不会:
    //     (a) 同一笔 tx 被两个 miner 同时 "确认"
    //     (b) miner 看到的 mempool 是撕裂的 (一半旧一半新)
    //     (c) add(tx) 跟 mine() 死锁
    //
    // 你要写的: SharedMempoolPart5 (内部数据结构 + 加锁/CAS),
    //          以及 MinerPart5.mine(mempool, blockSize) 调用并发安全的 take 接口.

    public static class SharedMempoolPart5 {
        public void add(Transaction2 tx) {
            throw new UnsupportedOperationException("TODO: Part 5 — mempool 并发添加");
        }

        public int size() {
            throw new UnsupportedOperationException("TODO: Part 5 — mempool size");
        }

        // hint: 让 miner 从里面拿一批不冲突的 tx, 返回的 tx 必须 atomically 从 mempool 移走
        public List<Transaction2> takeForBlock(int blockSize) {
            throw new UnsupportedOperationException("TODO: Part 5 — 原子地拿一批 tx 出来");
        }
    }

    public static class MinerPart5 {
        // mine 一轮 = 从 mempool 拿一个 block 的 tx, 返回它们的 id
        public List<String> mine(SharedMempoolPart5 mempool, int blockSize) {
            throw new UnsupportedOperationException("TODO: Part 5 — 并发 miner");
        }
    }

    // ====================================================================
    // PART 6  —  Mempool 内存管理 (bounded + eviction)              [⚠ TODO]
    // ====================================================================
    // 与 Part 5 比:
    //   同: 共享 mempool + 并发 add/mine 的形状
    //   变: mempool 有容量上限 (maxBytes); 达到上限后 add 必须淘汰一些 tx
    //       淘汰策略影响"被淘汰掉的 tx 还能不能进未来的 block"
    //   新: BoundedMempoolPart6 多了 maxBytes 构造参数
    //       evictedCount() 暴露统计, 便于讨论
    //
    // 场景: 池子不能无限大。新 tx 进来时若总占用会超过 maxBytes, 就得腾地方 ——
    //       淘汰掉池里 "最不值钱" 的 tx (fee/size 比最低的)。但若新 tx 本身就比
    //       池里所有 tx 都差, 那就没必要赶走好的去换个更差的 → 直接拒绝它。
    //
    // 接口形状:
    //   new BoundedMempoolPart6(maxBytes); add(tx) -> boolean (是否被接受);
    //   currentBytes() 当前占用; evictedCount() 累计淘汰数; takeForBlock 同 Part 5。
    //   不变量: 任何时刻 currentBytes() ≤ maxBytes。
    //
    // 例:
    //   maxBytes=50, 连加 6 笔 size=10 的 tx → currentBytes() ≤ 50,
    //     evictedCount() ≥ 1 (装不下 60, 必淘汰至少一笔)。
    //   maxBytes=30, 已有 hi/mid/lo (fee 1000/500/100, 各 size 10);
    //     add(trash, size=10, fee=1) → 比谁都差 → 返回 false, evictedCount 不变。
    //
    // 问题陈述:
    //   生产环境 mempool 不能无限大 —— Bitcoin Core 默认 300MB.
    //   达到上限时, 新 tx 进来要么被拒, 要么挤掉一些旧 tx.
    //   要求: 在并发下也能正确维护容量约束.
    //
    // 你要写的: BoundedMempoolPart6.add(tx) 在容量不够时按 fee/size 比淘汰最差的 tx,
    //          直到新 tx 装得下 (或新 tx 自己比所有现有都差 → 直接拒绝).

    public static class BoundedMempoolPart6 {
        public BoundedMempoolPart6(int maxBytes) {
            throw new UnsupportedOperationException("TODO: Part 6 — 带上限的 mempool 初始化");
        }

        public boolean add(Transaction2 tx) {
            throw new UnsupportedOperationException("TODO: Part 6 — 满了就淘汰最差的");
        }

        public int currentBytes() {
            throw new UnsupportedOperationException("TODO: Part 6 — 当前占用");
        }

        public int evictedCount() {
            throw new UnsupportedOperationException("TODO: Part 6 — 累计淘汰数");
        }

        public List<Transaction2> takeForBlock(int blockSize) {
            throw new UnsupportedOperationException("TODO: Part 6 — 同 Part 5 的 take, 但带容量统计");
        }
    }

    // ====================================================================
    // PART 7  —  链分叉 / Re-org (longest chain rule)               [⚠ TODO]
    // ====================================================================
    // 与 Part 6 比:
    //   同: 选 tx 进 block 的算法不变 (复用 Part 4 的 selectBlocks 思路)
    //   变: 不再只有一条链 —— 两个 miner 几乎同时出块就分叉了
    //       系统要在收到新 block 时判断:
    //         (a) 接在现有 tip 后 → 直接接
    //         (b) 接在某个老 block 后形成兄弟 → 出现 fork
    //         (c) 兄弟链变得比主链长 → 触发 re-org, 切到新主链
    //   新: BlockChainPart7 / BlockPart7 (id, parentId, txIds, height)
    //
    // 场景: 网络延迟下两个矿工几乎同时出块, 链就分叉成两条兄弟链。中本聪共识
    //       规定: 谁的链 "更长" (height 更大) 谁是主链。当一条原本较短的兄弟链
    //       后来居上变得更长, 系统就要 "re-org (链重组)": 把主链切过去, 同时
    //       原主链上那些 tx 因为不再被确认, 要退回 mempool 等待重新打包。
    //
    // 接口形状:
    //   new BlockChainPart7(genesisId): 用创世块 (height 0) 初始化。
    //   addBlock(BlockPart7(id, parentId, txIds, height)): 接一个块, 必要时 re-org。
    //   currentHead() 当前主链 tip 的 id; currentHeight() 主链高度;
    //   reorgCount() 累计 re-org 次数; lastReorgRevertedTxs() 上次 re-org 被踢回的 tx。
    //
    // 例:
    //   genesis g0 (head=g0, height=0)
    //   接 A1(parent g0,h1), A2(parent A1,h2) → head=A2, height=2, reorg=0
    //   接 B1(parent g0,h1) → 兄弟链更短 → head 仍 A2, reorg=0
    //   接 B2(h2), B3(parent B2,h3) → B 链更长 → head=B3, height=3, reorg≥1
    //     lastReorgRevertedTxs 包含旧 A 链的 tx_a1、tx_a2 (它们退回 mempool)
    //
    // 问题陈述:
    //   实现 longest-chain rule (中本聪共识):
    //     - 维护一棵区块树, 选出 height 最大的一条作为 "主链"
    //     - 主链切换时, 老主链上的 tx 重新进 mempool, 新主链的 tx 离开 mempool
    //   要求: addBlock 后能正确报告当前 head, 以及 re-org 时正确恢复 mempool.
    //
    // 你要写的: addBlock(block) + currentHead() + 处理 re-org 时的 tx 流动.

    public static record BlockPart7(String id, String parentId, List<String> txIds, int height) {}

    public static class BlockChainPart7 {
        public BlockChainPart7(String genesisId) {
            throw new UnsupportedOperationException("TODO: Part 7 — 用 genesis 初始化");
        }

        public void addBlock(BlockPart7 block) {
            throw new UnsupportedOperationException("TODO: Part 7 — 接 block + 必要时 re-org");
        }

        public String currentHead() {
            throw new UnsupportedOperationException("TODO: Part 7 — 当前主链 tip");
        }

        public int currentHeight() {
            throw new UnsupportedOperationException("TODO: Part 7 — 主链高度");
        }

        // 主链切换次数 (用于测试 re-org 真的发生过)
        public int reorgCount() {
            throw new UnsupportedOperationException("TODO: Part 7 — re-org 计数");
        }

        // re-org 时被踢出主链、应该 "退回" mempool 的 tx
        public List<String> lastReorgRevertedTxs() {
            throw new UnsupportedOperationException("TODO: Part 7 — 上一次 re-org 退回的 tx");
        }
    }

    // ====================================================================
    // PART 8  —  PoW vs PoS / 持久化与重启恢复 (设计讨论)          [⚠ TODO]
    // ====================================================================
    // 与 Part 7 比:
    //   同: Block / Chain 模型不变
    //   变: 两个偏设计的话题 —— 不强求写代码, 主要在 README 讨论
    //       1) 共识机制: PoW vs PoS 的 trade-off
    //       2) 持久化: 区块链本身是 append-only, 但 mempool 是易失的;
    //                  进程崩了, mempool 怎么恢复?
    //   新: 建议的轻量接口 ChainPersistencePart8, 写不写都行
    //
    // 场景: 把前面的内存模型放到一个会崩溃、会重启的真实进程里。链本身是
    //       append-only 的 (只追加新块), 天然好持久化; 但 mempool 是易失的 ——
    //       一崩就丢。讨论怎么落盘恢复, 以及换共识机制 (PoW→PoS) 后哪些假设要改。
    //       (没有可跑的测试; testPart8 直接抛 UnsupportedOperationException。)
    //
    // 问题陈述 (面试常以"你来设计"的方式问):
    //   - 如何让 chain 在崩溃后能恢复? (append-only 文件 + 启动 replay)
    //   - mempool 要不要也持久化? 如果不持久化, 崩溃丢失会有什么影响?
    //   - 切换到 PoS 后, 哪些 Part 1-7 的假设变了?
    //
    // 不强求写代码 —— 跟面试官把权衡讨论清楚就是高分.
    // 建议入口签名 (留着给愿意写的人):

    public interface ChainPersistencePart8 {
        void appendBlock(BlockPart7 block);          // 把新 block 落盘 (append-only)
        List<BlockPart7> loadAllBlocks();            // 启动时全量回放
        void snapshotMempool(List<Transaction2> txs); // 可选: 周期性 dump mempool
        List<Transaction2> loadMempoolSnapshot();    // 启动时恢复 mempool
    }

    // 实现留空 —— 这道题主要在 README 里讨论 PoW/PoS 取舍 + mempool 持久化策略.
}
