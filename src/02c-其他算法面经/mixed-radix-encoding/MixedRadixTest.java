import java.util.*;

public class MixedRadixTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", MixedRadixTest::testPart1);
        tests.put("part2", MixedRadixTest::testPart2);
        tests.put("part3", MixedRadixTest::testPart3);
        tests.put("part4", MixedRadixTest::testPart4);

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

    static void assertArrEq(int[] expected, int[] actual, String msg) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(msg + " — expected: " + Arrays.toString(expected) + ", actual: " + Arrays.toString(actual));
        }
    }

    static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    // ===== Part 1: encode ================================================

    static void testPart1() {
        // 10 进制 sanity
        assertEq(127L, MixedRadix.encode(new int[]{1, 2, 7}, new int[]{10, 10, 10}), "127 in base-10");
        assertEq(0L,   MixedRadix.encode(new int[]{0, 0, 0}, new int[]{10, 10, 10}), "000 in base-10");

        // mixed-radix README 例子: bases=[3,2,4], digits=[2,0,1] → 17
        assertEq(17L, MixedRadix.encode(new int[]{2, 0, 1}, new int[]{3, 2, 4}), "[2,0,1] in [3,2,4]");

        // 单位: 长度 1, digit 直接 == N
        assertEq(5L, MixedRadix.encode(new int[]{5}, new int[]{10}), "single digit");

        // 二进制 binary
        assertEq(13L, MixedRadix.encode(new int[]{1, 1, 0, 1}, new int[]{2, 2, 2, 2}), "1101 binary");

        // 所有合法编码必须落在 [0, total)
        int[] bases = {3, 2, 4};   // total = 24
        Set<Long> seen = new HashSet<>();
        for (int a = 0; a < 3; a++) {
            for (int b = 0; b < 2; b++) {
                for (int c = 0; c < 4; c++) {
                    long n = MixedRadix.encode(new int[]{a, b, c}, bases);
                    assertTrue(n >= 0 && n < 24, "n in [0, 24): " + n);
                    seen.add(n);
                }
            }
        }
        assertEq(24, seen.size(), "all 24 encodings distinct (bijection)");
    }

    // ===== Part 2: decode ================================================

    static void testPart2() {
        assertArrEq(new int[]{1, 2, 7}, MixedRadix.decode(127, new int[]{10, 10, 10}), "127 in base-10");
        assertArrEq(new int[]{0, 0, 0}, MixedRadix.decode(0,   new int[]{10, 10, 10}), "0 in base-10");

        assertArrEq(new int[]{2, 0, 1}, MixedRadix.decode(17, new int[]{3, 2, 4}), "17 in [3,2,4]");

        assertArrEq(new int[]{5}, MixedRadix.decode(5, new int[]{10}), "single digit");
        assertArrEq(new int[]{1, 1, 0, 1}, MixedRadix.decode(13, new int[]{2, 2, 2, 2}), "13 binary");

        // 全 [0, total) 都能 decode 出合法 digits
        int[] bases = {3, 2, 4};
        for (long n = 0; n < 24; n++) {
            int[] d = MixedRadix.decode(n, bases);
            assertEq(3, d.length, "length matches bases");
            for (int i = 0; i < 3; i++) {
                assertTrue(d[i] >= 0 && d[i] < bases[i], "digit[" + i + "] in [0, " + bases[i] + ")");
            }
        }
    }

    // ===== Part 3: kthCombination ========================================

    static void testPart3() {
        List<List<String>> v = List.of(
                List.of("a", "b", "c"),
                List.of("x", "y")
        );

        assertEq(List.of("a", "x"), MixedRadix.kthCombination(v, 0), "k=0");
        assertEq(List.of("a", "y"), MixedRadix.kthCombination(v, 1), "k=1");
        assertEq(List.of("b", "x"), MixedRadix.kthCombination(v, 2), "k=2");
        assertEq(List.of("b", "y"), MixedRadix.kthCombination(v, 3), "k=3");
        assertEq(List.of("c", "x"), MixedRadix.kthCombination(v, 4), "k=4");
        assertEq(List.of("c", "y"), MixedRadix.kthCombination(v, 5), "k=5");

        // 所有 [0, total) 必须产出 distinct combinations
        List<List<Integer>> w = List.of(
                List.of(1, 2),
                List.of(3, 4, 5),
                List.of(6, 7)
        );
        Set<List<Integer>> all = new HashSet<>();
        for (long k = 0; k < 12; k++) {
            all.add(MixedRadix.kthCombination(w, k));
        }
        assertEq(12, all.size(), "all 12 combinations distinct");
    }

    // ===== Part 4: kthPermutation (LC 60) ================================

    static void testPart4() {
        // n=3 所有 6 个 permutation
        assertArrEq(new int[]{1, 2, 3}, MixedRadix.kthPermutation(3, 0), "n=3 k=0");
        assertArrEq(new int[]{1, 3, 2}, MixedRadix.kthPermutation(3, 1), "n=3 k=1");
        assertArrEq(new int[]{2, 1, 3}, MixedRadix.kthPermutation(3, 2), "n=3 k=2");
        assertArrEq(new int[]{2, 3, 1}, MixedRadix.kthPermutation(3, 3), "n=3 k=3");
        assertArrEq(new int[]{3, 1, 2}, MixedRadix.kthPermutation(3, 4), "n=3 k=4");
        assertArrEq(new int[]{3, 2, 1}, MixedRadix.kthPermutation(3, 5), "n=3 k=5");

        // n=1 边界
        assertArrEq(new int[]{1}, MixedRadix.kthPermutation(1, 0), "n=1");

        // LC 60 经典: n=4 k=8 (0-indexed) → "2314" → [2,3,1,4]
        assertArrEq(new int[]{2, 3, 1, 4}, MixedRadix.kthPermutation(4, 8), "n=4 k=8");

        // 所有 n=4 的 24 个 permutation 必须 distinct
        Set<List<Integer>> all = new HashSet<>();
        for (int k = 0; k < 24; k++) {
            int[] p = MixedRadix.kthPermutation(4, k);
            List<Integer> boxed = new ArrayList<>();
            for (int x : p) boxed.add(x);
            all.add(boxed);
            // 每个 permutation 必须用到 1..4 每个数字一次
            Set<Integer> used = new HashSet<>(boxed);
            assertEq(Set.of(1, 2, 3, 4), used, "k=" + k + " uses {1,2,3,4}");
        }
        assertEq(24, all.size(), "all 24 permutations distinct");
    }
}
