https://docs.google.com/document/d/1tz_xKiShO1DpwSZDJJBcMtWIg-c9yfxLIT-iKsxWLFQ/edit#heading=h.gjdgxs
参考上面写一个 java 语法的总结。




# Map

## HashMap

### equals
HashMap 可以直接用 equals 比较是否全等。注意这个其实是 best practice.
因为 即使 key value 都是 object 他们比较的也是是否 key value 对 的 equals method 的结果是否相等。 所以一定要用 equals 来判断相等。以及使用 compareTo 去判断大小。

## TreeMap

Get top elements in sorted order
```java
TreeMap<Key, Value> treeMap = new TreeMap<>();

for (Map.Entry<Key, Value> entry : treeMap.entrySet()) {
    entry.getKey(); 
    entry.getValue();
} 

// 如果想反向输出可以用 
treeMap.descendingKeySet()

```

981 和 1244 是联系 TreeMap 的不错的题
