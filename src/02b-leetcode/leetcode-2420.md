
字节电面

```java
class Solution {
    public List<Integer> goodIndices(int[] nums, int k) {
        int[] preCache = new int[nums.length];
        int[] preCache2 = new int[nums.length];
        int curCache = 1;
        for (int i = 0; i < nums.length; i++) {
            if (i > 0 && nums[i - 1] >= nums[i]) {
                curCache++;
            } else {
                curCache = 1;
            }
            preCache[i] = curCache;
        }
        int curCache2 = 1;
        for (int i = 0; i < nums.length; i++) {
            if (i > 0 && nums[i] >= nums[i - 1]) { // non-decreasing
                curCache2++;
            } else {
                curCache2 = 1;
            }
            preCache2[i] = curCache2;
        }
        List<Integer> ans = new ArrayList<>();
        for (int i = k; i < nums.length - k; i++) {
            boolean first = preCache[i - 1] - preCache[i - k] == k - 1;
            boolean second = preCache2[i + k] - preCache2[i + 1] == k - 1;
            if (first && second) {
                ans.add(i);
            }
        }
        return ans;
    }
}
```