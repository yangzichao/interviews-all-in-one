https://leetcode.com/problems/count-sub-islands/description/

这个题的描述非常容易让人迷惑，如果作为一个200题的follow-up, 可以说是非常合适了。






这个思路是，抹除掉2中不符合条件的岛，然后再重复200题，计算小岛。
```java
class Solution {
    public int countSubIslands(int[][] grid1, int[][] grid2) {
        int row = grid1.length;
        int col = grid1[0].length;
        
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (grid1[i][j] == 0) {
                    dfs(grid2, i, j);
                }
            }
        }
        int ans = 0;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (grid2[i][j] == 1) {
                    ans++;
                    dfs(grid2, i, j);
                }
            }
        }
        
        return ans;
    }
    
    private static void dfs(int[][] g2, int r, int c) {
        if (r < 0 || c < 0 || r >= g2.length || c >= g2[0].length || g2[r][c] == 0) {
            return;
        }
        g2[r][c] = 0;
        dfs(g2, r + 1, c);
        dfs(g2, r - 1, c);
        dfs(g2, r, c + 1);
        dfs(g2, r, c - 1);
    }
}
```