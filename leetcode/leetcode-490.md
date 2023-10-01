
这个题很巧，以后再补充吧。


```java
class Solution {
    private static int[] DIRECTIONS = new int[]{1, 0, -1, 0, 1};
    private boolean[][] marked;
    private int[][] maze;
    private int[] destination;
    private int M;
    private int N;

    public boolean hasPath(int[][] maze, int[] start, int[] destination) {
        this.M = maze.length;
        this.N = maze[0].length;
        this.maze = maze;
        this.marked = new boolean[M][N];
        this.destination = destination;
        return dfs(start[0], start[1]);
    }

    private boolean dfs(int row, int col) {
        if (row < 0 || col < 0 || row >= M || col >= N) return false;
        if (marked[row][col] || maze[row][col] == 1) return false;
        if (row == destination[0] && col == destination[1]) return true;
        marked[row][col] = true;
        boolean resolved = false;
        for (int i = 0; i < 4; i++) {
            int nextRow = row;
            int nextCol = col;
            // 这个 while 可以说是精髓了，这个题关键就在于可以不用在乎中间沿着一个方向疯狂突进的过程，直接把他们略过就好了。
            // 只有边界上的方块值得被考虑。
            while (nextRow >= 0 && nextCol >= 0 && nextRow < M && nextCol < N && maze[nextRow][nextCol] == 0) {
                nextRow += DIRECTIONS[i];
                nextCol += DIRECTIONS[i + 1];
            }
            // 下面是一个很巧妙的对四个方向的dfs做 || 的方式。
            resolved = resolved || dfs(nextRow - DIRECTIONS[i], nextCol - DIRECTIONS[i + 1]); 
        }
        return resolved;
    }
}
```