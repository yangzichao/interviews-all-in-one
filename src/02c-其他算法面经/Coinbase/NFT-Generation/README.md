# Coinbase — NFT Generation

给定 trait 规格,生成 N 个 NFT。面试时 CoderPad 上**一个 Part 给一个**,做完再放下一个。

来源:Coinbase VO 高频题(#2)。不在 LeetCode 上,本质是**带约束的随机采样**——每个 Part 加一层约束。

---

## 背景:什么是 NFT collection?

像 **Bored Ape Yacht Club**、**CryptoPunks** 这种项目——一个 NFT collection 通常是 **10000 张图片**。每张图片由一组"特征"(**trait**)随机组合而成。

举个例子,假设我们做一个"猴子头像"项目,定义这些 trait:

| trait(特征) | 可选 values(取值) |
|---|---|
| `background` | blue / red / gold / purple |
| `fur` | brown / white / black / rainbow |
| `eyes` | round / slit / laser / sleepy |
| `hat` | none / crown / cap / wizard |
| `mouth` | smile / frown / open |

**每张 NFT = 每种 trait 各挑一个 value 拼起来**,比如:

> 蓝色背景 + 棕色毛 + 激光眼 + 戴皇冠 + 微笑

用数据结构表达就是一个字典:

```java
{
  "background": "blue",
  "fur":        "brown",
  "eyes":       "laser",
  "hat":        "crown",
  "mouth":      "smile"
}
```

前端拿着这 5 元组,把对应的 5 张 PNG 图层叠起来,渲染成最终那张图。

**这一题不管渲染,只负责生成那个"trait 分配字典"的列表。**

---

## 题目本体

写一个函数,输入:
1. `size` —— 这个 collection 总共多少张 NFT
2. `traits` —— trait 规格列表,每个 trait 是 `(名字, 该 trait 的合法 values)`

输出: 长度 == `size` 的列表,每个元素是一个 `{trait 名 → 选中的 value}` 字典。

**8 层约束/讨论,每个 Part 加一层:**

> Part 1–4 是面经原题(PracHub 上的 4-part 渐进设计题)。
> Part 5–8 是**超越面经**的 follow-up —— Coinbase 团队明确说重点考"并发 / 数据增长 / 取舍",
> 这些就是面试官真要追问、而面经经常省略的部分。

| Part | 加的约束/方向 |
|------|----------|
| 1 | 每个 trait 等概率独立采样, NFT 之间允许重复 |
| 2 | NFT 之间不能重复 |
| 3 | 每个 value 有用量上限(cap) |
| 4 | 每个 value 有权重(weighted random) |
| 5 | **并发生成 + 全局去重** |
| 6 | **流式批量生成**(不爆内存) |
| 7 | **限流 / 反作弊**(per-user rate limit) |
| 8 | **中断续传**(crash recovery / checkpoint) |

---

## API 全貌

```java
// 共享数据类型
NFTGeneration.Trait(String type, List<String> values)
NFTGeneration.WeightedTrait(String type, List<String> values, List<Integer> weights)
NFTGeneration.CappedTrait(String type, Map<String, Integer> valueCaps)

// Part 1 — 均匀随机, 允许重复
new GeneratorPart1(long seed).generate(int size, List<Trait> traits)
    -> List<Map<String,String>>

// Part 2 — 均匀随机, NFT 必须互不相同
new GeneratorPart2(long seed).generate(int size, List<Trait> traits)
    -> List<Map<String,String>>   // size > 总组合数 → IllegalArgumentException

// Part 3 — 每个 value 有 capacity cap
new GeneratorPart3(long seed).generate(int size, List<CappedTrait> traits)
    -> List<Map<String,String>>   // 不可行 → IllegalStateException

// Part 4 — 加权抽样, NFT 互不相同
new GeneratorPart4(long seed).generate(int size, List<WeightedTrait> traits)
    -> List<Map<String,String>>
```

每个 NFT = 每个 trait type 选一个 value,即 `Map<String,String>`,key 是 trait type,value 是被选中的 value。

---

## 4 个 Part 的 spec

> **写之前不想看到任何实现思路 / 提示。** 卡住了再翻 [hints.md](hints.md)。

### Part 1 — Uniform Random

```
traits = [
  {"eyes": ["round", "slit", "narrow"]},
  {"mouth": ["smile", "frown"]}
]
generate(3, traits)
  → 可能输出 [{eyes=round, mouth=smile}, {eyes=slit, mouth=smile}, {eyes=round, mouth=frown}]
```

每个 NFT 独立采样;NFT 之间 / trait 之间都互相独立。重复完全 ok。

测试用固定 seed → 输出 deterministic,但**不要 hardcode 具体序列**(脆弱)。验证:
- 长度 == size
- 每个 NFT 的 key set == trait type set
- 每个 value 都来自该 trait 的合法 values

### Part 2 — No Duplicates

NFT 之间必须互不相同(同样的 `Map<String,String>` 不能出现两次)。

不可行(size 物理上凑不出这么多 distinct NFT)→ `IllegalArgumentException`。

### Part 3 — Per-Value Capacity Caps

每个 (trait, value) 有用量上限。比如 `rare-eyes` 在 100 个 NFT 里最多出现 5 次。

```
CappedTrait("eyes", {"common": 80, "rare": 20})
```

NFT 还得**互不相同**(沿用 Part 2)。

不可行 → `IllegalStateException`。

### Part 4 — Weighted Probabilities

每个 value 带权重(整数,正)。被选中概率正比于权重。

```
WeightedTrait("eyes", ["common", "rare"], [9, 1])
  → 90% 概率选 "common", 10% 概率选 "rare"
```

去重保留(沿用 Part 2),但**这一 Part 不加 cap**——保持聚焦于"加权采样"概念。

---

## Part 5 — 并发生成 + 全局去重(Coinbase 必问)

**问题**:一个 10000 张的 collection,单线程跑随机采样 + uniqueness check 太慢。多个 worker 线程并发产 NFT,每张都要全局去重 —— 怎么保证两个线程不会同时把同一个 NFT 加入 batch?

> 注:Part 5-8 都基于最基础的 `Trait` (等概率) + uniqueness 这一层去叠新约束,**不再叠加** weighted / cap —— 每个 Part 专注它自己那个核心概念。

**面试官最常问的 4 个 follow-up**:

1. **"怎么保证两个线程不会同时加同一个 NFT?"**
   → 引出 `ConcurrentHashMap.putIfAbsent` / `compareAndSet` 的 CAS 讨论;
   `Collections.synchronizedSet` 也行但读写互斥,并发度低。

2. **"size 接近总组合数时,多线程 rejection sampling 会变成什么样?"**
   → 大量线程 retry 撞到同一个剩余组合,contention 灾难。
   要切换策略:**枚举所有组合 + shuffle + 按 worker 切片**。
   讨论 cross-over 阈值:size / total > 70%? 80%?

3. **"deterministic 怎么保证?多线程下 seed 还能复现吗?"**
   → 单线程天然 deterministic;多线程顺序不定。
   方案:每个 worker 独立 seeded Random(`seed = master_seed ^ worker_id`),
   但最终 batch 的 **list 顺序** 仍可能不同 —— 接受 "set 等价" 而非 "list 等价"?

4. **"一个 worker 抛异常,整个 generate 怎么处理?"**
   → fail-fast:`Future.cancel(true)` + 设置中断标志,其他 worker 检查到就早退;
   还是收集所有 error 最后汇报?

**取舍表**:

| 策略 | 去重开销 | 扩展性 | 实现复杂度 | 适用 |
|------|--------|--------|----------|------|
| 全局 synchronized Set | O(1) 但串行 | 差 | 简单 | size 小 |
| **ConcurrentHashMap.putIfAbsent** | O(1) 无锁 | 好 | 中等 | 默认推荐 |
| 分片 set(按 hash 分) | O(1) 局部 | 极好 | 高 | 超大 batch |
| 预枚举 + worker 切片 | 0 | 完美 | 中等 | size 接近上界 |

**自检题**:
- 我的 `putIfAbsent` 失败后会无限 retry 吗?有上限或自适应停止吗?
- 共享一个 Random 跟每个 worker 一个 Random,性能差多少?(共享要 lock,每个独立要小心 seed correlation)
- 一个 worker 卡住(GC pause),其他 worker 会等它吗?

---

## Part 6 — 流式批量生成(不爆内存)

**问题**:10 亿张 NFT 不能用 `List<Map>` 装进内存。改成流式 —— 生成一张推给 consumer 一张,生成器内部最多缓存 O(1) 或 O(window)。

**最难一点**:去重需要"已生成全集",这本身就是 O(n)。怎么破?

**面试官最常问的 4 个 follow-up**:

1. **"consumer 处理慢怎么办?生产者要 backpressure 吗?"**
   → 引出 `BlockingQueue` / Reactive Streams;队列满了就 block 生产线程。
   讨论队列大小:太大占内存,太小线程频繁阻塞。

2. **"Bloom filter 替代精确去重,误判率 0.1% 可以接受吗?"**
   → 公式 `m = -n·ln(p) / (ln 2)²`;10 亿 + 0.1% 大约 1.4GB。
   误判后果:**合法** NFT 被拒(说"已生成过"),会导致最终 size 不足。怎么补救?
   分级:先 Bloom 快速过滤,疑似重复再走精确 set。

3. **"consumer 写盘失败,这张 NFT 算成功还是失败?去重 set 回滚?"**
   → 经典 exactly-once vs at-least-once vs at-most-once 讨论。
   Coinbase 金融场景偏 **at-least-once + idempotent consumer**。

4. **"生成中途想看进度 / 取消,接口怎么设计?"**
   → 引出 `CompletableFuture` + cancellation token,或简单 `volatile boolean canceled` 标志。
   consumer 返回 `false` 也算 graceful cancel(这套 scaffold 用的方案)。

**取舍表**:

| 方案 | 内存 | 去重精度 | 速度 | 适用 |
|------|------|--------|------|------|
| in-memory HashSet | O(n) | 精确 | 快 | n < 1000 万 |
| Bloom filter | O(n) 小常数 | 有误判 | 快 | n 极大 + 容忍误判 |
| 外部 (Redis SET) | O(0) 本地 | 精确 | 慢 (网络) | 分布式生成 |
| 不去重(允许碰撞) | O(0) | 无 | 最快 | 总空间 >> size,概率论可控 |

**自检题**:
- consumer 抛异常会让我的 worker 线程死锁吗?(try/catch 包好,异常往外抛 + cleanup)
- 队列容量怎么选?`numThreads × 8` 是个粗略起点。
- 我的 Iterator 是 fail-fast 还是 weakly consistent?并发修改时行为定义清楚了吗?

---

## Part 7 — 限流 / 反作弊(per-user rate limit)

**问题**:开放给用户调用的 mint API。一个恶意脚本狂刷,把稀有 trait 全拿走。要按 userId 限流:每分钟最多 N 张,超了 throw。

**面试官最常问的 4 个 follow-up**:

1. **"token bucket 还是 leaky bucket?区别?"**
   → token bucket 允许突发(积累的 token 一次用完);
   leaky bucket 平滑输出(漏水速率恒定)。
   mint 业务通常用 token bucket —— 用户偶尔多 mint 一点没事。

2. **"sybil attack —— 用户用很多账号刷,仅靠 userId 不够,怎么办?"**
   → IP-based limit、device fingerprint、KYC binding、captcha;
   链上分析:新地址 + 没历史交易 = 可疑。
   Coinbase 真实场景:**多层防御**,单层不可能挡住所有。

3. **"limit 状态存哪?单机 Map 重启就丢,多机不同步。"**
   → 经典 Redis `INCR` + `EXPIRE` 方案;
   或本地 + 异步 sync 到中心(eventual consistent,允许小幅超额)。
   讨论 CAP:强一致 → 慢;弱一致 → 可能让攻击者多拿几张。

4. **"高 QPS 下 rate limit 本身成为瓶颈,怎么优化?"**
   → sliding window log(精确但贵)→ counter(粗)→ approximate counter (Lossy Counting);
   或客户端预扣:server 一次发 100 个 token 给客户端,客户端本地扣减。

**取舍表**:

| 算法 | 突发友好 | 内存 | 精度 | 实现 |
|------|--------|------|------|------|
| 固定窗口计数器 | 边界 burst | O(1)/user | 粗 | 极简 |
| 滑动窗口 log | 平滑 | O(N)/user | 精确 | 中等 |
| 滑动窗口 counter | 平滑 | O(1)/user | 近似 | 中等 |
| **token bucket** | 是 | O(1)/user | 精确 | 简单 |
| leaky bucket | 否(平滑) | O(1)/user | 精确 | 简单 |

**自检题**:
- 我的限流 counter 线程安全吗?`AtomicLong` 还是 `synchronized`?
- userId Map 会无限增长吗?用过一次的 user 永远在?怎么 evict?(LRU? TTL?)
- 时钟回拨 / NTP 同步会让限流失效吗?(用 `System.nanoTime()` 替 `currentTimeMillis()`)

---

## Part 8 — 可恢复 / 中断续传(crash recovery)

**问题**:10 万张的 batch 跑了 8 小时,9 万张时被 OOM kill。重启时不应该重头来。给一个 jobId,checkpoint 已经生成过的部分,resume 时接着上次进度继续。

**关键问题:哪些状态要持久化?**

- 已生成 NFT 列表(供去重)
- Random 状态(deterministic 继续)—— `java.util.Random` 内部 seed 可以反射或换 SplitMix64
- cap 剩余余量(Part 3 那种约束)
- 进度计数器(已生成 X / 总 size)
- traits 配置 checksum(防 resume 时配置变了)

**面试官最常问的 4 个 follow-up**:

1. **"checkpoint 写一半就崩了 —— checkpoint 本身怎么保证完整?"**
   → atomic file rename(write tmp → fsync → rename);
   或追加式 WAL + 重启时丢弃尾部不完整记录(CRC32 校验)。

2. **"resume 时,已生成 NFT 在外部系统(S3/DB)是否真的落地了?"**
   → idempotent write + reconciliation;
   checkpoint 必须只标记 **"本张 NFT 在外部已确认"** 的为完成。
   两阶段:本地 mark in-flight → 外部 ACK → 本地 commit。

3. **"随机数 seed 怎么 resume?重启后 Random 跳到之前状态吗?"**
   → `java.util.Random` 的内部 state(long seed)可反射存盘;
   更干净:用 counter-based PRNG(SplitMix64 / PCG),状态 = 一个 long counter,天然可 serialize。

4. **"多机分布式生成,一台机器挂了,它那部分谁来 resume?"**
   → work-stealing + leader election(ZooKeeper / etcd);
   或预切片分配(简单但 worker 不均时浪费)。

**取舍表**:

| checkpoint 策略 | 性能 | 崩溃丢失上限 | 实现复杂度 |
|----------------|------|-----------|----------|
| 每张 fsync | 极慢 | 0 | 简单 |
| **batch fsync(每 N 张)** | 中 | N 张 | 简单 |
| 时间触发(每 T ms) | 较快 | T ms 内的张数 | 简单 |
| 不 checkpoint | 最快 | 全部 | - |

**跟 In-Memory DB Part 7 (WAL) 同一套权衡** —— 可以参考那边的讨论。

**自检题**:
- checkpoint 文件被两个进程同时打开会怎样?用文件锁(`FileLock`)还是 jobId-as-lockfile?
- 我恢复出来的去重 set 真的等价于"之前生成过的所有 NFT"吗?漏掉一个就有 dup。
- traits 配置改了(加新 trait / 改 weight),之前已生成的 NFT 还合法吗?怎么 detect?(checksum 不匹配就拒绝 resume)

---

## 怎么练

```bash
cd src/02c-其他算法面经/Coinbase/NFT-Generation

javac NFTGeneration.java NFTGenerationTest.java

java NFTGenerationTest                # 跑所有 Part
java NFTGenerationTest part1          # 只跑 Part 1
java NFTGenerationTest part1 part2    # 跑指定的几个
```

输出格式:

```
Part 1 SKIPPED (not implemented)
Part 2 SKIPPED (not implemented)
Part 3 SKIPPED (not implemented)
Part 4 SKIPPED (not implemented)

Passed=0  Failed=0  Skipped=4
```

骨架在 [NFTGeneration.java](NFTGeneration.java),测试在 [NFTGenerationTest.java](NFTGenerationTest.java)。每个 Part 在文件里是一段独立的 `public static class`,后缀 `PartN`:

```
PART 1: NFTGeneration.GeneratorPart1   [✓ 起手]
PART 2: NFTGeneration.GeneratorPart2   [⚠ 你来写]
PART 3: NFTGeneration.GeneratorPart3   [⚠ 你来写]
PART 4: NFTGeneration.GeneratorPart4   [⚠ 你来写]
PART 5: NFTGeneration.GeneratorPart5   [⚠ 超越面经 follow-up — 并发生成]
PART 6: NFTGeneration.GeneratorPart6   [⚠ 超越面经 follow-up — 流式 / 不爆内存]
PART 7: NFTGeneration.GeneratorPart7   [⚠ 超越面经 follow-up — 限流 / 反作弊]
PART 8: NFTGeneration.GeneratorPart8   [⚠ 超越面经 follow-up — 中断续传]
```

**为什么独立 class 而不是继承同一个基类?** 真实面试是渐进的——做完 Part 1 才会让你看 Part 2。这种切片结构让你能专注当前 Part 而不破坏已完成的部分;做完之后还能讨论 "如果要复用采样逻辑,我会抽个 `AbstractGenerator` 带 hook"——这种 trade-off 讨论是面试加分点,但不是必做项。

**为什么 constructor 吃 seed?** 测试要 deterministic;面试时也可以直接讨论"如果不传 seed,我会用 `ThreadLocalRandom.current()`"。
