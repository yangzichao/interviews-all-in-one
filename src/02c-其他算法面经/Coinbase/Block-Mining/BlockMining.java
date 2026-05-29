import java.util.*;

/**
 * 4-part Coinbase interview practice — Block Mining (mempool tx selection).
 *
 * 每个 Part 是独立的 class，后缀 PartN。先无脑独立写，做完再讨论抽公共逻辑。
 *
 * 这不是产品代码，是练习代码 —— 让你能专注当前 Part 而不破坏已完成的部分。
 */
public class BlockMining {

    // 简单 tx (Part 1 用)
    public static record Transaction(String id, int size, long fee) {}

    // 带父依赖的 tx (Part 2/3/4 用)
    public static record Transaction2(String id, int size, long fee, List<String> parents) {}

    // ====================================================================
    // PART 1  —  Greedy Fee Maximization                            [⚠ TODO]
    // ====================================================================
    // 按 fee/size 比从大到小贪心选, 不可分割, 装不下跳过.
    //
    //   txs = [(a,30,90),(b,40,80),(c,30,60)]
    //   blockSize=70
    //   a: 3.0 → 选 (剩 40)
    //   c: 2.0 → 选 (剩 10)
    //   b: 2.0 → 装不下 (40 > 10)
    //   返回 ["a","c"]   total fee=150, size=60
    //
    // 这是近似解, 不是最优 —— 面试不让做 DP.

    public static class MinerPart1 {
        public List<String> selectTransactions(List<Transaction> txs, int blockSize) {
            throw new UnsupportedOperationException("MinerPart1.selectTransactions: not implemented");
        }
    }

    // ====================================================================
    // PART 2  —  Parent-Child Bundles                               [⚠ TODO]
    // ====================================================================
    // tx 有 parents, 父必须和子在同一 block. 选子 = 选它的祖先闭包.
    //
    //   p (10,10, parents=[]), c (10,100, parents=[p])
    //   bundle({c}) = {p,c} totalSize=20 totalFee=110 ratio=5.5
    //
    // 排序按 "bundle 总 fee / 总 size".
    // 选中 bundle → 所有成员都进 block.

    public static class MinerPart2 {
        public List<String> selectTransactions(List<Transaction2> txs, int blockSize) {
            throw new UnsupportedOperationException("MinerPart2.selectTransactions: not implemented");
        }
    }

    // ====================================================================
    // PART 3  —  Dedupe Shared Ancestors                            [⚠ TODO]
    // ====================================================================
    // 共享的 ancestor 在 block 里只算一份 size 一份 fee.
    // 选下一个 bundle 时, 已在 block 的 tx 不算 size 不算 fee (增量计算).
    //
    //   p(10,10), c1(20,100,parents=[p]), c2(20,80,parents=[p])
    //   blockSize=60
    //   选 c1 → block={p,c1}, used=30
    //   选 c2 → incremental={c2}, +size=20, 装得下 → used=50, fee=190
    //
    // hint: 每次重算剩余 candidate 的 incremental ratio, pick best.

    public static class MinerPart3 {
        public List<String> selectTransactions(List<Transaction2> txs, int blockSize) {
            throw new UnsupportedOperationException("MinerPart3.selectTransactions: not implemented");
        }
    }

    // ====================================================================
    // PART 4  —  Multiple Blocks                                    [⚠ TODO]
    // ====================================================================
    // 填 N 个 block, 按优先级顺序填. 同一 tx 只能进一个 block.
    // 依赖 + 去重规则继承 Part 3, 但跨 block 不共享 ancestor 折扣 ——
    // 每个 block 内部必须自己包含所有依赖.
    //
    //   selectBlocks(txs, blockSize=70, numBlocks=2)
    //   → [["a","c"], ["b","d"]]

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
