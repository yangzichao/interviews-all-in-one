# Design 设计类LC问题

* [155: Min-Stack](https://leetcode.com/problems/min-stack/)
    Q: 设计一个Stack, 除了时间O(1)地支持 push, pop, peekTop 之外，还要O(1) 返回最小元素。    
    A: 核心思想是，出入栈的时候额外携带一个当下最小值的信息。   
    option1: 双栈同步，一个栈记录数值，一个记录最小值。或使用int[] 出入栈；或改变node定义。

## easy
157