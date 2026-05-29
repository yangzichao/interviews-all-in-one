import java.util.*;

/**
 * 4-part Coinbase interview practice — NFT Generation.
 *
 * 每个 Part 是独立的 class,后缀 PartN。先无脑独立写,做完再讨论抽公共逻辑。
 *
 * 这不是产品代码,是练习代码 —— 让你能专注当前 Part 而不破坏已完成的部分。
 *
 * 共享数据类型:
 *   Trait          — 普通 trait, 一组等概率 values
 *   WeightedTrait  — Part 4 用, 每个 value 带权重
 *   CappedTrait    — Part 3 用, 每个 value 有用量上限
 */
public class NFTGeneration {

    // 共享数据类型 —— 跨 Part 用 ================================================

    public static record Trait(String type, List<String> values) {}

    public static record WeightedTrait(String type, List<String> values, List<Integer> weights) {}

    public static record CappedTrait(String type, Map<String, Integer> valueCaps) {}

    // ====================================================================
    // PART 1  —  Uniform Random Generation                          [⚠ TODO]
    // ====================================================================
    //
    // 一次 generate(size, traits) 调用 = 造一整批 NFT (size 张),
    // 返回 size 张 NFT 组成的 list. 不是只造一张.
    //
    // 数据形状:
    //   一张 NFT  = Map<String, String>
    //                key   = trait 名字  (如 "eyes")
    //                value = 该 trait 选中的取值 (如 "round")
    //   一批 NFT = List<Map<String,String>>, list 长度 == size
    //
    // 生成规则 (Part 1):
    //   - 对每张 NFT 独立: 对每个 trait 各掷一次骰子 (等概率), 选一个 value
    //   - trait 之间互相独立
    //   - NFT 之间允许重复 (这一 Part 不去重)
    //   - 没有 weight, 没有 cap (后面 Part 才加)
    //
    // 例:
    //   traits = [
    //     Trait("eyes",  ["round","slit","narrow"]),   // 3 个 value, 每个 1/3
    //     Trait("mouth", ["smile","frown"])            // 2 个 value, 每个 1/2
    //   ]
    //   generate(3, traits)
    //     可能返回:
    //       [
    //         {eyes=round,  mouth=frown},   // NFT 0
    //         {eyes=narrow, mouth=smile},   // NFT 1
    //         {eyes=round,  mouth=frown}    // NFT 2  ← 跟 NFT 0 一样, 允许
    //       ]
    //
    // 必须满足:
    //   - 返回 list 长度严格 == size
    //   - 每张 NFT 的 key set == 所有 trait 的 type 集合 (一个不漏)
    //   - 每个 value 必须来自该 trait 的合法 values
    //   - 同 seed + 同输入 → 同输出 (deterministic, 测试要)
    //
    // 边界:
    //   - size == 0  → 返回空 list
    //   - traits 空  → 返回 size 个空 Map

    public static class GeneratorPart1 {
        private final Random random;
        public GeneratorPart1(long seed) {
            random = new Random(seed);
        }

