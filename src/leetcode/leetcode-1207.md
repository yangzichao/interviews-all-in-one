
这个题没啥意思啊。
```java
class Solution {
    public boolean uniqueOccurrences(int[] arr) {
        Map<Integer, Integer> count = new HashMap<>();
        for (int n : arr) {
            count.put(n, count.getOrDefault(n, 0) + 1);
        }
        boolean[] buckets = new boolean[arr.length + 1];
        for (int key : count.keySet()) {
            if (buckets[count.get(key)]) return false;
            buckets[count.get(key)] = true;
        }
        return true;
    }
}
```