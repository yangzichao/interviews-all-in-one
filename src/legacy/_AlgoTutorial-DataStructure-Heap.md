Timestamp: 20221123 尚未完成
Todo: 
* 研究Java的PriorityQueue源代码。
* 更多的堆实现，比如斐波那契堆。
* Python, Ecmascript, C++ 等优先队列的实现。

# 堆
由于我们非常功利，把重要的放前面，闲言碎语放后面。

# 编程语言的实现和时间复杂度。

## Java 中的堆
Java 中的 PriorityQueue 是基于 complete binary tree
完全二叉树实现的二叉堆, 也就是说它可以基于数组实现。  

注意事项：
* 无法插入 null 元素。
* 默认为最小堆 minHeap。即比较器中为 -1/负整数的元素会排在堆顶。

时间复杂度：
* peek()和element()操作，都是获取堆顶元素，是常数时间O(1).
* add(), offer(), 无参数的remove()以及poll()方法的时间复杂度都是log(N)。以上都是进出优先队列的方法，对应不同的接口。
* remove(Object o), 即有参数的remove，查找需要O(N),  remove需要 O(logN).
* contains(Object o) O(N).
参考资料：https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/util/PriorityQueue.html

基于数组的完全二叉树，父子节点的index关系如下：
* leftIndex = parentIndex * 2 + 1;
* rightIndex = parentIndex * 2 + 2;
* parentIndex = (childIndex - 1) / 2;

比较器 Comparator
```java
PriorityQueue<Double> maxHeap = new PriorityQueue<>( (a, b) -> {
    if( a == b) return 0;
    return a > b ? -1 : 1;
});
```
以上是一个利用了Java8的Lambda expression写的最大堆比较器。有以下注意的点：
* 比较器必须返回整型，因此不能简单传入 b - a. 
* Java默认为 minHeap 因此实现maxHeap, a比b大的话需要返回负值。

