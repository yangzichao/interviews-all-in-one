import java.util.*;

import org.w3c.dom.Node;

/**
 * Coinbase interview practice — Currency Exchange  (8 parts).
 *
 * ════════════════════════════════════════════════════════════════════════
 *  背景故事 (BACKGROUND) —— 读这里就够入手, 不需要懂外汇/图论
 * ════════════════════════════════════════════════════════════════════════
 *
 *  在一个交易平台 (比如 Coinbase) 上, 用户手里有各种货币: 美元 (USD)、欧元
 *  (EUR)、英镑 (GBP)、日元 (JPY)…… 平台维护着两两之间的 "汇率 (rate)":
 *  比如 "1 USD 能换 0.9 EUR" 就记成 USD→EUR = 0.9。用户想知道:
 *  "我手里 100 USD, 能换成多少 GBP?"
 *
 *  把货币看成一张图: 每种货币是一个 "节点", 每条汇率是一条 "边"。换汇就是
 *  从起点货币沿着边走到终点货币, 一路把金额乘上每条边的 rate。
 *
 *      USD ──0.9──▶ EUR ──0.85──▶ GBP
 *      100 USD → 100*0.9=90 EUR → 90*0.85=76.5 GBP
 *
 *  这道题层层加码: 先做最朴素的换算 (Part 1), 再找 "对用户最划算" 的路径
 *  (Part 2), 然后汇率不再是一次性给定而是实时流式更新 (Part 3), 再到检测
 *  "套利机会" (Part 4), 后面几个 Part 升级到并发、预计算、多交易所报价带
 *  过期时间、历史回查等真实工程问题。
 *
 *  几个术语 (后面注释会用到, 先混个脸熟):
 *    · currency   : 一种货币, 用字符串代号表示 ("USD" / "EUR" / ...)
 *    · rate       : 汇率, from→to=r 表示 "1 单位 from 换 r 单位 to"
 *    · 双向边      : 给了 from→to=r, 就隐含反向 to→from=1/r (本题贯穿的约定)
 *    · convert    : 把一笔金额从 from 货币换成 to 货币 (沿路径连乘 rate)
 *    · best rate  : 所有可达路径里乘积最大的那条 (对用户最有利)
 *    · arbitrage  : 套利 —— 存在一个环, 沿环走一圈乘积 > 1, 凭空变多钱
 *    · stale      : 报价过期 —— 超过 TTL 没有新报价, 不可再信任 (Part 7)
 *
 * ════════════════════════════════════════════════════════════════════════
 *
 * 每个 Part 是独立的 class，后缀 PartN。先无脑独立写，做完再讨论抽公共逻辑。
 *
 * 这不是产品代码，是练习代码 —— 让你能专注当前 Part 而不破坏已完成的部分。
 *
 * (注: Part 1/2 的 class 体内残留了一些个人解题笔记, 那是上一次练习的草稿, 不是
 *  题面的一部分; 真正的题面是每个 Part 上方的 ═══ 注释块。)
 */
public class CurrencyExchange {

    // ====================================================================
    // PART 1  —  Basic Conversion                                   [⚠ TODO]
    // ====================================================================
    // 场景: 最基础的换汇 —— 给你一组汇率, 用户问 "from 货币的 amount, 换成 to
    //       货币是多少?" 你要找出一条从 from 到 to 的可达路径, 沿路把金额连乘
    //       每条边的 rate, 返回结果。中转几跳都行, 只要走得通。
    //
    // 输入: 构造时传入 List<String[]>, 每条是 ["from", "to", "rateStr"]
    //       (rate 是字符串, 需自行解析成 double)。
    // 接口: convert(from, to, amount) -> double。
    //
    //   rates = [["USD","EUR","0.9"], ["EUR","GBP","0.85"]]
    //   convert("USD","GBP",100) → 76.5   (USD→EUR→GBP: 100*0.9*0.85)
    //   convert("USD","EUR",100) → 90.0   (直连边)
    //   convert("EUR","USD",100) → 111.11…(反向边 = 1/0.9)
    //   convert("USD","USD",50)  → 50     (同币种恒等, 即使不在图里也成立)
    //
    // 注意 / 边界:
    //   - rate 双向: 给了 USD→EUR=0.9 就隐含 EUR→USD=1/0.9
    //   - from == to: 直接返 amount, 不要求该币种出现在图里
    //   - 未知币种 (from 或 to 不在图里) → 抛 NoSuchElementException
    //   - 两个货币在图里但不连通 (不同连通分量) → 抛 NoSuchElementException
    //   - 图可能有环, 别绕死循环

