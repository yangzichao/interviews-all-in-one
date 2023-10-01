
这道题的精髓就是用更多的数表示更多的状态，把状态理顺了，过两遍图就好了。
```java
class Solution {
    // live cell:
    // die = 0, 1
    // live = 2, 3
    // die = 4 - 8
    // dead cell: -> live = 3
    public void gameOfLife(int[][] board) {
        // in-place 的 state rule
        // dead cell -> dead cell (0 -> 0) : 0 -> 0
        // live cell -> live cell (1 -> 1) : 1 -> 1
        // dead cell -> live cell (0 -> 1) : 2 -> 1
        // live cell -> dead cell (1 -> 0) : 3 -> 0
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                int count = countNeighbor(board, i, j);
                if (board[i][j] % 2 == 1) {
                    if (count != 2 && count != 3) {
                        board[i][j] = 3;
                    }
                } else {
                    if (count == 3) {
                        board[i][j] = 2;
                    }
                }
            }
        }

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 3) {
                    board[i][j] = 0;
                } 
                if (board[i][j] == 2) {
                    board[i][j] = 1;
                }
            }
        }
    }

    private int countNeighbor(int[][] board, int row, int col) {
        int[][] directions = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
        int count = 0;
        for (int[] direction : directions) {
            int nextRow = row + direction[0];
            int nextCol = col + direction[1];
            if (nextRow < 0 || nextCol < 0 || nextRow >= board.length || nextCol >= board[0].length) continue;
            count += board[nextRow][nextCol] % 2;
        }
        return count;
    }
}
```