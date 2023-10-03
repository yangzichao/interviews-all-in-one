
相关的题可见774
这个题是非常好的二分搜索的题！
我们提供一个思路，因为这里在“找” 这个值。算法题里“找到”一个值的时候就该想到binary search了。
```java
class Solution {
    public int minEatingSpeed(int[] piles, int h) {
        if (piles.length > h) return -1;
        Arrays.sort(piles);
        int left = 1;
        int right = piles[piles.length - 1];
        while (left <= right) {
            int mid = (left + right) >>> 1;
            int totalHours = 0;
            for (int pile : piles) {
                totalHours += (pile + mid - 1) / mid;
                if (totalHours > h) break; // 注意这里只是为了防止 integer overflow
            }
            if (totalHours > h) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return left;
    }
}

```