import java.util.*;
import java.util.concurrent.*;

/**
 * ============================================================================
 *  PROBLEM — Design Hit Counter (LeetCode 362, progressive design)
 * ============================================================================
 *
 *  Design a hit counter which counts the number of hits received in the past
 *  5 minutes (i.e. the past 300 seconds). Every call carries a `timestamp`
 *  (in SECONDS granularity) supplied by the caller — you do NOT read a real
 *  clock. For Part 1/2 you may assume calls arrive in chronological order,
 *  i.e. `timestamp` is monotonically non-decreasing (later parts relax this).
 *
 *  The problem is delivered in PARTS. In a real interview you only see the
 *  next part after finishing the current one. Each part adds one capability
 *  (or one nasty real-world twist) on top of the previous part's API.
 *
 *  ----------------------------------------------------------------------
 *   术语 (GLOSSARY) —— 先混个脸熟, 后面注释会用到
 *   ----------------------------------------------------------------------
 *   · hit          : 一次请求 / 一次点击, 发生在某个 timestamp
 *   · timestamp    : 调用方传进来的逻辑时间, 单位是 *秒* (int); 不读真实时钟
 *   · window       : 统计窗口, 默认 "过去 5 分钟" = 300 秒
 *   · 窗口区间      : getHits(t) 统计的是半开区间 (t-300, t] 内的 hit
 *                    —— 含右端 t, 不含左端 t-300。所以 t'=t-300 的 hit 不算,
 *                    t'=t 的 hit 要算。
 *   · 半开区间      : (start, end] —— 不含 start 含 end
 *   · bucket        : 把同一秒内的多次 hit 聚成一个计数 (second -> count)
 *   · 单调非减      : Part 1/2 里 timestamp 只会不变或变大 (后面 Part 才放开)
 *
 *  注: 题面用英文写, 术语表给中文对照; 两边说的是同一件事。
 *
 *  ----------------------------------------------------------------------
 *   PART 1 — basic hit counter
 *   ----------------------------------------------------------------------
 *   hit(timestamp)
 *       Record a hit that happened at `timestamp` (seconds). Several hits may
 *       happen at the same timestamp. Returns nothing.
 *
 *   getHits(timestamp) -> int
 *       Return how many hits happened in the past 5 minutes (300 seconds),
 *       i.e. hits whose timestamp t' satisfies  timestamp - 300 < t' <= timestamp
 *       (the half-open window (timestamp-300, timestamp]).
 *
 *   Example:
 *       hit(1); hit(2); hit(3);
 *       getHits(4)   == 3      // 1,2,3 all inside (-296, 4]
 *       hit(300);
 *       getHits(300) == 4      // 1,2,3,300 all inside (0, 300]
 *       getHits(301) == 3      // hit at 1 falls out of (1, 301]; 2,3,300 stay
 *
 *  ----------------------------------------------------------------------
 *   PART 2 — high hit rate (the canonical LC 362 follow-up)
 *   ----------------------------------------------------------------------
 *   Same hit / getHits API, same observable answers. BUT: assume hits arrive
 *   at a very high rate — potentially thousands per second — for a long time.
 *   Storing one record per individual hit (Part 1's likely approach) wastes
 *   memory and makes getHits slow. Redesign the storage so that:
 *       - memory stays bounded by the WINDOW size, not the number of hits
 *       - hit() and getHits() stay cheap even under a flood of same-second hits
 *
 *   The external behavior must be identical to Part 1 (same example passes).
 *
 * ============================================================================
 *  Follow-ups (Part 3–8) turn the toy into an interview workout — each twist
 *  is something an interviewer actually drills, and each maps to a real-world
 *  scenario:
 *    Part 3 — configurable window + arbitrary range query  (dashboards)
 *    Part 4 — out-of-order / late timestamps               (log aggregation)
 *    Part 5 — concurrency                                  (high-QPS service)
 *    Part 6 — huge window / memory                         (day/week metrics)
 *    Part 7 — distributed / sharded                        (multi-node, HLL)
 *    Part 8 — rate limiter variant                         (API throttling)
 *  Each Part below is scaffolded as a stub (throws UnsupportedOperationException)
 *  with its scenario in the section header; full discussion lives in README.md.
 * ============================================================================
 *
 *  Practice-code conventions in this file:
 *    - Each Part is a public static class HitCounterPartN with clean method
 *      names (hit / getHits) — no suffix weirdness.
 *    - Parts whose schema is unchanged copy the same fields; only a schema
 *      change introduces a new internal representation.
 *    - Stubs throw UnsupportedOperationException — the test runner treats
 *      that as SKIPPED. We do NOT pre-fill the answer: this is practice.
 */
