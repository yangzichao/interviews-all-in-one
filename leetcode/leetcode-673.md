673 这个题是 LIS 的一个 follow up 了算是，竟然也算是独立想出来了

以下是写的一个代码，可读性一般般吧。
```java
class Solution {
    // step 1 find LCS and count
    // step 2 count
    public int findNumberOfLIS(int[] nums) {
        int[][] dp = new int[nums.length][2];
        for (int[] row : dp) {
            row[0] = 1;
            row[1] = 1;
        }
        int max = 1;
        for (int i = 0; i < nums.length; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[i] <= nums[j]) continue;
                if (dp[i][0] > dp[j][0] + 1) continue;
                if (dp[i][0] == dp[j][0] + 1) {
                    dp[i][1] += dp[j][1];
                    continue;
                }
                dp[i][0] = dp[j][0] + 1;
                dp[i][1] = dp[j][1];
                max = Math.max(max, dp[i][0]);
            }
        }
        int counter = 0;
        for (int[] row : dp) {
            if (row[0] == max) {
                counter += row[1];
            }
        } 
        return counter;
    }
}
```


稍微改一下可读性
这个题的思路就是用另一个数组记录下到当前最大LIS的路径有多少。题还是挺不错的，有很多容易考虑不周的地方。

```java
class Solution {
    // step 1 find LCS and count
    // step 2 count
    public int findNumberOfLIS(int[] nums) {
        int[] LIS = new int[nums.length];
        int[] counts = new int[nums.length];
        for (int i = 0; i < nums.length; i++) {
            LIS[i] = 1;
            counts[i] = 1;
        }
        int longestLIS = 1;
        for (int i = 0; i < nums.length; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[i] <= nums[j]) continue;
                if (LIS[i] > LIS[j] + 1) continue;
                if (LIS[i] == LIS[j] + 1) {
                    counts[i] += counts[j];
                    continue;
                }
                LIS[i] = LIS[j] + 1;
                counts[i] = counts[j];
                longestLIS = Math.max(longestLIS, LIS[i]);
            }
        }
        int counter = 0;
        for (int i = 0; i < nums.length; i++) {
            if (LIS[i] == longestLIS) {
                counter += counts[i];
            }
        } 
        return counter;
    }
}
```