        // 输入: size = 要生成的 NFT 总数; traits = trait 规格列表
        // 输出: size 张 NFT, 每张是 {trait 名 → 选中的 value}
        public List<Map<String, String>> generate(int size, List<Trait> traits) {
            List<Map<String, String>> nfts = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Map<String, String> nft = new HashMap<>();
                for (Trait trait : traits) {
                    String type = trait.type();
                    List<String> values = trait.values();
                    nft.put(type, values.get(random.nextInt(values.size())));
                }
                nfts.add(nft);
            }
            return nfts;
        }
    }

    // ====================================================================
    // PART 2  —  No Duplicates                                      [⚠ TODO]
    // ====================================================================
    //
    // 同 Part 1, 但 NFT 之间必须互不相同 (作为 Map 比较).
    //
    // 不可行 (size 物理上凑不出这么多 distinct NFT) → IllegalArgumentException

    public static class GeneratorPart2 {
        private final Random random;
        public GeneratorPart2(long seed) {
            this.random = new Random(seed);
        }

        public List<Map<String, String>> generate(int size, List<Trait> traits) {
            // size check 
            long combination = 1;
            for (Trait trait : traits) {
                if (combination > size) break;
                combination *= trait.values().size();
            }
            if (combination < size) throw new IllegalArgumentException();

            List<Map<String, String>> nfts = new ArrayList<>();
            Set<String> nftSet = new HashSet<>();
            for (int i = 0; i < size; i++) {
                Map<String, String> nft = new HashMap<>();
                StringBuilder nftSb = new StringBuilder();
                for (Trait trait : traits) {
                    String type = trait.type();
                    List<String> values = trait.values();
                    String value = values.get(random.nextInt(values.size()));
                    nft.put(type, value);
                    nftSb.append(type).append('-').append(value).append(';');
                }
                if (!nftSet.contains(nftSb.toString())) {
                    nfts.add(nft);
                    nftSet.add(nftSb.toString());
                } else {
                    i--;
                }
            }
            return nfts;
        }
    }

    // ====================================================================
    // PART 2-FOLLOWUP  —  Optimized for size ≈ total                [⚠ TODO]
    // ====================================================================
    //
    // 不在面经里 — 这是 Part 2 的延伸算法讨论, 用来练习"退化场景识别 + fallback".
    //
    // 同 Part 2 的 API 和契约, 但必须在 size 接近总组合数时仍然高效.
    //
    // 退化场景:
    //   Part 2 是 rejection sampling, 在 size == total 时
    //   后期每次抽几乎必撞, 期望复杂度退化到 ~total · ln(total).
    //
    // 额外: 总组合数可能 overflow long
    //   (trait 多 / 每个 trait values 多).
    //   这种情况下 size << total, 撞车率几乎为零, 怎么处理你想想.
    //
    // 验收:
    //   - size == total 时仍能在 O(size) 量级跑完
    //   - total overflow 时不抛 ArithmeticException, 不死循环
    //   - 同 seed 同输入 → 同输出 (deterministic)

    public static class GeneratorPart2Followup {
        public GeneratorPart2Followup(long seed) {
            throw new UnsupportedOperationException("GeneratorPart2Followup: not implemented");
        }

        public List<Map<String, String>> generate(int size, List<Trait> traits) {
            throw new UnsupportedOperationException("GeneratorPart2Followup.generate: not implemented");
        }
    }

    // ====================================================================
    // PART 3  —  Per-Value Capacity Caps                            [⚠ TODO]
    // ====================================================================
    //
    // 改用 CappedTrait: 每个 value 有用量上限 (整个 batch 内).
    // NFT 还得互不相同 (沿用 Part 2 的约束).
    //
    //   CappedTrait("eyes", {"common": 80, "rare": 20})
    //     → 整个 batch 内 "rare" 最多出现 20 次
    //
    // 不可行 → IllegalStateException

    public static class GeneratorPart3 {
        private final Random random;
        public GeneratorPart3(long seed) {
            random = new Random(seed);
        }

        public List<Map<String, String>> generate(int size, List<CappedTrait> traits) {
            throw new UnsupportedOperationException("GeneratorPart3.generate: not implemented");
        }
    }

    // ====================================================================
    // PART 4  —  Weighted Probabilities                             [⚠ TODO]
    // ====================================================================
    //
    // 改用 WeightedTrait: 每个 value 带权重 (正整数), 被选中概率 ∝ 权重.
    // NFT 互不相同 (沿用 Part 2). 这一 Part 不加 cap.
    //
    //   WeightedTrait("eyes", ["common","rare"], [9, 1])
    //     → 90% "common", 10% "rare"

    public static class GeneratorPart4 {
        public GeneratorPart4(long seed) {
            throw new UnsupportedOperationException("GeneratorPart4: not implemented");
        }

        public List<Map<String, String>> generate(int size, List<WeightedTrait> traits) {
            throw new UnsupportedOperationException("GeneratorPart4.generate: not implemented");
        }
    }

    // ====================================================================
    // PART 5  —  并发生成 + 全局去重                                 [⚠ TODO]
    // ====================================================================
    //
    // 与 Part 2 比: 同样要 NFT 互不相同, 但允许 numThreads 个 worker 并发产 NFT.
    //
    // API:  generate(size, traits, numThreads) → List<Map<String,String>>
    //
    // 约定:
    //   - size > 总组合数 → IllegalArgumentException
    //   - 并发下结果中 NFT 必须互不相同
    //   - 输出顺序不保证 (set 等价即可, 不强求 list 等价)
    //
    // (取舍 + follow-up 见 README.md / hints.md)

    public static class GeneratorPart5 {
        public GeneratorPart5(long seed) {
            throw new UnsupportedOperationException("GeneratorPart5: not implemented — Part 5 并发生成");
        }

        public List<Map<String, String>> generate(int size, List<Trait> traits, int numThreads) {
            throw new UnsupportedOperationException("GeneratorPart5.generate: not implemented");
        }
    }

    // ====================================================================
    // PART 6  —  流式批量生成 (不爆内存)                              [⚠ TODO]
    // ====================================================================
    //
    // 与 Part 2 比: 同样要互不相同, 但不返回 List, 改成回调把每张推送给 consumer.
    //
    // API:  streamGenerate(size, traits, consumer) → int (实际生成数量)
    //
    // 约定:
    //   - consumer.accept(nft) 返回 true  → 继续下一张
    //   - consumer.accept(nft) 返回 false → 提前结束, 返回当前已生成数量
    //   - 生成器内部最多缓存 O(1) ~ O(window) 个 NFT, 不要全量持有
    //
    // (取舍 + follow-up 见 README.md / hints.md)

    @FunctionalInterface
    public interface NFTConsumer {
        // 返回 false 表示消费方要求停止 (cancel)
        boolean accept(Map<String, String> nft) throws Exception;
    }

    public static class GeneratorPart6 {
        public GeneratorPart6(long seed) {
            throw new UnsupportedOperationException("GeneratorPart6: not implemented — Part 6 流式生成");
        }

        public int streamGenerate(int size, List<Trait> traits, NFTConsumer consumer) {
            throw new UnsupportedOperationException("GeneratorPart6.streamGenerate: not implemented");
        }
    }

    // ====================================================================
    // PART 7  —  限流 / 反作弊 (per-user rate limit)                  [⚠ TODO]
    // ====================================================================
    //
    // 与 Part 1 比: 加 userId 维度限流, 同 userId 在 windowMillis 内累计调用次数受限.
    //
    // API:  generateForUser(userId, size, traits) → List<Map<String,String>>
    //
    // 约定:
    //   - 同 userId 在 policy.windowMillis 内累计 mint > policy.maxNftsPerWindow
    //     → RateLimitExceededException
    //   - 不同 userId 互相独立 (per-user budget)
    //   - 单次 size 本身不会触发 (按跨调用累计算)
    //
    // (取舍 + follow-up 见 README.md / hints.md)

    public static class RateLimitPolicy {
        public final int maxNftsPerWindow;
        public final long windowMillis;
        public RateLimitPolicy(int maxNftsPerWindow, long windowMillis) {
            this.maxNftsPerWindow = maxNftsPerWindow;
            this.windowMillis = windowMillis;
        }
    }

    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String msg) { super(msg); }
    }

    public static class GeneratorPart7 {
        public GeneratorPart7(long seed, RateLimitPolicy policy) {
            throw new UnsupportedOperationException("GeneratorPart7: not implemented — Part 7 限流");
        }

        public List<Map<String, String>> generateForUser(String userId, int size, List<Trait> traits) {
            throw new UnsupportedOperationException("GeneratorPart7.generateForUser: not implemented");
        }
    }

    // ====================================================================
    // PART 8  —  中断续传 (crash recovery)                            [⚠ TODO]
    // ====================================================================
    //
    // 与 Part 2 比: 同样要互不相同, 但长时间 batch 中途崩了, 重启后能 resume.
    //
    // API:  resumableGenerate(jobId, size, traits) → List<Map<String,String>>
    //       同 jobId 第二次调用 = 从 checkpoint 接着继续.
    //
    // 约定:
    //   - 已生成的 NFT 持久化到 checkpointDir/jobId 下
    //   - resume 后总输出 == size 张, 已生成的部分不会重复
    //   - 不同 jobId 互相独立
    //
    // (取舍 + follow-up 见 README.md / hints.md)

    public static class GeneratorPart8 {
        public GeneratorPart8(long seed, String checkpointDir) {
            throw new UnsupportedOperationException("GeneratorPart8: not implemented — Part 8 中断续传");
        }

        public List<Map<String, String>> resumableGenerate(String jobId, int size, List<Trait> traits) {
            throw new UnsupportedOperationException("GeneratorPart8.resumableGenerate: not implemented");
        }

        // 显式触发一次 checkpoint flush
        public void checkpointPart8() {
            throw new UnsupportedOperationException("GeneratorPart8.checkpointPart8: not implemented");
        }
    }
}
