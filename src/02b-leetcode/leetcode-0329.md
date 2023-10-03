

这个题的一个加速的思路就是用 LIP 这个东西 cache 住已经访问过的最长上升的路线。

```java
class Solution {
    private int[][] matrix;
    private int[][] LIP;
    private int M;
    private int N;
    private int globalLIP;
    private int[] DIRECTIONS = new int[]{1, 0, -1, 0, 1};

    public int longestIncreasingPath(int[][] matrix) {
        this.matrix = matrix;
        this.M = matrix.length;
        this.N = matrix[0].length;
        this.LIP = new int[M][N];
        this.globalLIP = 1;
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                if (LIP[i][j] > 0) continue;
                dfs(i, j, 1);
            }
        }
        return globalLIP;
    }

    private int dfs(int row, int col, int count) {
        int localLIP = count;
        
        for (int i = 0; i < 4; i++) {
            int nextRow = row + DIRECTIONS[i];
            int nextCol = col + DIRECTIONS[i + 1];
            if (nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
            if (matrix[nextRow][nextCol] <= matrix[row][col]) continue;
            if (LIP[row][col] > 0) {
                localLIP = Math.max(localLIP, count + LIP[nextRow][nextCol]);
                continue;
            }
            int nextLIP = dfs(nextRow, nextCol, 1);
            localLIP = Math.max(localLIP, count + nextLIP);
        }

        LIP[row][col] = localLIP;
        globalLIP = Math.max(globalLIP, localLIP);
        return localLIP;
    }
}
```


I think it is worthy to mention that for most of this kind of questions that we could not add memorization upon a DFS. This question is a special case. Normally when you could move to 4 directions, there would be cycle so you could not memorize the result. However since this question is strictly increasing, thus it is a DAG.

