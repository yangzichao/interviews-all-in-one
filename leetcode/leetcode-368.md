这个题目就是一个 LIS,只是不太好想。

```java
// 首先要把数组排序，然后 dp[i] 代表着以当前数字为最大值的 Largest Divisible Subset
// 这样我们很容易就先 O(NlogN) 排序又 O(N^2)的时间找到最大的subset的数量是什么，以及最大的数是那个数。


class Solution {
    public List<Integer> largestDivisibleSubset(int[] nums) {
        Arrays.sort(nums);
        int globalMax = 0;
        int globalMaxIndex = 0;
        int[] dp = new int[nums.length];
        Arrays.fill(dp, 1);
        for(int i = 0; i < nums.length; i++) {
            int curMax = dp[i];
            for(int j = i - 1; j >= 0; j--) {
                if(nums[i] % nums[j] == 0) {
                    curMax = Math.max(curMax, dp[j] + 1);
                }
            }
            dp[i] = curMax;
            if(curMax > globalMax) {
                globalMax = curMax;
                globalMaxIndex = i;
            }
        }
        List<Integer> ans = new ArrayList<>();
        // 其实在重建subset 这里也很有技巧。
        for(int i = globalMaxIndex; i >= 0 && globalMax > 0; i--) {
            if(nums[globalMaxIndex] % nums[i] == 0 && globalMax == dp[i]) { //最大的技巧在这行。
                ans.add(nums[i]);
                globalMaxIndex = i;
                globalMax--; // 以及这一行。可以思考一下为什么。
            }
        }
        return ans;
    }
}

```
