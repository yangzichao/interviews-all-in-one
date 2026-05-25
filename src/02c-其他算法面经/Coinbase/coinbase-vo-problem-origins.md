# Coinbase VO Coding 题目原型与练习参考

对地里 89 篇 Coinbase 面经聚类出的 **9 个 VO 编码题**，逐一对照全网原型：LeetCode 编号、经典面试题、真实工程实现、官方/工业文档。每题给出"最强对照资源"+"刷题 ROI 排序"，可直接作为 VO 前 1-2 周的冲刺清单。

---

## TL;DR 速查表

| # | 题目 | 出现次数 | 原型匹配度 | 最强单一对照 | Pro Tip 亮点 |
|---:|---|---:|---|---|---|
| 1 | Iterator / Interleaving | 13 | ⭐⭐⭐ | **LC 281 Zigzag Iterator** + Google Tech Dev Guide "Flatten Iterators" | Coinbase "公平 round-robin 跳过耗尽列表"就是 LC 281 官方 follow-up 的原版 |
| 2 | NFT Generation | 12 | ⭐⭐ 组合 | **HashLips Art Engine** GitHub `createDna()`+`isDnaUnique()` | 业界 7k⭐ NFT 生成器的核心算法 = 面试三问标准答案 |
| 3 | Transaction Filter + Pagination | 11 | ⭐ 业务设计 | **Coinbase Prime API 官方文档** + Relay GraphQL Cursor Spec | Prime 文档明确警告 `before/after` 是 *positional* 不是 *chronological*——这是面试 follow-up 陷阱 |
| 4 | Crypto Order System | 9 | ⭐⭐ 设计 | **USPTO 专利 11710088**（consistent-hashing OMS sharding）+ FIX 协议 state spec | 专利文档几乎逐字对应 sharding follow-up 的标准答案 |
| 5 | Food Delivery (Drone) | 8 | ⭐⭐⭐ | **ttzztt gitbook "Drone Delivery"** + DoorDash System Design Handbook | 流传多年的 quant/HFT 经典题，Coinbase 直接拿来用 |
| 6 | Block Mining | 8 | ⭐⭐⭐ 工业 | **Bitcoin Core `txmempool.cpp` + PR #7600**（sdaftuar 真实 ancestor-feerate 算法） | 实际比较器用 `a*d > c*b` 避除法，tiebreak on tx hash—面试时报出来是亮点 |
| 7 | Log Parsing | 4 | ⭐⭐ | **LC 1152** + OpenTelemetry Logs Spec | TraceId/SpanId 关联是 follow-up "多机日志"的标准答案 |
| 8 | Kafka Event Processing | 3 | ⭐ 概念/设计 | **Coinbase 官方 "Kafka Infrastructure Renovation" 博客** | 面试官多半从自家 infra 出题——Coinbase Kafka SDK 有 circuit breaker + failover routing |
| 9 | Currency Exchange | 2 | ⭐⭐⭐ | **LC 399 Evaluate Division** + Princeton algs4 Arbitrage.java | 数学要点：`-log(rate)` 边权 + Bellman-Ford 负环检测 = 套利存在 |

### 刷题 ROI 排序（按"地里出现次数 × 与刷的题映射清晰度"）
1. **LC 281 + 284**（Iterator）→ cover 13 篇
2. **LC 399**（Currency）→ cover 2 篇但解法直接抄
3. **Bitcoin fractional knapsack + CPFP 思路**（Block Mining）→ cover 8 篇，理解就赢
4. **ttzztt "Drone Delivery"**（Food Delivery）→ cover 8 篇
5. **LC 1152 + OpenTelemetry**（Log Parsing）→ cover 4 篇
6. **HashLips + LC 528 + LC 710**（NFT）→ cover 12 篇
7. **纯设计题**（Pagination / Order System / Kafka）→ 看 Coinbase 自家博客 + 行业模式

---

## 1. Iterator / Interleaving Iterator

### Coinbase 题面
实现 iterator 接口 (`next()` / `hasNext()`)，变体包括 odd/even/negative/range iterator，以及 **InterleaveIterator**——输入 k 个 iterator（或 list of lists），公平 round-robin 输出，跳过耗尽的子序列。例如 `[[1,2,3],[4,5],[6],[],[7,8,9]] → [1,4,6,7,2,5,8,3,9]`。

