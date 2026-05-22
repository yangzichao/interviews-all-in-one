# 130J. Surrounded Regions
https://leetcode.com/problems/surrounded-regions/

## DFS
这个题DFS就行，如果想用UnionFind 可以参见2-D Union Find.

```java
class Solution {
    public void solve(char[][] board) {
        int M = board.length;
        if(M < 1) return;
        int N = board[0].length;        
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                if(  i == 0 || i == M - 1  || j == 0 || j == N-1  ){
                    dfs(board,i,j,'O','M');
                }
            }
        }
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                if( board[i][j] == 'O'){
                    board[i][j] = 'X';
                }
                if( board[i][j] == 'M'){
                    board[i][j] = 'O';
                }
            }
        }
      
    }
    
    private void dfs(char[][] board, int row, int column, char target, char goal){
        int M = board.length;
        int N = board[0].length;
        if(row < 0 || column < 0 || row + 1 > M || column + 1 > N) {
            return;
        }
        char c = board[row][column];
        if(c == target){
            board[row][column] = goal;
            dfs(board, row - 1, column, target,goal);
            dfs(board, row , column - 1, target,goal);
            dfs(board, row + 1, column, target,goal);
            dfs(board, row , column + 1, target,goal);            
        }else{
            return;
        }  
    }
}
```


## 2025

这个题应该写的太早了。
题目的意思就是，一个岛屿如果不靠边，就算是被围绕的区域，那么就需要被改写成X。


思路是从边缘开始DFS并mark。然后直接遍历马克所有其他未被访问的'O'为'X'.