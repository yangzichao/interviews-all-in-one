# 286 Walls-and-Gates

difficulty: Medium

<style>
        section pre{
          background-color: #eee;
          border: 1px solid #ddd;
          padding:10px;
          border-radius: 5px;
        }
      </style>
<section>
<div><p>You are given a <i>m x n</i> 2D grid initialized with these three possible values.</p>
<ol>
	<li><code>-1</code> - A wall or an obstacle.</li>
	<li><code>0</code> - A gate.</li>
	<li><code>INF</code> - Infinity means an empty room. We use the value <code>2<sup>31</sup> - 1 = 2147483647</code> to represent <code>INF</code> as you may assume that the distance to a gate is less than <code>2147483647</code>.</li>
</ol>
<p>Fill each empty room with the distance to its <i>nearest</i> gate. If it is impossible to reach a gate, it should be filled with <code>INF</code>.</p>
<p><strong>Example:&nbsp;</strong></p>
<p>Given the 2D grid:</p>
<pre>INF  -1  0  INF
INF INF INF  -1
INF  -1 INF  -1
  0  -1 INF INF
</pre>
<p>After running your function, the 2D grid should be:</p>
<pre>  3  -1   0   1
  2   2   1  -1
  1  -1   2  -1
  0  -1   3   4
</pre>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    private int rows;
    private int columns;
    // 这个题，我可以不用 marked, 因为只要不是 INF, 肯定都是不能去的。
    // 这个题很高级，多点同时BFS，给力
    public void wallsAndGates(int[][] rooms) {
        if(rooms.length < 1) return;
        this.rows = rooms.length;
        this.columns = rooms[0].length;
        
        Queue<Integer> bfsQueue =  new LinkedList<>();

        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < columns; j++) {
                if(rooms[i][j] != 0) {
                    continue;
                }
                bfsQueue.offer(i*columns + j);
            }
        }

        while(!bfsQueue.isEmpty()){
            int size = bfsQueue.size();
            while(size > 0) {
                size -= 1;
                int curId = bfsQueue.poll();
                int curRow = curId/columns;
                int curCol = curId%columns;


                if(curRow > 0 && isNotMarked(rooms, curRow - 1, curCol) ){
                    bfsQueue.offer( (curRow - 1)* columns + curCol );
                    rooms[curRow - 1][curCol] = rooms[curRow][curCol] + 1;
                }

                if(curRow + 1 < rows && isNotMarked(rooms, curRow + 1, curCol) ) {
                    bfsQueue.offer( (curRow + 1)* columns + curCol );
                    rooms[curRow + 1][curCol] = rooms[curRow][curCol] + 1;
                }

                if(curCol > 0 && isNotMarked(rooms, curRow, curCol - 1) ){
                    bfsQueue.offer( curRow * columns + curCol - 1);
                    rooms[curRow][curCol - 1] = rooms[curRow][curCol] + 1;
                }

                if(curCol + 1 < columns && isNotMarked(rooms, curRow , curCol + 1) ) {
                    bfsQueue.offer( curRow * columns + curCol + 1);
                    rooms[curRow][curCol + 1] = rooms[curRow][curCol] + 1;
                }
            }

        }

        return;
    }


    public boolean isNotMarked(int[][] rooms, int row, int col) {
        int curVal = rooms[row][col];
        return curVal == Integer.MAX_VALUE;
    }

}
​

这个题目有一个非常值得注意的地方

```java
class Solution {
    private int[] DIRECTIONS = new int[]{1, 0, -1, 0, 1};
    private int INF = Integer.MAX_VALUE;

    public void wallsAndGates(int[][] rooms) {
        // we can do a bfs, starting with all the gates.
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        int numRows = rooms.length;
        int numCols = rooms[0].length;
        for(int i = 0; i < numRows; i++) {
            for(int j = 0; j < numCols; j++) {
                if(rooms[i][j] == 0) {
                    queue.offer(new int[]{i, j});
                }
            }
        }
        int step = 0;
        while(!queue.isEmpty()) {
            int size = queue.size();
            while(size > 0) {
                int[] cur = queue.poll();
                int curRow = cur[0];
                int curCol = cur[1];
                // @不能在这里
                // rooms[curRow][curCol] = step;
                for(int i = 0; i < 4; i++) {
                    int nextRow = curRow + DIRECTIONS[i];
                    int nextCol = curCol + DIRECTIONS[i + 1];
                    if(nextRow < 0 || nextRow >= numRows || nextCol < 0 || nextCol >= numCols) continue;
                    if(rooms[nextRow][nextCol] <= step ) continue;
                    queue.offer(new int[]{nextRow, nextCol});
                    rooms[nextRow][nextCol] = step + 1; // 马克这一步必须在这里做，而不能在上面，否则会重复入queue某个元素
                }
                size--;
            }
            step++;
        }
    }
}
```

2023 和上面一样，BFS 的解法，很好看。
```java
class Solution {
    int WALL = -1;
    int GATE = 0;
    int INF = Integer.MAX_VALUE;
    int M;
    int N;
    int[] directions = new int[]{1, 0, -1, 0, 1};
    public void wallsAndGates(int[][] rooms) {
        this.M = rooms.length;
        this.N = rooms[0].length;
        Queue<int[]> queue = new ArrayDeque<>();
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                if (rooms[i][j] == GATE) {
                    queue.offer(new int[]{i, j});
                }
            }
        }
        int level = 0;
        while (!queue.isEmpty()) {
            level += 1;
            int size = queue.size();
            while (size > 0) {
                int[] cur = queue.poll();
                for (int i = 0; i < 4; i++) {
                    int nextRow = cur[0] + directions[i];
                    int nextCol = cur[1] + directions[i + 1];
                    if (nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
                    if (rooms[nextRow][nextCol] != INF) continue;
                    rooms[nextRow][nextCol] = level;
                    queue.offer(new int[]{nextRow, nextCol});
                }
                size--;
            }
        }
    }
}
```

虽然我们知道DFS肯定不如BFS，但是我们能不能用DFS呢？

## DFS method: