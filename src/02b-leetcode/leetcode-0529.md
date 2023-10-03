扫雷这个题就很好的揭示了，对于点格的图，应当先马克再入栈来规避无向图成环的问题。

```java
class Solution {
    private char MINE = 'M';
    private char OVER = 'X';
    private char EMPTY = 'E';
    private char BLANK = 'B';
    private int M;
    private int N;
    private int[][] directions = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1},
    {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};

    public char[][] updateBoard(char[][] board, int[] click) {
        this.M = board.length;
        this.N = board[0].length;
        int row = click[0];
        int col = click[1];
        if (board[row][col] == BLANK || Character.isDigit(board[row][col])) {
            return board;
        }
        if (board[row][col] == MINE) {
            board[row][col] = OVER;
            return board;
        }
        ArrayList<int[]> nextClicks = new ArrayList<>();
        int count = 0;
        for (int[] direction : directions) {
            int nextRow = row + direction[0];
            int nextCol = col + direction[1];
            if (nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
            if (board[nextRow][nextCol] == EMPTY) {
                nextClicks.add(new int[]{nextRow, nextCol});
            }
            if (board[nextRow][nextCol] == MINE) count++;
        }
        if (count > 0) {
            board[row][col] = (char) (count + '0');
            return board;
        }
        board[row][col] = BLANK;
        for (int[] nextClick : nextClicks) {
            updateBoard(board, nextClick);
        }
        return board;
    }
}
```