    public static class ConverterPart1 {
        private Map<String, Map<String, Double>> graph;
        public ConverterPart1(List<String[]> rates) {
            // step 1 build graph
            // 1.1 note we need bi directional edge, like A -> B and B -> A
            // 1.2 note be careful of cyclic paths
            this.graph = new HashMap<>();
            for (String[] rate : rates) {
                String from = rate[0];
                String to = rate[1];
                Double val = Double.valueOf(rate[2]);
                graph.putIfAbsent(from, new HashMap<>());
                graph.putIfAbsent(to, new HashMap<>());
                graph.get(from).put(to, val);
                graph.get(to).put(from, 1.0 / val);
            }
        }

        public double convert(String from, String to, double amount) {
            // query is basically doing a search, DFS might be better this case. 
            // 
            if (from.equals(to)) return amount;
            if (!graph.containsKey(from) || !graph.containsKey(to)) {
                throw new NoSuchElementException("No such currency");
            }
            boolean canConvert = false;
            Set<String> visited = new HashSet<>();
            double total = amount;


            ArrayDeque<String> nodeStack = new ArrayDeque<>();
            ArrayDeque<Double> valStack = new ArrayDeque<>();
            nodeStack.addFirst(from);
            valStack.addFirst(amount);
            visited.add(from);
            while (!nodeStack.isEmpty()) {
                String curNode = nodeStack.removeFirst();
                Double curVal = valStack.removeFirst();
                if (curNode.equals(to)) {
                    canConvert = true;
                    total = curVal;
                    break;
                }
                for (Map.Entry<String, Double> entry : graph.get(curNode).entrySet()) {
                    String nextNode = entry.getKey();
                    Double nextVal = entry.getValue() * curVal;
                    if (visited.contains(nextNode)) continue;
                    visited.add(nextNode);
                    nodeStack.addFirst(nextNode);
                    valStack.addFirst(nextVal);
                }
            }
            if (!canConvert) {
                throw new NoSuchElementException("No such currency");
            }
            return total;
        }
    }

    // ====================================================================
    // PART 2  —  Best Rate                                          [⚠ TODO]
    // ====================================================================
    // 场景: Part 1 随便找一条可达路径就行; 这一 Part 要找 "对用户最有利" 的那条 ——
    //       即所有从 from 到 to 的路径里, rate 连乘积最大的一条。同样起终点可能
    //       有多条路 (直连一条、绕一圈中转一条), 取乘积最大的结果返回。
    //
    // 接口: convertBest(from, to, amount) -> double。双向边规则同 Part 1。
    //
    //   rates = [["USD","EUR","0.9"], ["EUR","GBP","0.85"], ["USD","GBP","0.7"]]
    //   convertBest("USD","GBP",100) → 76.5
    //         (中转 0.9*0.85=0.765 比直连 0.7 划算, 选中转)
    //
    //   rates = [["USD","EUR","0.5"], ["EUR","GBP","0.5"], ["USD","GBP","0.9"]]
    //   convertBest("USD","GBP",100) → 90.0
    //         (这次直连 0.9 比中转 0.25 划算, 选直连)
    //
    // 注意 / 边界:
    //   - from == to: 恒等, 返 amount
    //   - 不可达 / 未知币种 → 抛 NoSuchElementException
    //   - 所有 rate > 0; 图可能有环, 别死循环

