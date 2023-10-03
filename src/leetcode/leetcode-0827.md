

这个题分成两个部分，
第一获取每个岛屿的尺寸，
第二给每个岛屿编号，记录尺寸，每个岛屿地块儿对应到岛屿编号
第三遍历每个空地，判断周围有多少unique的岛屿被连接。


```java
class Solution {
    // 这种难题首先还是考虑一个暴力的解法
    // 暴力来说，先获取每个岛屿的尺寸，然后给每个岛屿一个编号，把每个岛屿的地块儿都对应到那个编号上
    // 然后遍历每个 0 的位置，看看能不能把上下左右连一大片陆地
    // 这里要注意的一个特殊情况就是，如果判断上下左右是不是同一块儿陆地？即通过判断上下左右的岛屿id
    private int M;
    private int N;
    private int[] DIRECTIONS = new int[]{1, 0, -1, 0, 1};
    public int largestIsland(int[][] grid) {
        this.M = grid.length;
        this.N = grid[0].length;
        int[][] parents = new int[M][N];
        boolean[][] marked = new boolean[M][N];
        int maxArea = 0;
        Map<Integer, Integer> islandIdToArea = new HashMap<>();
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                if (grid[i][j] == 1 && !marked[i][j]) {
                    marked[i][j] = true;
                    int parentId = i * N + j;
                    parents[i][j] = parentId;
                    // getArea 是 getArea 并 mark parent
                    int islandSize = getArea(grid, marked, parents, parentId, i, j);
                    islandIdToArea.put(parentId, islandSize);
                    maxArea = Math.max(maxArea, islandSize);
                }
            }
        }

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                if (grid[i][j] == 0) {
                    int curArea = 1;
                    Set<Integer> parentIds= new HashSet<>();
                    for (int k = 0; k < 4; k++) {
                        int nextRow = i + DIRECTIONS[k];
                        int nextCol = j + DIRECTIONS[k + 1];
                        if (nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
                        if (grid[nextRow][nextCol] == 1) {
                            parentIds.add(parents[nextRow][nextCol]);
                        }
                    }
                    for (int parentId : parentIds) {
                        curArea += islandIdToArea.get(parentId);
                    }
                    maxArea = Math.max(maxArea, curArea);
                }
            }
        }
        return maxArea;
    }

    private int getArea(int[][] grid, boolean[][] marked, int[][] parents, int parentId, int row, int col) {
        int area = 1;
        for (int i = 0; i < 4; i++) {
            int nextRow = row + DIRECTIONS[i];
            int nextCol = col + DIRECTIONS[i + 1];
            if (nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
            if (grid[nextRow][nextCol] != 1 || marked[nextRow][nextCol]) continue;
            parents[nextRow][nextCol] = parentId;
            marked[nextRow][nextCol] = true;
            area += getArea(grid, marked, parents, parentId, nextRow, nextCol);
        }
        return area;
    }
}
```