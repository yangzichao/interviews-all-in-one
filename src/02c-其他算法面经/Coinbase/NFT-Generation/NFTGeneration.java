import java.util.*;

/**
 * Coinbase interview practice — NFT Generation  (8 parts).
 *
 * ════════════════════════════════════════════════════════════════════════
 *  背景故事 (BACKGROUND) —— 读这里就够入手, 不需要懂区块链
 * ════════════════════════════════════════════════════════════════════════
 *
 *  你可能见过那种 "10000 张头像" 的 NFT 系列 (CryptoPunks、Bored Ape 之类):
 *  每张头像由几个 "特征 (trait)" 拼出来 —— 眼睛 (eyes)、嘴 (mouth)、背景……
 *  每个特征可以取几个不同的 "值 (value)": 眼睛可以是 round / slit / narrow,
 *  嘴可以是 smile / frown。把每个特征各挑一个值组合起来, 就是一张独一无二的
 *  头像。这道题就是写这个 "批量生成器": 一次造出 size 张 NFT。
 *
 *  数据形状 (全题统一, 务必先看清):
 *      一张 NFT  = Map<String,String>      key=特征名, value=该特征选中的值
 *                  例如 {eyes=round, mouth=smile}
 *      一批 NFT = List<Map<String,String>>, 长度 == size
 *
 *  题目层层加码: 先等概率随机生成 (Part 1), 再要求批内互不重复 (Part 2),
 *  然后给每个值加 "用量上限" cap (Part 3)、加 "出现概率权重" weight (Part 4),
 *  后面几个 Part 升级到并发生成、流式不爆内存、按用户限流、崩溃续传等工程问题。
 *
 *  几个术语 (后面注释会用到, 先混个脸熟):
 *    · trait (特征) : NFT 的一个维度, 如 "eyes"; 带一组候选 value
 *    · value (取值) : 某个 trait 选中的具体值, 如 "round"
 *    · 一张 NFT     : 每个 trait 各选一个 value 的组合 = 一个 Map
 *    · distinct     : 两张 NFT 作为 Map 相等就算重复; Part 2 起要求互不相同
 *    · 总组合数      : 各 trait 的 value 个数相乘 = 最多能造出多少张 distinct NFT
 *    · cap (上限)    : 某个 value 在整批里最多能用多少次 (Part 3)
 *    · weight (权重) : 某个 value 被选中的相对概率 (Part 4)
 *    · seed (种子)   : 随机数种子; 同 seed + 同输入 → 同输出 (deterministic, 便于测试)
 *
 * ════════════════════════════════════════════════════════════════════════
 *
 * 每个 Part 是独立的 class,后缀 PartN。先无脑独立写,做完再讨论抽公共逻辑。
 *
 * 这不是产品代码,是练习代码 —— 让你能专注当前 Part 而不破坏已完成的部分。
 *
 * 共享数据类型:
 *   Trait          — 普通 trait, 一组等概率 values
 *   WeightedTrait  — Part 4 用, 每个 value 带权重 (values 和 weights 下标一一对应)
 *   CappedTrait    — Part 3 用, 每个 value 有用量上限 (valueCaps: value → 最多用几次)
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
    // 场景: 跟 Part 1 一样批量生成, 但这一批 NFT 必须互不相同 —— 两张 NFT 作为
    //       Map 完全相等就算重复, 不允许出现。同时要先判断 "physically 能不能凑够":
    //       所有 trait 的 value 个数相乘 = 总组合数, 如果 size 比它还大, 根本造不
    //       出这么多 distinct, 直接拒绝。
    //
    // 接口: generate(size, traits) -> List<Map<String,String>>, 长度 == size 且元素互异。
    //
    //   traits = [a∈{x,y}, b∈{p,q}]  (总组合数 2*2 = 4)
    //   generate(4, traits) → 4 张互不相同的 NFT (恰好穷尽全部 4 种组合)
    //   generate(5, traits) → IllegalArgumentException  (5 > 4, 凑不出)
    //
    //   traits = [eyes∈{round,slit,narrow}, mouth∈{smile,frown}]  (总组合数 6)
    //   generate(5, traits) → 5 张互不相同的合法 NFT
    //
    // 注意 / 边界:
    //   - size > 总组合数 → 抛 IllegalArgumentException
    //   - 同 seed + 同输入 → 同输出

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
    // 场景: 跟 Part 2 完全相同的需求 (生成 size 张互不相同的 NFT), 但要扛住两个
    //       极端: ① size 几乎等于总组合数时仍然要快; ② 总组合数大到超过 long 也
    //       不能崩。这是 Part 2 的延伸 (不在原始面经里), 练 "退化场景识别 + fallback"。
    //
    // 不在面经里 — 这是 Part 2 的延伸算法讨论, 用来练习"退化场景识别 + fallback".
    //
    // 同 Part 2 的 API 和契约, 但必须在 size 接近总组合数时仍然高效.
    //
    //   8 个 trait 各 2 个 value (总组合 256), generate(256, …) → 256 张全异, 要快 (<500ms)
    //   2*2 的小空间, generate(5, …) → IllegalArgumentException  (同 Part 2)
    //   64 个 trait 各 2 个 value (总组合 2^64 > long 上限), generate(100, …)
    //       → 100 张全异, 不能因为算总数溢出而崩或死循环
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
    // 场景: 真实 NFT 项目要控制 "稀有度" —— 某些值只能出现很少次 (越稀有越值钱)。
    //       改用 CappedTrait: 每个 value 带一个 cap, 表示它在整批里最多被用几次。
    //       NFT 仍要互不相同 (沿用 Part 2)。生成时既要满足 distinct, 又不能让任何
    //       value 超过它的 cap。
    //
    // 接口: generate(size, List<CappedTrait>) -> List<Map<String,String>>。
    //       CappedTrait.valueCaps 是 value → 最多用几次 的 map。
    //
    //   CappedTrait("eyes", {common:4, rare:1}), CappedTrait("mouth", {smile:5, frown:5})
    //     generate(4, …) → 4 张全异, 且整批里 "rare" 出现次数 ≤ 1
    //     generate(5, …) → 抛异常 (eyes×mouth 只有 2*2=4 种 distinct, 凑不出 5)
    //
    //   CappedTrait("eyes", {a:2}), CappedTrait("mouth", {x:2})
    //     generate(3, …) → 抛异常 (eyes 只有 a 一个值, distinct 组合上限是 1*1=1)
    //
    // 注意 / 边界:
    //   - 不可行 (distinct 不够 / cap 总量撑不起 size) → 抛 IllegalStateException
    //     (测试对 IllegalStateException 或 IllegalArgumentException 都接受)

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
    // 场景: 跟 Part 3 不同的稀有度做法 —— 不设硬上限, 而是给每个 value 一个 "权重"
    //       (正整数), 被选中的概率正比于权重。权重大的常见, 权重小的稀有。
    //       NFT 仍要互不相同 (沿用 Part 2), 这一 Part 不加 cap。
    //
    // 接口: generate(size, List<WeightedTrait>) -> List<Map<String,String>>。
    //       WeightedTrait 的 values 和 weights 下标一一对应。
    //
    //   WeightedTrait("eyes", [common, rare], [9, 1])
    //     → 选中 common 的概率 ≈ 90%, rare ≈ 10% (9 : 1)
    //
    //   WeightedTrait("eyes",[a,b,c],[1,1,1]) + WeightedTrait("mouth",[x,y],[2,1])
    //     generate(5, …) → 5 张全异的合法 NFT (key 集合 = {eyes,mouth}, 值在各自候选里)
    //
    // 注意 / 边界:
    //   - 概率正比于权重, 不是等概率
    //   - 仍要 distinct; 同 seed + 同输入 → 同输出

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
    // 场景: 量大了, 单线程造太慢, 开 numThreads 个 worker 并行造。难点是 "全局
    //       去重" —— 多个线程同时产 NFT, 必须保证最终结果里没有两张一样的, 即去重
    //       是跨线程的 (不能各管各的)。
    //
    // 与 Part 2 比: 同样要 NFT 互不相同, 但允许 numThreads 个 worker 并发产 NFT.
    //
    // API:  generate(size, traits, numThreads) → List<Map<String,String>>
    //
    //   traits = [eyes∈{a,b,c,d}, mouth∈{x,y,z}]  (总组合 4*3 = 12)
    //   generate(10, traits, 4) → 10 张互不相同的合法 NFT (4 个线程并发产出)
    //
    // 约定:
    //   - size > 总组合数 → IllegalArgumentException
    //   - 并发下结果中 NFT 必须互不相同
    //   - 输出顺序不保证 (set 等价即可, 不强求 list 顺序)
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
    // 场景: 要造的量可能极大, 全攒在一个 List 里会爆内存。改成 "流式" —— 每造出
    //       一张就立刻通过回调 (consumer) 推给下游, 自己不持有全量。consumer 还能
    //       中途喊停 (返回 false), 这时提前结束并返回到目前为止已生成的数量。
    //
    // 与 Part 2 比: 同样要互不相同, 但不返回 List, 改成回调把每张推送给 consumer.
    //
    // API:  streamGenerate(size, traits, consumer) → int (实际生成数量)
    //
    //   traits = [eyes∈{a,b,c}, mouth∈{x,y}]  (总组合 6)
    //   streamGenerate(6, traits, nft -> {收集; return true;})
    //       → 返回 6; consumer 收到 6 张互不相同的 NFT
    //   streamGenerate(6, traits, nft -> {收集; return 已收<2;})   // 收到第 2 张后喊停
    //       → 返回值 ≤ 2 (提前取消)
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
    // 场景: 防止某个用户一次性 mint 光所有库存 / 刷量, 给每个用户加配额: 在一个
    //       时间窗口 (windowMillis) 内, 累计 mint 数量不能超过 maxNftsPerWindow。
    //       配额是 per-user 的 —— 一个用户超额不影响别人。
    //
    // 与 Part 1 比: 加 userId 维度限流, 同 userId 在 windowMillis 内累计调用次数受限.
    //
    // API:  generateForUser(userId, size, traits) → List<Map<String,String>>
    //
    //   policy = (maxNftsPerWindow=3, windowMillis=1000)
    //   generateForUser("alice", 3, …) → 3 张 (alice 用满配额)
    //   generateForUser("alice", 1, …) → RateLimitExceededException (alice 已超额)
    //   generateForUser("bob",   2, …) → 2 张 (bob 自己的配额, 不受 alice 影响)
    //
    // 约定:
    //   - 同 userId 在 policy.windowMillis 内累计 mint > policy.maxNftsPerWindow
    //     → RateLimitExceededException
    //   - 不同 userId 互相独立 (per-user budget)
    //   - 按跨调用累计算 (多次调用的数量要叠加到同一窗口预算上)
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
    // 场景: 一个生成几万张的大 job 跑很久, 中途机器崩了, 不想从头再来。把已生成
    //       的部分持久化 (checkpoint 到磁盘), 用同一个 jobId 再次调用时, 从断点
    //       接着补齐到 size 张, 已经造好的那些不重复也不丢。
    //
    // 与 Part 2 比: 同样要互不相同, 但长时间 batch 中途崩了, 重启后能 resume.
    //
    // API:  resumableGenerate(jobId, size, traits) → List<Map<String,String>>
    //       同 jobId 第二次调用 = 从 checkpoint 接着继续.
    //
    //   gen = new GeneratorPart8(seed, checkpointDir)
    //   resumableGenerate("job-1", 5, traits) → 5 张
    //   resumableGenerate("job-1", 5, traits) → 仍 5 张, 且作为 set 跟第一次完全相同
    //                                            (从 checkpoint 拿回, 不重新造)
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