### 经典对照题 (LeetCode + 类似题)
- **LC 281: Zigzag Iterator** ([link](https://leetcode.com/problems/zigzag-iterator/)) — 原题给两个 1D vector 交替输出。**官方 follow-up** 直接问 "What if you are given k 1d vectors?"——这就是 Coinbase 的 InterleaveIterator 变体；"跳过空 list、不同长度"是这个 follow-up 的精确版本。
- **LC 284: Peeking Iterator** — 经典 iterator wrapper 题，对应 Coinbase 的 "negative iterator / range iterator" wrapper 思路（把已有 iterator 包一层做转换）。
- **Google "Iterator of Iterators"** ([Glassdoor](https://www.glassdoor.com/Interview/Implement-an-Iterator-of-Iterators-which-traverses-through-an-arbitrary-number-of-iterators-IE-an-iterator-which-iterate-QTN_1386690.htm)) — 原题就是 `a1,b1,c1,a2,b2,c2,...`，和 Coinbase 几乎逐字一致。

### 真实工程实现 / 深度资源
- **Google Tech Dev Guide — Flatten Iterators** ([link](https://techdevguide.withgoogle.com/resources/former-interview-question-flatten-iterators/)) — Google 官方公开的"前面试题"页面，原型几乎和 Coinbase 这题完全一致，含分步骤讲解和测试用例，是除 LC 281 之外最权威的练习材料。
- **doocs/leetcode #281 多语言实现** ([link](https://github.com/doocs/leetcode/blob/main/solution/0200-0299/0281.Zigzag%20Iterator/README_EN.md)) — 中文社区高 star 项目，对 k-list 推广（Zigzag II）给出了 deque 解法。
- **kamyu104/LeetCode-Solutions zigzag-iterator.py** ([link](https://github.com/kamyu104/LeetCode-Solutions/blob/master/Python/zigzag-iterator.py)) — `deque[(iter, idx)]` 的"取出-推进-末尾再入队"模式，就是 Coinbase 变种里"跳过耗尽列表"那一步的标准写法。
- **LeetCode Discuss — Coinbase phone-screen implement-iterators** ([link](https://leetcode.com/discuss/interview-question/1829936/coinbase-phone-screen-implement-iterators/)) — 候选人原帖，描述完整 follow-up 链：RangeIterator(start,end,step) → InterleavingIterator → CycleIterator；这正是地里反复提到的"逐步加需求"模式。
- **coding-stream-of-consciousness — Interleaving Iterator Java** ([link](https://coding-stream-of-consciousness.com/2018/09/19/interleaving-iterator-java/)) — Java 实现，queue-based 公平轮转，正是 Coinbase 答案模板。
- **learncswithus Coinbase VO 真题集合** ([link](https://learncswithus.com/2025/06/30/coinbase-vo-interview/)) — 中文整合，明确指出 Coinbase "不追极优复杂度，看抽象能力"，并列出 Interleave Iterator → streaming → step jump → iterator of iterators 完整升级路线。

### 评估
完全是已知问题。骨架题 = **LeetCode 281 Zigzag Iterator + 它的 k-vector follow-up**。各种 wrapper 变体是 Coinbase 自创的"组合 API 设计"包装，但每个独立子题都是教科书级 OO/iterator 模式题。

---

## 2. NFT Generation (Random Trait Combinations)

### Coinbase 题面
输入 `{size, traits: [{type, values}, ...]}`。要求：
1. 生成 size 个对象，每个对每种 trait 随机选一个 value
2. 不能重复
3. 每个 value 有数量上限，到达后不能再选
4. 带 weighted probability 的变体（e.g. 尖耳朵 60%/宽耳朵 40%）

### 经典对照题
- **LC 528: Random Pick with Weight** ([link](https://leetcode.com/problems/random-pick-with-weight/)) — 加权随机的标准做法（prefix sum + binary search），直接对应 weighted variant。
- **LC 710: Random Pick with Blacklist** — uniqueness / rejection sampling 思路，对应 (2) 不能重复。
- **Glassdoor 直接命中** ([link](https://www.glassdoor.com/Interview/Design-iterator-build-NFTs-with-random-values-for-traits-QTN_7326665.htm)) — "Design iterator, build NFTs with random values for traits" —— 印证这道题是 Coinbase 高频 VO。

### 真实工程实现 / 深度资源
- **HashLips/hashlips_art_engine** ([link](https://github.com/HashLips/hashlips_art_engine)) — 业界最知名的 NFT 生成器（>7k star）。文件名后缀 `#weight` 实现权重；核心算法 = "按权重抽样 + DNA hash 去重 + 达到 editionCount 停止"，与面试三要素一一对应。**强烈建议通读 `src/main.js` 的 `createDna()` 和 `isDnaUnique()`**。
- **nftchef/art-engine** ([link](https://github.com/nftchef/art-engine)) — HashLips 进阶 fork，引入 "incompatible traits / required traits" 约束，正好对应"某些组合不能一起出现"的 follow-up。
- **Rounak Banik — Tutorial: Create Generative NFT Art with Rarities** ([link](https://medium.com/scrappy-squirrels/tutorial-create-generative-nft-art-with-rarities-8ee6ce843133)) — 面经里提到的那篇 tutorial 原文。"Cheetah weight=5 vs no-band weight=100 → 20 倍稀有度"这种直观例子，配套 Python 库。
- **rounakbanik/generative-art-nft `nft.py`** ([link](https://github.com/rounakbanik/generative-art-nft/blob/master/nft.py)) — 用 `numpy.random.choice(traits, p=normalized_weights)` 一行实现加权抽样，set 去重，循环到 `TOTAL_IMAGES`——几乎是面试题的标准答案模板。
- **surgewomen — Generative Art Algorithms** ([link](https://www.surgewomen.io/learn-about-web3/generative-art-algorithms-how-to-build-an-nft-collection)) — 工程视角讨论 rejection sampling vs enumeration 的折中：小集合用 rejection sampling 简单够用，大集合或低稀有度组合多时要切换到"枚举所有组合再洗牌"或 decrement-pool。
- **Coinbase NFT Dapp Starter Kit** ([link](https://github.com/coinbase/nft-dapp-starter-kit)) — Coinbase 官方仓库，含 metadata 生成脚本和 merkle allowlist，说明 Coinbase 内部确实关心 NFT pipeline，面试出这题有业务动机。

### 评估
**Coinbase 半自创**——没有单一 LC 编号覆盖全部要求，但每个子需求都是经典：weighted random = LC 528，dedupe = hash set / rejection sampling，capacity 上限 = LC 710 风格的"动态可选集合维护"。Coinbase 把它们包装成贴合自家业务（NFT mint）的复合题。

---

## 3. Transaction Filter + Pagination

### Coinbase 题面
设计系统支持：(a) 添加交易；(b) 分页（cursor 或 page-based）获取交易；(c) 按多个字段过滤；follow-up 通常是 streaming / 大数据集 / 双向翻页。

### 经典对照题
- **LC 1146: Snapshot Array** / **LC 981: Time Based Key-Value Store** — 时序数据结构 + 按 key/timestamp 查询，和 transaction-by-time-cursor 同思路。
- **LC 1797 / 1244 (Leaderboard 类)** — 设计带 add/query/filter 的服务，模式接近。
- **No exact LC #** —— 这是经典的 **API/system design 题**，不是算法题。模板来源 Coinbase / Stripe 自家 API。

### 真实工程实现 / 深度资源
- **Coinbase Prime REST API Pagination 官方文档** ([link](https://docs.cdp.coinbase.com/prime/rest-api/pagination)) — 一手资料：`cursor` query 参数 + `CB-BEFORE` / `CB-AFTER` response header + `next_cursor`/`has_next`/`sort_direction` 三件套。面试题字段几乎照抄这里。**⚠️ 重点**：文档明确说 before/after 是"位置"不是"时间"——这是常见 follow-up 陷阱。
- **Relay GraphQL Cursor Connections Specification** ([link](https://relay.dev/graphql/connections.htm)) — "opaque base64 cursor" 模式的规范出处。服务端通常编码 `(sort_key, id)` 进 base64。面试聊到"为什么 cursor 要 base64"——答案就在这里。
- **Kraken Get Trades History 文档** ([link](https://docs.kraken.com/api/docs/rest-api/get-trade-history/)) — **反例**：Kraken 交易接口用 `ofs` offset 而不是 cursor；可在面试中引用"为什么 offset 在交易历史这种 append-heavy 场景会丢数据/重复"。
- **Kraken Get Status of Recent Deposits/Withdrawals** ([link](https://docs.kraken.com/api/docs/rest-api/get-status-recent-deposits/)) — 同一家公司里 deposits/withdrawals 用 cursor、trades 用 offset 的**混合实践**——讨论 trade-off 的绝佳素材。
- **ByteByteGo — How do we Perform Pagination in API Design** ([link](https://bytebytego.com/guides/how-do-we-perform-pagination-in-api-design/)) — 系统设计标准参考，覆盖 offset / cursor / keyset / seek 四种范式对比表。
- **Milan Jovanović — Understanding Cursor Pagination (Deep Dive)** ([link](https://www.milanjovanovic.tech/blog/understanding-cursor-pagination-and-why-its-so-fast-deep-dive)) — 深入讲为什么 cursor 是 O(1) 而 offset 随 N 退化；带 SQL `WHERE (created_at, id) < (?, ?)` 的 keyset 写法，正是 Coinbase 面试题"按 created_at 降序 + id tiebreaker"该写的查询。
- **prachub — Implement filters and cursor pagination (Coinbase)** ([link](https://prachub.com/coding-questions/implement-filters-and-cursor-pagination)) — **直接命中**：明确标注 Coinbase 题。
- **Merge.dev — Cursor pagination pros and cons** ([link](https://www.merge.dev/blog/cursor-pagination)) — 简洁列了 cursor 模式无法"跳到第 N 页"的缺点，方便回答面试官的 trade-off 问题。

### 评估
**Coinbase 原创 / 业务题**，但完全是行业标准模式题。没有对应 LC 编号——属于"系统/API 设计"类，模板答案直接对应 Coinbase 自家 Prime API。Coinbase VO 反复问这题，因为它直接映射到 Coinbase 后端日常工作。

---

## 4. Crypto Order System

### Coinbase 题面
Order management——`place_order`, `pause_order`, `cancel_order`，加 `cancel all orders from a user`。Follow-up: 跨机器 sharded 如何设计。面试官透露的 optimal 数据结构: `Dict[State, Dict[str, Order]]`。

### 经典对照题
- **Order Lifecycle / State Machine Pattern (经典 OOD)** — UML state-machine for e-commerce orders；states: Draft/Validated/InProgress/Cancelled；"PreDelivery composite state" 让 `cancel_all` 统一适用。
- **LC 146 LRU Cache 风格的 nested-dict 设计** — optimal `Dict[State, Dict[order_id, Order]]` 是同款 "两层索引 O(1) lookup + O(1) bucket-iteration" 技巧。

### 真实工程实现 / 深度资源
- **Coinbase OEMS 官方博客** ([link](https://www.coinbase.com/blog/developing-a-simple-reference-order-and-execution-management-system-oems-for)) — Coinbase 官方"参考 OEMS"：core 是 order lifecycle (NEW/ROUTED/PARTIAL/FILLED/CANCELLED) + adapter pattern 接多家场所。几乎肯定是 VO 题原型。
- **FIX Trading — Order State Changes spec** ([link](https://www.fixtrading.org/online-specification/order-state-changes/)) — **工业标准 order state transition 图**；面试白板直接搬，比自己画的更权威。
- **theslyone/open-oms (GitHub)** ([link](https://github.com/theslyone/open-oms)) — FIX 5.0 SP1 OMS 开源实现，可看真实 order book + state machine 代码。
- **uberdeveloper/omspy (GitHub)** ([link](https://github.com/uberdeveloper/omspy)) — Python broker-agnostic OMS，结构和 `Dict[State, Dict[id, Order]]` + per-broker adapter 高度一致。
- **USPTO 11710088 — Scalable order management monitoring** ([link](https://image-ppubs.uspto.gov/dirsearch-public/print/downloadPdf/11710088)) — **专利明确写"consistent hashing 分配 sequence number + event store 重建 order"**，几乎逐字对应 sharding follow-up 标准答案。
- **Hello Interview — Sharding** ([link](https://www.hellointerview.com/learn/system-design/core-concepts/sharding)) — shard by user_id 让 cancel-all-by-user 变 single-shard 操作；这是面试官想听的关键 sound bite。
- **csoahelp — Coinbase OA/VO 总结** ([link](https://csoahelp.com/2024/12/24/coinbase-oa2-2025-start-22-dec/)) — 中文面经，确认 order system + event stream 是当下 OA2 主力题型。
- **Medium — Designing scalable OMS** ([link](https://medium.com/@umesh382.kushwaha/designing-a-scalable-reliable-order-management-system-65a5646931c5)) — scalable/sharded OMS（匹配 sharding follow-up）。

### 评估
不是 LC 原题，而是 Coinbase 自家 OEMS 业务的简化版 OOD 题。Optimal structure `Dict[State, Dict[order_id, Order]]` 同时支持 O(1) state transition、O(1) cancel-by-id、O(k) cancel-all-by-state；加一层 `user_id -> Set[order_id]` 即可 O(k) 实现 cancel-all-from-user。Sharding follow-up 标准答案：consistent hashing on user_id, per-shard order log, two-phase commit / saga for cross-shard cancellations。

---

## 5. Food Delivery / Restaurant System

### Coinbase 题面 (多变种)
1. **Drone 变种**: 无人机从位置 0 配送货物到 target，沿直线有 charging stations。无人机最多飞 10 单位每段（从 station 起飞落地）。求总搬运距离。
2. **Restaurant 变种**: 两张表 (restaurant_id+items, restaurant_coordinates)。找最近/最便宜的 item；时间窗口订单计数 + 最频繁 item。
3. **Knapsack 子题**: packing limited capacity with items by value（背包问题讲思路）。

### 经典对照题
- **"Drone Delivery"——经典 quant/HFT 面试题** ([ttzztt gitbook](https://ttzztt.gitbooks.io/lc/content/quant-dev/drone-delivery.html)) — 逐字一致的设置：搬货到最近前方 station, 满电起飞 (max range R), 搬到落点, 重复。**Coinbase 题就是这道**。
- **LC 774 Minimize Max Distance to Gas Station** — 同 1D-stations-on-a-line 设置。
- **LC 871 Minimum Number of Refueling Stops** — greedy/heap on stations along a line。
- **LC 45 Jump Game II** — 同款 "advance as far as possible from current reachable set" 贪心。
- **LC 692 / 347 Top K Frequent** — 对应 "most-frequent item over time window"。
- **0/1 Knapsack (LC 416)** 或 Fractional Knapsack — 对应 packing 子题。

### 真实工程实现 / 深度资源
- **askfilo — Drone delivery 题面** ([link](https://askfilo.com/user-question-answers-smart-solutions/you-are-designing-a-delivery-system-using-drones-in-a-linear-3434383538383333)) — 完整原题文本（"max 10 units after full charge, walk to next station or target, deploy drone…"）。
- **DoorDash System Design Handbook** ([link](https://www.systemdesignhandbook.com/guides/doordash-system-design-interview/)) — geo-hashed shards + 优先队列匹配 driver/restaurant；和 Coinbase "nearest item" 子题同构。
- **Design DoorDash — DEV.to walkthrough** ([link](https://dev.to/matt_frank_usa/design-doordash-interview-walkthrough-24a7)) — 完整 dispatch 设计 + sliding window for peak demand，对应"time-window 订单计数"子题。
- **AlgoCademy — DoorDash 题型组合** ([link](https://algocademy.com/blog/top-doordash-interview-questions-ace-your-technical-interview/)) — 把 k-closest restaurants → heap、menu search → trie、peak window → sliding window 三件套拆开；Coinbase 餐厅题就是这三件套组合。
- **DoorDash Interview Questions — prachub** ([link](https://prachub.com/companies/doordash/categories/system-design)) — 多道餐厅 + top-k frequent 复合题；风格几乎一致。
- **GfG Fractional Knapsack** ([link](https://www.geeksforgeeks.org/dsa/fractional-knapsack-problem/)) — knapsack 标准贪心模板。

### 评估
Drone 子题强匹配——流传多年的经典 quant 题。**标准解法 O(n)**：双指针扫 stations，每段 walk = max(0, next_station - (cur_station + 10))；从 0 走到第一个站，最后一段从最后能到的位置走到 target。Restaurant 子题是 Coinbase 把"food-delivery domain"当 mini-system design 容器——每个子部分都是 well-known pattern。

---

## 6. Block Mining / Transaction Fee Maximization

### Coinbase 题面
N 笔交易 `<id, size, fee>`，block size 100，最大化总 fee。面试官明确**不要 DP**，要 "scalable / production-optimal" 贪心。

**Follow-up**: 交易有 parent-child 依赖，含 child 必须含 parent。1→2→3 和 1→2→4 共用 1→2，只能选 {3,4} 之一。提示做法：DFS 所有 root-to-leaf 路径作为 bundle，对 bundle 再做同样贪心。

### 经典对照题
- **Fractional Knapsack (GfG / 经典)** — 按 value/weight 比降序贪心，是 Coinbase 想要的 "production" 解法。⚠️ 严格地说交易不可分割是 0/1 knapsack（NP-hard），面试官接受的 fee/size 排序贪心是真实矿池里的 **2-approximation**。
- **Bitcoin Mempool Transaction Selection / CPFP (Child-Pays-For-Parent)** — 真实比特币矿工算法。Bitcoin Core 的 `ancestor feerate` 方法 = 把每条交易和所有祖先打包成 bundle，按 bundle 的 (sum_fee / sum_size) 排序贪心。**Follow-up 完全对应这个**。
- **LC 2542 Maximum Subsequence Score** — 同款 "sort by ratio, greedy pick" 模式。

### 真实工程实现 / 深度资源
- **Bitcoin Core `txmempool.cpp` (master)** ([link](https://github.com/bitcoin/bitcoin/blob/master/src/txmempool.cpp)) — 实际源码。**`CompareTxMemPoolEntryByAncestorFee` 用 `a*d > c*b` 避除法做 ancestor-feerate 排序，tiebreak 用 tx hash**——面试时引用这点很加分。
- **Bitcoin Core PR #7600 — feerate-with-ancestors** ([link](https://github.com/bitcoin/bitcoin/pull/7600)) — 把 mining 从 per-tx feerate 改成 ancestor-package feerate 的关键 PR (sdaftuar)；这就是 Coinbase 题 parent-child follow-up 的真实工业算法。
- **Bitcoin Core Review Club #26152** ([link](https://bitcoincore.reviews/26152)) — "bump unconfirmed ancestors to target feerate" corner case；面试 follow-up "两条 path 共用前缀如何去重"就是这里讨论的问题。
- **Bitcoin Optech — CPFP topic** ([link](https://bitcoinops.org/en/topics/cpfp/)) — CPFP 概览，最权威非学术介绍，解释为何贪心 ancestor-feerate 是 incentive-compatible。
- **Bitcoin Core Doxygen — CTxMemPool** ([link](https://doxygen.bitcoincore.org/class_c_tx_mem_pool.html)) — `mapLinks` 跟踪 in-mempool parents/children；正是题目 hint "DFS root-to-leaf 形成 bundle"的数据结构。
- **shablag — Child Pays for Parent (series)** ([link](https://shablag.com/article/child-pays-for-parent/)) — 深入讲解贪心 vs 最优 (0/1 knapsack NP-hard) 的取舍——**正是面试官"不要 DP，要 production"背后的理由**。
- **LearnMeABitcoin — Transaction Fees** ([link](https://learnmeabitcoin.com/technical/transaction/fee/)) — 解释矿工按 fee/byte 贪心选交易。
- **prachub — Maximize profit with transaction fees** ([link](https://prachub.com/interview-questions/maximize-profit-with-transaction-fees)) — Coinbase 题库收录此题。

### 评估
**强匹配——本质就是 Bitcoin 矿池交易选择算法的简化面试版**：
1. 主题：fractional-knapsack-by-ratio 贪心（sort by fee/size desc, greedily fill block）。
2. Follow-up：parent-child 依赖 = Bitcoin 的 CPFP / ancestor packages。提示的"DFS root-to-leaf 形成 bundle，再贪心"就是 ancestor-feerate 算法简化版。
3. **重要 corner case**：两条 path 共用前缀（1→2）在选第二条时要扣掉已计 size/fee 避免重复——这点面试中要主动 call out。

题目设计明显从真实业务里抽出来，比纯 LC 更考"是否懂 Bitcoin 内部"。

---

## 7. Log Parsing / Filter

### Coinbase 题面
解析日志文件。输入一组 log 字符串，输出按 thread_id 分组、每个 thread 内部按 timestamp 排序的日志。部分帖子提到 filter follow-up（按级别/关键字过滤等）。

### 经典对照题
- **LC 1152 Analyze User Website Visit Pattern** ([link](https://leetcode.com/problems/analyze-user-website-visit-pattern/)) — 同样是按 user 分组、按 timestamp 排序的日志处理范式。
- **LC 937 Reorder Data in Log Files** — 经典 log 字符串解析与排序题。
- **LC 359 Logger Rate Limiter** — filter follow-up（按时间窗口过滤）常见原型。

### 真实工程实现 / 深度资源
- **OpenTelemetry Logs Spec — TraceId/SpanId 关联** ([link](https://opentelemetry.io/docs/specs/otel/logs/)) — **业界标准做法**：每条 LogRecord 内嵌 TraceId/SpanId，下游 collector 按这两个字段 group。**Coinbase 题的"按 thread_id group"在分布式场景升级版就是按 trace_id group**——是 follow-up "如果是多机日志"的标准答案。
- **OpenTelemetry Context Propagation** ([link](https://opentelemetry.io/docs/concepts/context-propagation/)) — 解释为什么跨服务日志能用同一 trace_id 串起来。
- **Logstash Grok 过滤插件官方文档** ([link](https://www.elastic.co/guide/en/logstash/current/plugins-filters-grok.html)) — ELK 栈中 120+ 内置 grok 模式，工业级 log parsing 事实标准；可引用做 follow-up "如何鲁棒解析多种格式"。
- **Logit.io — Advanced Grok Patterns** ([link](https://logit.io/blog/post/advanced-grok-patterns-data-parsing-logstash/)) — 含 `\[%{REQUEST_ID:request_id}\]` 这种自定义 pattern 的实操，正是题面 thread_id/request_id 提取方式。
- **Vector (Datadog OSS, Rust) 日志管线** ([link](https://github.com/vectordotdev/vector)) — **单台机器处理 500TB/天**的开源 pipeline；可作 follow-up "high throughput 时如何 scale" 的引用答案 (sources → transforms (VRL) → sinks 架构)。
- **Datadog Trace ID Remapper** ([link](https://docs.datadoghq.com/logs/log_configuration/pipelines/)) — 生产系统如何在 pipeline 阶段 normalize trace_id 字段。
- **LeetCode Discuss — Coinbase Interview Thread** ([link](https://leetcode.com/discuss/post/1857602/coinbase-interview-thread/)) — 候选人复盘提到 Coinbase 两道 coding tasks 其中之一是 parse + group log file by thread id。

### 评估
没有完全对应的 LC 编号，是 Coinbase 自定义"实务型"题。算法本质极简（`HashMap<thread_id, List<Log>>` + sort by timestamp），考察点在于：
1. 字符串解析鲁棒性（格式变种、空行、坏行）
2. follow-up 扩展性（按 level filter、流式处理、超大文件分块/外部排序）

Filter follow-up 通常引向 Strategy/Predicate 设计或 streaming pipeline 讨论。最接近的"原型"是 LC 1152 的 group-and-sort 模式 + LC 937 解析风味。

---

## 8. Transaction Event Processing via Kafka

### Coinbase 题面
从 Kafka 消费 transaction events，进行处理 / 聚合 / 去重 / 处理乱序事件。帖子描述较模糊，更像 system design + coding hybrid。

### 经典对照题
- **Exactly-once processing / idempotent consumer** 模式——Kafka 经典范式。
- **"Design a payment/transaction processing system"** 系统设计题——Coinbase / Stripe / Square 高频 VO。
- **LC 类比**：LC 362 Design Hit Counter（时间窗口聚合）；LC 1146 / 981（带 timestamp 的事件存储）。
- **Out-of-order stream processing** ——类似 Flink/Beam 中的 watermark + windowing 模式。

### 真实工程实现 / 深度资源
- **Coinbase 官方博客 — Kafka Infrastructure Renovation** ([link](https://www.coinbase.com/blog/kafka-infrastructure-renovation)) — **Coinbase 自家 Kafka SDK 内置 circuit breaker + failover/roundrobin/replicate 路由**，是 mission-critical 服务的轻量级 cluster federation。**面经里"transaction events"题的真实业务背景**——面试官多半从自家 infra 出题。
- **Coinbase Blog — How we scaled data streaming with AWS MSK** ([link](https://www.coinbase.com/blog/how-we-scaled-data-streaming-at-coinbase-using-aws-msk)) — Coinbase 用 MSK 做 service-to-service + CDC + ETL；follow-up "为什么用 Kafka 而不是 SQS" 标准答案。
- **Chris Richardson — Transactional Outbox Pattern (microservices.io)** ([link](https://microservices.io/patterns/data/transactional-outbox.html)) — 解决"DB 写入 + Kafka 发送"双写不一致经典模式，**金融交易系统必引用**。
- **Confluent Developer — Idempotent Reader Pattern** ([link](https://developer.confluent.io/patterns/event-processing/idempotent-reader/)) — `isolation.level=read_committed` + 在同一事务里 commit offset，end-to-end exactly-once 工程实现。
- **Confluent Blog — Exactly-Once Semantics is Possible** ([link](https://www.confluent.io/blog/exactly-once-semantics-are-possible-heres-how-apache-kafka-does-it/)) — 原始权威文 (Apurva Mehta)，讲 idempotent producer + transactions 内部机制。
- **Nejc Korasa — Idempotent Processing with Kafka** ([link](https://nejckorasa.github.io/posts/idempotent-kafka-procesing/)) — 实战 blog，把 message_id 持久化到 DB 做去重 + 事务一致性，**最贴近面试中要 coding 的"in-memory dedupe + DB-backed idempotency"**。
- **confluent-kafka-go idempotent producer example** ([link](https://github.com/confluentinc/confluent-kafka-go/blob/master/examples/idempotent_producer_example/idempotent_producer_example.go)) — 可直接抄的最小 idempotent producer 配置。
- **AutoMQ — Kafka use cases in Coinbase** ([link](https://www.automq.com/blog/kafka-use-cases-in-coinbase)) — Coinbase 内部 Kafka 架构，处理 market data + user transactions。

### 评估
这题不像传统 LC 题，更像 **system design + coding hybrid**。核心考点：
1. Idempotent consumer（message id + DB 表去重，或 Kafka idempotent producer/transactional API）
2. 乱序处理（event_time + buffer/window，或 reorder by sequence number）
3. at-least-once vs exactly-once 取舍

Coinbase 做金融交易，**exactly-once 与对账是核心痛点**。面试中通常先 coding 一个 in-memory dedupe + ordered aggregator，再扩展到分布式语义讨论。

---

## 9. Currency Exchange / DFS

### Coinbase 题面
给定一组 currency exchange rates（如 USD→EUR = 0.9, EUR→GBP = 0.85），通过 graph/DFS 求任意两种货币之间的转换率。可能 follow-up：最短路径、最佳汇率、套利检测。

### 经典对照题
- **LC 399 Evaluate Division** ([link](https://leetcode.com/problems/evaluate-division/)) — **几乎完全对应**，a/b = 2.0, b/c = 3.0 → 查询 a/c。本质就是 currency exchange + DFS/Union-Find。
- **LC 3387 Maximize Amount After Two Days of Conversions** — 升级版，两天汇率不同求最大值。
- **Currency Arbitrage (Bellman-Ford)** — follow-up 求最佳套利路径的经典题，用 `-log(rate)` 转化为最短路径检测负环。

### 真实工程实现 / 深度资源
- **Princeton algs4 — Arbitrage.java (Sedgewick)** ([link](https://algs4.cs.princeton.edu/44sp/Arbitrage.java.html)) — **Sedgewick 教科书原版实现**：完全有向图 + Bellman-Ford 找负环，O(V³)。把 `-log(rate)` 作为边权是核心 trick——面试可口述这套来证明对 follow-up "detect arbitrage" 的掌握。
- **Reasonable Deviations — Graph Algorithms and Currency Arbitrage** ([link](https://reasonabledeviations.com/2019/03/02/currency-arbitrage-graphs/)) — **极清晰的数学推导**：为什么 `sum(-log(r)) < 0 ⇔ product(r) > 1 ⇔ 套利`。强烈推荐作面试前热身。
- **Anil Pai (Medium) — Currency Arbitrage using Bellman-Ford** ([link](https://anilpai.medium.com/currency-arbitrage-using-bellman-ford-algorithm-8938dcea56ea)) — Python 实现 + 真实 forex feed，工程视角。
- **a-r-d/Bellman-Form-BTCe-Arbitrager (GitHub)** ([link](https://github.com/a-r-d/Bellman-Form-BTCe-Arbitrager)) — **直接跑在 Bitcoin 交易所上的套利检测**，Coinbase 场景几乎 1:1。
- **kcmoffat/bellman-ford-forex (GitHub)** ([link](https://github.com/kcmoffat/bellman-ford-forex)) — Java 实现 + XML 汇率 feed，可作"如何工程化"的参考。
- **Stripe 面试题 — Currency Conversion 3 部曲** ([link](https://www.bigtechexperts.com/companies/stripe/swe-algorithm-questions/interview-question-1)) — Stripe 同款渐进题：Part 1 直接汇率 → Part 2 多跳 → Part 3 最佳路径。Coinbase 的 follow-up 走向与此完全一致。
- **interviewing.io — Currency Conversion mock** ([link](https://interviewing.io/questions/currency-conversion)) — 完整 mock 录像 + 解题，包含面试官 cue 与 follow-up 节奏。
- **Alex Golec (ex-Google) — Ratio Finder** ([link](https://alexgolec.dev/ratio-finder/)) — Google 工程师 blog 详解 DFS / BFS / 预计算三种解法 trade-off。
- **LeetCode Discuss — Google Phone Currency Conversion** ([link](https://leetcode.com/discuss/post/483660/google-phone-currency-conversion-by-anon-upqo/)) — Google 同款题，DFS 建图。

### 评估
**原型非常明确就是 LeetCode 399 Evaluate Division**，且这道题是 Google / Coinbase / Stripe / Airbnb 高频题。

标准解法三选一：
1. **DFS 建图遍历**（最直观，O(Q·(V+E))）
2. **Union-Find with weighted edges**（查询 O(α)）
3. **Floyd-Warshall** 预计算所有 pair（O(V³) 但查询 O(1)）

Follow-up 常见方向：
- "best rate"（改 DFS 取 max product）
- "shortest hops"
- "detect arbitrage"（Bellman-Ford 负环）
- 流式更新汇率
- thread-safe 实现

**Coinbase 因为是交易所，arbitrage detection follow-up 尤其自然。**