    public static class BestRateConverterPart2 {
        private Map<String, Map<String, Double>> graph;
        public BestRateConverterPart2(List<String[]> rates) {
            this.graph = new HashMap<>();
            for (String[] rate : rates) {
                String from = rate[0];
                String to = rate[1];
                Double val = Double.valueOf(rate[2]);
                graph.putIfAbsent(from, new HashMap<>());
                graph.putIfAbsent(to, new HashMap<>());
                graph.get(from).put(to, val);
                graph.get(to).put(from, 1.0 / val);
            }
        }

        public double convertBest(String from, String to, double amount) {
            Map<String, Double> bestInverse = new HashMap<>();
            ArrayDeque<String> nodeQueue = new ArrayDeque<>();
            ArrayDeque<Double> valQueue = new ArrayDeque<>();

            nodeQueue.offer(to);
            valQueue.offer(amount);

            
        }
    }

    // ====================================================================
    // PART 3  —  Streaming Updates                                  [⚠ TODO]
    // ====================================================================
    // 场景: 真实行情是实时变的 —— 构造时没有任何初始汇率, 汇率一条条地流式推进来,
    //       你边收边建图; 随时可能有 convert 查询打进来。convert 的语义跟 Part 1
    //       完全一样 (任意可达路径), 只是图是动态长出来的。
    //
    // 接口: update(from, to, rate) 新增或覆盖一条边; convert(from, to, amount) 同 Part 1。
    //
    //   c.update("USD","EUR",0.9); c.update("EUR","GBP",0.85);
    //   c.convert("USD","GBP",100) → 76.5
    //   c.update("USD","EUR",1.0);            // 覆盖旧的 0.9
    //   c.convert("USD","GBP",100) → 85.0     // 用新 rate 重算
    //   c.convert("EUR","USD",100) → 100.0    // 反向边跟着用 1/新rate
    //   c.update("GBP","JPY",150.0);          // 新增边, 把新货币连进来
    //   c.convert("USD","JPY",100) → 12750.0  // 100*1.0*0.85*150
    //
    // 注意 / 边界:
    //   - update 既能新增边, 也能覆盖已有边 (同一对 (A,B) 再 update 取最新值)
    //   - 双向规则不变: update(A,B,r) 隐含 B→A = 1/r
    //   - 未知币种 / 不可达 → 抛 NoSuchElementException
    //   - from == to 恒等, 即使图是空的也成立: empty.convert("USD","USD",7) → 7

    public static class StreamingConverterPart3 {
        public StreamingConverterPart3() {
            throw new UnsupportedOperationException("StreamingConverterPart3: not implemented");
        }

        public void update(String from, String to, double rate) {
            throw new UnsupportedOperationException("StreamingConverterPart3.update: not implemented");
        }

        public double convert(String from, String to, double amount) {
            throw new UnsupportedOperationException("StreamingConverterPart3.convert: not implemented");
        }
    }

    // ====================================================================
    // PART 4  —  Arbitrage Detection                                [⚠ TODO]
    // ====================================================================
    // 场景: "套利 (arbitrage)" 是指存在一个换汇环, 沿环走一圈回到起点货币, 金额
    //       反而变多了 —— 也就是环上所有 rate 的乘积 > 1。这意味着你可以空手套利。
    //       这一 Part 只问一个 yes/no: 当前这张汇率图里, 存不存在这样的环?
    //
    // 接口: 构造时给定全部 rates; hasArbitrage() -> boolean。
    //
    //   rates = [["USD","EUR","0.9"], ["EUR","GBP","0.85"], ["GBP","USD","1.4"]]
    //   hasArbitrage() → true   (0.9 * 0.85 * 1.4 = 1.071 > 1)
    //
    //   rates = [["A","B","2.0"], ["B","C","3.0"], ["C","A","1/6"]]
    //   hasArbitrage() → false  (2 * 3 * (1/6) = 1, 恰好等于 1 不算套利)
    //
    //   rates = [["USD","EUR","0.9"], ["EUR","GBP","0.85"]]
    //   hasArbitrage() → false  (没有环)
    //
    // 注意 / 边界:
    //   - 乘积必须严格 > 1 才算套利; 恰好 == 1 不算
    //   - 双向边 A→B→A 的乘积永远是 1 (因为 B→A=1/(A→B)), 不算套利;
    //     真正的套利环长度 ≥ 3
    //   - 空图 → false

