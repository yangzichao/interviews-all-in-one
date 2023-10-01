这个题目的难点就是在于，想太多。
这个题可以简单的想出一个暴力解法，就是 binary search, 重复 DFS。

这个解法写出来还意外的挺快的。
很容易看出时间复杂度是 O(N^2 log(N))

```java
class Solution {
    int N;
    public int swimInWater(int[][] grid) {
        this.N = grid.length;
        int lo = grid[0][0];
        int hi = Integer.MIN_VALUE;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                hi = Math.max(hi, grid[i][j]);
            }
        }

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (!bfs(grid, mid)) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return lo;
    }

    private boolean bfs (int[][] grid, int level) {
        int[] directions = new int[]{1, 0, -1, 0, 1};
        boolean[][] seen = new boolean[N][N];
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{0, 0});
        seen[0][0] = true;
        while (!stack.isEmpty()) {
            int[] cur = stack.pop();
            for (int i = 0; i < 4; i++) {
                int nextRow = cur[0] + directions[i];
                int nextCol = cur[1] + directions[i + 1];
                if (nextRow < 0 || nextCol < 0 || nextRow >= N || nextCol >= N) continue;
                if (seen[nextRow][nextCol]) continue;
                if (grid[nextRow][nextCol] > level) continue; // 大水漫灌的核心就在于这一句
                seen[nextRow][nextCol] = true;
                stack.push(new int[]{nextRow, nextCol});
            }
        }
        return seen[N - 1][N - 1];
    }
}
```


这个题你还能看出来是一个 Dijkstra的最短路径的问题。

```java
/**
考虑如下格子，可以看作从 1 -> 4 的距离是 3, 1 -> 2 的距离是 1
题目就变成了从左上到右下的最小权重路径的问题。
最小的权重即高度，当然这个有效性不是特别好证明。

[[1,4],
 [2,3],
 [4,3]]
 
注意，在PQ中我们是按 grid[i][j] 来排序的，所以第一次到达右下角的时候就是权重最小的路径。

*/
class Solution {
    int N;
    int[] directions = new int[]{1, 0, -1, 0, 1};
    public int swimInWater(int[][] grid) {
        this.N = grid.length;
        // doing Dijkstra
        boolean[][] seen = new boolean[N][N];
        PriorityQueue<int[]>  pq = new PriorityQueue<>((a, b) -> a[2] - b[2]);
        pq.offer(new int[]{0, 0, grid[0][0]});
        int maxHeight = grid[0][0];
        seen[0][0] = true;
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            maxHeight = Math.max(maxHeight, grid[cur[0]][cur[1]]);
            if (cur[0] == N - 1 && cur[1] == N - 1) return maxHeight;
            for (int i = 0; i < 4; i++) {
                int nextRow = cur[0] + directions[i];
                int nextCol = cur[1] + directions[i + 1];
                if (nextRow < 0 || nextCol < 0 || nextRow >= N || nextCol >= N) continue;
                if (seen[nextRow][nextCol]) continue;
                seen[nextRow][nextCol] = true;
                pq.offer(new int[]{nextRow, nextCol, grid[nextRow][nextCol]});
            }
        }
        return maxHeight;
    }
}
```