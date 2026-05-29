# Coinbase — Iterator / Interleaving Iterator

实现一组 `Iterator` 风格的 API,逐步加需求。面试时 CoderPad 上**一个 Part 给一个**,做完再放下一个。

来源:Coinbase VO 高频题(13/89 篇面经)。最接近 LeetCode 编号:**LC 281 Zigzag Iterator**(及其 k-vector follow-up) + **LC 284 Peeking Iterator**。完整原型与参考资源见 [problem-origin.md](problem-origin.md)。

---

## API 全貌

```java
// Part 1
new RangeIteratorPart1(int start, int end, int step)
    .hasNext() -> boolean
    .next()    -> int

// Part 2
new InterleavingIteratorPart2(List<Iterator<Integer>> iters)
    .hasNext() -> boolean
    .next()    -> int

// Part 3
new FilterIteratorPart3(Iterator<Integer> it, IntPredicate keep)
    .hasNext() -> boolean
    .next()    -> int

// Part 4
new CycleIteratorPart4(List<Integer> source)
    .hasNext() -> boolean       // 只要 source 非空就永远 true
    .next()    -> int

// === 以下是超越面经 follow-up ===

// Part 5 — 并发安全的 InterleavingIterator
new ConcurrentInterleavingIteratorPart5(List<Iterator<Integer>> iters)
    .hasNext() -> boolean       // 线程安全
    .next()    -> int           // 线程安全

// Part 6 — 批量 next + 观测
new BatchedInterleavingIteratorPart6(List<Iterator<Integer>> iters)
    .nextBatch(int n) -> List<Integer>     // 一次取 n 个 (不足就少返)
    .stats()          -> StatsPart6        // emitted / exhausted / calls

// Part 7 — 在 mutating Collection 上的 Snapshot / fail-fast 语义
new SnapshotIteratorPart7(Collection<Integer> source)
    .hasNext() / .next() —— 实现要明确选 fail-fast 或 weakly-consistent

// Part 8 — Resumable / cursor 模式
new ResumableInterleavingIteratorPart8(List<List<Integer>> sources)
    .saveCursor() -> String
    static restore(String cursor, List<List<Integer>> sources)
```

---

## 4 个 Part 的题面

### Part 1 — RangeIterator(start, end, step)

类似 Python 的 `range`。半开区间 `[start, end)`。

```
new RangeIteratorPart1(0, 5, 1)   →  0, 1, 2, 3, 4
new RangeIteratorPart1(0, 10, 3)  →  0, 3, 6, 9
new RangeIteratorPart1(5, 5, 1)   →  (空)
```

**Clarifications**:
- `step` 题面里只考虑正整数(`step >= 1`),不做负步长。
- `end <= start` 时 iterator 为空,`hasNext()` 直接 false。
- `next()` 在 `hasNext()` 为 false 时调用 → 抛 `NoSuchElementException`。

---

### Part 2 — InterleavingIterator

输入 k 个 `Iterator<Integer>`,**公平 round-robin** 输出,**跳过已耗尽**的子 iterator。

```
输入: [[1,2,3], [4,5], [6], [], [7,8,9]]
输出: 1, 4, 6, 7, 2, 5, 8, 3, 9
```

**Clarifications**:
- 输入是 `List<Iterator<Integer>>`,不是 `List<List<Integer>>`(差别:不能预先知道长度,只能 `hasNext()`)。
- 空 list 完全没问题(`hasNext()` 永远 false)。
- 内含的空 iterator 要跳过,不影响顺序。
- 输入 list 内的 iterator 顺序就是 round-robin 顺序,不重排。

---

### Part 3 — FilterIterator

wrap 一个已有的 `Iterator<Integer>`,只暴露满足 `IntPredicate` 的元素。

```
src = [1, -2, 3, -4, 5, 6]
new FilterIteratorPart3(src.iterator(), x -> x > 0)
  → 1, 3, 5, 6

src = [1, 2, 3, 4]
new FilterIteratorPart3(src.iterator(), x -> x % 2 == 0)
  → 2, 4
```

**Clarifications**:
- `hasNext()` 必须只在"真的还有"元素时返回 true(不能因为底层 iterator 还没耗尽就乐观返 true,可能后面所有元素都被过滤掉)。
- `hasNext()` **可以**被多次调用,不能跳元素、不能重复消费。
- 这就是 LC 281 follow-up 里反复说的 "odd / even / negative / positive iterator"——本质是同一个 wrapper 的不同 predicate。