    public static class ArbitrageDetectorPart4 {
        public ArbitrageDetectorPart4(List<String[]> rates) {
            throw new UnsupportedOperationException("ArbitrageDetectorPart4: not implemented");
        }

        public boolean hasArbitrage() {
            throw new UnsupportedOperationException("ArbitrageDetectorPart4.hasArbitrage: not implemented");
        }
    }

    // ====================================================================
    // PART 5  —  并发安全 + 实时汇率流式更新                          [⚠ TODO]
    // ====================================================================
    // 与 Part 3 比:
    //   同: API 形状 update(from,to,rate) + convert(from,to,amount); 双向规则不变
    //   变: 多个 producer 线程高频 update (汇率推送), 多个 consumer 线程并发 convert
    //   新: 没有新方法签名 —— 但所有操作必须线程安全
    //
    // 问题陈述:
    //   行情系统每秒推 10000 条 rate update, 同时有 1000 个用户线程在 convert.
    //   要求:
    //     (a) convert 看到的 rate 必须是某个真实存在过的版本 (不能撕裂: from 用旧版本, to 用新版本)
    //     (b) 高频 convert 不能被 update 严重阻塞
    //     (c) update 顺序不能乱 —— 如果同一对 (USD,EUR) 先后 update 0.9 再 0.95,
    //         最终留下的必须是 0.95, 不能是 0.9
    //     (d) convert 中途如果有 update, 是看新版本还是旧版本? —— 面试官会问"语义"
    //
    // 面试要讨论的取舍 (Coinbase 团队明确说重点考的):
    //   1. synchronized this  —— 一把大锁, 简单, 但 convert 跟 update 互相阻塞, 吞吐低
    //   2. ReentrantReadWriteLock —— update 拿写锁, convert 拿读锁; 写少读多场景好;
    //      但行情场景写也不少 (10k/s update vs 1k/s convert), 反而退化
    //   3. ConcurrentHashMap (邻接表) —— 单条边更新 lock-free, 但 convert 跨多条边
    //      没有 snapshot 语义 (走到一半 rate 变了)
    //   4. Copy-on-Write —— 每次 update 整张图拷一份, convert 完全 lock-free 读;
    //      行情场景写太频繁, 复制成本爆炸
    //   5. 版本号 / MVCC —— 每张图带 version, convert 进来时 snapshot 一个 version;
    //      所有同 version 的边一起换 —— 实现复杂
    //   6. Single-writer pattern —— 单线程消费一个 update queue, 多 reader 直接读;
    //      读不加锁 (volatile 引用切换), 写顺序天然有序
    //
    // 面试官最常追问:
    //   - "10k update/s + 1k convert/s, 你的方案吞吐是多少? 延迟 p99?"
    //   - "如果 convert 中途看到新 rate, 用户可能用了一个组合不存在过的汇率, 这能接受吗?"
    //   - "我能保证读到的是 'monotonic' 的版本吗? (不会看到比之前更老的)"
    //
    // 你要写的: 把 Part 3 重做一遍, 加并发. 实现一个就够, 但要能说清楚其他方案的对比.
    // 提示 (放代码里, 不是答案): rate 是 immutable double, 但邻接表本身需要同步.

