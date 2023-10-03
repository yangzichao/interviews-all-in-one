

比较好写的 dp 


```java
class Solution {
    public int maxMoves(int[][] grid) {
        int M = grid.length;
        int N = grid[0].length;
        int[][] maxMoves = new int[M][N];

        for (int j = N - 2; j >= 0; j--) {
            for (int i = 0; i < M; i++) {
                for (int k = -1; k <= 1; k++) {
                    if (i + k < 0 || i + k >= M || grid[i + k][j + 1] <= grid[i][j]) continue;
                    maxMoves[i][j] = Math.max(maxMoves[i][j], maxMoves[i + k][j + 1] + 1);
                }
            }
        }

        int maxMove = 0;
        for (int i = 0; i < M; i++) {
            maxMove = Math.max(maxMove, maxMoves[i][0]);
        }
        return maxMove;
    }
}
```