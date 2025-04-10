
这个题用路径当作哈希值是非常好的。我觉得不错。
其核心的思想是，相同的形状，理应有相同的dfs或者bfs路径。    
具体到操作上，需要注意的核心内容是，当dfs或者bfs进入一个点之后，要标记下这个点，这样就把不同的点之间的内容给隔开了，否则会有撞车的情景。

下面是个错误的答案：
```java
// 这个答案基本正确，但是有个问题，就是会出现不同形状但是路径撞车的情况。

// 比如这个 test case

// [[1,1,0],[0,1,1],[0,0,0],[1,1,1],[0,1,0]] 

// 两个陆块的打印结果都是 rdr。  
// 解决方案是什么呢？就是在每一层都加上一个标记, 比如'o'。


class Solution {
    private int[] directions = new int[]{1, 0, -1, 0, 1};
    private char[] directionChars = new char[]{'d', 'l', 'u', 'r'};
    private int[][] grid;
    private boolean[][] marked;
    private int M;
    private int N;

    public int numDistinctIslands(int[][] grid) {
        this.grid = grid;
        this.M = grid.length;
        this.N = grid[0].length;
        this.marked = new boolean[M][N];
        Set<String> graphVisitPathHash = new HashSet<>();
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                if(marked[i][j] || grid[i][j] == 0) continue;
                StringBuilder graphVisitPath = new StringBuilder();
                marked[i][j] = true;
                dfs(i, j, graphVisitPath);
                graphVisitPathHash.add(graphVisitPath.toString());
            }
        }

        return graphVisitPathHash.size();
    }
    
    private void dfs(int row, int col, StringBuilder graphVisitPath){
        for(int i = 0; i < 4; i++){
            int nextRow = row + directions[i];
            int nextCol = col + directions[i + 1];
            if(nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
            if(marked[nextRow][nextCol] || grid[nextRow][nextCol] == 0) continue;
            marked[nextRow][nextCol] = true;
            graphVisitPath.append(directionChars[i]);
            dfs(nextRow, nextCol, graphVisitPath);
        }
    }
}
```

这样就不会出错了。

```java
class Solution {
    private int[] directions = new int[]{1, 0, -1, 0, 1};
    private char[] directionChars = new char[]{'d', 'l', 'u', 'r'};
    private int[][] grid;
    private boolean[][] marked;
    private int M;
    private int N;

    public int numDistinctIslands(int[][] grid) {
        this.grid = grid;
        this.M = grid.length;
        this.N = grid[0].length;
        this.marked = new boolean[M][N];
        Set<String> graphVisitPathHash = new HashSet<>();
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                if(marked[i][j] || grid[i][j] == 0) continue;
                StringBuilder graphVisitPath = new StringBuilder();
                marked[i][j] = true;
                dfs(i, j, graphVisitPath);
                graphVisitPathHash.add(graphVisitPath.toString());
            }
        }

        return graphVisitPathHash.size();
    }
    
    private void dfs(int row, int col, StringBuilder graphVisitPath){
        for(int i = 0; i < 4; i++){
            int nextRow = row + directions[i];
            int nextCol = col + directions[i + 1];
            if(nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
            if(marked[nextRow][nextCol] || grid[nextRow][nextCol] == 0) continue;
            marked[nextRow][nextCol] = true;
            graphVisitPath.append(directionChars[i]);
            dfs(nextRow, nextCol, graphVisitPath);
        }
        graphVisitPath.append('o');
    }
}
```


bfs也可以写。我试过标记每层，是不行的。
当进入每一个点之后，都应该标记一下o.这样我们就知道直到下一个o之前的路径，都代表这个o的可行的选择。这样就很好的区分开了每一种不同的形状，防止撞车。

```java
class Solution {
    private int[] directions = new int[]{1, 0, -1, 0, 1};
    private char[] directionChars = new char[]{'d', 'l', 'u', 'r'};
    private int[][] grid;
    private boolean[][] marked;
    private int M;
    private int N;

    public int numDistinctIslands(int[][] grid) {
        this.grid = grid;
        this.M = grid.length;
        this.N = grid[0].length;
        this.marked = new boolean[M][N];
        Set<String> graphVisitPathHash = new HashSet<>();
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                if(marked[i][j] || grid[i][j] == 0) continue;
                StringBuilder graphVisitPath = new StringBuilder();
                bfs(i, j, graphVisitPath);
                graphVisitPathHash.add(graphVisitPath.toString());
            }
        }

        return graphVisitPathHash.size();
    }
    
    private void bfs(int row, int col, StringBuilder graphVisitPath){
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.offer(new int[]{row, col});
        marked[row][col] = true;
        while(!queue.isEmpty()){
            int size = queue.size();
            while(size > 0){
                int[] cur = queue.poll();
                graphVisitPath.append('o');
                for(int i = 0; i < 4; i++){
                    int nextRow = cur[0] + directions[i];
                    int nextCol = cur[1] + directions[i + 1];
                    if(nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
                    if(marked[nextRow][nextCol] || grid[nextRow][nextCol] == 0) continue;
                    marked[nextRow][nextCol] = true;
                    graphVisitPath.append(directionChars[i]);
                    queue.offer(new int[]{nextRow, nextCol});
                }
                size--;
            }
        }

   
    }
}

```


这个题的后续711，是一个竞赛题的简化版，所以不要深究。
711的一个思路是，记录下所有的坐标，然后计算总的，每一对坐标之间的距离的平方，并求这个和。这样相同的形状就会有相同的总和。

711 还有思路:
把形状编码一下再hash就完了，比如把岛内所有点的坐标都按照 (min_x, min_y) normalize 一下，再90度旋转4遍加到 hash set里。对每个岛都这么操作一下，碰到没见过的岛答案就+1。