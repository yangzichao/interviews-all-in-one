# Java 常用的Syntax 备忘

## Arrays, List, Queue, Stack

* List, Deque -> array
利用 .toArray(); 
示例： 
```java
ArrayDeque<int[]> arrayDeque = new ArrayDeque<>();
for(int i = 0; i < 10; i++){
    arrayDeque.addLast(new int[]{i, i});
}
int[][] arrayFromArrayDeque = arrayDeque.toArray(new int[arrayDeque.size()][]);
```
适用于 [056: Merge-Intervals](https://leetcode.com/problems/merge-intervals/)