public class HitCounter {

    // ====================================================================
    // PART 1  —  基础 hit / getHits                                [⚠ TODO]
    // ====================================================================
    // 场景: 最朴素的命中计数器。hit(t) 记一次命中; getHits(t) 返回过去 300 秒
    //       (半开区间 (t-300, t]) 内的命中次数。同一秒可以有多次 hit。
    //       Part 1/2 里 timestamp 单调非减 (后面 Part 才放开乱序)。
    //
    //   hit(1); hit(2); hit(3);
    //   getHits(4)   → 3      // 1,2,3 都在 (-296, 4]
    //   hit(300);
    //   getHits(300) → 4      // 1,2,3,300 都在 (0, 300]
    //   getHits(301) → 3      // t'=1 掉出 (1, 301]; 留下 2,3,300
    //
    // 你要写的: hit(t) / getHits(t)。先怎么简单怎么来, 不用管高频。

    public static class HitCounterPart1 {
       ArrayDeque<Integer> deque;
       int interval;
       public HitCounterPart1(int interval) {
         this.deque = new ArrayDeque<>();
         this.interval = interval;
       }


        public void hit(int timestamp) {
            deque.addLast(timestamp);
        }

        public int getHits(int timestamp) {
            while (!deque.isEmpty() && deque.peekFirst() <= timestamp - interval) {
                deque.removeFirst();
            }
            return deque.size();
        }
    }

    // ====================================================================
    // PART 2  —  高频 hit (LC 362 经典 follow-up)                    [⚠ TODO]
    // ====================================================================
    // 场景: 假设 hit 来得非常猛 —— 每秒成千上万次, 持续很久。Part 1 如果每个 hit
    //       存一条记录, 内存会爆, getHits 也会变慢。重新设计存储, 使得:
    //         - 内存只跟 *窗口大小* (300) 有关, 跟总 hit 数无关
    //         - 同一秒灌入海量 hit 时, hit() 和 getHits() 仍然便宜
    //       对外行为跟 Part 1 完全一致 (同样的例子要过)。
    //
    // 与 Part 1 比:
    //   同: 对外 API 和返回值跟 Part 1 一样 (hit / getHits)
    //   变: 内部存储换成跟窗口大小绑定的结构, 而不是逐 hit 存
    //   新: 无新方法 —— 只换内部表示
    //
    // 你要写的: 用 O(窗口) 内存重新实现 hit(t) / getHits(t)。

    public static class HitCounterPart2 {
        int[] buckets;
        int[] lastBucketTime;
        int window;

        public HitCounterPart2(int window) {
            this.buckets = new int[window];
            this.lastBucketTime = new int[window];
            this.window = window;
        }

        public void hit(int timestamp) {
            int bucketIndex = timestamp % window;
            if (lastBucketTime[bucketIndex] == timestamp) {
                buckets[bucketIndex]++;
            } else {
                lastBucketTime[bucketIndex] = timestamp;
                buckets[bucketIndex] = 1;
            }
        }

        public int getHits(int timestamp) {
            int total = 0;
            for (int i = 0; i < window; i++) {
                if (lastBucketTime[i] <= timestamp - window) {
                    continue;
                }
                total += buckets[i];
            }
            return total;
        }
    }

