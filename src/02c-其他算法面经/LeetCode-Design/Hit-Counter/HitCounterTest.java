import java.util.*;

public class HitCounterTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", HitCounterTest::testPart1);
        tests.put("part2", HitCounterTest::testPart2);
        tests.put("part3", HitCounterTest::testPart3);
        tests.put("part4", HitCounterTest::testPart4);
        tests.put("part5", HitCounterTest::testPart5);
        tests.put("part6", HitCounterTest::testPart6);
        tests.put("part7", HitCounterTest::testPart7);
        tests.put("part8", HitCounterTest::testPart8);
        // ... 加新 Part 时在这里加一行

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

    // ===== Part 1 — basic hit / getHits =================================

    static void testPart1() {
        HitCounter.HitCounterPart1 c = new HitCounter.HitCounterPart1(300);
        c.hit(1);
        c.hit(2);
        c.hit(3);
        assertEq(3, c.getHits(4), "1,2,3 all inside (-296, 4]");

        c.hit(300);
        assertEq(4, c.getHits(300), "1,2,3,300 all inside (0, 300]");
        assertEq(3, c.getHits(301), "hit at 1 falls out of (1, 301]");

        // 整窗滑过去后清零: t=601 时 (301, 601] 内一个 hit 都没有
        assertEq(0, c.getHits(601), "whole window slid past — nothing left");
    }

    // ===== Part 2 — high hit rate (same behavior, bounded memory) =======

    static void testPart2() {
        HitCounter.HitCounterPart2 c = new HitCounter.HitCounterPart2(300);
        for (int i = 0; i < 1000; i++) c.hit(1);  // 同一秒灌 1000 次
        c.hit(2);
        assertEq(1001, c.getHits(2), "high-frequency same-second hits all counted");
        assertEq(1001, c.getHits(300), "still inside (0, 300]");
        assertEq(1, c.getHits(301), "second 1 (1000 hits) evicted; only second 2 remains");
        assertEq(0, c.getHits(303), "both seconds out of (3, 303]");
    }

    // ===== Part 3 — configurable window + arbitrary range ===============

    static void testPart3() {
        HitCounter.HitCounterPart3 c = new HitCounter.HitCounterPart3(300);
        c.hit(1);
        c.hit(2);
        c.hit(3);
        c.hit(300);
        assertEq(4, c.getHits(300), "single-arg window (0, 300]");
        assertEq(3, c.getHits(2, 300), "[2,300] inclusive excludes t'=1");
        assertEq(2, c.getHits(1, 2), "[1,2] inclusive");
        assertEq(1, c.getHits(300, 999), "[300,999] inclusive — only t'=300");
        assertEq(0, c.getHits(4, 299), "[4,299] inclusive — empty gap");

        c.hit(301);
        assertEq(1, c.getHits(1, 1), "arbitrary range must still find old t'=1 after slot collision");
        assertEq(1, c.getHits(301, 301), "new colliding second is also preserved");

        // 自定义窗口大小生效
        HitCounter.HitCounterPart3 c2 = new HitCounter.HitCounterPart3(10);
        c2.hit(1);
        c2.hit(5);
        assertEq(2, c2.getHits(10), "W=10: (0,10] has both");
        assertEq(1, c2.getHits(11), "W=10: (1,11] drops t'=1");
    }

    // ===== Part 4 — out-of-order / late timestamps ======================

    static void testPart4() {
        HitCounter.HitCounterPart4 c = new HitCounter.HitCounterPart4(300);
        c.hit(100);
        c.hit(50);
        c.hit(200);
        c.hit(150);  // arrives out of order
        assertEq(2, c.getHits(120), "(-180,120] holds 100,50");
        assertEq(3, c.getHits(199), "(-101,199] holds 100,50,150 (200 excluded)");
        assertEq(4, c.getHits(300), "(0,300] holds all four");

        c.hit(10);  // late backfill of an old hit
        assertEq(3, c.getHits(120), "late t'=10 now counted in (-180,120]");
        assertEq(4, c.getHits(310), "(10,310] excludes the left-boundary t'=10 and keeps 50,100,150,200");
    }

    // ===== Part 5 — concurrency (no lost updates) =======================

    static void testPart5() {
        final HitCounter.HitCounterPart5 c = new HitCounter.HitCounterPart5(300);
        final int threadCount = 8, perThread = 50_000;
        Thread[] ts = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < perThread; j++) c.hit(1);
            });
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) {
            try { t.join(); } catch (InterruptedException e) { throw new RuntimeException(e); }
        }
        assertEq(threadCount * perThread, c.getHits(1), "no lost updates under concurrent hits");
        assertEq(0, c.getHits(302), "window slid past second 1");
    }

    // ===== Part 6 — huge window / coarse buckets ========================

    static void testPart6() {
        // 1-hour window, 1-minute granularity. Data lands in minute-buckets
        // 0, 1, 3 (minute-2 is an empty gap). We only query at window edges
        // that fall in gaps / past all data, so the coarse-bucket answer is
        // unambiguous regardless of the candidate's bucket-edge convention
        // (the in-bucket boundary error is discussed in README, not tested).
        HitCounter.HitCounterPart6 c = new HitCounter.HitCounterPart6(3600, 60);
        c.hit(0);
        c.hit(60);
        for (int i = 0; i < 100; i++) c.hit(180);  // 100 hits in the t=180 minute (bucket 3)
        assertEq(102, c.getHits(240), "all 3 buckets fully inside (240-3600,240]");
        assertEq(100, c.getHits(3750), "edge=150 (empty gap): buckets 0,1 evicted, bucket 3 (×100) stays");
        assertEq(0, c.getHits(7000), "(3400,7000]: all data evicted");
    }

    // ===== Part 7 — sharded / distributed merge =========================

    static void testPart7() {
        HitCounter.ShardedHitCounter c = new HitCounter.ShardedHitCounter(4, 300);
        c.hit("user-a", 1);
        c.hit("user-b", 1);
        c.hit("user-a", 2);
        c.hit("user-c", 3);
        assertEq(4, c.getHits(3), "merge counts across all shards: t'=1,1,2,3");
        assertEq(2, c.getHits(301), "(1,301] drops the two t'=1 hits, keeps t'=2,3");
        assertEq(1, c.getHits(302), "(2,302] keeps only t'=3");
    }

    // ===== Part 8 — sliding-window rate limiter =========================

    static void testPart8() {
        HitCounter.RateLimiterPart8 rl = new HitCounter.RateLimiterPart8(10, 3);  // 3 per 10s
        assertEq(true, rl.allow(1), "1st within limit");
        assertEq(true, rl.allow(1), "2nd within limit");
        assertEq(true, rl.allow(1), "3rd hits the limit but still allowed");
        assertEq(false, rl.allow(1), "4th in (−9,1] exceeds limit");
        assertEq(false, rl.allow(5), "(-5,5] still holds the 3 accepted hits");
        assertEq(true, rl.allow(11), "(1,11] — the 3 hits at t=1 fell out, allow again");
        assertEq(true, rl.allow(11), "2nd in new window");
    }
}
