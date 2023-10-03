
观看1143

```java
class Solution {
    public int maxUncrossedLines(int[] nums1, int[] nums2) {
        // 这个题代码竟然和 1143 LCS 一样，说实话我并不能看出这两个题有什么直接的对应。
        // 一个很重要的观察是，如果你要画线，那么 nums1[i] == nums2[j], 此时只能考虑 dp[i][j] + 1, 否则就有 cross的可能
        // 如果不画线，就考虑之前的情况。
       int m = nums1.length;
       int n = nums2.length;
       int[][] dp = new int[m + 1][n + 1];
       for (int i = 0; i < m; i++) {
           for (int j = 0; j < n; j++) {
               if (nums1[i] == nums2[j]) {
                   dp[i + 1][j + 1] = dp[i][j] + 1;
               } else {
                   dp[i + 1][j + 1] = Math.max(dp[i][j + 1], dp[i + 1][j]);
               }
           }
       } 
       return dp[m][n];
    }
}
```