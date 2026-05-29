import java.util.*;

public class KafkaEventProcessingTest {

    // default package 里没法 import 嵌套类型, 直接用全名 KafkaEventProcessing.Xxx.

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", KafkaEventProcessingTest::testPart1);
        tests.put("part2", KafkaEventProcessingTest::testPart2);
        tests.put("part3", KafkaEventProcessingTest::testPart3);
        tests.put("part4", KafkaEventProcessingTest::testPart4);
        tests.put("part5", KafkaEventProcessingTest::testPart5);
        tests.put("part6", KafkaEventProcessingTest::testPart6);
        tests.put("part7", KafkaEventProcessingTest::testPart7);
        tests.put("part8", KafkaEventProcessingTest::testPart8);

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

    static List<Long> seqs(List<KafkaEventProcessing.Event> events) {
        List<Long> out = new ArrayList<>();
        for (KafkaEventProcessing.Event e : events) out.add(e.sequence());
        return out;
    }

    static KafkaEventProcessing.Event evt(String mid, String user, long seq, long t) {
        return new KafkaEventProcessing.Event(mid, user, seq, t, "p");
    }

    // ===== Part 1: basic consume + count ================================

    static void testPart1() {
        KafkaEventProcessing.ProcessorPart1 p = new KafkaEventProcessing.ProcessorPart1();
        p.consume(evt("m1", "alice", 1, 100));
        p.consume(evt("m2", "bob",   1, 200));
        p.consume(evt("m3", "alice", 2, 300));

        assertEq(3, p.totalConsumed(), "totalConsumed = 3");
        assertEq(2, p.consumedByUser("alice"), "alice count = 2");
        assertEq(1, p.consumedByUser("bob"), "bob count = 1");
        assertEq(0, p.consumedByUser("charlie"), "unknown user count = 0");
    }

    // ===== Part 2: idempotent dedupe ====================================

    static void testPart2() {
        KafkaEventProcessing.ProcessorPart2 p = new KafkaEventProcessing.ProcessorPart2();
        p.consume(evt("m1", "alice", 1, 100));
        p.consume(evt("m1", "alice", 1, 100));  // 完全相同, 忽略
        p.consume(evt("m1", "bob",   9, 999));  // mid 相同, 仍忽略
        p.consume(evt("m2", "alice", 2, 200));

        assertEq(2, p.totalConsumed(), "distinct messageIds = 2");
        assertEq(2, p.consumedByUser("alice"), "alice distinct = 2");
        assertEq(0, p.consumedByUser("bob"), "bob's m1 dropped (mid already seen)");
    }

    // ===== Part 3: ordered by sequence ==================================

    static void testPart3() {
        KafkaEventProcessing.ProcessorPart3 p = new KafkaEventProcessing.ProcessorPart3("alice", 1);

        assertEq(List.of(), seqs(p.consume(evt("m2", "alice", 2, 0))), "ahead seq=2 buffered");
        assertEq(List.of(), seqs(p.consume(evt("m3", "alice", 3, 0))), "ahead seq=3 buffered");
        assertEq(List.of(1L, 2L, 3L),
                seqs(p.consume(evt("m1", "alice", 1, 0))),
                "gap-fill drains [1, 2, 3]");

        assertEq(List.of(), seqs(p.consume(evt("m5", "alice", 5, 0))), "ahead again");
        assertEq(List.of(4L, 5L),
                seqs(p.consume(evt("m4", "alice", 4, 0))),
                "gap-fill drains [4, 5]");

        // 重复 seq 忽略
        assertEq(List.of(),
                seqs(p.consume(evt("mDup", "alice", 4, 0))),
                "duplicate seq=4 ignored");

        // 不属于本 user 忽略
        assertEq(List.of(),
                seqs(p.consume(evt("mx", "bob", 6, 0))),
                "wrong userId ignored");

        // 继续 normal flow
        assertEq(List.of(6L),
                seqs(p.consume(evt("m6", "alice", 6, 0))),
                "next in-order emits");
    }

    // ===== Part 4: time-window aggregation ==============================

    static void testPart4() {
        KafkaEventProcessing.ProcessorPart4 p = new KafkaEventProcessing.ProcessorPart4(10);
        p.consume(evt("m1", "a", 1, 0));
        p.consume(evt("m2", "a", 2, 5));
        p.consume(evt("m2", "a", 2, 5));   // dup
        p.consume(evt("m3", "b", 1, 11));
        p.consume(evt("m4", "b", 2, 15));

        // window [5, 15]: m2(t=5), m3(t=11), m4(t=15) = 3
        assertEq(3, p.countInWindow(15), "count in window [5,15]");
        assertEq(Map.of("a", 1, "b", 2), p.countByUserInWindow(15), "byUser at now=15");

        // window [10, 20]: m3, m4 = 2
        assertEq(2, p.countInWindow(20), "count in window [10,20]");
        assertEq(Map.of("b", 2), p.countByUserInWindow(20), "byUser at now=20 (no a)");

        // window 完全在所有 event 之后 → 0
        assertEq(0, p.countInWindow(1000), "future window has no events");
        assertEq(Map.of(), p.countByUserInWindow(1000), "future window empty map");

        // 端点闭区间: now=0 → [-10, 0] 应包含 t=0 的 m1
        assertEq(1, p.countInWindow(0), "left endpoint inclusive at t=0");
    }

