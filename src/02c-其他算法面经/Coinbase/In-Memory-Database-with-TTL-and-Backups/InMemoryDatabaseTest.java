import java.util.*;

public class InMemoryDatabaseTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", InMemoryDatabaseTest::testPart1);
        tests.put("part2", InMemoryDatabaseTest::testPart2);
        tests.put("part3", InMemoryDatabaseTest::testPart3);
        tests.put("part4", InMemoryDatabaseTest::testPart4);

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

    // ===== Part 1 =======================================================

    static void testPart1() {
        InMemoryDatabase db = new InMemoryDatabase();
        db.putPart1("a", "x", 1);
        assertEq("x", db.getPart1("a", 2), "basic put/get");
        db.putPart1("a", "y", 3);
        assertEq("y", db.getPart1("a", 4), "overwrite");
        assertEq(null, db.getPart1("b", 5), "missing key returns null");
    }

    // ===== Part 2 =======================================================

    static void testPart2() {
        InMemoryDatabase db = new InMemoryDatabase();
        db.putPart2("apple", "1", 1);
        db.putPart2("app", "2", 2);
        db.putPart2("banana", "3", 3);
        assertEq("app(2), apple(1)", db.scanPart2("app", 4), "prefix 'app' sorted");
        assertEq("app(2), apple(1), banana(3)", db.scanPart2("", 5), "empty prefix returns all sorted");
        assertEq("", db.scanPart2("z", 6), "no match returns empty string");
    }

    // ===== Part 3 =======================================================

    static void testPart3() {
        InMemoryDatabase db = new InMemoryDatabase();
        db.putPart3("a", "x", 1, 5);  // valid [1, 6)
        assertEq("x", db.getPart3("a", 1), "ttl start boundary");
        assertEq("x", db.getPart3("a", 5), "inside ttl");
        assertEq(null, db.getPart3("a", 6), "ttl end boundary is half-open");

        db.putPart3("b", "y", 10, 0);
        assertEq(null, db.getPart3("b", 10), "ttl=0 immediately expired");

        db.putPart3("c", "z", 1);  // 无 ttl 重载 — 永久
        assertEq("z", db.getPart3("c", 1_000_000), "no ttl = forever");

        db.putPart3("d", "old", 1, 100);  // [1, 101)
        db.putPart3("d", "new", 2, 1);    // [2, 3) — 新 ttl 替换旧
        assertEq("new", db.getPart3("d", 2), "overwrite: new value visible");
        assertEq(null, db.getPart3("d", 3), "overwrite: new ttl applies, not old");

        InMemoryDatabase db2 = new InMemoryDatabase();
        db2.putPart3("p", "1", 1, 2);   // [1, 3) — expired by t=5
        db2.putPart3("q", "2", 1);      // forever
        db2.putPart3("r", "3", 1, 10);  // [1, 11)
        assertEq("q(2), r(3)", db2.scanPart3("", 5), "scan filters expired entries");
    }

    // ===== Part 4 =======================================================

    static void testPart4() {
        InMemoryDatabase db = new InMemoryDatabase();
        db.putPart4("temp", "v", 1, 10);          // original expire = 11
        int bid = db.backupPart4(5);              // remaining = 6
        assertEq(1, bid, "first backup_id is 1");
        db.putPart4("temp", "overwritten", 100);  // mutate after backup
        db.restorePart4(bid, 20);                 // new expire = 20 + 6 = 26
        assertEq("v", db.getPart4("temp", 25), "restored value with remaining ttl");
        assertEq(null, db.getPart4("temp", 26), "expires at restore_time + remaining_ttl (half-open)");

        InMemoryDatabase db2 = new InMemoryDatabase();
        db2.putPart4("p", "forever", 1);
        db2.backupPart4(5);
        db2.restorePart4(1, 100);
        assertEq("forever", db2.getPart4("p", 1_000_000_000), "no-ttl entry survives restore");

        InMemoryDatabase db3 = new InMemoryDatabase();
        db3.putPart4("a", "1", 1);
        assertEq(1, db3.backupPart4(2), "first backup_id");
        assertEq(2, db3.backupPart4(3), "second backup_id increments");

        InMemoryDatabase db4 = new InMemoryDatabase();
        db4.putPart4("short", "gone", 1, 1);  // [1, 2)
        db4.putPart4("keep", "here", 1);      // forever
        int bid4 = db4.backupPart4(10);
        db4.restorePart4(bid4, 100);
        assertEq(null, db4.getPart4("short", 100), "expired entry not in backup");
        assertEq("here", db4.getPart4("keep", 100), "live entry survives backup/restore");
    }
}
