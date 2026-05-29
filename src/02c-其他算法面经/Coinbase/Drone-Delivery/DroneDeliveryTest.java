import java.util.*;

public class DroneDeliveryTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", DroneDeliveryTest::testPart1);
        tests.put("part2", DroneDeliveryTest::testPart2);
        tests.put("part3", DroneDeliveryTest::testPart3);
        tests.put("part4", DroneDeliveryTest::testPart4);
        tests.put("part5", DroneDeliveryTest::testPart5);
        tests.put("part6", DroneDeliveryTest::testPart6);
        tests.put("part7", DroneDeliveryTest::testPart7);
        tests.put("part8", DroneDeliveryTest::testPart8);

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

    // ===== Part 1: Single Drone, Fixed Range ============================

    static void testPart1() {
        // canonical: walk 0→5 (5), fly 5→15, walk 15→20 (5). Total 10.
        assertEq(10L, DroneDelivery.DronePart1.walkDistance(20, new int[]{5, 15}, 10),
                "basic target=20 stations=[5,15] range=10");

        // no stations → walk full target
        assertEq(20L, DroneDelivery.DronePart1.walkDistance(20, new int[]{}, 10),
                "no stations → walk full target");

        // target reachable from last station via fly
        assertEq(5L, DroneDelivery.DronePart1.walkDistance(10, new int[]{5}, 10),
                "single station, fly covers final leg");

        // gap larger than range → walk diff
        // target=30, stations=[5,15], range=5
        // walk 0→5 (5), fly 5→10, walk 10→15 (5), fly 15→20, walk 20→30 (10). Total 20.
        assertEq(20L, DroneDelivery.DronePart1.walkDistance(30, new int[]{5, 15}, 5),
                "gaps larger than range");

        // station at 0... but spec says origin is not a station; we'll skip that edge.
        // target=0 trivial
        assertEq(0L, DroneDelivery.DronePart1.walkDistance(0, new int[]{}, 10),
                "target=0 no walk needed");

        // first station beyond range still requires walking to it
        // target=100, stations=[50], range=1000 → walk 0→50 (50), fly to 100. Total 50.
        assertEq(50L, DroneDelivery.DronePart1.walkDistance(100, new int[]{50}, 1000),
                "huge range still requires walk to first station");
    }

    // ===== Part 2: Multiple Packages =====================================

    static void testPart2() {
        // 10: walk 0→5 (5), fly 5→10. = 5
        // 20: walk 0→5 (5), fly 5→15, walk 15→20 (5). = 10
        // Total = 15
        assertEq(15L, DroneDelivery.DronePart2.totalWalkDistance(
                        new int[]{10, 20}, new int[]{5, 15}, 10),
                "two independent deliveries");

        // empty targets → 0
        assertEq(0L, DroneDelivery.DronePart2.totalWalkDistance(
                        new int[]{}, new int[]{5, 15}, 10),
                "no targets");

        // single target same as Part 1
        assertEq(10L, DroneDelivery.DronePart2.totalWalkDistance(
                        new int[]{20}, new int[]{5, 15}, 10),
                "single target == Part 1");
    }

    // ===== Part 3: Per-Drone Range ======================================

    static void testPart3() {
        // No global drone, one station with enough range.
        // target=10, stations=[5], ranges=[5], globalStartRange=0
        // walk 0→5 (5), fly 5→10. = 5
        assertEq(5L, DroneDelivery.DronePart3.walkDistance(
                        10, new int[]{5}, new int[]{5}, 0),
                "single station, range covers final leg");

        // global start range covers everything
        // target=10, stations=[], globalStartRange=10
        // fly 0→10. = 0
        assertEq(0L, DroneDelivery.DronePart3.walkDistance(
                        10, new int[]{}, new int[]{}, 10),
                "global drone covers entire trip");

        // no drones anywhere
        assertEq(15L, DroneDelivery.DronePart3.walkDistance(
                        15, new int[]{}, new int[]{}, 0),
                "no drones at all → walk full");

        // alternating ranges, only the long one helps
        // target=30, stations=[5,10,20], ranges=[20,1,2], globalStartRange=0
        // walk 0→5 (5), fly 5→25 (range=20), walk 25→30 (5). = 10
        // (skipping the bad-range stations is allowed)
        assertEq(10L, DroneDelivery.DronePart3.walkDistance(
                        30, new int[]{5, 10, 20}, new int[]{20, 1, 2}, 0),
                "use only the high-range station");
    }

    // ===== Part 4: 0/1 Knapsack ==========================================

    static void testPart4() {
        // canonical 22
        assertEq(22L, DroneDelivery.PackingPart4.maxValue(
                        new int[]{1, 2, 3}, new int[]{6, 10, 12}, 5),
                "classic 0/1 knapsack capacity=5");

        // capacity 0 → 0
        assertEq(0L, DroneDelivery.PackingPart4.maxValue(
                        new int[]{1, 2, 3}, new int[]{6, 10, 12}, 0),
                "capacity 0");

        // empty items
        assertEq(0L, DroneDelivery.PackingPart4.maxValue(
                        new int[]{}, new int[]{}, 10),
                "no items");

        // single item fits
        assertEq(10L, DroneDelivery.PackingPart4.maxValue(
                        new int[]{5}, new int[]{10}, 5),
                "single item exact fit");

        // single item too heavy
        assertEq(0L, DroneDelivery.PackingPart4.maxValue(
                        new int[]{6}, new int[]{10}, 5),
                "single item too heavy");

        // 0/1 not unbounded — cannot pick same item twice
        // weights=[2], values=[5], capacity=4 → 5 (NOT 10)
        assertEq(5L, DroneDelivery.PackingPart4.maxValue(
                        new int[]{2}, new int[]{5}, 4),
                "0/1: cannot duplicate item");
    }

    // ===== Part 5: Concurrent Dispatch ===================================

    static void testPart5() {
        // 仅触发 stub → SKIPPED, 等 user 实现.
        DroneDelivery.DispatcherPart5 d =
                new DroneDelivery.DispatcherPart5(new int[]{5, 15}, 10);
        d.submitOrder(1L, 20);
        try {
            Long walked = d.assignNext(42L);
            assertTrue(walked != null && walked >= 0, "dispatcher returns non-negative walk distance");
        } catch (InterruptedException ie) {
            throw new AssertionError("interrupted: " + ie);
        }
        d.shutdown();
    }

    // ===== Part 6: Online Re-Planning ====================================

    static void testPart6() {
        DroneDelivery.PlannerPart6 p =
                new DroneDelivery.PlannerPart6(2, new int[]{5, 15}, 10);
        p.addOrderAt(0L, 1L, 20);
        p.tick(5L);
        p.addOrderAt(5L, 2L, 10);
        p.tick(20L);
        double avg = p.averageLatency(20L);
        assertTrue(avg >= 0, "average latency non-negative");
    }

    // ===== Part 7: Failover & Recovery ===================================

    static void testPart7() {
        DroneDelivery.FailoverDispatcherPart7 d =
                new DroneDelivery.FailoverDispatcherPart7(new int[]{5, 15}, 10, 5000L);
        d.heartbeatPart7(1L, 0L);
        byte[] snap = d.checkpointPart7();
        assertTrue(snap != null, "snapshot non-null");
        d.recoverPart7(snap);
        d.markFailedPart7(1L);
    }

    // ===== Part 8: Geo Sharding (设计讨论, 这里只占个 stub) ==============

    static void testPart8() {
        // Part 8 主要是设计讨论. 这里抛一个 UOE 标记 SKIPPED, 等 user 决定要不要写代码.
        throw new UnsupportedOperationException("Part 8 — 偏设计讨论, README 里讨论即可");
    }
}
