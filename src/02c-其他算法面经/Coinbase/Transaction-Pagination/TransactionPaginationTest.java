import java.util.*;

public class TransactionPaginationTest {

    // default package 里没法 import 嵌套类型, 直接用全名 TransactionPagination.Xxx.

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", TransactionPaginationTest::testPart1);
        tests.put("part2", TransactionPaginationTest::testPart2);
        tests.put("part3", TransactionPaginationTest::testPart3);
        tests.put("part4", TransactionPaginationTest::testPart4);
        tests.put("part5", TransactionPaginationTest::testPart5);
        tests.put("part6", TransactionPaginationTest::testPart6);
        tests.put("part7", TransactionPaginationTest::testPart7);
        tests.put("part8", TransactionPaginationTest::testPart8);

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

    static List<String> ids(List<TransactionPagination.Transaction> txs) {
        List<String> out = new ArrayList<>();
        for (TransactionPagination.Transaction tx : txs) out.add(tx.id());
        return out;
    }

    // ===== Part 1: add + filter + list ==================================

    static void testPart1() {
        TransactionPagination.StorePart1 store = new TransactionPagination.StorePart1();

        // 故意乱序插入, 测试排序
        store.add(new TransactionPagination.Transaction("b", "alice", "BUY",  500, 10));
        store.add(new TransactionPagination.Transaction("a", "alice", "BUY",  300, 10));   // tie-breaker: id ASC
        store.add(new TransactionPagination.Transaction("c", "bob",   "SELL", 700, 20));
        store.add(new TransactionPagination.Transaction("d", "alice", "BUY",  100, 5));
        store.add(new TransactionPagination.Transaction("e", "bob",   "BUY",  200, 15));

        TransactionPagination.Filter empty = TransactionPagination.Filter.empty();

        // 排序: c(20) > e(15) > a(10) > b(10) > d(5)
        assertEq(List.of("c", "e", "a", "b", "d"),
                ids(store.list(empty)), "full sort order");
        assertEq(List.of("a", "b", "d"),
                ids(store.list(new TransactionPagination.Filter("alice", null, null, null))),
                "filter by userId=alice");
        assertEq(List.of("e", "a", "b", "d"),
                ids(store.list(new TransactionPagination.Filter(null, "BUY", null, null))),
                "filter by type=BUY");
        assertEq(List.of("c", "e", "a", "b"),
                ids(store.list(new TransactionPagination.Filter(null, null, 200L, 700L))),
                "filter amount in range");

        // duplicate id → IllegalArgumentException
        boolean threw = false;
        try {
            store.add(new TransactionPagination.Transaction("a", "alice", "BUY", 1, 1));
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue(threw, "duplicate id should throw IllegalArgumentException");
    }

    // ===== Part 2: cursor pagination with explicit Filter ================

    static void testPart2() {
        TransactionPagination.StorePart2 store = new TransactionPagination.StorePart2();
        store.add(new TransactionPagination.Transaction("t1", "alice", "BUY",   100, 10));
        store.add(new TransactionPagination.Transaction("t2", "alice", "SELL",  500, 20));
        store.add(new TransactionPagination.Transaction("t3", "bob",   "BUY",   200, 30));
        store.add(new TransactionPagination.Transaction("t4", "alice", "BUY",  1000, 40));
        store.add(new TransactionPagination.Transaction("t5", "bob",   "SELL", 5000, 50));

        TransactionPagination.Filter alice =
                new TransactionPagination.Filter("alice", null, null, null);
        TransactionPagination.Filter buy =
                new TransactionPagination.Filter(null, "BUY", null, null);
        TransactionPagination.Filter empty = TransactionPagination.Filter.empty();

        // filter by userId only, with cursor paging
        TransactionPagination.Page<TransactionPagination.Transaction> alicePage1 =
                store.page(alice, null, 2);
        assertEq(List.of("t4", "t2"), ids(alicePage1.items()), "alice page 1");
        assertTrue(alicePage1.nextCursor() != null, "alice page 1 has nextCursor");

        TransactionPagination.Page<TransactionPagination.Transaction> alicePage2 =
                store.page(alice, alicePage1.nextCursor(), 2);
        assertEq(List.of("t1"), ids(alicePage2.items()), "alice page 2");
        assertEq(null, alicePage2.nextCursor(), "alice page 2 nextCursor == null");

        // filter by type only
        assertEq(List.of("t4", "t3", "t1"),
                ids(store.page(buy, null, 10).items()),
                "filter by type=BUY");

        // filter by amount range
        assertEq(List.of("t2", "t3"),
                ids(store.page(new TransactionPagination.Filter(null, null, 200L, 500L),
                        null, 10).items()),
                "filter amount ∈ [200, 500]");

        // combined filter
        assertEq(List.of("t4", "t1"),
                ids(store.page(new TransactionPagination.Filter("alice", "BUY", null, null),
                        null, 10).items()),
                "userId=alice AND type=BUY");

        // empty filter = all
        assertEq(List.of("t5", "t4", "t3", "t2", "t1"),
                ids(store.page(empty, null, 10).items()),
                "empty filter returns all");
    }

    // ===== Part 3: bidirectional cursor pagination =======================

    static void testPart3() {
        TransactionPagination.StorePart3 store = new TransactionPagination.StorePart3();
        store.add(new TransactionPagination.Transaction("t1", "u", "BUY", 1, 50));
        store.add(new TransactionPagination.Transaction("t2", "u", "BUY", 1, 40));
        store.add(new TransactionPagination.Transaction("t3", "u", "BUY", 1, 30));
        store.add(new TransactionPagination.Transaction("t4", "u", "BUY", 1, 20));
        store.add(new TransactionPagination.Transaction("t5", "u", "BUY", 1, 10));

        TransactionPagination.Filter empty = TransactionPagination.Filter.empty();

        // page through 5 results with limit=2 → 3 pages
        TransactionPagination.Page<TransactionPagination.Transaction> p1 =
                store.page(empty, null, 2, TransactionPagination.Direction.FORWARD);
        assertEq(List.of("t1", "t2"), ids(p1.items()), "page 1");
        assertTrue(p1.nextCursor() != null, "page 1 has nextCursor");

        TransactionPagination.Page<TransactionPagination.Transaction> p2 =
                store.page(empty, p1.nextCursor(), 2, TransactionPagination.Direction.FORWARD);
        assertEq(List.of("t3", "t4"), ids(p2.items()), "page 2");
        assertTrue(p2.nextCursor() != null, "page 2 has nextCursor");

        TransactionPagination.Page<TransactionPagination.Transaction> p3 =
                store.page(empty, p2.nextCursor(), 2, TransactionPagination.Direction.FORWARD);
        assertEq(List.of("t5"), ids(p3.items()), "page 3 last");
        assertEq(null, p3.nextCursor(), "page 3 nextCursor == null");

        // cursor stability: insert older record between page calls — cursor still valid
        TransactionPagination.StorePart3 store2 = new TransactionPagination.StorePart3();
        store2.add(new TransactionPagination.Transaction("a", "u", "BUY", 1, 30));
        store2.add(new TransactionPagination.Transaction("b", "u", "BUY", 1, 20));
        store2.add(new TransactionPagination.Transaction("c", "u", "BUY", 1, 10));
        TransactionPagination.Page<TransactionPagination.Transaction> q1 =
                store2.page(empty, null, 1, TransactionPagination.Direction.FORWARD);
        assertEq(List.of("a"), ids(q1.items()), "stability page 1");
        // insert something older — should not shift already-issued cursor
        store2.add(new TransactionPagination.Transaction("d", "u", "BUY", 1, 5));
        TransactionPagination.Page<TransactionPagination.Transaction> q2 =
                store2.page(empty, q1.nextCursor(), 10, TransactionPagination.Direction.FORWARD);
        assertEq(List.of("b", "c", "d"), ids(q2.items()),
                "cursor stable across appends; new older record appears in next page");

        TransactionPagination.Page<TransactionPagination.Transaction> back =
                store.page(empty, p1.nextCursor(), 2, TransactionPagination.Direction.BACKWARD);
        assertEq(List.of("t1"), ids(back.items()),
                "backward from C1 returns items strictly newer than cursor");
    }

    // ===== Part 4: thread-safe bidirectional traversal ===================

    static void testPart4() {
        TransactionPagination.StorePart4 store = new TransactionPagination.StorePart4();
        store.add(new TransactionPagination.Transaction("t1", "u", "BUY", 1, 50));
        store.add(new TransactionPagination.Transaction("t2", "u", "BUY", 1, 40));
        store.add(new TransactionPagination.Transaction("t3", "u", "BUY", 1, 30));
        store.add(new TransactionPagination.Transaction("t4", "u", "BUY", 1, 20));
        store.add(new TransactionPagination.Transaction("t5", "u", "BUY", 1, 10));

        TransactionPagination.Filter empty = TransactionPagination.Filter.empty();

        // forward 第一页
        TransactionPagination.Page<TransactionPagination.Transaction> p1 =
                store.page(empty, null, 2, TransactionPagination.Direction.FORWARD);
        assertEq(List.of("t1", "t2"), ids(p1.items()), "forward page 1");

        // forward 第二页
        TransactionPagination.Page<TransactionPagination.Transaction> p2 =
                store.page(empty, p1.nextCursor(), 2, TransactionPagination.Direction.FORWARD);
        assertEq(List.of("t3", "t4"), ids(p2.items()), "forward page 2");

        // backward from page-1's nextCursor (= t2) → items strictly newer than cursor.
        // cursor 指 "上一页最后一条", 所以 backward 不应该重新包含它本身.
        TransactionPagination.Page<TransactionPagination.Transaction> back =
                store.page(empty, p1.nextCursor(), 2, TransactionPagination.Direction.BACKWARD);
        assertEq(List.of("t1"), ids(back.items()),
                "backward from C1 returns items strictly newer than cursor");

        // backward 取的内容仍按 DESC 呈现
        TransactionPagination.StorePart4 s2 = new TransactionPagination.StorePart4();
        s2.add(new TransactionPagination.Transaction("a", "u", "BUY", 1, 30));
        s2.add(new TransactionPagination.Transaction("b", "u", "BUY", 1, 20));
        s2.add(new TransactionPagination.Transaction("c", "u", "BUY", 1, 10));
        TransactionPagination.Page<TransactionPagination.Transaction> fA =
                s2.page(empty, null, 2, TransactionPagination.Direction.FORWARD); // [a, b]
        TransactionPagination.Page<TransactionPagination.Transaction> fB =
                s2.page(empty, fA.nextCursor(), 2, TransactionPagination.Direction.FORWARD); // [c]
        // 从 fA.nextCursor() (= b) 反向: 严格更新 → [a]
        TransactionPagination.Page<TransactionPagination.Transaction> bk =
                s2.page(empty, fA.nextCursor(), 5, TransactionPagination.Direction.BACKWARD);
        assertEq(List.of("a"), ids(bk.items()), "backward order is DESC, items strictly newer");
        assertEq(List.of("c"), ids(fB.items()), "forward last page");
    }

    // ===== Part 5: indexed filter execution =============================

    static void testPart5() {
        TransactionPagination.StorePart5 store = new TransactionPagination.StorePart5();

        store.add(new TransactionPagination.Transaction("t1", "alice", "BUY",  100, 10));
        store.add(new TransactionPagination.Transaction("t2", "alice", "SELL", 500, 20));
        store.add(new TransactionPagination.Transaction("t3", "bob",   "BUY",  200, 30));
        store.add(new TransactionPagination.Transaction("t4", "alice", "BUY", 1000, 40));

        TransactionPagination.Filter alice =
                new TransactionPagination.Filter("alice", null, null, null);
        TransactionPagination.Page<TransactionPagination.Transaction> p1 =
                store.page(alice, null, 10, TransactionPagination.Direction.FORWARD);
        assertEq(List.of("t4", "t2", "t1"), ids(p1.items()),
                "userId filter should use the same externally visible ordering");

        TransactionPagination.Filter aliceBuy =
                new TransactionPagination.Filter("alice", "BUY", null, null);
        TransactionPagination.Page<TransactionPagination.Transaction> p2 =
                store.page(aliceBuy, null, 10, TransactionPagination.Direction.FORWARD);
        assertEq(List.of("t4", "t1"), ids(p2.items()),
                "userId index candidates still need remaining predicates");
    }

    // ===== Part 6: 二级索引 + sort by amount ============================

    static void testPart6() {
        TransactionPagination.StorePart6 store = new TransactionPagination.StorePart6();
        store.add(new TransactionPagination.Transaction("t1", "u", "BUY",  500, 10));
        store.add(new TransactionPagination.Transaction("t2", "u", "BUY", 1000, 20));
        store.add(new TransactionPagination.Transaction("t3", "u", "BUY",  200, 30));
        store.add(new TransactionPagination.Transaction("t4", "u", "BUY",  800, 40));

        TransactionPagination.Filter empty = TransactionPagination.Filter.empty();

        // sort by AMOUNT_DESC: 1000 > 800 > 500 > 200
        TransactionPagination.Page<TransactionPagination.Transaction> pd =
                store.page(empty, TransactionPagination.SortKey.AMOUNT_DESC, null, 10);
        assertEq(List.of("t2", "t4", "t1", "t3"), ids(pd.items()), "AMOUNT_DESC ordering");

        // sort by AMOUNT_ASC
        TransactionPagination.Page<TransactionPagination.Transaction> pa =
                store.page(empty, TransactionPagination.SortKey.AMOUNT_ASC, null, 10);
        assertEq(List.of("t3", "t1", "t4", "t2"), ids(pa.items()), "AMOUNT_ASC ordering");

        // keyset cursor on AMOUNT_DESC
        TransactionPagination.Page<TransactionPagination.Transaction> pd1 =
                store.page(empty, TransactionPagination.SortKey.AMOUNT_DESC, null, 2);
        assertEq(List.of("t2", "t4"), ids(pd1.items()), "AMOUNT_DESC page 1");
        TransactionPagination.Page<TransactionPagination.Transaction> pd2 =
                store.page(empty, TransactionPagination.SortKey.AMOUNT_DESC, pd1.nextCursor(), 2);
        assertEq(List.of("t1", "t3"), ids(pd2.items()), "AMOUNT_DESC page 2");
    }

    // ===== Part 7: 跨分片分页 ============================================

    static void testPart7() {
        TransactionPagination.ShardedStorePart7 store =
                new TransactionPagination.ShardedStorePart7(4);

        // 不同 userId 应该会被 hash 到不同 shard
        store.add(new TransactionPagination.Transaction("t1", "alice",   "BUY", 1, 50));
        store.add(new TransactionPagination.Transaction("t2", "bob",     "BUY", 1, 40));
        store.add(new TransactionPagination.Transaction("t3", "carol",   "BUY", 1, 30));
        store.add(new TransactionPagination.Transaction("t4", "dave",    "BUY", 1, 20));
        store.add(new TransactionPagination.Transaction("t5", "eve",     "BUY", 1, 10));

        TransactionPagination.Filter empty = TransactionPagination.Filter.empty();

        // 跨 shard merge: top-3 全局最新
        TransactionPagination.Page<TransactionPagination.Transaction> p1 =
                store.page(empty, null, 3);
        assertEq(List.of("t1", "t2", "t3"), ids(p1.items()), "sharded page 1 top-3");

        // 用 cursor 继续翻
        TransactionPagination.Page<TransactionPagination.Transaction> p2 =
                store.page(empty, p1.nextCursor(), 10);
        assertEq(List.of("t4", "t5"), ids(p2.items()), "sharded page 2 remaining");
    }

    // ===== Part 8: cache + stateful vs stateless cursor ================

    static void testPart8() {
        TransactionPagination.CachedStorePart8 store = new TransactionPagination.CachedStorePart8();
        store.add(new TransactionPagination.Transaction("t1", "u", "BUY", 1, 50));
        store.add(new TransactionPagination.Transaction("t2", "u", "BUY", 1, 40));
        store.add(new TransactionPagination.Transaction("t3", "u", "BUY", 1, 30));

        TransactionPagination.Filter empty = TransactionPagination.Filter.empty();
        TransactionPagination.Page<TransactionPagination.Transaction> p1 = store.page(empty, null, 2);
        assertEq(List.of("t1", "t2"), ids(p1.items()), "cached page 1");

        // 同样请求再来一次: 应该命中 cache, 行为对外不可观测但结果一致.
        TransactionPagination.Page<TransactionPagination.Transaction> p1Again = store.page(empty, null, 2);
        assertEq(List.of("t1", "t2"), ids(p1Again.items()), "cached page 1 hit");

        // 翻第二页 (cache miss typical)
        TransactionPagination.Page<TransactionPagination.Transaction> p2 = store.page(empty, p1.nextCursor(), 2);
        assertEq(List.of("t3"), ids(p2.items()), "cached page 2");
    }
}