---

### Part 4 — CycleIterator

走到 source 末尾后,**从头循环**。

```
new CycleIteratorPart4([1, 2, 3])
  → 1, 2, 3, 1, 2, 3, 1, 2, 3, ... (永远 hasNext()==true)

new CycleIteratorPart4([])
  → (永远 hasNext()==false)
```

**Clarifications**:
- 接口仍然是 `hasNext()/next()`,只是 `hasNext()` 对非空 source 永远返 true。
- 测试不会真的无限调用,会调用前 N 个验证模式。
- source 是 `List<Integer>`(不是 Iterator),因为要循环就必须能从头重新读。

---

## Part 5–8 总览（超越面经的 follow-up）

> Part 1–4 是面经原题。
> Part 5–8 是**超越面经**的 follow-up —— Coinbase 团队明确说重点考"数据结构 / 数据增长 / 并发 / 取舍"，
> 面经描述常常省略这部分，但真实 VO 在做完 Part 4 之后才开始拉开候选人差距。

| Part | 一句话 | 易踩的坑 / 讨论点 |
|------|--------|---------|
| 5 | **并发安全 InterleavingIterator** | `hasNext()` 和 `next()` 的 TOCTOU；锁粒度；多线程下顺序保证退化为 multiset 等价 |
| 6 | **批量 + 观测性 (batch + metrics)** | 装箱开销；LongAdder vs AtomicLong；批 size 自适应 |
| 7 | **fail-fast vs weakly-consistent** | 在 mutating Collection 上选哪种语义；modCount best-effort |
| 8 | **可序列化游标 / 断点续传** | iterator 状态可 dump；exactly-once vs at-least-once；类 Kafka offset |

---

## Part 5 — 并发安全（Coinbase 面试最常追问的方向）

**问题**：N 个消费者线程并发跑 `while (it.hasNext()) consume(it.next());`，要求线程安全 —— 不出现 `NoSuchElementException`，不重复 emit，不撕裂某个 sub-iterator。

**面试官最常问的 4 个 follow-up**：

1. **"`hasNext()` 返回 true 之后 `next()` 抛 NoSuchElement 怎么办？"**
   → 经典 TOCTOU。引出 "把 hasNext+next 合并成 `Optional<E> tryNext()`" 的讨论；
   或者 `next()` 内部重新检查并抛/不抛的策略选择。

2. **"99% 是 `next()`、`hasNext()` 很少叫，选什么锁？"**
   → 单把 `ReentrantLock` 就够；写多读少时 `ReadWriteLock` 的读锁优势不明显，反而 lock 状态更新更贵。

3. **"如果传进来的某个 sub-iterator 自己不是线程安全的怎么办？"**
   → 答：我们已经串行化了所有对它的访问（同一时刻只有一个线程在 `poll → it.next() → push`），
   所以**借助外层锁可以让非线程安全的 sub-iterator 在这个 wrapper 下也安全**。

4. **"并发后 round-robin 顺序还成立吗？"**
   → 不严格成立 —— 线程调度本身非确定。应该主动跟面试官 call out：
   "我保证的是 *输出元素的多重集合* 等于输入元素的多重集合；顺序在并发下只能保证 per-thread FIFO，不保证全局轮转。"

**取舍表**：

| 方案 | 实现复杂度 | 吞吐 | 顺序保证 | 适用 |
|------|-----------|------|---------|------|
| `synchronized` 整个对象 | 简单 | 低 | round-robin | 入门答案 |
| `ReentrantLock` only on `next` | 简单 | 中 | round-robin | 推荐 |
| `ReadWriteLock` | 中 | 中（读没并行收益） | round-robin | 不推荐 |
| Lock-free CAS on Deque | 复杂 | 高 | 退化为 multiset | 高 QPS |
| `BlockingQueue` + 单 producer | 中 | 高 | 退化为 multiset | 解耦消费 |

**自检题**：
- 我能保证不会有两个线程同时调 sub-iterator 的 `next()` 吗？（共享 sub-iterator 的写就是这种 bug 的根源）
- 我的 `hasNext()` 在另一个线程刚拿走最后一个元素时返回 true 会怎样？
- 如果我把锁去掉，最容易爆什么 Exception？

