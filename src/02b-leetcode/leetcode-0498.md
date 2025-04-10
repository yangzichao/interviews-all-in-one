

```java
class Solution {
    public int[] findDiagonalOrder(int[][] mat) {
        if (mat.length == 0) return new int[]{};
        int M = mat.length;
        int N = mat[0].length;
        int[][] directions = new int[][]{{-1, 1}, {1, -1}};
        // initial place at virtual dummy position
        // state 0, go upright direction,
        // state 1, to downleft direction
        int state = 0;
        int curRow = +1;
        int curCol = -1;
        int[] ans = new int[M * N];
        for (int i = 0; i < ans.length; i++) {
            int nextRow = curRow + directions[state][0];
            int nextCol = curCol + directions[state][1];
            if (nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) {
                // 这个题 最为 tricky 的地方就在于此
                // 一共有四种碰壁的情况，我们按 state 分为两两一组
                // 其中注意到两类情况 一类只依靠 col 判断，一列只依靠 row 判定
                if (state == 0) {
                    // curCol = N - 1对应碰右侧壁的情况，否则就是碰上壁
                    nextRow = curCol == N - 1 ? curRow + 1 : curRow + 0;
                    nextCol = curCol == N - 1 ? curCol + 0 : curCol + 1;
                } else {
                    // curRow == M - 1 对应碰下侧壁的情况，否则就是碰左壁
                    nextRow = curRow == M - 1 ? curRow + 0 : curRow + 1;
                    nextCol = curRow == M - 1 ? curCol + 1 : curCol + 0;
                }
                state = (state + 1) % 2;
            }
            ans[i] = mat[nextRow][nextCol];
            curRow = nextRow;
            curCol = nextCol;
        }
        return ans;
    } 
}
/** 
1: {-1, 1}
2: {1, -1}


row < 0: upper bound, move 1 row down, -> state 2
col < 0: left bound, move 1 column right, -> state 1
row >= M: lower bound, move 1 row up, -> state 1
col >= N: right bound, move 1 col left -> state 2

edge: 
1. top right corner.(row + 2, col - 1) switch state
2. lower left corner (row - 1, col + 2) switch state



*/
```