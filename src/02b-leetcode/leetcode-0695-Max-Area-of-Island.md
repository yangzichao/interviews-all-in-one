# 695 Max-Area-of-Island

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
<div><p>Given a non-empty 2D array <code>grid</code> of 0's and 1's, an <b>island</b> is a group of <code>1</code>'s (representing land) connected 4-directionally (horizontal or vertical.) You may assume all four edges of the grid are surrounded by water.</p>
<p>Find the maximum area of an island in the given 2D array. (If there is no island, the maximum area is 0.)</p>
<p><b>Example 1:</b></p>
<pre>[[0,0,1,0,0,0,0,1,0,0,0,0,0],
 [0,0,0,0,0,0,0,1,1,1,0,0,0],
 [0,1,1,0,1,0,0,0,0,0,0,0,0],
 [0,1,0,0,1,1,0,0,<b>1</b>,0,<b>1</b>,0,0],
 [0,1,0,0,1,1,0,0,<b>1</b>,<b>1</b>,<b>1</b>,0,0],
 [0,0,0,0,0,0,0,0,0,0,<b>1</b>,0,0],
 [0,0,0,0,0,0,0,1,1,1,0,0,0],
 [0,0,0,0,0,0,0,1,1,0,0,0,0]]
</pre>
Given the above grid, return <code>6</code>. Note the answer is not 11, because the island must be connected 4-directionally.
<p><b>Example 2:</b></p>
<pre>[[0,0,0,0,0,0,0,0]]</pre>
Given the above grid, return <code>0</code>.
<p><b>Note:</b> The length of each dimension in the given <code>grid</code> does not exceed 50.</p>
</div></section>
 
 ## Method One 
 
``` Java

class Solution {
   public int maxAreaOfIsland(int[][] grid) {
       if(grid == null || grid.length < 1) {
           return 0;
      }
       int M = grid.length; // Number of rows
       int N = grid[0].length; // Number of cols

int maxArea = 0;

for(int i = 0; i < M; i++) {
           for( int j = 0; j < N; j++ ) {
               if(grid[i][j] == 0) continue;
               int curArea = 0;
               Queue<Integer> ids = new LinkedList<>();
               ids.offer(i\*N + j );
               while(!ids.isEmpty()) {
                   int size = ids.size();
                   while(size > 0) {
                       size--;

int id = ids.poll();
                       int row = id/N;
                       int col = id%N;

if( grid[row][col] == 1) {
                           curArea += 1;
                           grid[row][col] = 0;
                      }else{
                           continue;
                      }
                       if( row - 1 >= 0 ) {
                           ids.offer( (row - 1)*N + col);
                      }
                       if( row + 1 < M) {
                           ids.offer( (row + 1)*N + col);
                      }
                       if( col - 1 >= 0) {
                           ids.offer( row* N + col - 1);
                      }
                       if( col + 1 < N) {
                           ids.offer( row*N + col + 1);
                      }
                  }
              }
               if(curArea > maxArea) {
                   maxArea = curArea;
              }
          }
      }
       return maxArea;
  }
​

````

2023 做的
```java
class Solution {
    private int[] DIRECTIONS = new int[]{1, 0, -1, 0, 1};
    public int maxAreaOfIsland(int[][] grid) {
        int M = grid.length;
        int N = grid[0].length;
        int max = 0;
        boolean[][] marked = new boolean[M][N];

        for(int i = 0; i < M; i++) {
            for(int j = 0; j < N; j++) {
                if(grid[i][j] == 0 || marked[i][j]) {
                    continue;
                }
                int total = 0;
                ArrayDeque<int[]> stack = new ArrayDeque<>();
                stack.push(new int[]{i, j});
                marked[i][j] = true;
                while(!stack.isEmpty()) {
                    int[] cur = stack.pop();
                    int curRow = cur[0];
                    int curCol = cur[1];
                    total += 1;
                    for(int k = 0; k < 4; k++) {
                        int nextRow = curRow + DIRECTIONS[k];
                        int nextCol = curCol + DIRECTIONS[k + 1];
                        if(nextRow < 0 || nextRow >= M || nextCol < 0 || nextCol >= N) continue;
                        if(marked[nextRow][nextCol] || grid[nextRow][nextCol] == 0) continue;
                        stack.push(new int[]{nextRow, nextCol});
                        marked[nextRow][nextCol] = true;
                    }
                }
                max = Math.max(max, total);
            }
        }

        return max;
    }
}
````

这个题目还是挺好的，最好写一下 recursive 的方法，因为可能以后用得到。



```java
class Solution {
    int M;
    int N;
    int[] DIRECTIONS = new int[]{1, 0, -1, 0, 1};

    public int maxAreaOfIsland(int[][] grid) {
        int maxArea = 0;
        this.M = grid.length;
        this.N = grid[0].length;
        boolean[][] marked = new boolean[M][N];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                if (grid[i][j] == 0 || marked[i][j]) continue;
                marked[i][j] = true;
                int area = getArea(grid, marked, i, j);
                maxArea = Math.max(maxArea, area);
            }
        }
        return maxArea;
    }

    private int getArea(int[][] grid, boolean[][] marked, int row, int col) {
        int area = 1;
         for (int i = 0; i < 4; i++) {
            int nextRow = row + DIRECTIONS[i];
            int nextCol = col + DIRECTIONS[i + 1];
            if (nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
            if (grid[nextRow][nextCol] != 1 || marked[nextRow][nextCol]) continue;
            marked[nextRow][nextCol] = true;
            area += getArea(grid, marked, nextRow, nextCol);
         }
         return area;
    }
}
```


这个题还有一个 follow up
就是如果岛屿当中有湖的话，而且这个湖也要被算作岛屿的面积。应该怎么办？
需要考虑的一个额外的问题就是，如果一个岛屿在边上，或者在角上包围了一块儿水域，这个情况怎么办。
假设边上或者角上被包围的水域，我们不算做是一个岛中的湖泊，因而我们不算做是岛屿的面积，
那么解法就是先从四边开始水域dfs，把水域给马克成别的记号，或者先标记住。
然后再做和本题几乎同样的dfs来判断岛屿面积。