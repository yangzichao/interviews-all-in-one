

分析的过于复杂了，把它写的非常的啰嗦，这是原始版本。

```java
class Solution {
    public int maximalSquare(char[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        int[][] dp = new int[m + 1][n + 1];
        int max = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] == '0') {
                    dp[i + 1][j + 1] = 0;
                    continue;
                }
                max = Math.max(max, 1);
                if (i == 0 || j == 0) {
                    dp[i + 1][j + 1] = 1;
                    continue;
                }

                if (matrix[i - 1][j - 1] == '1' && matrix[i - 1][j] == '1' && matrix[i][j - 1] == '1') {
                    dp[i + 1][j + 1] = Math.min(dp[i][j], Math.min(dp[i + 1][j], dp[i][j + 1])) + 1;
                    max = Math.max(max, dp[i + 1][j + 1]);
                    continue;
                }
                dp[i + 1][j + 1] = 1;
            }
        }
        return max * max;
    }
}
```



化简之后就是这样了

```java
class Solution {
    public int maximalSquare(char[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        int[][] dp = new int[m + 1][n + 1];
        int max = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] == '0') {
                    continue;
                }
                dp[i + 1][j + 1] = Math.min(dp[i][j], Math.min(dp[i + 1][j], dp[i][j + 1])) + 1;
                max = Math.max(max, dp[i + 1][j + 1]);
            }
        }
        return max * max;
    }
}
```

dp记录的是以当前格子为右下角的最大的正方形的边长。