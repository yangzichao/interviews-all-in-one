import java.util.*;

public class IteratorsTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", IteratorsTest::testPart1);
        tests.put("part2", IteratorsTest::testPart2);
        tests.put("part3", IteratorsTest::testPart3);
        tests.put("part4", IteratorsTest::testPart4);
        tests.put("part5", IteratorsTest::testPart5);
        tests.put("part6", IteratorsTest::testPart6);
        tests.put("part7", IteratorsTest::testPart7);
        tests.put("part8", IteratorsTest::testPart8);

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

    static List<Integer> drain(Iterator<Integer> it) {
        List<Integer> out = new ArrayList<>();
        while (it.hasNext()) out.add(it.next());
        return out;
    }

    static List<Integer> drainN(Iterator<Integer> it, int n) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            assertTrue(it.hasNext(), "expected hasNext()==true at step " + i);
            out.add(it.next());
        }
        return out;
    }

    // ===== Part 1: RangeIterator ========================================

    static void testPart1() {
        assertEq(List.of(0, 1, 2, 3, 4),
                drain(new Iterators.RangeIteratorPart1(0, 5, 1)), "basic [0,5) step 1");

        assertEq(List.of(0, 3, 6, 9),
                drain(new Iterators.RangeIteratorPart1(0, 10, 3)), "[0,10) step 3");

        assertEq(List.of(),
                drain(new Iterators.RangeIteratorPart1(5, 5, 1)), "empty when start==end");

        assertEq(List.of(),
                drain(new Iterators.RangeIteratorPart1(10, 5, 1)), "empty when start>end");

        assertEq(List.of(2, 4, 6),
                drain(new Iterators.RangeIteratorPart1(2, 8, 2)), "[2,8) step 2");

        // NoSuchElementException after exhaustion
        Iterator<Integer> it = new Iterators.RangeIteratorPart1(0, 2, 1);
        it.next(); it.next();
        assertTrue(!it.hasNext(), "exhausted hasNext==false");
        boolean threw = false;
        try { it.next(); } catch (NoSuchElementException e) { threw = true; }
        assertTrue(threw, "next() after exhaustion should throw NoSuchElementException");
    }

    // ===== Part 2: InterleavingIterator =================================

    static void testPart2() {
        List<Iterator<Integer>> in1 = List.of(
                List.of(1, 2, 3).iterator(),
                List.of(4, 5).iterator(),
                List.of(6).iterator(),
                List.<Integer>of().iterator(),
                List.of(7, 8, 9).iterator()
        );
        assertEq(List.of(1, 4, 6, 7, 2, 5, 8, 3, 9),
                drain(new Iterators.InterleavingIteratorPart2(in1)), "skip-exhausted round-robin");

        // empty input
        assertEq(List.of(),
                drain(new Iterators.InterleavingIteratorPart2(List.of())), "empty input list");

        // all sub-iters empty
        List<Iterator<Integer>> in2 = List.of(
                List.<Integer>of().iterator(),
                List.<Integer>of().iterator()
        );
        assertEq(List.of(),
                drain(new Iterators.InterleavingIteratorPart2(in2)), "all sub-iters empty");

        // single iter
        List<Iterator<Integer>> in3 = List.of(List.of(1, 2, 3).iterator());
        assertEq(List.of(1, 2, 3),
                drain(new Iterators.InterleavingIteratorPart2(in3)), "single iterator pass-through");

        // equal-length 2 iters = LC 281 baseline
        List<Iterator<Integer>> in4 = List.of(
                List.of(1, 3, 5).iterator(),
                List.of(2, 4, 6).iterator()
        );
        assertEq(List.of(1, 2, 3, 4, 5, 6),
                drain(new Iterators.InterleavingIteratorPart2(in4)), "LC 281 zigzag");
    }

    // ===== Part 3: FilterIterator =======================================

    static void testPart3() {
        // keep positives
        Iterator<Integer> a = List.of(1, -2, 3, -4, 5, 6).iterator();
        assertEq(List.of(1, 3, 5, 6),
                drain(new Iterators.FilterIteratorPart3(a, x -> x > 0)), "keep positives");

        // keep evens
        Iterator<Integer> b = List.of(1, 2, 3, 4).iterator();
        assertEq(List.of(2, 4),
                drain(new Iterators.FilterIteratorPart3(b, x -> x % 2 == 0)), "keep evens");

        // everything filtered out
        Iterator<Integer> c = List.of(1, 3, 5).iterator();
        assertEq(List.of(),
                drain(new Iterators.FilterIteratorPart3(c, x -> x % 2 == 0)), "all filtered out");

        // empty source
        Iterator<Integer> d = List.<Integer>of().iterator();
        assertEq(List.of(),
                drain(new Iterators.FilterIteratorPart3(d, x -> true)), "empty source");

        // hasNext idempotent (multiple calls without next() shouldn't drop elements)
        Iterator<Integer> e = new Iterators.FilterIteratorPart3(
                List.of(1, -1, 2, -2, 3).iterator(), x -> x > 0);
        assertTrue(e.hasNext(), "hasNext call 1");
        assertTrue(e.hasNext(), "hasNext call 2 (idempotent)");
        assertTrue(e.hasNext(), "hasNext call 3 (idempotent)");
        assertEq(1, e.next(), "after 3x hasNext, still get 1");
        assertEq(2, e.next(), "then 2");
        assertEq(3, e.next(), "then 3");
        assertTrue(!e.hasNext(), "exhausted");
    }

    // ===== Part 5: ConcurrentInterleavingIterator =======================

    static void testPart5() {
        // basic single-threaded sanity — should still produce the same multiset
        // as Part 2 even though order may differ when accessed concurrently.
        List<Iterator<Integer>> in = List.of(
                List.of(1, 2, 3).iterator(),
                List.of(4, 5).iterator(),
                List.of(6).iterator()
        );
        Iterator<Integer> it = new Iterators.ConcurrentInterleavingIteratorPart5(in);
        List<Integer> drained = drain(it);
        List<Integer> sorted = new ArrayList<>(drained);
        Collections.sort(sorted);
        assertEq(List.of(1, 2, 3, 4, 5, 6), sorted, "single-threaded total multiset");

        // empty case
        Iterator<Integer> empty = new Iterators.ConcurrentInterleavingIteratorPart5(List.of());
        assertTrue(!empty.hasNext(), "empty hasNext==false");

        // Concurrent stress: 4 threads pull until exhausted, union must equal the
        // input multiset, no element should be emitted twice, no NoSuchElement.
        int n = 5000;
        List<Iterator<Integer>> iters = new ArrayList<>();
        for (int s = 0; s < 4; s++) {
            List<Integer> l = new ArrayList<>();
            for (int i = 0; i < n; i++) l.add(s * n + i);
            iters.add(l.iterator());
        }
        Iterator<Integer> sharedIt = new Iterators.ConcurrentInterleavingIteratorPart5(iters);
        java.util.concurrent.ConcurrentLinkedQueue<Integer> collected = new java.util.concurrent.ConcurrentLinkedQueue<>();
        int threadCount = 4;
        Thread[] threads = new Thread[threadCount];
        for (int t = 0; t < threadCount; t++) {
            threads[t] = new Thread(() -> {
                try {
                    while (sharedIt.hasNext()) {
                        try {
                            collected.add(sharedIt.next());
                        } catch (NoSuchElementException ignore) {
                            // Allowed only if the implementation chose TOCTOU-tolerant style;
                            // count it as a fail to be strict.
                            throw new AssertionError("NoSuchElementException leaked to consumer");
                        }
                    }
                } catch (AssertionError ae) {
                    throw ae;
                }
            });
        }
        for (Thread th : threads) th.start();
        for (Thread th : threads) {
            try { th.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        assertEq(4 * n, collected.size(), "all elements emitted exactly once (count)");
        Set<Integer> uniq = new HashSet<>(collected);
        assertEq(4 * n, uniq.size(), "no duplicate emit across threads");
    }

    // ===== Part 6: BatchedInterleavingIterator + stats ==================

    static void testPart6() {
        List<Iterator<Integer>> in = List.of(
                List.of(1, 2, 3).iterator(),
                List.of(4, 5).iterator(),
                List.of(6).iterator(),
                List.<Integer>of().iterator(),
                List.of(7, 8, 9).iterator()
        );
        Iterators.BatchedInterleavingIteratorPart6 it = new Iterators.BatchedInterleavingIteratorPart6(in);

        // batch larger than n=4: should return first 4 in interleaving order
        List<Integer> b1 = it.nextBatch(4);
        assertEq(List.of(1, 4, 6, 7), b1, "first batch of 4 in round-robin order");

        // next batch of 100 should drain the rest (5 elements)
        List<Integer> b2 = it.nextBatch(100);
        assertEq(List.of(2, 5, 8, 3, 9), b2, "remaining elements when batch > available");

        // exhausted → empty list, NOT NoSuchElementException
        List<Integer> b3 = it.nextBatch(10);
        assertEq(List.of(), b3, "exhausted nextBatch returns empty list");

        // stats sanity
        Iterators.StatsPart6 st = it.stats();
        assertEq(9L, st.emittedCount, "stats emittedCount");
        assertTrue(st.exhaustedSourceCount >= 4, "stats exhaustedSourceCount >= 4 (incl. initial empty)");
        assertTrue(st.callCount >= 3, "stats callCount tracks nextBatch invocations");

        // empty input
        Iterators.BatchedInterleavingIteratorPart6 empty =
                new Iterators.BatchedInterleavingIteratorPart6(List.of());
        assertEq(List.of(), empty.nextBatch(10), "empty input nextBatch");
        assertTrue(!empty.hasNext(), "empty hasNext");
    }

    // ===== Part 7: SnapshotIterator =====================================

    static void testPart7() {
        // basic: iterate snapshot of a list
        List<Integer> source = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        Iterator<Integer> it = new Iterators.SnapshotIteratorPart7(source);
        assertEq(List.of(1, 2, 3, 4, 5), drain(it), "snapshot basic drain");

        // mutation policy: implementation must commit to ONE of:
        //   (i)  fail-fast: throws CME on next hasNext()/next()
        //   (ii) weakly-consistent: yields the pre-mutation snapshot
        List<Integer> src2 = new ArrayList<>(List.of(10, 20, 30));
        Iterator<Integer> it2 = new Iterators.SnapshotIteratorPart7(src2);
        assertEq(10, it2.next(), "snapshot first elem before mutation");
        src2.add(40);   // mutate after iterator created
        src2.remove(Integer.valueOf(20));

        boolean threwCME = false;
        List<Integer> rest = new ArrayList<>();
        try {
            while (it2.hasNext()) rest.add(it2.next());
        } catch (ConcurrentModificationException e) {
            threwCME = true;
        }

        // Accept either semantics, but require consistency with the chosen one.
        if (threwCME) {
            // fail-fast path — that's a valid implementation
        } else {
            // weakly-consistent path: should see the original 20, 30 (and NOT 40)
            assertEq(List.of(20, 30), rest, "weakly-consistent snapshot rest");
        }

        // empty
        Iterator<Integer> emptyIt = new Iterators.SnapshotIteratorPart7(new ArrayList<>());
        assertEq(List.of(), drain(emptyIt), "empty snapshot");
    }

    // ===== Part 8: ResumableIterator (cursor) ===========================

    static void testPart8() {
        List<List<Integer>> sources = List.of(
                List.of(1, 2, 3),
                List.of(4, 5),
                List.of(6),
                List.of(),
                List.of(7, 8, 9)
        );
        Iterators.ResumableInterleavingIteratorPart8 it =
                new Iterators.ResumableInterleavingIteratorPart8(sources);

        // emit first 4 = [1, 4, 6, 7] in canonical round-robin order
        List<Integer> first4 = drainN(it, 4);
        assertEq(List.of(1, 4, 6, 7), first4, "first 4 in canonical order");

        // save cursor mid-flight
        String cursor = it.saveCursor();
        assertTrue(cursor != null && !cursor.isEmpty(), "cursor should be a non-empty string");

        // restore from cursor — should continue, NOT restart
        Iterators.ResumableInterleavingIteratorPart8 it2 =
                Iterators.ResumableInterleavingIteratorPart8.restore(cursor, sources);
        List<Integer> rest = drain(it2);
        assertEq(List.of(2, 5, 8, 3, 9), rest, "restored iterator emits remaining elements");

        // also: the original iterator (if we kept consuming it) should yield the same rest
        // (i.e. saveCursor() must NOT mutate the iterator state)
        List<Integer> origRest = drain(it);
        assertEq(List.of(2, 5, 8, 3, 9), origRest, "saveCursor is read-only on iterator state");
    }

    // ===== Part 4: CycleIterator ========================================

    static void testPart4() {
        // basic cycling — first 9 of [1,2,3]
        Iterator<Integer> a = new Iterators.CycleIteratorPart4(List.of(1, 2, 3));
        assertEq(List.of(1, 2, 3, 1, 2, 3, 1, 2, 3), drainN(a, 9), "cycle 3 elements 3 times");

        // single element cycle
        Iterator<Integer> b = new Iterators.CycleIteratorPart4(List.of(7));
        assertEq(List.of(7, 7, 7, 7, 7), drainN(b, 5), "cycle single element");

        // empty source → never hasNext
        Iterator<Integer> c = new Iterators.CycleIteratorPart4(List.of());
        assertTrue(!c.hasNext(), "empty source hasNext()==false");
        boolean threw = false;
        try { c.next(); } catch (NoSuchElementException e) { threw = true; }
        assertTrue(threw, "empty cycle next() should throw");

        // hasNext stays true through many iterations
        Iterator<Integer> d = new Iterators.CycleIteratorPart4(List.of(1, 2));
        for (int i = 0; i < 100; i++) {
            assertTrue(d.hasNext(), "still hasNext at step " + i);
            d.next();
        }
    }
}
