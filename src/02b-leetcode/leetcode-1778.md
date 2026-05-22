
写了一个 TLE 的, 但是还挺不错的。


```java
/**
 * // This is the GridMaster's API interface.
 * // You should not implement it, or speculate about its implementation
 * class GridMaster {
 *     boolean canMove(char direction);
 *     void move(char direction);
 *     boolean isTarget();
 * }
 */

class Solution {
    // note, both directionChars and directions are carefully matched
    static char[] directionChars = new char[]{'U', 'L', 'D', 'R'}; // reverse direction is (cur + 2) % 4, smart
    static int[][] directions = new int[][]{{-1, 0}, {0, -1}, {1, 0}, {0, 1}};

    public int findShortestPath(GridMaster master) {
        // use dfs to find all reachableSet, and find target 
        Set<Long> reachableSet = new HashSet<>();
        reachableSet.add(getId(0, 0));
        int[] target = new int[3]; // 0: row; 1: col; 2: isTargetFind
        if (master.isTarget()) return 0;
        dfs(master, reachableSet, 0, 0, target); // get all reachableSet
        if (target[2] != 1) return -1; // not reachable to target

        // bfs find target
        Queue<int[]> bfs = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();
        bfs.offer(new int[]{0, 0});
        visited.add(getId(0, 0));
        int step = 0;
        while (!bfs.isEmpty() && step < 250000) { // at most 250000 steps (500 * 500)
            int size = bfs.size();
            while (size > 0) {
                int[] cur = bfs.poll();
                int row = cur[0];
                int col = cur[1];
                if (row == target[0] && col == target[1]) return step;
                for (int i = 0; i < 4; i++) {
                    int nextRow = row + directions[i][0];
                    int nextCol = col + directions[i][1];
                    Long nextId = getId(nextRow, nextCol);
                    if (reachableSet.contains(nextId) && !visited.contains(nextId)) {
                        visited.add(nextId);
                        bfs.offer(new int[]{nextRow, nextCol});
                    }
                }
                size--;
            }
            step += 1;
        }
        return -1;
    }

    private void dfs(GridMaster master, Set<Long> reachableSet,int row, int col, int[] target) {

        for (int i = 0; i < 4; i++) {
            int nextRow = row + directions[i][0];
            int nextCol = col + directions[i][1];
            if (!reachableSet.contains(getId(nextRow, nextCol)) && master.canMove(directionChars[i])) {
                reachableSet.add(getId(nextRow, nextCol));
                master.move(directionChars[i]);
                if (master.isTarget()) {
                    target[0] = nextRow;
                    target[1] = nextCol;
                    target[2] = 1;
                }
                dfs(master, reachableSet, nextRow, nextCol, target);
                master.move(directionChars[(i + 2) % 4]);
            }
        }
    }

    private long getId(int row, int col) {
        return ((long) row << 32) | (col & 0xffffffffL);
    }
}
```