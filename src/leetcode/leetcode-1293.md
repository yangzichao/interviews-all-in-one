这个题目我的答案通不过第 49 个 test case,不知道为啥，先这样吧。

```java
class Solution {
    private int[] DIRECTIONS = new int[]{-1, 0, 1, 0, -1};

    public int shortestPath(int[][] grid, int k) {
        if(k == 0 && grid[0][0] == 1) return -1;
        if(grid.length == 1 && grid[0].length == 1) return 0;
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.offer(new int[]{0, 0, k});
        grid[0][0] = -1;
        int steps = 0;
        while(!queue.isEmpty()) {
            int size = queue.size();
            while(size > 0) {
                int[] curInfo = queue.poll();
                for(int i = 0; i < 4; i++) {
                    int nextRow = curInfo[0] + DIRECTIONS[i];
                    int nextCol = curInfo[1] + DIRECTIONS[i + 1];
                    if(nextRow < 0 || nextRow >= grid.length || nextCol < 0 || nextCol >= grid[0].length) continue;
                    if(grid[nextRow][nextCol] == -1) continue;
                    if(grid[nextRow][nextCol] == 1) {
                        if(curInfo[2] <= 0) continue;
                        queue.offer(new int[]{nextRow, nextCol, curInfo[2] - 1});
                        if(nextRow == grid.length - 1 && nextCol == grid[0].length - 1) return steps + 1;
                        continue;
                    }
                    grid[nextRow][nextCol] = -1;
                    queue.offer(new int[]{nextRow, nextCol, curInfo[2]});
                    if(nextRow == grid.length - 1 && nextCol == grid[0].length - 1) return steps + 1;
                }
                size--;
            }
            steps++;
        }
        return -1;
    }
}
```
