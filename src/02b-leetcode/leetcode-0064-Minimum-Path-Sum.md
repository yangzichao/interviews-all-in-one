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


这个题的follow up会问如何储存这个路径。可以另开一个  int[][] 或者 String[][]来储存路径.
String[][]的思路就是直接记录下全部的path信息，但是过于复杂。
int[][]就是记录下全部的方向，然后从终点就可以倒着推回去。



```java
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

class Solution {
    public List<int[]> minPathSum(int[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;
        int[][] directions = new int[rows][cols]; // To record the direction from which each cell was filled

        // Initialize the first row
        for (int j = 1; j < cols; j++) {
            grid[0][j] += grid[0][j - 1];
            directions[0][j] = 1; // 1 indicates coming from the left
        }

        // Initialize the first column
        for (int i = 1; i < rows; i++) {
            grid[i][0] += grid[i - 1][0];
            directions[i][0] = 2; // 2 indicates coming from above
        }

        // Fill in the rest of the grid
        for (int i = 1; i < rows; i++) {
            for (int j = 1; j < cols; j++) {
                if (grid[i - 1][j] < grid[i][j - 1]) {
                    grid[i][j] += grid[i - 1][j];
                    directions[i][j] = 2; // Coming from above
                } else {
                    grid[i][j] += grid[i][j - 1];
                    directions[i][j] = 1; // Coming from the left
                }
            }
        }

        // Reconstruct the path
        List<int[]> path = new ArrayList<>();
        int r = rows - 1;
        int c = cols - 1;
        while (r != 0 || c != 0) {
            path.add(new int[]{r, c});
            if (directions[r][c] == 1) {
                c--;
            } else {
                r--;
            }
        }
        path.add(new int[]{0, 0}); // Add the start point

        // Reverse the path to start from the beginning
        List<int[]> resultPath = new ArrayList<>();
        for (int i = path.size() - 1; i >= 0; i--) {
            resultPath.add(path.get(i));
        }

        return resultPath;
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        int[][] grid = {
            {1, 3, 1},
            {1, 5, 1},
            {4, 2, 1}
        };
        List<int[]> path = solution.minPathSum(grid);
        System.out.println("Path:");
        for (int[] point : path) {
            System.out.println(Arrays.toString(point));
        }
    }
}


//
// Finished in 70 ms
// Path:
// [0, 0]
// [0, 1]
// [0, 2]
// [1, 2]
// [2, 2]

```