---

## Part 6 — 批量 next + 观测性

**问题**：单元素 `next()` 在高 QPS 下锁竞争 + 装箱开销重。提供 `nextBatch(n)` 一次取 N 个；同时暴露 `stats()` 用于 metrics。

**面试官最常问的 4 个 follow-up**：

1. **"`nextBatch(n)` 整个 batch 一把锁拿完，还是每个元素独立拿锁？"**
   → 一把锁：吞吐高、调用方拿到的是一致快照；但持锁时间长，影响其他线程的 `next()` 延迟。
   每元素拿锁：尾延迟稳定，但吞吐低。**没有银弹** —— 看 batch size 和锁竞争情况。

2. **"返回 `List<Integer>` 还是 `int[]`？"**
   → `List<Integer>` 跟 `Iterator<Integer>` 接口对齐，但每个元素装箱 16 字节对象头 + 缓存不友好。
   `int[]` 零装箱，但要单独处理 "不足 n 个" 的截断。生产代码：高 QPS 选 `int[]`。

3. **"`stats()` 用 `AtomicLong` 还是 `LongAdder`？"**
   → 多线程写、少线程读 → `LongAdder` 完胜（分段累加，读时汇总）。
   单线程访问 → `long` 普通字段就够，别过早优化。

4. **"batch size N 怎么选？怎么动态调整？"**
   → 引出自适应批 size：类似 Linux NAPI / RxJava `request(n)` backpressure。
   消费侧慢就调小 N（降延迟），消费侧快就调大 N（升吞吐）。

**取舍表（返回类型 + 计数器）**：

| 维度 | 选项 A | 选项 B | 何时选 A |
|------|--------|--------|---------|
| 返回类型 | `List<Integer>` | `int[]` | API 一致性 > 性能 |
| 锁粒度 | 整 batch 一把锁 | 每元素独立 | batch 小、要快照一致 |
| 计数器 | `AtomicLong` | `LongAdder` | 单线程或写少 |
| 暴露口径 | since-creation cumulative | sliding window | Prometheus 风格 |

**自检题**：
- `nextBatch(0)` / `nextBatch(-1)` 我处理了吗？
- `stats()` 的瞬时值是否会 "撕裂"（emittedCount 是 100 但只看到 99 个元素出去）？
- 用 `LongAdder` 的话 `stats()` 看到的总数是不是某个一致时间点的？

---

## Part 7 — Snapshot 语义 / fail-fast vs weakly-consistent

**问题**：输入不再是构造好的 `Iterator`，而是一个 *活的* `Collection<Integer>` —— 迭代过程中可能被别的线程 add/remove。应该 fail-fast 抛 CME，还是 weakly-consistent 容忍变更？

**面试官最常问的 4 个 follow-up**：

1. **"`ArrayList` 是 fail-fast，`ConcurrentHashMap` 是 weakly-consistent，为什么？"**
   → 前者优先暴露 bug（"你写错了，迭代里别改"）；
   后者优先 *永不阻塞调用方*（"我反正能跑下去，你看到的可能不是最新"）。
   反映两种设计哲学。

2. **"weakly-consistent 一定要 copy 整个集合吗？"**
   → 不一定。三种实现：
   - 立即 `new ArrayList<>(source)` —— O(n) 内存
   - lazy snapshot / COW —— 第一次 mutate 才 copy
   - `ConcurrentSkipListMap.iterator()` —— 节点级 snapshot，迭代过程中节点变更 *可能* 被看到

3. **"我能做到真正的 fail-fast 吗？"**
   → 严格意义上**做不到**。Java spec 明确说 CME 是 best-effort，不保证一定抛。
   尤其是多线程下，modCount 本身就是 happens-before 边界外的字段。

4. **"如果 source 是别人传进来的，我不能改它的代码，我还能 fail-fast 吗？"**
   → 只能 best-effort：构造时记录 `source.size()`，每次 `hasNext()` 比对。
   能抓到 *尺寸变化*，抓不到 *同尺寸内容变化*。

**取舍表**：

| 语义 | 检测延迟 | 内存 | 多线程友好 | 适用 |
|------|---------|------|----------|------|
| **fail-fast (modCount)** | 即时 | O(1) | 否（单线程惯用法） | `ArrayList`, `HashMap` |
| **weakly-consistent (copy)** | 永不抛 | O(n) | 是 | 一次性快照 |
| **weakly-consistent (live)** | 永不抛 | O(1) | 是 | `ConcurrentSkipListMap` |
| **COW lazy snapshot** | 永不抛 | 摊销 O(1) | 是 | 写少读多 |

