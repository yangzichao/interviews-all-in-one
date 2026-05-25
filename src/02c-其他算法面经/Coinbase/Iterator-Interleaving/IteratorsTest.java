import java.util.*;

public class IteratorsTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", IteratorsTest::testPart1);
        tests.put("part2", IteratorsTest::testPart2);
        tests.put("part3", IteratorsTest::testPart3);
        tests.put("part4", IteratorsTest::testPart4);

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
