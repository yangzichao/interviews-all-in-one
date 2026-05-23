# 200. Number of Islands

https://leetcode.com/problems/number-of-islands/

## DFS

方法是找到 1 的话，就用 DFS 把周围相邻的 1 都标记成 0。

```java
class Solution {
    public int numIslands(char[][] grid) {
        if (grid == null || grid.length == 0) {
          return 0;
        }
        int count = 0;
        for(int i = 0; i < grid.length; i++){
            for(int j = 0; j < grid[0].length; j++){
                if(grid[i][j]=='1'){
                    dfs(grid,i,j);
                    count++;
                }
            }
        }
        return count;
    }
    public void dfs(char[][] grid, int row, int column){
        int m = grid.length;
        int n = grid[0].length;
        if(row < 0||column < 0|| row >= m|| column >= n || grid[row][column] == '0') return;
        grid[row][column] = '0';//必须要先标记为0，否则会影响周围四个点DFS从而报错。
        dfs(grid, row - 1, column);
        dfs(grid, row + 1, column);
        dfs(grid, row, column - 1);
        dfs(grid, row, column + 1);
    }
}
```

## BFS: Best

<pre>
BFS和Queue就很有联系。
这里我们给整个棋盘每个位置都给一个id，id入Queue。
可以想象这里BFS是更好的解法，因为在空间上更省。
</pre>

```java
class Solution {
    public int numIslands(char[][] grid) {
        if (grid == null || grid.length == 0) {
          return 0;
        }
        int m = grid.length;
        int n = grid[0].length;
        int ans = 0;

        for(int i = 0; i < m; i++){
            for(int j = 0; j < n; j++){
                if(grid[i][j] == '1') {
                    ans++;
                    grid[i][j] = '0';
                    Queue<Integer> neighbors = new LinkedList<>();
                    neighbors.add(i*n+j);
                    while(!neighbors.isEmpty()){
                        int id = neighbors.remove();
                        int row = id/n;
                        int col = id%n;
                        if (row - 1 >= 0 && grid[row-1][col] == '1') {
                          neighbors.add((row-1) * n + col);
                          grid[row-1][col] = '0';
                        }
                        if (row + 1 < m && grid[row+1][col] == '1') {
                          neighbors.add((row+1) * n + col);
                          grid[row+1][col] = '0';
                        }
                        if (col - 1 >= 0 && grid[row][col-1] == '1') {
                          neighbors.add(row * n + col-1);
                          grid[row][col-1] = '0';
                        }
                        if (col + 1 < n && grid[row][col+1] == '1') {
                          neighbors.add(row * n + col+1);
                          grid[row][col+1] = '0';
                        }
                    }
                }

            }
        }
        return ans;
    }
}
```

## BFS v2: 方向数组 + 独立 marked + ArrayDeque

最近重写的一版 BFS，相对上面的 BFS 有几点改进：

- **不修改入参 `grid`**：用单独的 `boolean[][] marked` 记录访问状态，保留输入。
- **方向数组用 `{1, 0, -1, 0, 1}` 这个 trick**：相邻两个元素 `(directions[k], directions[k+1])` 正好构成四个方向 `(1,0), (0,-1), (-1,0), (0,1)`，比 `int[][] dirs = {{...}}` 更省空间。
- **`ArrayDeque` 替代 `LinkedList`**：更快，cache 更友好。
- **队列里存 `int[]{row, col}`**：避免 `id = row*N + col` 的编解码，可读性更好（代价是每个节点多一点对象开销）。
- **入队时立即 `marked = true`**：和上面的 BFS 一样，避免重复入队。

```java
class Solution {
    int[] directions = new int[]{1, 0, -1, 0, 1};
    public int numIslands(char[][] grid) {
        int M = grid.length;
        int N = grid[0].length;
        boolean[][] marked = new boolean[M][N];
        int total = 0;
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                if (marked[i][j] || grid[i][j] == '0') continue;
                marked[i][j] = true;
                total += 1;
                ArrayDeque<int[]> queue = new ArrayDeque<>();
                queue.offer(new int[]{i, j});
                while (!queue.isEmpty()) {
                    int[] cur = queue.poll();
                    int curRow = cur[0];
                    int curCol = cur[1];
                    for (int k = 0; k < 4; k++) {
                        int nextRow = curRow + directions[k];
                        int nextCol = curCol + directions[k + 1];
                        if (nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
                        if (grid[nextRow][nextCol] == '0' || marked[nextRow][nextCol]) continue;
                        marked[nextRow][nextCol] = true;
                        queue.offer(new int[]{nextRow, nextCol});
                    }
                }
            }
        }
        return total;
    }
}
```

## Union Find

为了学习 2D Union Find.

```java
class Solution {
    class UnionFind{
        int count;
        int[] parent;
        int[] size;

        public UnionFind(char[][] grid){ // for 2D char grid

            int M = grid.length;
            int N = grid[0].length;
            count = 0;
            parent = new int[M*N];
            size   = new int[M*N];
            for(int i = 0; i < M; i++){
                for(int j = 0; j < N; j++){
                    // 这个题中 0 是边界，不需要UnionFind.
                    if(grid[i][j] == '1'){
                        parent[i*N +j] = i*N + j;
                        size[i*N+j] = 1;
                        count+=1;
                    }
                }
            }
        }
        public int findroot(int id){
            while(parent[id] != id){
                id = parent[id];
            }
            return id;
        }

        public void union(int x, int y){
            int rootx = findroot(x);
            int rooty = findroot(y);
            if(rootx == rooty){
                return;
            }

            if(size[rootx] > size[rooty]){
                parent[rooty] = rootx;
                size[rootx] += size[rooty];
            }else{
                parent[rootx] = rooty;
                size[rooty] += size[rootx];
            }
            count--;
        }
        public int getCount(){
            return count;
        }
    }
    public int numIslands(char[][] grid) {
        if(grid == null || grid.length == 0){
            return 0;
        }

        int M = grid.length;
        int N = grid[0].length;
        int numOfIslands = 0;
        UnionFind uf = new UnionFind(grid);
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                if(grid[i][j] != '1') continue;
                grid[i][j] = '0';
                int curId = i*N + j;
                if(i > 0 && grid[i - 1][j] == '1'){
                    uf.union(curId, curId - N);
                }
                if(i < M - 1 && grid[i + 1][j] == '1'){
                    uf.union(curId, curId + N);
                }
                if(j > 0 && grid[i][j - 1] == '1'){
                    uf.union(curId, curId - 1);
                }
                if(j < N -1 && grid[i][j + 1] == '1'){
                    uf.union(curId, curId + 1);
                }

            }
        }
        return uf.getCount();
    }
}
```