    // ====================================================================
    // PART 3  —  可配置窗口 + 任意区间查询                          [⚠ TODO]
    // ====================================================================
    // 真实场景: 监控大盘 (dashboard) 不只问"最近 5 分钟", 还要画"昨天 14:00–15:00
    //          这一小时的曲线"。窗口大小要可配, 还要支持任意 [start, end] 查询。
    //
    // 场景: 构造时给一个窗口大小 W (秒); 单参 getHits(t) 统计 (t-W, t]; 再新增
    //       一个双参 getHits(start, end) 统计任意闭区间 [start, end] (含两端)。
    //       注意 (t-W, t] 在整数秒上 == [t-W+1, t], 两个重载语义要自洽。
    //
    //   c = new HitCounterPart3(300)
    //   hit(1); hit(2); hit(3); hit(300);
    //   getHits(300)      → 4    // (0, 300]
    //   getHits(2, 300)   → 3    // [2,300] 含两端, 排除 t'=1
    //   getHits(1, 2)     → 2    // [1,2]
    //
    // 与 Part 2 比:
    //   同: 单参 getHits(t) 的窗口语义一致 (半开 (t-W, t])
    //   变: 窗口不再写死 300; 任意区间查询意味着 *不能* 只保留最近一窗的数据
    //   新: 构造函数 HitCounterPart3(int windowSeconds)
    //       getHits(int start, int end) 重载
    //
    // 你要写的: 构造函数 + hit + getHits(t) + getHits(start, end)。
    //           想清楚: 任意区间查询下, 老数据还能不能丢? 用什么结构做区间求和?

    public static class HitCounterPart3 {

        private final TreeMap<Integer, Integer> hitsBySecond;
        private final int windowSeconds;

        public HitCounterPart3(int windowSeconds) {
            this.windowSeconds = windowSeconds;
            this.hitsBySecond = new TreeMap<>();
        }

        public void hit(int timestamp) {
            hitsBySecond.merge(timestamp, 1, Integer::sum);
        }

        // single-arg: 窗口 (t-W, t] 在整数秒上 == [t-W+1, t]，直接委托给区间版，
        // 这样只有一份真正的实现，两个重载天然自洽。
        public int getHits(int timestamp) {
            return getHits(timestamp - windowSeconds + 1, timestamp);
        }

        // 任意闭区间 [start, end]：不能用固定 W 槽环形数组，因为旧秒会被新秒覆盖。
        // TreeMap 按绝对 timestamp 聚合，查询时只扫区间内实际出现过 hit 的秒。
        public int getHits(int start, int end) {
            if (start > end) {
                return 0;
            }
            int total = 0;
            for (int hitsAtSecond : hitsBySecond.subMap(start, true, end, true).values()) {
                total += hitsAtSecond;
            }
            return total;
        }
    }

    // ====================================================================
    // PART 4  —  乱序 / 迟到时间戳                                  [⚠ TODO]
    // ====================================================================
    // 真实场景: 命中来自多台机器 / 多个数据源汇聚, 网络抖动让事件迟到 —— hit 的
    //          timestamp 可能比上一条 *更小*。这下"按当前时间索引的环形数组"直接废了。
    //
    // 场景: hit(t) 的 t 不再保证单调非减, 可能乱序到达 (甚至晚到补录历史)。
    //       getHits(asOf) 仍然统计 (asOf-W, asOf] 内的命中 —— 但因为可能有迟到,
    //       同一个 asOf 在不同时刻问可能得到不同答案 (后补的历史会被算进去)。
    //
    //   c = new HitCounterPart4(300)
    //   hit(100); hit(50); hit(200); hit(150);   // 乱序
    //   getHits(120) → 2     // (-180,120] 里只有 100,50
    //   getHits(199) → 3     // (-101,199] 里有 100,50,150 (200 在外)
    //   hit(10);             // 迟到补录一条历史
    //   getHits(120) → 3     // 现在 (-180,120] 里多了 t'=10
    //
    // 与 Part 3 比:
    //   同: 窗口语义、可配置 W 都不变
    //   变: 放弃"timestamp 单调"假设; 不能再用"以 now 为基准的环形数组"
    //   新: 无新方法 —— 但内部结构要能容纳任意时间点 + 高效区间求和 + 老数据回收
    //
    // 你要写的: 能容纳乱序时间戳的 hit + getHits(asOf)。
    //           想清楚: 用什么结构? 老到永远不会再被查询的数据怎么回收?

    public static class HitCounterPart4 {

        private final TreeMap<Integer, Integer> hitsBySecond;
        private final int windowSeconds;