    // ===== Part 5: concurrent consume + partition 分配 ==================

    static void testPart5() {
        KafkaEventProcessing.ProcessorPart5 p = new KafkaEventProcessing.ProcessorPart5(4);
        // 多线程入站. 同 user 的 event 仍由同一 worker 处理 (路由稳定).
        int N = 1000;
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < 8; t++) {
            final int tid = t;
            threads.add(new Thread(() -> {
                for (int i = 0; i < N; i++) {
                    String user = "u" + (i % 16);
                    String mid = "t" + tid + "-i" + i;
                    p.consume(evt(mid, user, i, i));
                }
            }));
        }
        for (Thread th : threads) th.start();
        for (Thread th : threads) {
            try { th.join(); } catch (InterruptedException e) { throw new RuntimeException(e); }
        }
        p.shutdown();

        assertEq(8 * N, p.totalConsumed(), "concurrent total across 8 threads");
        int sum = 0;
        for (int u = 0; u < 16; u++) sum += p.consumedByUser("u" + u);
        assertEq(8 * N, sum, "sum over per-user equals total");
    }

    // ===== Part 6: retry + DLQ =========================================

    static void testPart6() {
        // Handler 对 messageId 含 "bad" 的总是抛, 其他成功.
        // 对 "flaky" 的前 1 次抛, 第 2 次成功.
        Map<String, Integer> attempts = new HashMap<>();
        KafkaEventProcessing.EventHandlerPart6 handler = e -> {
            int n = attempts.getOrDefault(e.messageId(), 0) + 1;
            attempts.put(e.messageId(), n);
            if (e.messageId().contains("bad")) throw new RuntimeException("poison");
            if (e.messageId().contains("flaky") && n == 1) throw new RuntimeException("transient");
        };
        KafkaEventProcessing.ProcessorPart6 p =
                new KafkaEventProcessing.ProcessorPart6(handler, 3);

        p.consume(evt("ok1", "a", 1, 0));
        p.consume(evt("flaky1", "a", 2, 0));   // 1 次重试后成功
        p.consume(evt("bad1", "a", 3, 0));     // 重试到顶进 DLQ
        p.consume(evt("ok2", "b", 1, 0));

        assertEq(3, p.successCount(), "ok1 + flaky1 + ok2 succeeded");
        assertEq(1, p.deadLetterQueue().size(), "bad1 in DLQ");
        assertEq("bad1", p.deadLetterQueue().get(0).messageId(), "DLQ contains bad1");
        assertTrue(p.totalRetries() >= 1, "at least one retry happened");
    }

    // ===== Part 7: consumer lag monitoring =============================

    static void testPart7() {
        KafkaEventProcessing.ProcessorPart7 p = new KafkaEventProcessing.ProcessorPart7();
        p.recordProducerHighWatermark("a", 100);
        p.recordProducerHighWatermark("b", 50);

        p.consume(evt("m1", "a", 10, 0));
        p.consume(evt("m2", "a", 20, 0));
        p.consume(evt("m3", "b", 5, 0));

        // a: producer 到 100, consumed 到 20 → lag 80
        // b: producer 到 50,  consumed 到 5  → lag 45
        assertEq(80L, p.lag("a"), "lag for a");
        assertEq(45L, p.lag("b"), "lag for b");
        assertEq(125L, p.totalLag(), "total lag");
        assertEq("a", p.maxLagUser(), "a has higher lag");
    }

    // ===== Part 8: offset checkpoint + restore =========================

    static void testPart8() {
        // 一个简单的内存 OffsetStore.
        Map<String, Long> backing = new HashMap<>();
        KafkaEventProcessing.OffsetStorePart8 store = new KafkaEventProcessing.OffsetStorePart8() {
            public void save(Map<String, Long> snapshot) {
                backing.clear();
                backing.putAll(snapshot);
            }
            public Map<String, Long> load() {
                return new HashMap<>(backing);
            }
        };

        KafkaEventProcessing.ProcessorPart8 p1 = new KafkaEventProcessing.ProcessorPart8(store);
        p1.consume(evt("m1", "a", 1, 0));
        p1.consume(evt("m2", "a", 2, 0));
        p1.consume(evt("m3", "b", 7, 0));
        p1.checkpoint();

        // "重启": 重新构造, 应从 store 恢复.
        KafkaEventProcessing.ProcessorPart8 p2 = new KafkaEventProcessing.ProcessorPart8(store);
        assertEq(2L, p2.lastSequenceFor("a"), "a recovered to 2");
        assertEq(7L, p2.lastSequenceFor("b"), "b recovered to 7");
        assertEq(0L, p2.lastSequenceFor("never"), "unknown user defaults to 0");
    }
}
