import java.util.*;

public class BlockMiningTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", BlockMiningTest::testPart1);
        tests.put("part2", BlockMiningTest::testPart2);
        tests.put("part3", BlockMiningTest::testPart3);
        tests.put("part4", BlockMiningTest::testPart4);
        tests.put("part5", BlockMiningTest::testPart5);
        tests.put("part6", BlockMiningTest::testPart6);
        tests.put("part7", BlockMiningTest::testPart7);
        tests.put("part8", BlockMiningTest::testPart8);

        List<String> toRun = args.length == 0 ? new ArrayList<>(tests.keySet()) : Arrays.asList(args);
        for (String name : toRun) {
            Runnable t = tests.get(name);
            if (t == null) {
                System.out.println("unknown part: " + name + ", available: " + tests.keySet());
                System.exit(2);
            }
            run(name, t);
        }
        System.out.printf("%nPassed=%d  Failed=%d  Skipped=%d%n", passed, failed, skipped);
        if (failed > 0) System.exit(1);
    }

    static void run(String name, Runnable test) {
        String label = "Part " + name.substring(4);
        try {
            test.run();
            System.out.println(label + " PASSED");
            passed++;
        } catch (UnsupportedOperationException e) {
            System.out.println(label + " SKIPPED (not implemented)");
            skipped++;
        } catch (AssertionError e) {
            System.out.println(label + " FAILED: " + e.getMessage());
            failed++;
        } catch (Throwable e) {
            System.out.println(label + " ERROR: " + e);
            e.printStackTrace(System.out);
            failed++;
        }
    }

    static void assertEq(Object expected, Object actual, String msg) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(msg + " — expected: " + expected + ", actual: " + actual);
        }
    }

    static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    // 把 List<String> 当 set 比较 (顺序不强制)
    static void assertSetEq(Set<String> expected, List<String> actual, String msg) {
        Set<String> actualSet = new HashSet<>(actual);
        if (!expected.equals(actualSet)) {
            throw new AssertionError(msg + " — expected set: " + expected + ", actual: " + actualSet);
        }
        if (actual.size() != actualSet.size()) {
            throw new AssertionError(msg + " — duplicates in output: " + actual);
        }
    }

    static BlockMining.Transaction tx(String id, int size, long fee) {
        return new BlockMining.Transaction(id, size, fee);
    }

    static BlockMining.Transaction2 tx2(String id, int size, long fee, String... parents) {
        return new BlockMining.Transaction2(id, size, fee, Arrays.asList(parents));
    }

    // ===== Part 1: Greedy Fee Maximization ==============================

    static void testPart1() {
        BlockMining.MinerPart1 m = new BlockMining.MinerPart1();

        // 经典: a 比率最高, c 第二, b 装不下
        List<BlockMining.Transaction> txs = List.of(
                tx("a", 30, 90),   // 3.0
                tx("b", 40, 80),   // 2.0
                tx("c", 30, 60)    // 2.0
        );
        assertSetEq(Set.of("a", "c"), m.selectTransactions(txs, 70), "greedy ratio picks a,c (b doesn't fit)");

        // 全部都装得下
        List<BlockMining.Transaction> txs2 = List.of(
                tx("a", 10, 50),
                tx("b", 10, 40),
                tx("c", 10, 30)
        );
        assertSetEq(Set.of("a", "b", "c"), m.selectTransactions(txs2, 100), "all fit");

        // 空输入
        assertEq(List.of(), m.selectTransactions(List.of(), 100), "empty input");

        // blockSize=0
        assertEq(List.of(), m.selectTransactions(txs, 0), "zero block size picks nothing");

        // 单 tx 大于 block
        List<BlockMining.Transaction> txs3 = List.of(tx("huge", 1000, 9999));
        assertEq(List.of(), m.selectTransactions(txs3, 10), "oversize tx skipped");
    }

    // ===== Part 2: Parent-Child Bundles =================================

    static void testPart2() {
        BlockMining.MinerPart2 m = new BlockMining.MinerPart2();

        // c 必须带上 p
        List<BlockMining.Transaction2> txs = List.of(
                tx2("p", 10, 10),
                tx2("c", 10, 100, "p")
        );
        assertSetEq(Set.of("p", "c"), m.selectTransactions(txs, 25), "child pulls parent");

        // bundle 装不下 → 一个都不进
        assertEq(List.of(), m.selectTransactions(txs, 15), "bundle exceeds block: skip whole bundle");

        // 没依赖 = Part 1 行为
        List<BlockMining.Transaction2> txs2 = List.of(
                tx2("a", 30, 90),
                tx2("b", 40, 80),
                tx2("c", 30, 60)
        );
        assertSetEq(Set.of("a", "c"), m.selectTransactions(txs2, 70), "no deps reduces to part 1");

        // 多层祖先
        List<BlockMining.Transaction2> txs3 = List.of(
                tx2("gp", 10, 5),
                tx2("p", 10, 5, "gp"),
                tx2("c", 10, 200, "p")
        );
        assertSetEq(Set.of("gp", "p", "c"), m.selectTransactions(txs3, 30), "transitive ancestors");
    }

    // ===== Part 3: Dedupe Shared Ancestors ==============================

    static void testPart3() {
        BlockMining.MinerPart3 m = new BlockMining.MinerPart3();

        // 共享 parent p, blockSize 刚好够装 {p, c1, c2}=50, 不去重就会以为要 60
        List<BlockMining.Transaction2> txs = List.of(
                tx2("p", 10, 10),
                tx2("c1", 20, 100, "p"),
                tx2("c2", 20, 80, "p")
        );
        assertSetEq(Set.of("p", "c1", "c2"), m.selectTransactions(txs, 50), "shared ancestor counted once");

        // blockSize 不够装第二个 child, 但够装第一个 bundle
        assertSetEq(Set.of("p", "c1"), m.selectTransactions(txs, 30), "second bundle doesn't fit even with dedupe");

        // 没共享时退化为 Part 2
        List<BlockMining.Transaction2> txs2 = List.of(
                tx2("p1", 10, 10),
                tx2("c1", 10, 100, "p1"),
                tx2("p2", 10, 10),
                tx2("c2", 10, 100, "p2")
        );
        assertSetEq(Set.of("p1", "c1", "p2", "c2"), m.selectTransactions(txs2, 40), "no shared ancestors");

        // 空输入
        assertEq(List.of(), m.selectTransactions(List.of(), 100), "empty input");
    }

    // ===== Part 4: Multiple Blocks ======================================

    static void testPart4() {
        BlockMining.MinerPart4 m = new BlockMining.MinerPart4();

        // 同 Part 1 数据, 切两个 block: a+c (60) 进 block1, b (40) 进 block2
        List<BlockMining.Transaction2> txs = List.of(
                tx2("a", 30, 90),
                tx2("b", 40, 80),
                tx2("c", 30, 60)
        );
        List<List<String>> blocks = m.selectBlocks(txs, 70, 2);
        assertEq(2, blocks.size(), "returns exactly numBlocks lists");
        assertSetEq(Set.of("a", "c"), blocks.get(0), "block 1 highest priority");
        assertSetEq(Set.of("b"), blocks.get(1), "block 2 leftover");

        // 同一 tx 不能进两个 block
        Set<String> allChosen = new HashSet<>();
        for (List<String> b : blocks) allChosen.addAll(b);
        assertEq(3, allChosen.size(), "no duplicate tx across blocks");

        // 块数比需求多 → 后面是空 list
        List<List<String>> blocks2 = m.selectBlocks(txs, 100, 3);
        assertEq(3, blocks2.size(), "returns numBlocks lists");
        assertSetEq(Set.of("a", "b", "c"), blocks2.get(0), "everything fits in block 1");
        assertEq(List.of(), blocks2.get(1), "block 2 empty");
        assertEq(List.of(), blocks2.get(2), "block 3 empty");

        // 依赖必须在同一个 block 内 (不能跨 block 借 ancestor)
        List<BlockMining.Transaction2> txs3 = List.of(
                tx2("p", 50, 1),
                tx2("c", 50, 1000, "p")
        );
        // blockSize=60: bundle {p,c}=100 装不下 → 两个 block 也都装不下 (一个 block 必须自包含)
        List<List<String>> blocks3 = m.selectBlocks(txs3, 60, 2);
        assertEq(2, blocks3.size(), "returns numBlocks even if empty");
        assertEq(List.of(), blocks3.get(0), "bundle doesn't fit in block 1");
        assertEq(List.of(), blocks3.get(1), "bundle doesn't fit in block 2 either");
    }

    // ===== Part 5: 并发挖矿 ==============================================

    static void testPart5() {
        BlockMining.SharedMempoolPart5 mempool = new BlockMining.SharedMempoolPart5();
        mempool.add(tx2("a", 30, 90));
        mempool.add(tx2("b", 40, 80));
        mempool.add(tx2("c", 30, 60));
        assertEq(3, mempool.size(), "mempool has 3 tx after add");

        // 单线程 take 一个 block: 验证基本 take 行为
        List<BlockMining.Transaction2> taken = mempool.takeForBlock(70);
        Set<String> takenIds = new HashSet<>();
        for (BlockMining.Transaction2 t : taken) takenIds.add(t.id());
        assertTrue(takenIds.equals(Set.of("a", "c")), "single-threaded take picks a,c (greedy by ratio)");
        // 被 take 的 tx 应该已经离开 mempool
        assertEq(1, mempool.size(), "taken tx removed from mempool");

        // 并发 mine: 两个 miner 同时抢, 同一笔 tx 不能被两边都拿到
        BlockMining.SharedMempoolPart5 mp2 = new BlockMining.SharedMempoolPart5();
        for (int i = 0; i < 100; i++) {
            mp2.add(tx2("t" + i, 10, 100 - i));
        }
        BlockMining.MinerPart5 m1 = new BlockMining.MinerPart5();
        BlockMining.MinerPart5 m2 = new BlockMining.MinerPart5();
        List<List<String>> results = Collections.synchronizedList(new ArrayList<>());
        Thread th1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) results.add(m1.mine(mp2, 50));
        });
        Thread th2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) results.add(m2.mine(mp2, 50));
        });
        th1.start(); th2.start();
        try { th1.join(); th2.join(); } catch (InterruptedException e) { throw new RuntimeException(e); }
        Set<String> allMined = new HashSet<>();
        int totalSelected = 0;
        for (List<String> r : results) {
            for (String id : r) {
                totalSelected++;
                if (!allMined.add(id)) {
                    throw new AssertionError("tx " + id + " mined twice across miners");
                }
            }
        }
        assertEq(totalSelected, allMined.size(), "no tx mined twice");
    }

    // ===== Part 6: Mempool 内存管理 (容量上限 + 淘汰) ====================

    static void testPart6() {
        // 容量 50 字节, 加 6 笔 size=10 的 tx, 必须淘汰一些
        BlockMining.BoundedMempoolPart6 mp = new BlockMining.BoundedMempoolPart6(50);
        for (int i = 0; i < 6; i++) {
            mp.add(tx2("t" + i, 10, 100L + i));
        }
        assertTrue(mp.currentBytes() <= 50, "respects maxBytes");
        assertTrue(mp.evictedCount() >= 1, "at least one tx evicted");

        // 比所有现有都差的 tx → 直接拒绝, 不淘汰任何东西
        BlockMining.BoundedMempoolPart6 mp2 = new BlockMining.BoundedMempoolPart6(30);
        mp2.add(tx2("hi", 10, 1000));
        mp2.add(tx2("mid", 10, 500));
        mp2.add(tx2("lo", 10, 100));
        int beforeEvicted = mp2.evictedCount();
        boolean accepted = mp2.add(tx2("trash", 10, 1));
        assertEq(false, accepted, "tx worse than everything is rejected");
        assertEq(beforeEvicted, mp2.evictedCount(), "no eviction when new tx is worst");
    }

    // ===== Part 7: 链分叉 / Re-org ======================================

    static void testPart7() {
        BlockMining.BlockChainPart7 chain = new BlockMining.BlockChainPart7("g0");
        assertEq("g0", chain.currentHead(), "genesis is initial head");
        assertEq(0, chain.currentHeight(), "genesis height is 0");

        // 主链: g0 -> A1 -> A2
        chain.addBlock(new BlockMining.BlockPart7("A1", "g0", List.of("tx_a1"), 1));
        chain.addBlock(new BlockMining.BlockPart7("A2", "A1", List.of("tx_a2"), 2));
        assertEq("A2", chain.currentHead(), "head is A2");
        assertEq(2, chain.currentHeight(), "height 2");
        assertEq(0, chain.reorgCount(), "no re-org yet");

        // 分叉: g0 -> B1 (同 height 1, 现在 tip 应该还是 A2)
        chain.addBlock(new BlockMining.BlockPart7("B1", "g0", List.of("tx_b1"), 1));
        assertEq("A2", chain.currentHead(), "shorter fork doesn't switch head");
        assertEq(0, chain.reorgCount(), "still no re-org");

        // B1 -> B2 -> B3, B 链更长 → 触发 re-org
        chain.addBlock(new BlockMining.BlockPart7("B2", "B1", List.of("tx_b2"), 2));
        chain.addBlock(new BlockMining.BlockPart7("B3", "B2", List.of("tx_b3"), 3));
        assertEq("B3", chain.currentHead(), "longer chain wins");
        assertEq(3, chain.currentHeight(), "height switched to 3");
        assertTrue(chain.reorgCount() >= 1, "re-org counted");

        // re-org 时, 旧主链上的 tx (tx_a1, tx_a2) 应该被报为退回
        Set<String> reverted = new HashSet<>(chain.lastReorgRevertedTxs());
        assertTrue(reverted.contains("tx_a1") && reverted.contains("tx_a2"),
                "reverted txs include old chain txs, got: " + reverted);
    }

    // ===== Part 8: 持久化 / PoW vs PoS (设计讨论) =======================

    static void testPart8() {
        // Part 8 主要是 README 讨论. 如果用户写了 ChainPersistencePart8 的实现可以在这里测.
        throw new UnsupportedOperationException("Part 8 is design-discussion; see README");
    }
}