        public HitCounterPart4(int windowSeconds) {
            this.windowSeconds = windowSeconds;
            this.hitsBySecond = new TreeMap<>();
        }

        public void hit(int timestamp) {
            hitsBySecond.merge(timestamp, 1, Integer::sum);
        }

        public int getHits(int asOf) {
            int total = 0;
            int startExclusive = asOf - windowSeconds;
            for (int hitsAtSecond : hitsBySecond.subMap(startExclusive, false, asOf, true).values()) {
                total += hitsAtSecond;
            }
            return total;
        }
    }

    // ====================================================================
    // PART 5  —  并发 (thread-safety)                              [⚠ TODO]
    // ====================================================================
    // 真实场景: 高 QPS 服务里, 几十个工作线程同时把命中打进同一个计数器, 另有线程
    //          在读 getHits 上报监控。非原子的 ++ 会丢更新, 读到的也可能是撕裂值。
    //
    // 场景: N 个线程并发 hit, 一些线程并发 getHits。要求:
    //       (a) 同一秒的高并发自增不能丢 (no lost updates)
    //       (b) getHits 读到的是某个一致状态, 不是加到一半的脏值
    //       (c) 高频 hit 不应被低频 getHits 长时间阻塞 (反之亦然)
    //
    // 与 Part 2 比:
    //   同: 对外 API/语义跟前面一致 (这里用回固定窗口即可)
    //   变: 多线程同时调用; 所有方法必须线程安全
    //   新: 无新方法 —— 换并发策略 (原子计数 / 分段锁 / 读写锁 / 无锁结构)
    //
    // 面试要讨论的取舍 (Coinbase 必问方向, 见 README):
    //   一把大锁 vs ReadWriteLock vs 每桶 AtomicLong/LongAdder vs 分段锁。
    //   经典追问: "热点 bucket (同一秒所有线程都在加) 怎么减少竞争?" → LongAdder。
    //
    // 你要写的: 线程安全的 hit + getHits。先正确, 再谈减少竞争。

    public static class HitCounterPart5 {
        public HitCounterPart5(int windowSeconds) {
            throw new UnsupportedOperationException("TODO: Part 5 — make it thread-safe");
        }

        public void hit(int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 5");
        }

        public int getHits(int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 5");
        }
    }

    // ====================================================================
    // PART 6  —  超长窗口 / 省内存 (分层 rollup)                    [⚠ TODO]
    // ====================================================================
    // 真实场景: 现在要的不是"过去 5 分钟", 而是"过去一天 / 一周"的命中。秒级桶存
    //          一天就是 86400 个、一周 604800 个 —— 还要乘以 key 数量。内存吃不消。
    //
    // 场景: 窗口很大。用可配置 *粒度* (bucketSeconds) 把命中聚成粗桶, 用精度换内存:
    //       同一个粗桶内的 hit 只存一个计数, getHits 在桶边界上是精确的, 桶内是近似的。
    //       构造: HitCounterPart6(windowSeconds, bucketSeconds)。
    //
    //   c = new HitCounterPart6(3600, 60)   // 1 小时窗口, 1 分钟粒度 (60 个桶)
    //   hit(0); hit(60); for 100 次 hit(180);
    //   getHits(240) → 102    // 0 + 60 + 180(×100), 三个桶都整体落在窗口里
    //                         // 注: 窗口边缘落在桶中间时是近似的, 见 README 边界讨论
    //
    // 与 Part 2 比:
    //   同: 单参 getHits(t) 的窗口语义 (t-W, t]
    //   变: 桶不再是 1 秒一个, 而是 bucketSeconds 一个; getHits 在桶内近似
    //   新: 构造函数 HitCounterPart6(int windowSeconds, int bucketSeconds)
    //
    // 面试讨论 (见 README): 定粒度桶 vs 分层 rollup (秒→分→时) vs 时间衰减/指数衰减。
    //
    // 你要写的: 粗粒度桶的 hit + getHits。想清楚边界上的近似误差有多大、可否接受。

    public static class HitCounterPart6 {
        public HitCounterPart6(int windowSeconds, int bucketSeconds) {
            throw new UnsupportedOperationException("TODO: Part 6 — coarse buckets / rollup");
        }

