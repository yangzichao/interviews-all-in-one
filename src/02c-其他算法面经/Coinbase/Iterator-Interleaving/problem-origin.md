# Iterator / Interleaving Iterator — 题目原型与参考

> 摘自 [`../coinbase-vo-problem-origins.md`](../coinbase-vo-problem-origins.md) 第 1 题。
> Coinbase 89 篇面经里出现 13 次 ⭐⭐⭐,9 道 VO 题里出现频率最高的一道。

## Coinbase 题面

实现 iterator 接口 (`next()` / `hasNext()`),变体包括 odd/even/negative/range iterator,以及 **InterleaveIterator**——输入 k 个 iterator(或 list of lists),公平 round-robin 输出,跳过耗尽的子序列。例如 `[[1,2,3],[4,5],[6],[],[7,8,9]] → [1,4,6,7,2,5,8,3,9]`。

## 经典对照题 (LeetCode + 类似题)

- **LC 281: Zigzag Iterator** ([link](https://leetcode.com/problems/zigzag-iterator/)) — 原题给两个 1D vector 交替输出。**官方 follow-up** 直接问 "What if you are given k 1d vectors?"——这就是 Coinbase 的 InterleaveIterator 变体;"跳过空 list、不同长度"是这个 follow-up 的精确版本。
- **LC 284: Peeking Iterator** — 经典 iterator wrapper 题,对应 Coinbase 的 "negative iterator / range iterator" wrapper 思路(把已有 iterator 包一层做转换)。
- **Google "Iterator of Iterators"** ([Glassdoor](https://www.glassdoor.com/Interview/Implement-an-Iterator-of-Iterators-which-traverses-through-an-arbitrary-number-of-iterators-IE-an-iterator-which-iterate-QTN_1386690.htm)) — 原题就是 `a1,b1,c1,a2,b2,c2,...`,和 Coinbase 几乎逐字一致。

## 真实工程实现 / 深度资源

- **Google Tech Dev Guide — Flatten Iterators** ([link](https://techdevguide.withgoogle.com/resources/former-interview-question-flatten-iterators/)) — Google 官方公开的"前面试题"页面,原型几乎和 Coinbase 这题完全一致,含分步骤讲解和测试用例,是除 LC 281 之外最权威的练习材料。
- **doocs/leetcode #281 多语言实现** ([link](https://github.com/doocs/leetcode/blob/main/solution/0200-0299/0281.Zigzag%20Iterator/README_EN.md)) — 中文社区高 star 项目,对 k-list 推广(Zigzag II)给出了 deque 解法。
- **kamyu104/LeetCode-Solutions zigzag-iterator.py** ([link](https://github.com/kamyu104/LeetCode-Solutions/blob/master/Python/zigzag-iterator.py)) — `deque[(iter, idx)]` 的"取出-推进-末尾再入队"模式,就是 Coinbase 变种里"跳过耗尽列表"那一步的标准写法。
- **LeetCode Discuss — Coinbase phone-screen implement-iterators** ([link](https://leetcode.com/discuss/interview-question/1829936/coinbase-phone-screen-implement-iterators/)) — 候选人原帖,描述完整 follow-up 链:RangeIterator(start,end,step) → InterleavingIterator → CycleIterator;这正是地里反复提到的"逐步加需求"模式。
- **coding-stream-of-consciousness — Interleaving Iterator Java** ([link](https://coding-stream-of-consciousness.com/2018/09/19/interleaving-iterator-java/)) — Java 实现,queue-based 公平轮转,正是 Coinbase 答案模板。
- **learncswithus Coinbase VO 真题集合** ([link](https://learncswithus.com/2025/06/30/coinbase-vo-interview/)) — 中文整合,明确指出 Coinbase "不追极优复杂度,看抽象能力",并列出 Interleave Iterator → streaming → step jump → iterator of iterators 完整升级路线。

## 评估

完全是已知问题。骨架题 = **LeetCode 281 Zigzag Iterator + 它的 k-vector follow-up**。各种 wrapper 变体是 Coinbase 自创的"组合 API 设计"包装,但每个独立子题都是教科书级 OO/iterator 模式题。
