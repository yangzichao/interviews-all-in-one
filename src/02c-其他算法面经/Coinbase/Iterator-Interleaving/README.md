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
PART 1: Iterators.RangeIteratorPart1         [⚠ 你来写]
PART 2: Iterators.InterleavingIteratorPart2  [⚠ 你来写]
PART 3: Iterators.FilterIteratorPart3        [⚠ 你来写]
PART 4: Iterators.CycleIteratorPart4         [⚠ 你来写]
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