    public static class ConcurrentStreamingConverterPart5 {
        public ConcurrentStreamingConverterPart5() {
            throw new UnsupportedOperationException("ConcurrentStreamingConverterPart5: not implemented");
        }

        public void update(String from, String to, double rate) {
            throw new UnsupportedOperationException("ConcurrentStreamingConverterPart5.update: not implemented");
        }

        public double convert(String from, String to, double amount) {
            throw new UnsupportedOperationException("ConcurrentStreamingConverterPart5.convert: not implemented");
        }
    }

    // ====================================================================
    // PART 6  —  算法选型对比: Dijkstra / Bellman-Ford / Floyd-Warshall  [⚠ TODO]
    // ====================================================================
    // 与 Part 2 比:
    //   同: 目标还是 best rate (乘积最大路径)
    //   变: 题量级变了 —— 1000+ 种货币, convertBest 调用每秒 10k 次
    //   新: precompute() 在初始化时把所有 pair 的 best rate 预算好 (类似 Floyd);
    //       convertBest 退化成 O(1) 查表
    //
    // 问题陈述:
    //   面试官问: "如果 convertBest(from, to, amount) 被高频调用, Part 2 的 DFS+memo
    //   每次都从头算, 怎么办?"
    //
    // 三种算法的取舍:
    //   1. Dijkstra (单源最长路径, 因为 log 后等价于最短路径):
    //      - 复杂度: O((V+E) log V) per source, 每次 convertBest 都跑一次
    //      - 适合: 货币数 V 中等, convert 频率不极端高
    //      - 注意: best path 是乘积最大, 等价于 -log(rate) 后求最短路径; 需要边权 >= 0,
    //        而 rate > 0 → -log(rate) 可能为负 (rate > 1 时), 所以 Dijkstra 不能直接用!
    //      - 解决: 改成最长路径直接做, 或者验证 rate <= 1 (用 1/rate 反向边规范化)
    //   2. Bellman-Ford:
    //      - 复杂度: O(V * E), 慢但支持负权
    //      - 适合: 边数少, 或者需要顺便检测套利环 (Part 4 的扩展)
    //   3. Floyd-Warshall (all-pairs):
    //      - 复杂度: O(V^3) 一次性算好所有对, 之后查 O(1)
    //      - 适合: V 小 (≤ 几百), convertBest 高频调用 —— Coinbase 货币种类有限, 完美场景
    //      - 缺点: 每次有 rate update 都要重算 O(V^3), 或者增量维护
    //
    // 面试官最常追问:
    //   - "如果加了套利 (rate>1 的反向边), Dijkstra 还能用吗? 为什么?"
    //   - "Floyd 预算了, 现在 update 一条边怎么办? 全表重算还是增量?"
    //   - "1000 种货币 + 1M 条边, 内存放得下 1M^2 的矩阵吗?"
    //
    // 你要写的: 实现 precompute() 用任一你选的算法; convertBest() 查预计算结果.
    // 用 Part 2 的 best path 语义 (反向边 = 1/rate).

    public static class PrecomputedBestRateConverterPart6 {
        public PrecomputedBestRateConverterPart6(List<String[]> rates) {
            throw new UnsupportedOperationException("PrecomputedBestRateConverterPart6: TODO — 选算法并预计算");
        }

        // 选择性暴露: 写完后能讲清楚复杂度
        public double convertBest(String from, String to, double amount) {
            throw new UnsupportedOperationException("PrecomputedBestRateConverterPart6.convertBest: TODO — 查预计算结果");
        }
    }

