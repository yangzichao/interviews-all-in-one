


```java
class Solution {
    public int furthestBuilding(int[] heights, int bricks, int ladders) {
        // 这个题，挺难思考的到这个解答的，估计以后也不会做了吧
        // 假如换个问题，我们遇到的障碍是越来越高的，那么如果跑的更远呢？
        // 答案显而易见，先使用 bricks 过那些矮的，然后用 ladder 过高的障碍
        // 这题我们就换了个思路，我们用 pq 解决了这样一个障碍不是排好序过来的问题。
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        for (int i = 1; i < heights.length; i++) {
            int diff = heights[i] - heights[i - 1];
            if (diff <= 0) continue;
            pq.offer(diff);

            if (pq.size() > ladders) {
                bricks -= pq.poll();
            }
            if (bricks < 0) return i - 1;
        }
        return heights.length - 1;
    }
}
```