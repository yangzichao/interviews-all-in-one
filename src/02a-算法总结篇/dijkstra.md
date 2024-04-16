Timestamp: 20221123 未完成    
Todo: 
* 继续找适用于Dijkstra的算法。 
* 增加关于稠密图和稀疏图的分析。   
* 深究一下算法第四版

# Dijkstra 算法功利的简介
Dijkstra（英：戴克斯抓，荷：戴克斯特拉，是个荷兰姓）算法
* 用来解决单源最短路径问题。是此类问题最可能考的算法，没有之一。如果考，是Dijkstra的概率大于九成。 
* 单源指的是，求图中某一个节点出发到各个节点的最短路径，而不是全局的最短。
* 要求图中的边全部非负，不在乎是不是有向图。好在实际中非负权重非常常见。
* Dijkstra的思想是基于贪心算法的思想，如果贪心的思路错了，它就错了。
* 典型的算法实现是基于BFS + 优先队列。但是面试中要强调一下是Dijkstra based on minHeap/PriorityQueue。因为一般意义上的Dijkstra算法并不强制使用优先队列。    
* 使用优先队列的Dijkstra，需点明图是一个稀疏图(Sparce Graph) 而非稠密图 (Dense Graph)。因为只有这样才有更为明显的时间复杂度优势。  

注意:
Dijkstra算法只在特殊情况下可以修改用以找最长路径。注意到在有环的情况下，最长路径可以是无穷无尽的。

参考：
1. https://zhuanlan.zhihu.com/p/33162490
2. 算法第四版

## 基于优先队列的Dijkstra算法原理
以下描述的算法，是我们面试当中选择的解法，并不意味着只有这一种Dijkstra的写法。   
此算法允许同一个顶点进入队列超过一次。
* 维护一个优先队列heap 和 一个哈希表map。
* 哈希表用来记录，源点到每个点到最短距离，同时可以判断是否已经找到到该点到最短距离，因此不需要令一个节点重复入列。   
* 优先队列储存一个元组 （node, currentTotalDistance)，顶部的元素是 currentTotalDistance 最短的点。
* 将源点和总路径0入列。即（origin, 0).   
* 从heap中取顶点，则该点是当下heap中距离源点最近点的点：
    * 如果map中已存在此点，说明到这个点的最短距离已经找到，跳过它。
    * 如果没有，则说明我们第一次遇到这个点。由于贪心的算法，我们将它和总路径加入map中。
    并将他的边相邻的点都加入到heap当中。
* heap空了之后，判断map中是否记录了全部的点的最短路径。如果不是，则说明有一些点是从源点不可到达的。

时间复杂度 T: O(E log V), 或者 O((E + V) log V). 额外的空间复杂度 S: O(V) or O(E);   
时间复杂度和空间复杂度会因各种语言和具体的实现方法而异，所以必须具体情况具体分析，不能简单网上一搜了事。


时间复杂度简析：
* 我们堆中元素的总数不超过总的边数E。一般来说, 因为允许一个点重复入列，所以PQ的尺寸会超过顶点数V而小于等于边数E。而一般地 E < V^2, 又因为 log(V^2) = 2 log(V), 我们因此取 log(V). 
* 我们一共 V 次取出堆中的元素，E 次进入堆中元素。因此是 (E + V) log V. 一般也可以选择忽略掉 V，
* 注意对于稠密图，总时间复杂度为 O( V^2 log (V^2) )。此时使用优先队列反而不如不使用。因此如果遇到Dijkstra的题，问题目是稠密图还是稀疏图，是加分项。

空间复杂度简析：
* 使用Java自带的数据结构，无法保证在上述的时间复杂度下，实现O(V). 实际上是 O(E) 的空间复杂度。主要的原因是因为Java的PQ对 contains 方法是线性复杂度。
* 算法第四版的 O(V) 的空间复杂度，是使用了自己的 IndexPQ的数据结构，可以O(1)的进行 contains的操作。

注意：对于没有任何优化的Dijkstra算法，其时间复杂度为O(V^2 + E). 在稠密图的情况下，反而有更好的表现。
参考：
1. https://stackoverflow.com/questions/26547816/understanding-time-complexity-calculation-for-dijkstra-algorithm

// 优化743的代码，加上一个map记录已经入列的。
// 继续分析Dijkstra.
// 嗯就没别的了。刷题多刷几个。




## Dijkstra LC 题目
LeetCode上最典型的题目是 
[743: Network-Delay-Time](https://leetcode.com/problems/network-delay-time/)
因此时间不够的话可以先用这一个题复习。    
并看这个视频 https://www.youtube.com/watch?v=9wV1VxlfBlI 

简单的Dijkstra的实现是把 BFS 的 Queue 换成 PriorityQueue. 

其他常考题目
* [787: Cheapest-Flights-Within-K-Stops](https://leetcode.com/problems/cheapest-flights-within-k-stops/)

更多练习

1514 https://leetcode.com/problems/path-with-maximum-probability/description/

1631
https://leetcode.com/problems/path-with-minimum-effort/

505
https://leetcode.com/problems/the-maze-ii/



743. Network Delay Time
2290. Minimum Obstacle Removal to Reach Corner
787. Cheapest Flights Within K Stops
1514. Path with Maximum Probability
1976. Number of Ways to Arrive at Destination
2045. Second Minimum Time to Reach Destination
1631. Path With Minimum Effort
778. Swim in Rising Water


## 基于LeetCode 743 的 Dijkstra 算法Java实现
743 是一个有向图，权重均为正，有环。    
图的实现可以改为Adjancency List邻接表。
```java

```