    // ====================================================================
    // PART 7  —  汇率 TTL / Staleness + 跨交易所聚合                  [⚠ TODO]
    // ====================================================================
    // 与 Part 5 比:
    //   同: 还是流式 update + convert; 还是并发场景
    //   变: 每条 rate 带 source (哪个交易所) + timestamp; rate 有 TTL (e.g. 5 秒过期);
    //       同一对 (from,to) 可能有多个 source 同时报价
    //   新: updateWithSource(from,to,rate,source,timestamp);
    //       convert 时如果 rate 过期了或不存在, 抛 StaleRateException
    //
    // 问题陈述:
    //   真实世界: Binance / Coinbase / Kraken 都在推 BTC→USD 的 rate, 但价格不一样.
    //   而且行情可能断 —— 5 秒没新报价就不能信了 (stale).
    //   要求:
    //     (a) convert 时, 多个 source 报价怎么合 —— 平均? 最优? 最新? VWAP?
    //     (b) 任意一条 hop 上没有 fresh rate → convert 失败 (StaleRateException)
    //     (c) 不能因为某个 source 挂了影响其他 source —— 单 source 的过期不能拖累整体
    //     (d) 一对 (from,to) 上, 不同 source 在不同时间点 update, 怎么定义"全局有效 rate"
    //
    // 面试要讨论的取舍:
    //   1. 多 source 聚合策略:
    //      - 最新 (last write wins) —— 简单, 但被某 source 抖动主导
    //      - 平均 / median —— 抗异常, 但延迟高 (要等多个 source 报价)
    //      - VWAP (按成交量加权) —— 最准, 但需要额外 volume 数据
    //      - 最优 (买最高卖最低) —— 实质是 Part 2 的 best rate, 但单对场景
    //   2. TTL 实现:
    //      - Lazy: convert 时检查 timestamp 是否过期 —— 简单, 但内存里堆积老数据
    //      - 后台清理 (类似 IMDB Part 6) —— 占内存少, 但加复杂度
    //      - PriorityQueue 按 expireAt —— 知道下一个过期点, 但 update 频繁会有"幽灵"
    //   3. 时钟来源:
    //      - System.currentTimeMillis() —— 简单, 跟现实时间一致, 但测试难 mock
    //      - 外部传入 timestamp —— 测试友好, 但要求调用方一致
    //   4. 部分 stale 怎么办:
    //      - USD→EUR 新鲜, EUR→GBP 已过期 → 整条路径作废? 还是只用新鲜段?
    //      - 业务决策: 金融场景一般"任一段过期则失败" —— 保守优先
    //
    // 面试官最常追问:
    //   - "5 秒 TTL, 但用户的 convert 请求恰好在第 4.99 秒来, rate 第 5.00 秒过期, 怎么算?"
    //   - "如果 Binance 的 BTC→USD 是 50000, Kraken 是 49000, 你给用户 convert 用哪个?"
    //   - "用户做 convert(BTC, EUR) 中转 USD, BTC→USD 用 Binance, USD→EUR 用 Coinbase, OK 吗?"
    //
    // 你要写的: 实现 updateWithSource + convert (聚合策略自选 + staleness check).
    // 自定义 StaleRateException (静态内部类).

    public static class StaleRateException extends RuntimeException {
        public StaleRateException(String message) { super(message); }
    }

    public static class MultiSourceConverterPart7 {
        public MultiSourceConverterPart7(long ttlMillis) {
            throw new UnsupportedOperationException("MultiSourceConverterPart7: TODO — 多 source + TTL");
        }

        public void updateWithSource(String from, String to, double rate, String source, long timestampMillis) {
            throw new UnsupportedOperationException("MultiSourceConverterPart7.updateWithSource: TODO");
        }

        // 在 nowMillis 时刻发起 convert; 任一 hop 没有 fresh rate → StaleRateException
        public double convert(String from, String to, double amount, long nowMillis) {
            throw new UnsupportedOperationException("MultiSourceConverterPart7.convert: TODO");
        }
    }

