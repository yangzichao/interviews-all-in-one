import java.util.*;
import java.util.function.Predicate;

public class LogParsingTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", LogParsingTest::testPart1);
        tests.put("part2", LogParsingTest::testPart2);
        tests.put("part3", LogParsingTest::testPart3);
        tests.put("part4", LogParsingTest::testPart4);
        tests.put("part5", LogParsingTest::testPart5);
        tests.put("part6", LogParsingTest::testPart6);
        tests.put("part7", LogParsingTest::testPart7);
        tests.put("part8", LogParsingTest::testPart8);

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

    static LogParsing.LogEntry entry(long ts, String tid, String level, String msg) {
        return new LogParsing.LogEntry(ts, tid, level, msg);
    }

    // ===== Part 1: Parse & Group ========================================

    static void testPart1() {
        LogParsing.LogParserPart1 p = new LogParsing.LogParserPart1();
        List<String> lines = List.of(
                "1000 t1 INFO start",
                "1500 t2 WARN slow query",
                "1200 t1 ERROR boom in core module",
                "not a real line",                          // malformed — drop
                "abc t1 INFO bad timestamp"                 // malformed — drop
        );
        Map<String, List<LogParsing.LogEntry>> grouped = p.parseAndGroup(lines);

        assertEq(2, grouped.size(), "two threads expected");
        assertTrue(grouped.containsKey("t1"), "has t1");
        assertTrue(grouped.containsKey("t2"), "has t2");

        List<LogParsing.LogEntry> t1 = grouped.get("t1");
        assertEq(2, t1.size(), "t1 has 2 entries");
        assertEq(entry(1000, "t1", "INFO", "start"), t1.get(0), "t1 first sorted by ts");
        assertEq(entry(1200, "t1", "ERROR", "boom in core module"), t1.get(1),
                "t1 second; message keeps spaces");

        List<LogParsing.LogEntry> t2 = grouped.get("t2");
        assertEq(1, t2.size(), "t2 has 1 entry");
        assertEq(entry(1500, "t2", "WARN", "slow query"), t2.get(0), "t2 entry message");

        // empty input → empty map
        assertEq(0, p.parseAndGroup(List.of()).size(), "empty input → empty map");
    }

    // ===== Part 2: Filter by Predicate ==================================

    static void testPart2() {
        LogParsing.LogParserPart2 p = new LogParsing.LogParserPart2();
        List<String> lines = List.of(
                "1000 t1 INFO ok",
                "1100 t1 ERROR boom",
                "1200 t2 INFO ok",
                "1300 t3 INFO normal"        // entirely filtered out
        );

        Predicate<LogParsing.LogEntry> onlyErrors = e -> e.level().equals("ERROR");
        Map<String, List<LogParsing.LogEntry>> grouped = p.parseAndGroup(lines, onlyErrors);

        assertEq(1, grouped.size(), "only t1 has ERROR");
        assertTrue(grouped.containsKey("t1"), "t1 present");
        assertTrue(!grouped.containsKey("t2"), "t2 not present (no ERROR)");
        assertTrue(!grouped.containsKey("t3"), "t3 not present (no ERROR)");
        assertEq(1, grouped.get("t1").size(), "t1 has 1 ERROR");
        assertEq("boom", grouped.get("t1").get(0).message(), "t1 ERROR message");

        // predicate accepting everything == Part 1 semantics
        Map<String, List<LogParsing.LogEntry>> all = p.parseAndGroup(lines, e -> true);
        assertEq(3, all.size(), "three threads when keep=all");
    }

    // ===== Part 3: Time Window Query ====================================

    static void testPart3() {
        List<String> lines = List.of(
                "100 t1 INFO a",
                "200 t1 INFO b",
                "300 t1 INFO c",
                "150 t2 INFO d",
                "250 t2 INFO e"
        );
        LogParsing.LogParserPart3 p = new LogParsing.LogParserPart3(lines);

        // t1 in [150, 300) → [200 b]
        List<LogParsing.LogEntry> r1 = p.query(150, 300, "t1");
        assertEq(1, r1.size(), "t1 window [150,300)");
        assertEq(200L, r1.get(0).timestamp(), "t1 window entry ts");

        // inclusive from, exclusive to
        List<LogParsing.LogEntry> r2 = p.query(100, 301, "t1");
        assertEq(3, r2.size(), "t1 window covers all three");

        List<LogParsing.LogEntry> r3 = p.query(100, 300, "t1");
        assertEq(2, r3.size(), "t1 window [100,300) excludes 300");

        // null threadId → all threads merged
        List<LogParsing.LogEntry> all = p.query(0, 1000, null);
        assertEq(5, all.size(), "null thread → all entries");
        // sorted by ts: 100,150,200,250,300
        long[] expected = {100, 150, 200, 250, 300};
        for (int i = 0; i < expected.length; i++) {
            assertEq(expected[i], all.get(i).timestamp(), "merged ts at pos " + i);
        }

        // empty window
        assertEq(0, p.query(1000, 2000, "t1").size(), "empty window");
        // unknown thread
        assertEq(0, p.query(0, 1000, "tX").size(), "unknown thread");
    }

    // ===== Part 4: Streaming Append =====================================

    static void testPart4() {
        LogParsing.LogParserPart4 p = new LogParsing.LogParserPart4();
        p.append("1000 t1 INFO a");
        p.append("3000 t1 INFO c");
        p.append("2000 t1 INFO b");   // out-of-order
        p.append("500 t2 INFO x");

        // recent 2 from t1: timestamps 2000, 3000 — oldest-first
        List<LogParsing.LogEntry> r = p.recent("t1", 2);
        assertEq(2, r.size(), "recent 2 size");
        assertEq(2000L, r.get(0).timestamp(), "recent oldest-first first ts");
        assertEq(3000L, r.get(1).timestamp(), "recent oldest-first second ts");

        // recent more than exists → all
        List<LogParsing.LogEntry> r2 = p.recent("t1", 10);
        assertEq(3, r2.size(), "recent n > all → all");
        assertEq(1000L, r2.get(0).timestamp(), "recent all oldest-first first");

        // unknown thread → empty
        assertEq(0, p.recent("tX", 3).size(), "unknown thread → empty");

        // n=0 → empty
        assertEq(0, p.recent("t1", 0).size(), "n=0 → empty");

        // malformed line silently skipped, does not affect existing data
        p.append("bogus line");
        List<LogParsing.LogEntry> r3 = p.recent("t1", 10);
        assertEq(3, r3.size(), "malformed append silently ignored");
    }

    // ===== Part 5: Sliding Time-Window Aggregation ======================

    static void testPart5() {
        // 5-second window
        LogParsing.LogParserPart5 p = new LogParsing.LogParserPart5(5_000L);
        p.appendPart5("1000 t1 INFO  a");
        p.appendPart5("2000 t1 ERROR boom");
        p.appendPart5("3000 t2 INFO  ok");
        p.appendPart5("4000 t1 ERROR boom2");
        p.appendPart5("5000 t2 WARN  slow");

        // at now=5500, window covers [500, 5500): all 5 entries
        // 2 ERROR / 5 total = 0.4
        double rate = p.errorRatePart5(5500L);
        assertTrue(Math.abs(rate - 0.4) < 1e-9, "errorRate at 5500: " + rate);

        Map<String, Integer> dist = p.countByLevelPart5(5500L);
        assertEq(2, dist.get("INFO"), "INFO count");
        assertEq(2, dist.get("ERROR"), "ERROR count");
        assertEq(1, dist.get("WARN"), "WARN count");

        // topN by count: t1=3, t2=2
        List<String> top = p.topNThreadsPart5(5500L, 2);
        assertEq(2, top.size(), "top2 size");
        assertEq("t1", top.get(0), "top1 thread");

        // slide forward: at now=10_000, window=[5000,10_000) covers only the 5000 WARN
        Map<String, Integer> dist2 = p.countByLevelPart5(10_000L);
        Integer infoCount = dist2.getOrDefault("INFO", 0);
        Integer errCount = dist2.getOrDefault("ERROR", 0);
        Integer warnCount = dist2.getOrDefault("WARN", 0);
        assertEq(0, infoCount, "INFO dropped");
        assertEq(0, errCount, "ERROR dropped");
        assertEq(1, warnCount, "WARN remains");
    }

    // ===== Part 6: Concurrent Streaming + Querying ======================

    static void testPart6() {
        LogParsing.LogParserPart6 p = new LogParsing.LogParserPart6(60_000L);
        // single-threaded sanity: just check API works before any concurrency
        p.appendPart6("1000 t1 INFO  ok");
        p.appendPart6("2000 t1 ERROR boom");
        Map<String, Integer> dist = p.countByLevelPart6(3000L);
        assertEq(1, dist.getOrDefault("INFO", 0), "INFO count single thread");
        assertEq(1, dist.getOrDefault("ERROR", 0), "ERROR count single thread");

        // light concurrency smoke: 4 writer threads append 1000 each
        LogParsing.LogParserPart6 q = new LogParsing.LogParserPart6(60_000L);
        int writers = 4, perWriter = 1000;
        Thread[] ts = new Thread[writers];
        for (int w = 0; w < writers; w++) {
            final int wid = w;
            ts[w] = new Thread(() -> {
                for (int i = 0; i < perWriter; i++) {
                    String lvl = (i % 10 == 0) ? "ERROR" : "INFO";
                    q.appendPart6((10_000 + i) + " t" + wid + " " + lvl + " msg" + i);
                }
            });
        }
        for (Thread t : ts) t.start();
        try {
            for (Thread t : ts) t.join();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new AssertionError("interrupted while joining writers");
        }

        Map<String, Integer> agg = q.countByLevelPart6(70_000L);
        int total = agg.values().stream().mapToInt(Integer::intValue).sum();
        assertEq(writers * perWriter, total, "no entries lost under concurrent append");
        double r = q.errorRatePart6(70_000L);
        assertTrue(r >= 0.0 && r <= 1.0, "errorRate is a valid probability: " + r);
    }

    // ===== Part 7: Cold/Hot Tiering + Archival ==========================

    static void testPart7() {
        LogParsing.LogParserPart7 p = new LogParsing.LogParserPart7();
        p.appendPart7("1000 t1 INFO  a");
        p.appendPart7("2000 t1 INFO  b");
        p.appendPart7("3000 t1 INFO  c");
        p.appendPart7("4000 t1 INFO  d");

        // roll over everything before 3000 to cold storage
        String segId = p.rolloverPart7(3000L);
        assertTrue(segId != null && !segId.isEmpty(), "rollover returns non-empty segment id");

        // unified query spanning hot+cold boundary
        List<LogParsing.LogEntry> all = p.queryUnifiedPart7(1000L, 5000L, "t1");
        assertEq(4, all.size(), "unified query returns all 4 entries, no dup, no loss");
        // entries should be sorted asc by ts
        long[] expected = {1000, 2000, 3000, 4000};
        for (int i = 0; i < expected.length; i++) {
            assertEq(expected[i], all.get(i).timestamp(), "unified query ts at " + i);
        }

        // cold-only query
        List<LogParsing.LogEntry> cold = p.queryUnifiedPart7(1000L, 3000L, "t1");
        assertEq(2, cold.size(), "cold-only [1000,3000) -> 2 entries");
    }

    // ===== Part 8: Inverted Index + Sharding + Compaction ===============

    static void testPart8() {
        LogParsing.LogParserPart8 p = new LogParsing.LogParserPart8(4);
        p.indexPart8("1000 t1 INFO  request timeout from upstream");
        p.indexPart8("2000 t1 ERROR connection timeout");
        p.indexPart8("3000 t2 INFO  request ok");
        p.indexPart8("4000 t2 WARN  slow response");

        // search "timeout" over full range -> 2 entries (ts 1000, 2000)
        List<LogParsing.LogEntry> hits = p.searchPart8("timeout", 0L, 10_000L);
        assertEq(2, hits.size(), "search 'timeout' hit count");
        // ascending by ts
        assertEq(1000L, hits.get(0).timestamp(), "first hit ts");
        assertEq(2000L, hits.get(1).timestamp(), "second hit ts");

        // time-window filter excludes earlier hit
        List<LogParsing.LogEntry> hits2 = p.searchPart8("timeout", 1500L, 10_000L);
        assertEq(1, hits2.size(), "windowed search hit count");

        // term not present
        assertEq(0, p.searchPart8("nonexistent", 0L, 10_000L).size(), "missing term -> empty");

        // compact should not lose data
        p.compactPart8();
        assertEq(2, p.searchPart8("timeout", 0L, 10_000L).size(), "compaction preserves data");
    }
}
