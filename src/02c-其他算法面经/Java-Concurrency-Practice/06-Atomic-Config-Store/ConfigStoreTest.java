import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ConfigStoreTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) throws Exception {
        Map<String, RunnableEx> tests = new LinkedHashMap<>();
        tests.put("single", ConfigStoreTest::testSingle);
        tests.put("reader_consistency", ConfigStoreTest::testReaderConsistency);
        tests.put("get_under_load", ConfigStoreTest::testGetUnderLoad);
        tests.put("snapshot_immutable", ConfigStoreTest::testSnapshotImmutable);

        List<String> toRun = args.length == 0 ? new ArrayList<>(tests.keySet()) : Arrays.asList(args);
        for (String name : toRun) {
            RunnableEx t = tests.get(name);
            if (t == null) { System.out.println("unknown case: " + name); System.exit(2); }
            run(name, t);
        }
        System.out.printf("%nPassed=%d  Failed=%d  Skipped=%d%n", passed, failed, skipped);
        if (failed > 0) System.exit(1);
    }

    static void run(String name, RunnableEx test) {
        try { test.run(); System.out.println(name + " PASSED"); passed++; }
        catch (UnsupportedOperationException e) { System.out.println(name + " SKIPPED (not implemented)"); skipped++; }
        catch (AssertionError e) { System.out.println(name + " FAILED: " + e.getMessage()); failed++; }
        catch (Throwable e) { System.out.println(name + " ERROR: " + e); e.printStackTrace(System.out); failed++; }
    }

    static void assertEq(Object expected, Object actual, String msg) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(msg + " — expected: " + expected + ", actual: " + actual);
        }
    }

    // ========================================================================
    static void testSingle() {
        ConfigStore s = new ConfigStore();
        Map<String, String> u1 = new HashMap<>();
        u1.put("a", "1"); u1.put("b", "2");
        s.putAll(u1);
        Map<String, String> u2 = new HashMap<>();
        u2.put("b", "3"); u2.put("c", "4");
        s.putAll(u2);

        assertEq("1", s.get("a"), "a preserved across second putAll");
        assertEq("3", s.get("b"), "b overwritten");
        assertEq("4", s.get("c"), "c added");
        assertEq(null, s.get("missing"), "missing returns null");

        Map<String, String> snap = s.snapshot();
        assertEq(3, snap.size(), "snapshot size");
    }

    // ========================================================================
    // reader_consistency — the heart of this problem
    // ========================================================================
    static void testReaderConsistency() throws Exception {
        final ConfigStore s = new ConfigStore();
        Map<String, String> usd = new HashMap<>(); usd.put("currency", "USD"); usd.put("symbol", "$");
        Map<String, String> eur = new HashMap<>(); eur.put("currency", "EUR"); eur.put("symbol", "€");
        s.putAll(usd);

        final AtomicBoolean stop = new AtomicBoolean(false);
        final AtomicLong mismatches = new AtomicLong();
        final AtomicLong reads = new AtomicLong();

        ExecutorService pool = Executors.newFixedThreadPool(21);
        // writer
        pool.submit(() -> {
            boolean flag = false;
            while (!stop.get()) {
                s.putAll(flag ? eur : usd);
                flag = !flag;
            }
        });
        // 20 readers — each one keeps grabbing snapshots and verifying currency/symbol pair
        for (int r = 0; r < 20; r++) {
            pool.submit(() -> {
                while (!stop.get()) {
                    Map<String, String> snap = s.snapshot();
                    String c = snap.get("currency");
                    String sym = snap.get("symbol");
                    reads.incrementAndGet();
                    boolean ok = ("USD".equals(c) && "$".equals(sym)) || ("EUR".equals(c) && "€".equals(sym));
                    if (!ok) mismatches.incrementAndGet();
                }
            });
        }

        Thread.sleep(200);
        stop.set(true);
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.SECONDS);

        if (reads.get() < 1_000) {
            throw new AssertionError("too few reads to be a real stress test: " + reads.get());
        }
        if (mismatches.get() > 0) {
            throw new AssertionError("inconsistent snapshot observed " + mismatches.get() + " times out of " + reads.get() + " reads — putAll is not atomic");
        }
    }

    // ========================================================================
    static void testGetUnderLoad() throws Exception {
        final ConfigStore s = new ConfigStore();
        Map<String, String> init = new HashMap<>();
        for (int i = 0; i < 20; i++) init.put("k" + i, "v0");
        s.putAll(init);

        final AtomicBoolean stop = new AtomicBoolean(false);
        ExecutorService pool = Executors.newFixedThreadPool(9);
        // writer
        pool.submit(() -> {
            int gen = 1;
            while (!stop.get()) {
                Map<String, String> u = new HashMap<>();
                for (int i = 0; i < 20; i++) u.put("k" + i, "v" + gen);
                s.putAll(u);
                gen++;
            }
        });
        // 8 readers
        for (int r = 0; r < 8; r++) {
            pool.submit(() -> {
                while (!stop.get()) {
                    for (int i = 0; i < 20; i++) s.get("k" + i);
                }
            });
        }

        Thread.sleep(150);
        stop.set(true);
        pool.shutdown();
        if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
            throw new AssertionError("deadlock or stuck thread under load");
        }
    }

    // ========================================================================
    static void testSnapshotImmutable() {
        ConfigStore s = new ConfigStore();
        Map<String, String> u = new HashMap<>(); u.put("a", "1");
        s.putAll(u);
        Map<String, String> snap = s.snapshot();
        try {
            snap.put("a", "999");
            // mutable — but mutation must not affect store
            assertEq("1", s.get("a"), "snapshot mutation must not affect store");
        } catch (UnsupportedOperationException ignore) {
            // truly immutable, also OK
        }
    }

    @FunctionalInterface interface RunnableEx { void run() throws Exception; }
}
