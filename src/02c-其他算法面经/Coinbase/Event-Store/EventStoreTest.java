import java.util.*;

public class EventStoreTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("basic",       EventStoreTest::testBasic);
        tests.put("outoforder",  EventStoreTest::testOutOfOrder);
        tests.put("sametimestamp", EventStoreTest::testSameTimestamp);
        tests.put("multiuser",   EventStoreTest::testMultiUser);

        List<String> toRun = args.length == 0 ? new ArrayList<>(tests.keySet()) : Arrays.asList(args);
        for (String name : toRun) {
            Runnable t = tests.get(name);
            if (t == null) {
                System.out.println("unknown test: " + name + ", available: " + tests.keySet());
                System.exit(2);
            }
            run(name, t);
        }
        System.out.printf("%nPassed=%d  Failed=%d  Skipped=%d%n", passed, failed, skipped);
        if (failed > 0) System.exit(1);
    }

    static void run(String name, Runnable test) {
        try {
            test.run();
            System.out.println(name + " PASSED");
            passed++;
        } catch (UnsupportedOperationException e) {
            System.out.println(name + " SKIPPED (not implemented)");
            skipped++;
        } catch (AssertionError e) {
            System.out.println(name + " FAILED: " + e.getMessage());
            failed++;
        } catch (Throwable e) {
            System.out.println(name + " ERROR: " + e);
            e.printStackTrace(System.out);
            failed++;
        }
    }

    static void assertEq(Object expected, Object actual, String msg) {
        if (!Objects.equals(expected, actual))
            throw new AssertionError(msg + " — expected: " + expected + ", actual: " + actual);
    }

    // ----------------------------------------------------------------

    static void testBasic() {
        EventStore store = new EventStore();
        store.record(new EventStore.Event("alice", 100, "a"));
        store.record(new EventStore.Event("alice", 200, "b"));
        store.record(new EventStore.Event("alice", 300, "c"));

        assertEq(2, store.count("alice", 100, 200), "count [100,200]");
        List<EventStore.Event> result = store.query("alice", 100, 200);
        assertEq(2, result.size(), "query size [100,200]");
    }

    static void testOutOfOrder() {
        EventStore store = new EventStore();
        // arrive out of order
        store.record(new EventStore.Event("bob", 300, "c"));
        store.record(new EventStore.Event("bob", 100, "a"));
        store.record(new EventStore.Event("bob", 200, "b"));

        assertEq(3, store.count("bob", 100, 300), "out-of-order count");
        List<EventStore.Event> result = store.query("bob", 150, 300);
        assertEq(2, result.size(), "out-of-order range [150,300]");
    }

    static void testSameTimestamp() {
        EventStore store = new EventStore();
        store.record(new EventStore.Event("carol", 100, "x"));
        store.record(new EventStore.Event("carol", 100, "y")); // same timestamp
        store.record(new EventStore.Event("carol", 100, "z")); // same timestamp

        assertEq(3, store.count("carol", 100, 100), "same timestamp count");
    }

    static void testMultiUser() {
        EventStore store = new EventStore();
        store.record(new EventStore.Event("alice", 100, "a"));
        store.record(new EventStore.Event("bob",   100, "b"));
        store.record(new EventStore.Event("alice", 200, "c"));

        assertEq(2, store.count("alice", 100, 200), "alice count");
        assertEq(1, store.count("bob",   100, 200), "bob count");
        assertEq(0, store.count("carol", 100, 200), "unknown user count");
    }
}