**自检题**：
- 我的 `hasNext()` 还是 `next()` 抛 CME？后者更标准（前者破坏了 "hasNext 永不抛" 的契约）。
- weakly-consistent 模式下 `source` 在我构造完之后被 *clear()* 了，我还会输出原数据吗？应该会。
- 我对 *null* 元素的处理跟 Part 3 / Part 4 一致吗？

---

## Part 8 — Resumable Iterator（可序列化游标）

**问题**：跑 1 小时的 batch job，中间机器重启，不想从头来。`saveCursor()` 把迭代状态序列化成字符串；`restore(cursor, sources)` 重建同样状态的 iterator，从断点继续。

**面试官最常问的 4 个 follow-up**：

1. **"上游 source 必须是什么类型才能 resume？"**
   → `Iterator` 本身没法 dump（它是不可逆的状态机）。必须是 *可重建* 的：
   `List<Integer>` + 一个 index、文件 + offset、DB query + last_id。
   候选人意识到这点是关键 —— iterator 是状态，状态要能 dump，就必须知道怎么回放。

2. **"cursor 编码用 JSON 还是自定义紧凑格式？"**
   → JSON：可读、易演化（加字段不破坏旧 cursor）、大。
   Protobuf / 自定义 binary：紧凑、快、不可读、字段演化要管 wire format。
   **金融场景常选 JSON** —— audit 比性能重要。

3. **"exactly-once 还是 at-least-once？"**
   → 看 `saveCursor()` 是在 emit *之前* 还是 *之后* 调用：
   - emit 前 save：崩了重启会重复 emit 上次最后那个 → at-least-once
   - emit 后 save：崩了重启会丢上次最后那个 → at-most-once
   - 真正的 exactly-once 需要 emit + save 原子 —— 不可能纯本地实现，要下游配合做幂等。

4. **"多线程并发消费时，cursor 怎么推进？"**
   → 类似 Kafka consumer offset commit：
   - 单 cursor 共享：要锁；每个线程 emit 后更新最大 emit 位置 → 但有 "中间的洞"
   - per-thread cursor：每线程一个 cursor，崩溃后并发恢复；要解决 work-stealing

**取舍表**：

| 维度 | 选项 A | 选项 B | A 的代价 |
|------|--------|--------|---------|
| 编码格式 | JSON | Binary (Protobuf) | 大 + 慢 |
| save 时机 | emit 前 | emit 后 | 重复 emit |
| 并发模型 | 单 cursor + 锁 | per-thread cursor | 写竞争 |
| 跨进程兼容 | 含 schema 版本 | 不含 | 演化 lock-in |

**自检题**：
- `saveCursor()` 调用之后再 `next()`，原来的 cursor 还能恢复到 *save 时刻* 的状态吗？（saveCursor 必须是 read-only）
- 同一个 cursor 字符串调用两次 `restore()`，得到的两个 iterator 是独立的吗？
- cursor 是否包含 sub-iterator 的身份信息？如果 `sources` 列表顺序变了，restore 还正确吗？

---

## 怎么练

```bash
cd src/02c-其他算法面经/Coinbase/Iterator-Interleaving

javac *.java

java IteratorsTest                # 跑所有 Part
java IteratorsTest part1          # 只跑 Part 1
java IteratorsTest part1 part2    # 跑指定的几个
```

输出格式:

```
Part 1 SKIPPED (not implemented)
Part 2 SKIPPED (not implemented)
Part 3 SKIPPED (not implemented)
Part 4 SKIPPED (not implemented)

Passed=0  Failed=0  Skipped=4
```

骨架在 [Iterators.java](Iterators.java),测试在 [IteratorsTest.java](IteratorsTest.java)。每个 Part 在文件里是一段独立的 `public static class`,后缀 `PartN`:

