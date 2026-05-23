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

    // ===== Part 1 ======================================================

    static void testPart1() {
        InMemoryDatabase db = new InMemoryDatabase();
        db.put("a", "x", 1);
        assertEq("x", db.get("a", 2), "basic put/get");
        db.put("a", "y", 3);
        assertEq("y", db.get("a", 4), "overwrite");
        assertEq(null, db.get("b", 5), "missing key returns null");
    }

    // ===== Part 2 ======================================================

    static void testPart2() {
        InMemoryDatabase db = new InMemoryDatabase();
        db.put("apple", "1", 1);
        db.put("app", "2", 2);
        db.put("banana", "3", 3);
        assertEq("app(2), apple(1)", db.scan("app", 4), "prefix 'app' sorted");
        assertEq("app(2), apple(1), banana(3)", db.scan("", 5), "empty prefix returns all sorted");
        assertEq("", db.scan("z", 6), "no match returns empty string");
    }

    // ===== Part 3 ======================================================

    static void testPart3() {
        InMemoryDatabase db = new InMemoryDatabase();
        db.put("a", "x", 1, 5);  // valid [1, 6)
        assertEq("x", db.get("a", 1), "ttl start boundary");
        assertEq("x", db.get("a", 5), "inside ttl");
        assertEq(null, db.get("a", 6), "ttl end boundary is half-open");

        db.put("b", "y", 10, 0);
        assertEq(null, db.get("b", 10), "ttl=0 immediately expired");

        db.put("c", "z", 1);  // no ttl
        assertEq("z", db.get("c", 1_000_000), "no ttl = forever");

        db.put("d", "old", 1, 100);  // [1, 101)
        db.put("d", "new", 2, 1);    // [2, 3) — new ttl replaces old
        assertEq("new", db.get("d", 2), "overwrite: new value visible");
        assertEq(null, db.get("d", 3), "overwrite: new ttl applies, not old");

        InMemoryDatabase db2 = new InMemoryDatabase();
        db2.put("p", "1", 1, 2);   // [1, 3)  — expired at t=5
        db2.put("q", "2", 1);      // forever
        db2.put("r", "3", 1, 10);  // [1, 11)
        assertEq("q(2), r(3)", db2.scan("", 5), "scan filters expired entries");
    }

    // ===== Part 4 ======================================================

    static void testPart4() {
        // restore preserves REMAINING ttl
        InMemoryDatabase db = new InMemoryDatabase();
        db.put("temp", "v", 1, 10);          // original expire = 11
        int bid = db.backup(5);              // remaining = 6
        assertEq(1, bid, "first backup_id is 1");
        db.put("temp", "overwritten", 100);  // mutate after backup
        db.restore(bid, 20);                 // new expire = 20 + 6 = 26
        assertEq("v", db.get("temp", 25), "restored value with remaining ttl");
        assertEq(null, db.get("temp", 26), "expires at restore_time + remaining_ttl (half-open)");

        // forever entry stays forever after restore
        InMemoryDatabase db2 = new InMemoryDatabase();
        db2.put("p", "forever", 1);
        db2.backup(5);
        db2.restore(1, 100);
        assertEq("forever", db2.get("p", 1_000_000_000), "no-ttl entry survives restore");

        // backup_id increments monotonically
        InMemoryDatabase db3 = new InMemoryDatabase();
        db3.put("a", "1", 1);
        assertEq(1, db3.backup(2), "first backup_id");
        assertEq(2, db3.backup(3), "second backup_id increments");

        // already-expired entries are NOT included in backup
        InMemoryDatabase db4 = new InMemoryDatabase();
        db4.put("short", "gone", 1, 1);  // [1, 2) — expired by t=10
        db4.put("keep", "here", 1);      // forever
        int bid4 = db4.backup(10);
        db4.restore(bid4, 100);
        assertEq(null, db4.get("short", 100), "expired entry not in backup");
        assertEq("here", db4.get("keep", 100), "live entry survives backup/restore");
    }
}
