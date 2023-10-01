# 054J. Spiral Matrix
https://leetcode.com/problems/spiral-matrix/

## 朴素法
就是纯模拟
遇到边界则依次改变方向。
这是 空间 O(MN)的方法
```java
class Solution {
    public List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> ans = new ArrayList<>();
        int M = matrix.length;
        if(M < 1) return ans;
        int N = matrix[0].length;
        
        boolean[][] seen = new boolean[M][N];
        
        int[] dR = new int[]{0,1,0,-1};
        int[] dC = new int[]{1,0,-1,0};
        
        int r = 0;
        int c = 0;
        int dir = 0;
        
        for(int i = 0; i < M*N; i++){ // i is meaningless, 运行MN次停止
            ans.add(matrix[r][c]);
            seen[r][c] = true;
            int nr = r+dR[dir];
            int nc = c+dC[dir];
            if( nr >= 0 && nc >= 0 && nr <= M - 1 && nc <= N - 1 && !seen[nr][nc]){
                r = nr;
                c = nc;
            }else{
                dir = (dir + 1) %4;
                r += dR[dir];
                c += dC[dir];
            }
        }
        return ans;
        
    }
}
```


这是2022年写的，似乎两者综合一下更好。
```java
class Solution {
    private int[][] directions = new int[][]{{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
    public List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> ans = new ArrayList<>();

        int M = matrix.length;
        int N = matrix[0].length;
        
        boolean[][] marked = new boolean[M][N];
        int curRow = 0;
        int curCol = 0;
        int curDirection = 0;
        
        while(curRow >= 0 && curCol >= 0 && curRow < M && curCol < N && !marked[curRow][curCol]){
            marked[curRow][curCol] = true;
            ans.add(matrix[curRow][curCol]);
            int nextRow = curRow + directions[curDirection][0];
            int nextCol = curCol + directions[curDirection][1];
            if(nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N || marked[nextRow][nextCol]) {
                curDirection = (curDirection + 1) % 4;
                nextRow = curRow + directions[curDirection][0];
                nextCol = curCol + directions[curDirection][1];
            }
            curRow = nextRow;
            curCol = nextCol;
        }
        
        return ans;
    }
}
```