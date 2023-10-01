# 064J. Minimum Path Sum
https://leetcode.com/problems/minimum-path-sum/


## Method DP;

道理同前两题 还行。
```java
class Solution {
    public int minPathSum(int[][] grid) {
        int m = grid.length;
        int n = grid[0].length;
        for(int i = 1;  i < m; i++){
            grid[i][0] += grid[i-1][0];
        }
        for(int i = 1;  i < n; i++){
            grid[0][i] += grid[0][i-1];
        }       
        for(int i = 1; i < m; i++){
            for(int j = 1; j < n; j++){
                grid[i][j] += Math.min(grid[i][j-1], grid[i-1][j]);
            }
        }
        return grid[m-1][n-1];
    }
}
```


```java

class Solution {
    public int minPathSum(int[][] grid) {
        int m = grid.length;
        if (grid.length < 1) return 0;
        int n = grid[0].length;
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (i == 0 && j == 0) {
                    dp[i + 1][j + 1] = grid[i][j];
                } else if (i == 0) {
                    dp[i + 1][j + 1] = dp[i + 1][j] + grid[i][j];
                } else if (j == 0) {
                    dp[i + 1][j + 1] = dp[i][j + 1] + grid[i][j];
                } else {
                    dp[i + 1][j + 1] = Math.min(dp[i][j + 1], dp[i + 1][j]) + grid[i][j];       
                }
            }
        }
        return dp[m][n];
    }
}


```