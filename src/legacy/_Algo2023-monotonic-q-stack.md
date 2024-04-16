## 3 Monotonic Queue

> [参考 1](https://medium.com/algorithms-and-leetcode/monotonic-queue-explained-with-leetcode-problems-7db7c530c1d6) 
> [参考 2](https://leetcode.com/problems/shortest-subarray-with-sum-at-least-k/discuss/204290/Monotonic-Queue-Summary)

### 3.1 定义

- **Monotonic increasing queue**
  单调上升队列： 从后面添加新元素，如果前面有比自己大(或者不比自己小)的元素，让他们都出列之后再入列。
- **Monotonic decreasing queue**
  单调下降队列： 从后面添加新元素，如果前面有比自己小(或者不比自己大)的元素，让他们都出列之后再入列。

解释 1，在一个武力说话的世界里排队，我把前面所有比我弱鸡的人都打飞，直到打不过为止就老实排着。
解释 2，类比游戏的定段制度。

### 3.2 实现

原则上只需要从队尾加入和删除就行。但是结合题目，一般不仅仅只考察它，用 Deque 比较灵活。

### 3.3 相关题目

- [LC084]
- [LC085]
- [LC122]
- [LC239](leetCode-239-Sliding-Window-Maximum.md)
- [LC496]
- [LC503]
- [LC581]
- [LC739]
- [LC862](leetCode-862-Shortest-Subarray-with-Sum-at-Least-K.md)
- [LC901]
- [LC907]
