# 994 Rotting-Oranges 
 
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
<div><p>In a given grid, each cell can have one of three&nbsp;values:</p>
<ul>
	<li>the value <code>0</code> representing an empty cell;</li>
	<li>the value <code>1</code> representing a fresh orange;</li>
	<li>the value <code>2</code> representing a rotten orange.</li>
</ul>
<p>Every minute, any fresh orange that is adjacent (4-directionally) to a rotten orange becomes rotten.</p>
<p>Return the minimum number of minutes that must elapse until no cell has a fresh orange.&nbsp; If this is impossible, return <code>-1</code> instead.</p>
<p>&nbsp;</p>
<div>
<p><strong>Example 1:</strong></p>
<p><strong><img alt="" src="https://assets.leetcode.com/uploads/2019/02/16/oranges.png" style="width: 712px; height: 150px;"></strong></p>
<pre><strong>Input: </strong><span id="example-input-1-1">[[2,1,1],[1,1,0],[0,1,1]]</span>
<strong>Output: </strong><span id="example-output-1">4</span>
</pre>
<div>
<p><strong>Example 2:</strong></p>
<pre><strong>Input: </strong><span id="example-input-2-1">[[2,1,1],[0,1,1],[1,0,1]]</span>
<strong>Output: </strong><span id="example-output-2">-1</span>
<strong>Explanation: </strong> The orange in the bottom left corner (row 2, column 0) is never rotten, because rotting only happens 4-directionally.
</pre>
<div>
<p><strong>Example 3:</strong></p>
<pre><strong>Input: </strong><span id="example-input-3-1">[[0,2]]</span>
<strong>Output: </strong><span id="example-output-3">0</span>
<strong>Explanation: </strong> Since there are already no fresh oranges at minute 0, the answer is just 0.
</pre>
<p>&nbsp;</p>
<p><strong>Note:</strong></p>
<ol>
	<li><code>1 &lt;= grid.length &lt;= 10</code></li>
	<li><code>1 &lt;= grid[0].length &lt;= 10</code></li>
	<li><code>grid[i][j]</code> is only <code>0</code>, <code>1</code>, or <code>2</code>.</li>
</ol>
</div>
</div>
</div>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public int orangesRotting(int[][] grid) {
        int rows = grid.length;
        if( rows < 1 ) return 0;
        int columns = grid[0].length;
        
        Queue<int[]> bfsQueue = new LinkedList<>();
        int oranges = 0;
        
        for( int i = 0; i < rows; i++ ) {
            for( int j = 0; j < columns; j++ ) {
                if( grid[i][j] == 1 || grid[i][j] == 2) {
                    oranges += 1;
                }
                if( grid[i][j] != 2 ) {
                    continue;
                }
                bfsQueue.offer( new int[]{i,j} );
            }
        }
        
        if( bfsQueue.size() == 0 ) {
            return oranges == 0 ? 0 : -1;
        } 
        
        int time = -1;
        
        while( !bfsQueue.isEmpty() ) {
            int size = bfsQueue.size();
            while( size > 0 ) {
                size -= 1;
                oranges -= 1;
                int[] cur = bfsQueue.poll();
                int row = cur[0];
                int col = cur[1];
                if( row > 0 && grid[ row - 1 ][ col ] == 1 ) {
                    bfsQueue.offer( new int[]{ row - 1, col } );
                    grid[ row - 1 ][ col ] = 2;
                }
                if( row < rows - 1 && grid[ row + 1 ][ col ] == 1 ) {
                    bfsQueue.offer( new int[]{ row + 1, col });
                    grid[ row + 1 ][ col ] = 2;
                }
                if( col > 0 && grid[ row ][ col - 1 ] == 1 ) {
                    bfsQueue.offer( new int[]{ row, col - 1} );
                    grid[ row ][ col - 1 ] = 2;
                }
                if( col < columns - 1 && grid[ row ][ col + 1 ] == 1 ) {
                    bfsQueue.offer( new int[]{ row, col + 1 });
                    grid[ row ][ col + 1 ] = 2;
                }               
            }
            time += 1;
        }
        return oranges == 0 ? time : -1;
        
    }
}
​
```

Same idea, better code; 

```java
class Solution {
    private int[] directions = {1, 0, -1, 0, 1};
    public int orangesRotting(int[][] grid) {
        //bfs
        int M = grid.length;
        int N = grid[0].length;
        int totalFresh = 0;
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                if(grid[i][j] == 1) totalFresh++;
                if(grid[i][j] == 0) continue;
                if(grid[i][j] == 2) queue.offer(new int[]{i, j});
            }
        }
        if(totalFresh == 0) return 0;
        int steps = -1;
        while(!queue.isEmpty()){
            int size = queue.size();
            while(size > 0){
                int[] cur = queue.poll();
                for(int i = 0; i < directions.length - 1; i++){
                    int nextRow = cur[0] + directions[i];
                    int nextCol = cur[1] + directions[i + 1];
                    if(nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
                    if(grid[nextRow][nextCol] == 1) {
                        grid[nextRow][nextCol] = 2;
                        totalFresh--;
                        queue.offer(new int[]{nextRow, nextCol});
                    }
                }
                size--;
            }
            steps++;
        }
        return totalFresh == 0 ? steps : -1;
    }
}
```




似乎这个 code 才是更好的, while 加了一层判断 比较丝滑。

```java

class Solution {
    private int[][] DIRECTIONS = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
    private final int BAD = 2;
    private final int GOOD = 1;
    private final int EMPTY = 0;

    public int orangesRotting(int[][] grid) {
        int totalGood = 0;
        int M = grid.length;
        int N = grid[0].length;
        
        Queue<int[]> bfs = new ArrayDeque<>();
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                if (grid[i][j] == EMPTY) continue;
                if (grid[i][j] == GOOD) totalGood++;
                if (grid[i][j] == BAD) {
                    bfs.offer(new int[]{i, j});
                }
            }
        }

        int steps = 0;
 
        while (totalGood > 0 && !bfs.isEmpty()) {
            int size = bfs.size();
            while (size > 0) {
                size--;
                int[] curPos = bfs.poll();
                for (int[] direction : DIRECTIONS) {
                    int nextRow = curPos[0] + direction[0];
                    int nextCol = curPos[1] + direction[1];
                    if (nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
                    if (grid[nextRow][nextCol] == GOOD) {
                        grid[nextRow][nextCol] = BAD;
                        totalGood -= 1;
                        bfs.offer(new int[]{nextRow, nextCol});
                    }
                }
            }
            steps++;
        }

        return totalGood > 0 ? -1 : steps; 
    }
}

```