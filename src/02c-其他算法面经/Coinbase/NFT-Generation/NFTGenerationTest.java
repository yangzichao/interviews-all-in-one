import java.util.*;

public class NFTGenerationTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", NFTGenerationTest::testPart1);
        tests.put("part2", NFTGenerationTest::testPart2);
        tests.put("part2followup", NFTGenerationTest::testPart2Followup);
        tests.put("part3", NFTGenerationTest::testPart3);
        tests.put("part4", NFTGenerationTest::testPart4);
        tests.put("part5", NFTGenerationTest::testPart5);
        tests.put("part6", NFTGenerationTest::testPart6);
        tests.put("part7", NFTGenerationTest::testPart7);
        tests.put("part8", NFTGenerationTest::testPart8);

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

    // helpers ============================================================

    static List<NFTGeneration.Trait> sampleTraits() {
        return List.of(
                new NFTGeneration.Trait("eyes", List.of("round", "slit", "narrow")),
                new NFTGeneration.Trait("mouth", List.of("smile", "frown"))
        );
    }

    static void assertValidNFT(Map<String, String> nft, List<NFTGeneration.Trait> traits, String tag) {
        Set<String> expectedKeys = new HashSet<>();
        Map<String, Set<String>> allowed = new HashMap<>();
        for (NFTGeneration.Trait t : traits) {
            expectedKeys.add(t.type());
            allowed.put(t.type(), new HashSet<>(t.values()));
        }
        assertEq(expectedKeys, nft.keySet(), tag + ": NFT key set mismatch");
        for (var e : nft.entrySet()) {
            assertTrue(allowed.get(e.getKey()).contains(e.getValue()),
                    tag + ": value '" + e.getValue() + "' not in trait '" + e.getKey() + "'");
        }
    }

    // ===== Part 1: Uniform Random =======================================

    static void testPart1() {
        List<NFTGeneration.Trait> traits = sampleTraits();
        NFTGeneration.GeneratorPart1 gen = new NFTGeneration.GeneratorPart1(42L);
        List<Map<String, String>> out = gen.generate(10, traits);

        assertEq(10, out.size(), "size == requested");
        for (int i = 0; i < out.size(); i++) {
            assertValidNFT(out.get(i), traits, "nft[" + i + "]");
        }

        // size = 0 → 空 list
        assertEq(0, new NFTGeneration.GeneratorPart1(1L).generate(0, traits).size(), "size=0 → empty");

        // 同 seed → 同输出 (deterministic)
        var a = new NFTGeneration.GeneratorPart1(7L).generate(20, traits);
        var b = new NFTGeneration.GeneratorPart1(7L).generate(20, traits);
        assertEq(a, b, "same seed → same output");
    }

    // ===== Part 2: No Duplicates ========================================

    static void testPart2() {
        // 2 × 2 = 4 总组合
        List<NFTGeneration.Trait> small = List.of(
                new NFTGeneration.Trait("a", List.of("x", "y")),
                new NFTGeneration.Trait("b", List.of("p", "q"))
        );

        var out = new NFTGeneration.GeneratorPart2(1L).generate(4, small);
        assertEq(4, out.size(), "size == 4");
        Set<Map<String, String>> uniq = new HashSet<>(out);
        assertEq(4, uniq.size(), "all 4 NFTs distinct");

        // size > 总组合数 → throw
        boolean threw = false;
        try {
            new NFTGeneration.GeneratorPart2(1L).generate(5, small);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue(threw, "size > combos should throw IllegalArgumentException");

        // 较大空间 — 3 × 2 = 6 组合, 取 5 个 distinct
        List<NFTGeneration.Trait> mid = sampleTraits();  // 3 × 2 = 6
        var out2 = new NFTGeneration.GeneratorPart2(2L).generate(5, mid);
        assertEq(5, out2.size(), "size == 5");
        assertEq(5, new HashSet<>(out2).size(), "all distinct");
        for (int i = 0; i < out2.size(); i++) {
            assertValidNFT(out2.get(i), mid, "nft[" + i + "]");
        }
    }

    // ===== Part 2-Followup: Optimized for size ≈ total ==================

    static void testPart2Followup() {
        // 基本 sanity — 跟 Part 2 同输入
        List<NFTGeneration.Trait> small = List.of(
                new NFTGeneration.Trait("a", List.of("x", "y")),
                new NFTGeneration.Trait("b", List.of("p", "q"))
        );

        var out = new NFTGeneration.GeneratorPart2Followup(1L).generate(4, small);
        assertEq(4, out.size(), "size == 4");
        assertEq(4, new HashSet<>(out).size(), "all 4 NFTs distinct");

        // size > 总组合数 → throw
        boolean threw = false;
        try {
            new NFTGeneration.GeneratorPart2Followup(1L).generate(5, small);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue(threw, "size > combos should throw IllegalArgumentException");

        // 退化场景: size == total (rejection sampling 会卡死)
        // 8 trait × 2 value = 256 组合, 全取
        List<NFTGeneration.Trait> dense = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            dense.add(new NFTGeneration.Trait("t" + i, List.of("a", "b")));
        }
        long t0 = System.nanoTime();
        var dout = new NFTGeneration.GeneratorPart2Followup(42L).generate(256, dense);
        long elapsedMs = (System.nanoTime() - t0) / 1_000_000;
        assertEq(256, dout.size(), "size == 256 (full enumeration)");
        assertEq(256, new HashSet<>(dout).size(), "all 256 distinct");
        assertTrue(elapsedMs < 500, "should be fast even at size==total, took " + elapsedMs + "ms");

        // overflow 场景: 64 trait × 2 value = 2^64 > Long.MAX_VALUE
        List<NFTGeneration.Trait> huge = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            huge.add(new NFTGeneration.Trait("t" + i, List.of("a", "b")));
        }
        var hout = new NFTGeneration.GeneratorPart2Followup(7L).generate(100, huge);
        assertEq(100, hout.size(), "size == 100 (huge space, no overflow crash)");
        assertEq(100, new HashSet<>(hout).size(), "all 100 distinct");

        // deterministic
        var d1 = new NFTGeneration.GeneratorPart2Followup(99L).generate(50, dense);
        var d2 = new NFTGeneration.GeneratorPart2Followup(99L).generate(50, dense);
        assertEq(d1, d2, "same seed → same output");
    }

    // ===== Part 3: Per-Value Capacity Caps ==============================

    static void testPart3() {
        // 2 traits: eyes ∈ {common(cap=4), rare(cap=1)}, mouth ∈ {smile(cap=5), frown(cap=5)}
        // 总 unique combos = 2 × 2 = 4. size=5 distinct 必然不可行 (>4).
        List<NFTGeneration.CappedTrait> traits = List.of(
                new NFTGeneration.CappedTrait("eyes", Map.of("common", 4, "rare", 1)),
                new NFTGeneration.CappedTrait("mouth", Map.of("smile", 5, "frown", 5))
        );

        var out = new NFTGeneration.GeneratorPart3(1L).generate(4, traits);
        assertEq(4, out.size(), "size == 4");
        assertEq(4, new HashSet<>(out).size(), "all distinct");

        // 检查 rare 用量 ≤ 1
        int rareCount = 0;
        for (var nft : out) {
            if ("rare".equals(nft.get("eyes"))) rareCount++;
        }
        assertTrue(rareCount <= 1, "rare-eyes used at most 1 time, got " + rareCount);

        // 不可行 — size 超过 unique combos
        boolean threw = false;
        try {
            new NFTGeneration.GeneratorPart3(1L).generate(5, traits);
        } catch (IllegalStateException | IllegalArgumentException e) {
            threw = true;
        }
        assertTrue(threw, "infeasible should throw");

        // 不可行 — cap 总量 < size
        List<NFTGeneration.CappedTrait> tight = List.of(
                new NFTGeneration.CappedTrait("eyes", Map.of("a", 2)),
                new NFTGeneration.CappedTrait("mouth", Map.of("x", 2))
        );
        boolean threw2 = false;
        try {
            new NFTGeneration.GeneratorPart3(1L).generate(3, tight);
        } catch (IllegalStateException | IllegalArgumentException e) {
            threw2 = true;
        }
        assertTrue(threw2, "cap-bound exceeded should throw");
    }

    // ===== Part 4: Weighted Probabilities ===============================

    static void testPart4() {
        // 重权重 + 1 个 trait, 测分布偏向 (松界限)
        List<NFTGeneration.WeightedTrait> skewed = List.of(
                new NFTGeneration.WeightedTrait("eyes",
                        List.of("common", "rare"),
                        List.of(9, 1))
        );

        // size=2 distinct, 总 combos = 2 → 全列出来, 没法测概率, 改测 size=1 多次
        // 跑很多次小 batch, 统计
        int n = 2000;
        int common = 0;
        for (int i = 0; i < n; i++) {
            var single = new NFTGeneration.GeneratorPart4(i).generate(1, skewed);
            if ("common".equals(single.get(0).get("eyes"))) common++;
        }
        // 期望 ~90%, 留宽 ±10%
        double ratio = common / (double) n;
        assertTrue(ratio > 0.75 && ratio < 0.99,
                "weighted bias toward 'common' (got " + ratio + ", expected ~0.9)");

        // 多 trait + 唯一性
        List<NFTGeneration.WeightedTrait> multi = List.of(
                new NFTGeneration.WeightedTrait("eyes",
                        List.of("a", "b", "c"), List.of(1, 1, 1)),
                new NFTGeneration.WeightedTrait("mouth",
                        List.of("x", "y"), List.of(2, 1))
        );
        var out = new NFTGeneration.GeneratorPart4(123L).generate(5, multi);
        assertEq(5, out.size(), "size == 5");
        assertEq(5, new HashSet<>(out).size(), "all distinct");
        for (var nft : out) {
            assertEq(Set.of("eyes", "mouth"), nft.keySet(), "key set");
            assertTrue(Set.of("a", "b", "c").contains(nft.get("eyes")), "eyes value legal");
            assertTrue(Set.of("x", "y").contains(nft.get("mouth")), "mouth value legal");
        }
    }

    // ===== Part 5: 并发生成 ============================================

    static void testPart5() {
        List<NFTGeneration.Trait> traits = List.of(
                new NFTGeneration.Trait("eyes",  List.of("a", "b", "c", "d")),
                new NFTGeneration.Trait("mouth", List.of("x", "y", "z"))
        );
        // 4 × 3 = 12 unique combos, ask for 10 distinct, 4 threads
        var out = new NFTGeneration.GeneratorPart5(42L).generate(10, traits, 4);
        assertEq(10, out.size(), "size == 10");
        assertEq(10, new HashSet<>(out).size(), "all distinct under concurrency");
        for (var nft : out) {
            assertEq(Set.of("eyes", "mouth"), nft.keySet(), "key set");
        }
    }

    // ===== Part 6: 流式生成 ============================================

    static void testPart6() {
        List<NFTGeneration.Trait> traits = List.of(
                new NFTGeneration.Trait("eyes",  List.of("a", "b", "c")),
                new NFTGeneration.Trait("mouth", List.of("x", "y"))
        );
        List<Map<String, String>> seen = new ArrayList<>();
        int produced = new NFTGeneration.GeneratorPart6(7L).streamGenerate(6, traits, nft -> {
            seen.add(nft);
            return true;
        });
        assertEq(6, produced, "streamGenerate returned count");
        assertEq(6, seen.size(), "consumer received all NFTs");
        assertEq(6, new HashSet<>(seen).size(), "all distinct in stream");

        // cancel mid-stream: consumer returns false after 2
        List<Map<String, String>> partial = new ArrayList<>();
        int got = new NFTGeneration.GeneratorPart6(7L).streamGenerate(6, traits, nft -> {
            partial.add(nft);
            return partial.size() < 2;
        });
        assertTrue(got <= 2, "consumer cancel stops generation early, got " + got);
    }

    // ===== Part 7: 限流 ================================================

    static void testPart7() {
        var traits = List.of(
                new NFTGeneration.Trait("eyes", List.of("a", "b", "c"))
        );
        // 限制: 每个窗口最多 3 张, 1 秒窗口
        var policy = new NFTGeneration.RateLimitPolicy(3, 1000);
        var gen = new NFTGeneration.GeneratorPart7(1L, policy);

        var out = gen.generateForUser("alice", 3, traits);
        assertEq(3, out.size(), "alice within budget");

        // 第二次 alice 超额 → throw
        boolean threw = false;
        try {
            gen.generateForUser("alice", 1, traits);
        } catch (NFTGeneration.RateLimitExceededException e) {
            threw = true;
        }
        assertTrue(threw, "alice should be rate-limited");

        // bob 不受影响 (per-user)
        var bob = gen.generateForUser("bob", 2, traits);
        assertEq(2, bob.size(), "bob has own budget");
    }

    // ===== Part 8: 中断续传 ============================================

    static void testPart8() {
        var traits = List.of(
                new NFTGeneration.Trait("eyes",  List.of("a", "b", "c", "d")),
                new NFTGeneration.Trait("mouth", List.of("x", "y", "z"))
        );
        String tmpDir = System.getProperty("java.io.tmpdir") + "/nft-part8-test-" + System.nanoTime();
        var gen = new NFTGeneration.GeneratorPart8(99L, tmpDir);
        String jobId = "job-" + System.nanoTime();
        var first = gen.resumableGenerate(jobId, 5, traits);
        assertEq(5, first.size(), "first run size");

        // 同 jobId 再调一次, 应该从 checkpoint 拿出来 (空增量 or 同样的 5 张)
        var resumed = gen.resumableGenerate(jobId, 5, traits);
        assertEq(5, resumed.size(), "resume returns same size");
        // 比较顺序无关的等价
        assertEq(new HashSet<>(first), new HashSet<>(resumed), "resume reproduces same set");
    }
}