        public void hit(int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 6");
        }

        public int getHits(int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 6 — approximate within a bucket");
        }
    }

    // ====================================================================
    // PART 7  —  分布式 / 分片 (sharded)                           [⚠ TODO]
    // ====================================================================
    // 真实场景: 单机扛不住命中量, 按 key (用户/接口) hash 分散到 N 个分片; 查询时
    //          scatter 到所有分片再 gather 求和。若问的是"独立用户数"而非"命中数",
    //          就引出近似去重 (HyperLogLog)。
    //
    // 场景: hit(key, t) 按 hash(key) 路由到某个分片的子计数器; getHits(t) 汇总所有
    //       分片在 (t-W, t] 的命中数。
    //
    //   c = new ShardedHitCounter(4, 300)
    //   hit("user-a", 1); hit("user-b", 1); hit("user-a", 2);
    //   getHits(2) → 3     // 跨分片合并
    //
    // 与 Part 5 比:
    //   同: 每个分片内部就是前面那套计数器 (可复用 Part 2/5)
    //   变: 多了一层路由; 读要 scatter/gather; 跨分片一致性/时钟偏移要讨论
    //   新: hit(String key, int timestamp); 内部 N 个子计数器 + 路由
    //
    // 面试讨论 (见 README): 哈希分片 vs 一致性哈希; 跨分片快照; 命中数(可加)
    //   vs 独立用户数(需 HLL 这类可合并的近似去重 sketch)。
    //
    // 你要写的: 路由 + 跨分片 getHits 求和。HLL 去重是讨论题, 不强求写。

    public static class ShardedHitCounter {
        public ShardedHitCounter(int numShards, int windowSeconds) {
            throw new UnsupportedOperationException("TODO: Part 7 — init N shards + router");
        }

        public void hit(String key, int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 7 — route by hash(key)");
        }

        public int getHits(int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 7 — scatter/gather sum");
        }
    }

    // ====================================================================
    // PART 8  —  限流变体 (sliding-window rate limiter)             [⚠ TODO]
    // ====================================================================
    // 真实场景: 把"数命中"反过来用 —— API 网关限流。"每个用户每 10 秒最多 3 次请求",
    //          超了就拒。这正是 hit counter 的孪生兄弟: 滑动窗口限流器。
    //
    // 场景: 构造 RateLimiterPart8(windowSeconds, maxHits)。allow(t) 在记录这次请求
    //       *之前* 先看 (t-W, t] 内已有多少次: 没到上限就接受 (计入并返回 true),
    //       到上限就拒绝 (返回 false, 且 *不* 计入 —— 被拒的请求不占额度)。
    //
    //   rl = new RateLimiterPart8(10, 3)    // 每 10 秒最多 3 次
    //   allow(1)=true  allow(1)=true  allow(1)=true   // 前 3 次放行
    //   allow(1)=false                                 // 第 4 次超限被拒
    //   allow(5)=false                                 // (-5,5] 里仍有那 3 次
    //   allow(11)=true                                 // (1,11] 里 t'=1 已掉出 → 放行
    //
    // 与 Part 2 比:
    //   同: 底层还是"窗口内计数", 可复用 Part 2 的桶
    //   变: 不再返回计数, 而是返回"是否放行"的布尔决策; 被拒不计入
    //   新: 构造函数带 maxHits; allow(int timestamp) -> boolean
    //
    // 面试讨论 (见 README): sliding-window log vs sliding-window counter vs
    //   token bucket vs leaky bucket; 固定窗口的边界突刺问题; 被拒请求是否计入。
    //
    // 你要写的: allow(t) 的判定 + 记账。注意"先判定后记账"和"被拒不占额度"。

    public static class RateLimiterPart8 {
        public RateLimiterPart8(int windowSeconds, int maxHits) {
            throw new UnsupportedOperationException("TODO: Part 8 — sliding-window rate limiter");
        }

        public boolean allow(int timestamp) {
            throw new UnsupportedOperationException("TODO: Part 8 — accept iff under limit in (t-W, t]");
        }
    }
}
