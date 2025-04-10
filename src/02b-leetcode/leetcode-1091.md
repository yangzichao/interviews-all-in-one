

这个题是少有的8个方向走路。有一点tricky。如果写不好反而会 TLE。

引发我一个思考：是不是 undirected graph 我们需要入queue之前先马克？

```java
class Solution {
    public int shortestPathBinaryMatrix(int[][] grid) {
        // undirected graph
        // either dfs or bfs will find if there is such a path
        // for finding shortest path, dfs is probably not a good idea
        // use bfs we will see how many steps to reach the bottom right cell

        int[][] directions = new int[][]{ {1,1}, {1,-1}, {-1,1}, {-1,-1}, {1,0}, {0,1}, {-1,0}, {0,-1}};
        int M = grid.length;
        int N = grid[0].length;
        if (grid[0][0] == 1 || grid[M - 1][N - 1] == 1) return -1;
        boolean[][] marked = new boolean[M][N];
        marked[0][0] = true;
        Queue<int[]> queue = new ArrayDeque<>();
        queue.offer(new int[]{0, 0});
        int steps = 0;
        while (!queue.isEmpty()) {
            steps += 1;
            int size = queue.size();
            while (size > 0) {
                int[] cur = queue.poll();
                int curRow = cur[0];
                int curCol = cur[1];
                if (curRow == M - 1 && curCol == N - 1) return steps;

                for (int[] direction : directions) {
                    int nextRow = curRow + direction[0];
                    int nextCol = curCol + direction[1];
                    if (nextRow < 0 || nextRow >= M || nextCol < 0 || nextCol >= N) continue;
                    if (grid[nextRow][nextCol] == 1) continue;
                    if (marked[nextRow][nextCol]) continue;
                    marked[nextRow][nextCol] = true;
                    queue.offer(new int[]{nextRow, nextCol});
                }
                size--;
            }
        }
        
        return -1;
    }
}
```