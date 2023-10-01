```java
class Solution {
    public int wiggleMaxLength(int[] nums) {
        // 注意 subsequence 不是 subarray 这里面的区别很重要
        // state 0: 以当前结尾且上升的最长subsequence
        // state 1: 以当前结尾且下降的最长subsequence
        int[][] dp = new int[nums.length + 1][2];
        for(int i = 0; i < nums.length; i++) {
            boolean isIncreased = i == 0 ? true : nums[i] > nums[i - 1];
            boolean isDecreased = i == 0 ? true : nums[i] < nums[i - 1];
            dp[i + 1][0] = isIncreased ? dp[i][1] + 1 : dp[i][0];
            dp[i + 1][1] = isDecreased ? dp[i][0] + 1 : dp[i][1];
        }
        return Math.max(dp[nums.length][0], dp[nums.length][1]);
    }
}
```
