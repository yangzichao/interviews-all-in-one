https://docs.google.com/document/d/1tz_xKiShO1DpwSZDJJBcMtWIg-c9yfxLIT-iKsxWLFQ/edit#heading=h.gjdgxs
参考上面写一个 java 语法的总结。





# TreeMap

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
