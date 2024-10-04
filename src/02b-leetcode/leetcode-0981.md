
这个题要求是不好的，不应该说都是时间序列进来。

因此这个题最优解确实，可以用 binary search + `Map<String, List<Pair<Integer, String>>>`
但是不如用这个 TreeMap 练手好。



```java
class TimeMap {
    Map<String, TreeMap<Integer, String>> timeMap;
    public TimeMap() {
        this.timeMap = new HashMap<>();
        
    }
    
    public void set(String key, String value, int timestamp) {
        timeMap.putIfAbsent(key, new TreeMap<>());
        timeMap.get(key).put(timestamp, value);
    }
    
    public String get(String key, int timestamp) {
        if (!timeMap.containsKey(key)) return "";
        Integer floorKey = timeMap.get(key).floorKey(timestamp);
        if (floorKey == null) return "";
        return timeMap.get(key).get(floorKey);

        // Map.Entry<Integer, String> entry = timeMap.get(key).floorEntry(timestamp);
        // if (entry == null) return "";
        // return entry.getValue();
    }
}

/**
 * Your TimeMap object will be instantiated and called as such:
 * TimeMap obj = new TimeMap();
 * obj.set(key,value,timestamp);
 * String param_2 = obj.get(key,timestamp);
 */
```