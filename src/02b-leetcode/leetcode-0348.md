
Design Tic Tac Toe的比较基础的思路如下，就是每一步我都检查整个棋盘的四个方向看看有没有赢。
这个空间复杂度就是保留了一个整个棋盘，时间复杂度就是每一步都遍历了四条边.

```java
class TicTacToe {
    private int[][] board;
    private int n;
    public TicTacToe(int n) {
        this.n = n;
        this.board = new int[n][n];
    }
    
    public int move(int row, int col, int player) {
        board[row][col] = player;
        if (isWin(row, col, player)) return player;
        return 0;
    }

    private boolean isWin(int row, int col, int player) {
        boolean[] check = new boolean[]{true, true, true, true};
        // check 0, 1 are checking rows and cols
        // check 2, 3 are checking diag and anti-diag
        for (int i = 0; i < n; i++) {
            check[0] = check[0] && (board[row][i] == player);
            check[1] = check[1] && (board[i][col] == player);
            check[2] = check[2] && (board[i][i] == player);
            check[3] = check[3] && (board[n - 1 - i][i] == player);
        }

        return check[0] || check[1] || check[2] || check[3];
    }
}

/**
 * Your TicTacToe object will be instantiated and called as such:
 * TicTacToe obj = new TicTacToe(n);
 * int param_1 = obj.move(row,col,player);
 */
```


改进就是，因为棋类游戏只有两个玩家，那么设一个玩家为正贡献，一个为负贡献，就可以显著提升时空复杂度。

```java

class TicTacToe {
    private int[] rows;
    private int[] cols;
    private int[] diags;
    private int n;
    public TicTacToe(int n) {
        this.n = n;
        this.rows = new int[n];
        this.cols = new int[n];
        this.diags = new int[2];
    }
    
    public int move(int row, int col, int player) {
        int val = player == 1 ? 1 : -1;
        rows[row] += val;
        cols[col] += val;
        if (row == col) diags[0] += val;
        if (row + col == n - 1) diags[1] += val;
        if (rows[row] == n * val || cols[col] == n * val || diags[0] == n * val || diags[1] == n * val) return player;
        return 0;
    }
}
```

不敢想象，上面是我自己想出来的答案吗？

下面是2025年写的
```java
class TicTacToe {
    // boolean[][] board;
    int[] rowCheck;
    int[] colCheck;
    int[] diagCheck;

    public TicTacToe(int n) {
        // board = new boolean[n][n];
        rowCheck = new int[n];
        colCheck = new int[n];
        diagCheck = new int[2];
    }
    
    public int move(int row, int col, int player) {
        int N = rowCheck.length;
        int val = player == 1 ? 1 : -1;
        int winner = 0;
        rowCheck[row] += val; if (Math.abs(rowCheck[row]) == N) winner =  rowCheck[row] > 0 ? 1 : 2;
        colCheck[col] += val; if (Math.abs(colCheck[col]) == N) winner =  colCheck[col] > 0 ? 1 : 2;
        if (row == col) {
            diagCheck[0] += val;
            if (Math.abs(diagCheck[0]) == N) winner =  diagCheck[0] > 0 ? 1 : 2;
        }
        if (row + col == rowCheck.length - 1) {
            diagCheck[1] += val;
            if (Math.abs(diagCheck[1]) == N) winner =  diagCheck[1] > 0 ? 1 : 2;
        }
        return winner;
    }
}

/**
 * Your TicTacToe object will be instantiated and called as such:
 * TicTacToe obj = new TicTacToe(n);
 * int param_1 = obj.move(row,col,player);
 */
```