
这个题思路不难。

```java
class Solution {
    public int shortestBridge(int[][] grid) {
        // bfs from one island.
        int M = grid.length;
        int N = grid[0].length;
        int[][] directions = new int[][]{{1, 0}, {0, -1}, {-1, 0}, {0, 1}};

        // first bfs, find one island, and mark.
        Queue<int[]> shortBridgeQueue = new ArrayDeque<>();
        boolean islandFound = false;
        for (int i = 0; i < M; i++) {
            if (islandFound) break;
            for (int j = 0; j < N; j++) {
                if (grid[i][j] == 0 || islandFound) continue;
                islandFound = true;
                Queue<int[]> queue = new ArrayDeque<>();
                queue.offer(new int[]{i, j});
                grid[i][j] = -1;
                while (!queue.isEmpty()) {
                    int size = queue.size();
                    while (size > 0) {
                        int[] cur = queue.poll();
                        int curRow = cur[0];
                        int curCol = cur[1];
                        for (int[] direction : directions) {
                            int nextRow = curRow + direction[0];
                            int nextCol = curCol + direction[1];
                            if (nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
                            if (grid[nextRow][nextCol] == 0) {
                                shortBridgeQueue.offer(new int[]{curRow, curCol}); // means curreant node is an edge node
                            }
                            if (grid[nextRow][nextCol] != 1) continue;
                            grid[nextRow][nextCol] = -1;
                            queue.offer(new int[]{nextRow, nextCol});
                        }
                        size--;
                    }
                }
                break;
            }
        }

        int step = 0;
        while (!shortBridgeQueue.isEmpty()) {
            int size = shortBridgeQueue.size();
            while (size > 0) {
                int[] cur = shortBridgeQueue.poll();
                int curRow = cur[0];
                int curCol = cur[1];
                for (int[] direction : directions) {
                    int nextRow = curRow + direction[0];
                    int nextCol = curCol + direction[1];
                    if (nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
                    if (grid[nextRow][nextCol] == 1) return step;
                    if (grid[nextRow][nextCol] != 0) continue;
                    grid[nextRow][nextCol] = -1;
                    shortBridgeQueue.offer(new int[]{nextRow, nextCol});
                }
                size--;
            }
            step++;
        }
        return step;
    }
}
```