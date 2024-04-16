参考资料
https://chiclaim.blog.csdn.net/article/details/80643017
https://blog.csdn.net/Yaokai_AssultMaster/article/details/79599809
https://www.youtube.com/watch?v=Oq2E2yGadnU

其他：
树状数组。
https://blog.csdn.net/Yaokai_AssultMaster/article/details/79492190


# 简介
* Segment Tree 是一个基于数组的完全二叉树结构。线段树是一个完全二叉树 complete binary tree，但不一定是满二叉树 full binary tree。
* 它用来处理数组相应的区间查询（range query）和元素更新（update）操作
* update 时间复杂度 O(logN)，更新输入数组中的某一个元素并对线段树做相应的改变。
* range query 时间复杂度也是 O(logN)，比如一个区间的最大值(max)，最小值(min)，和(sum)等等

基于数组的完全二叉树，父子节点的index关系如下：
* leftIndex = parentIndex * 2 + 1;
* rightIndex = parentIndex * 2 + 2;
* parentIndex = (childIndex - 1) / 2;

对于 n 个节点的信息，我们用 2n 尺寸的 segment tree 来表达。