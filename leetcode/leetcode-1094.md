

这个题直觉来说就是重新构建 trips 然后排序。
注意，这个 comparator 是代表这在同一地点，先下车，再上车的情况。所以非常关键。
```java
class Solution {
    public boolean carPooling(int[][] trips, int capacity) {
        PriorityQueue<int[]> minHeap = new PriorityQueue<>((a, b) -> {
            if (a[0] == b[0]) return - a[1] + b[1];
            return a[0] - b[0];
        });
        for (int[] trip : trips) {
            minHeap.offer(new int[]{trip[1], - trip[0]});
            minHeap.offer(new int[]{trip[2], + trip[0]});
        }
        int emptySeats = capacity;
        while (!minHeap.isEmpty()) {
            capacity += minHeap.poll()[1];
            if (capacity < 0) return false;
        }
        return true;
    }
}
```


假如说，我们车站数量有一个不大的上限的话，倒是可以优化这个题到 O(N) 复杂度。