这个题目的思路就是什么呢？
就是一个doublylinkedlist 和一个 hashmap
这个doubly linkedlist 是按index排序的，其中index代表的是frequency. 
而每个 doubly linkedlist 的 node 存的信息就是其中有的 key.
用一个hashmap来o(1)找到相应的node.