```
PART 1: Iterators.RangeIteratorPart1                    [✓ done]
PART 2: Iterators.InterleavingIteratorPart2             [✓ done]
PART 3: Iterators.FilterIteratorPart3                   [✓ done]
PART 4: Iterators.CycleIteratorPart4                    [✓ done]
PART 5: Iterators.ConcurrentInterleavingIteratorPart5   [⚠ 超越面经 follow-up]
PART 6: Iterators.BatchedInterleavingIteratorPart6      [⚠ 超越面经 follow-up]
PART 7: Iterators.SnapshotIteratorPart7                 [⚠ 超越面经 follow-up]
PART 8: Iterators.ResumableInterleavingIteratorPart8    [⚠ 超越面经 follow-up，偏设计讨论]
```

**为什么独立 class 而不是继承同一个基类?** 真实面试是渐进的——做完 Part 1 才会让你看 Part 2。这种切片结构让你能专注当前 Part 而不破坏已完成的部分;做完之后还能讨论 "如果要复用 hasNext 的 buffer 逻辑,我会抽个 `AbstractBufferedIterator`"——这种 trade-off 讨论是面试加分点,但不是必做项。

---

## 复盘 Notes(刷完一遍后总结)

### Java `Iterator` 契约 —— 最容易栽的两条

1. **`next()` exhausted 时必须 `throw new NoSuchElementException()`,不是 `return null`。** 4 道题里这个坑反复踩。Returning null 会让调用方拿到一个看起来合法的"值",静默 bug。
2. **`hasNext()` 永远不能 throw**,只能 return true/false。它是调用方"安全探测"的入口,一旦会爆,`while (it.hasNext()) { ... }` 这个标准 idiom 就废了。

### 每题的核心技巧

| Part | 题型 | 核心 |
|------|------|------|
| **Part 1 RangeIterator** | 生成器 | **lazy** —— 只存 `(current, end, step)` 3 个 int,`next()` 时 `current += step`。**绝不**预先把所有值塞进 list/deque(否则 `Range(0, Integer.MAX_VALUE, 1)` 直接 OOM) |
| **Part 2 InterleavingIterator** | k-way 合并 | **`ArrayDeque<Iterator<Integer>>` 当 round-robin 队列**:头出 → 取一个值 → 还有就尾入。耗尽的 iter 自然脱队,**免去 index / remove / `% size` 边界**。严格 O(1) per `next()` |
| **Part 3 FilterIterator** | wrapper | **预取缓存 + 独立 `boolean hasCached` flag**。`hasNext()` 多次调用必须 idempotent(连调 N 次不能消费/跳元素)。**别用 `cur != null` 当 sentinel**——如果底层元素可能是 null,会跟"无缓存"混淆 |
| **Part 4 CycleIterator** | 循环 | **defensive copy 输入 list**(隔离外部修改) + `(index + 1) % list.size()` 让 index 永远落回合法范围 |

### 反复踩到的设计气味

- **null-as-sentinel**:用 null 同时表示"无值"和"是值",拆箱时炸。修法:独立 boolean flag。
- **变量遮蔽 (shadowing)**:参数名 == field 名,不加 `this.` 就操作到错对象。修法:rename 参数,或 IDE 高亮检查。
- **mutate 调用方的输入**:`iters.remove(...)` 直接改 caller 的 list,踩到 immutable 输入(`List.of(...)`)就抛 `UnsupportedOperationException`。修法:`new ArrayList<>(input)` defensive copy。
- **不该 final 的不 final**:构造后不变的引用一律 `private final`,reader 一眼能区分"配置" vs "运行时状态"。

### 面试时主动 call out 的 trade-off(加分点)

1. **null 元素的处理 policy**:skip / throw / 透传 —— 没有标准答案,但要**主动说明**自己的选择。"I'm silently filtering nulls here; in production I'd document or throw — depends on the API contract."
2. **defensive copy 的代价**:`O(k)` 一次性 + 隔离外部修改 vs 共享引用 + 调用方约束。
3. **`ArrayDeque` 优于 `ArrayList.remove(i)`**:后者是 O(k)(shift),前者是 O(1)。"清晰" 和 "高效" 在这题不冲突。

### 测试 runner 的小陷阱

骨架里 `throw new UnsupportedOperationException(...)` 是 "SKIPPED" 标记。如果你的代码里**不小心也抛了** UOE(例如 `List.of(...).remove(...)` 或 `addAll` 到 unmodifiable list),test runner 会**把 FAILED 误报成 SKIPPED**。看到意外的 SKIPPED 别以为是测试没跑,优先怀疑 UOE。