    // ====================================================================
    // PART 8  —  持久化 + 历史汇率回查 (time-travel query)            [⚠ TODO]
    // ====================================================================
    // 与 Part 7 比:
    //   同: 流式 update + 多 source 仍然存在
    //   变: 所有 update 持久化记录; 支持按时间点回查 "T 时刻 USD→EUR 是多少"
    //   新: convertAt(from, to, amount, asOfMillis) —— 历史时间点的 convert;
    //       rateAt(from, to, asOfMillis) —— 单边历史 rate
    //
    // 问题陈述:
    //   监管 / audit / 对账场景: 用户 5 分钟前下的单, 用的汇率是多少? 现在能复现吗?
    //   Coinbase 是金融公司, 历史汇率审计是硬需求.
    //   要求:
    //     (a) update 历史完整保留, 不能被新 update 覆盖丢失
    //     (b) rateAt(from,to,T) —— 返回 T 时刻"最新有效"的 rate (T 之前最近一次 update)
    //     (c) convertAt 用 T 时刻的整张图 (snapshot) 做 best rate / 任意路径
    //     (d) 历史数据持续增长, 怎么存怎么查
    //
    // 面试要讨论的取舍:
    //   1. 存储模型:
    //      - 每对 (from,to) 维护一个 TreeMap<timestamp, rate> —— rateAt 用 floorEntry O(log n);
    //        简单但所有版本在内存里, 占用大
    //      - Append-only log (类似 IMDB Part 7 的 WAL) —— 顺序写快, 查需要扫 / 建索引
    //      - LSM-tree style —— 写优化, 加 bloom filter / 分级合并; 实现复杂
    //   2. 内存 vs 磁盘:
    //      - 全内存: 1 年 * 86400s * 10000 update/s = 3e11 条 → 不可能
    //      - 热数据内存 (近 1 小时) + 冷数据磁盘 / S3 (parquet 按天分片)
    //   3. convertAt 的算法:
    //      - 时间点 T 的整张图 = 每对 (a,b) 用 rateAt(a,b,T)
    //      - 直接用 Part 2 best path 算, 但每条边查 rateAt 都 O(log n) —— 还行
    //      - 或者预计算 "T 时刻的快照", 但快照不能无限多
    //   4. 跟 Part 5 并发结合:
    //      - update 在写 TreeMap (有锁), rateAt 在读 TreeMap (要 ConcurrentSkipListMap?)
    //      - convertAt 跨多个 (from,to) 的 TreeMap, 是否需要"全局 snapshot 一致"?
    //        → 同一 asOf 时间点, 每对独立查就是一致的 (因为 asOf 固定)
    //   5. 数据压缩 / 留存:
    //      - 老数据合并 (e.g. 1 天前的, 每分钟保留一个采样)
    //      - GDPR / 监管要求: 7 年完整 audit log
    //
    // 面试官最常追问:
    //   - "10k update/s, 1 年的数据怎么存得下? 怎么查得快?"
    //   - "asOf 时间点查不到 rate (那时还没 update), 是返 null 还是 throw?"
    //   - "convertAt 跨多个 hop, 每条边的 'asOf 时刻最新 rate' 来自不同 update 时间,
    //      但都在 asOf 之前. 这种'拼凑'的汇率有意义吗?"
    //
    // 这是开放设计题. 不强求方法签名. 给两个建议入口:

    public static class HistoricalConverterPart8 {
        public HistoricalConverterPart8() {
            throw new UnsupportedOperationException("HistoricalConverterPart8: TODO — 历史汇率回查");
        }

        public void update(String from, String to, double rate, long timestampMillis) {
            throw new UnsupportedOperationException("HistoricalConverterPart8.update: TODO");
        }

        // T 时刻的单边 rate (T 之前最近一次 update, 双向规则不变)
        public double rateAt(String from, String to, long asOfMillis) {
            throw new UnsupportedOperationException("HistoricalConverterPart8.rateAt: TODO");
        }

        // T 时刻整张图上的转换 (语义同 Part 1 任意路径或 Part 2 best path, 自选并说明)
        public double convertAt(String from, String to, double amount, long asOfMillis) {
            throw new UnsupportedOperationException("HistoricalConverterPart8.convertAt: TODO");
        }
    }
}
