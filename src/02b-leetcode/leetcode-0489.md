
扫地机器人这个题还是挺蛋疼的, 蛋疼在于一不小心看了那个傻逼动图，被误导了。

我们先考虑一个普通的dfs的场景，其实就是遍历所有的可能，入栈又出栈。
dfs -> 回溯其实也一样的道理
机器人这里就是变了一点儿，即回溯postOrder这一步你需要手动操作机器人回家。
顺时针或者逆时针其实并无所谓，乃至于你上左右下，下上右左这样的顺序扫地都没关系，
只要你的代码能配合好。选择上右下左只是为了好写代码一点。



```java
/**
 * // This is the robot's control interface.
 * // You should not implement it, or speculate about its implementation
 * interface Robot {
 *     // Returns true if the cell in front is open and robot moves into the cell.
 *     // Returns false if the cell in front is blocked and robot stays in the current cell.
 *     public boolean move();
 *
 *     // Robot will stay in the same cell after calling turnLeft/turnRight.
 *     // Each turn will be 90 degrees.
 *     public void turnLeft();
 *     public void turnRight();
 *
 *     // Clean the current cell.
 *     public void clean();
 * }
 */

class Solution {
    private int[] DIRECTIONS = new int[]{-1, 0, 1, 0, -1}; // 上右下左 顺时针
    private Set<String> marked;
    private Robot robot;
    public void cleanRoom(Robot robot) {
        // 坐标为 0,0
        this.robot = robot;
        this.marked = new HashSet<>();
        marked.add(getId(0, 0));
        robot.clean();
        backtrack(0, 0, 0);
    }
    private void backtrack(int row, int col, int dir) {

        for (int i = 0; i < 4; i++) {
            int nextDir = (dir + i) % 4;
            int nextRow = row + DIRECTIONS[nextDir];
            int nextCol = col + DIRECTIONS[nextDir + 1];
            String nextId = getId(nextRow, nextCol);
            if (marked.contains(nextId)) {
                robot.turnRight();
                continue;
            }
            if (robot.move()) {
                marked.add(nextId);
                robot.clean();
                backtrack(nextRow, nextCol, nextDir);
                moveBack();
            }
            robot.turnRight();
        }
    }

    private String getId(int row, int col) {
        return Integer.valueOf(row) + "," + Integer.valueOf(col);
    }

    private void moveBack() {
        // 返回的意思是 我们不仅要位置复原，方向也要复原
        robot.turnRight();
        robot.turnRight();
        robot.move();
        robot.turnRight();
        robot.turnRight();
    }
}
```



# 2025

2025 年经过一番折腾，写了这个

```java
/**
 * // This is the robot's control interface.
 * // You should not implement it, or speculate about its implementation
 * interface Robot {
 *     // Returns true if the cell in front is open and robot moves into the cell.
 *     // Returns false if the cell in front is blocked and robot stays in the current cell.
 *     public boolean move();
 *
 *     // Robot will stay in the same cell after calling turnLeft/turnRight.
 *     // Each turn will be 90 degrees.
 *     public void turnLeft();
 *     public void turnRight();
 *
 *     // Clean the current cell.
 *     public void clean();
 * }
 */

class Solution {
    // 这个题有更多的思考，
    // 第一：我们建立的坐标系 其实和我们的 directions 有最大的关系
    // 比如 int[][] directions = new int[][]{{1, 0}, {0, -1}, {-1, 0}, {0, 1}};
    // 相当于是认为这个坐标系中机器人初始方向朝下。但是初始方向重要吗，不重要。唯一重要的是，我们尊重这个建立的坐标系不变。
    // 我们使用顺时针或者逆时针的策略是比较容易
    // 第二：做DFS 或者 BFS 的时候，由于我们不能通过指针ptr “瞬移”
    // 那么BFS就被排除了, 太难写！
    // DFS 如果用 iterative 的方法 必须保留path然后手动复位 还是不好写！
    // 因此写代码来说 DFS 的递归 backtracking 几乎是唯一的办法
    public void cleanRoom(Robot robot) {
        Set<String> visited = new HashSet<>();
        int[][] directions = new int[][]{{1, 0}, {0, -1}, {-1, 0}, {0, 1}};
        int direction = 0;
        robot.clean();
        visited.add(getId(0, 0));
        backtracking(robot, visited, directions, direction, 0, 0);
    }
    private void backtracking(Robot robot, 
    Set<String> visited, 
    int[][] directions,
    int direction,
    int row, int col) {
        for (int i = 0; i < 4; i++) {
            int nextDirection = (direction + i) % 4;
            int nextRow = row + directions[nextDirection][0];
            int nextCol = col + directions[nextDirection][1];
 
            if (visited.contains(getId(nextRow, nextCol))){
                robot.turnRight();
                continue;
            }

            if (!robot.move()) {
                robot.turnRight();
                continue;
            }
            visited.add(getId(nextRow, nextCol));
            robot.clean();
            backtracking(robot, visited, directions, nextDirection, nextRow, nextCol);
            reverse(robot);
            robot.turnRight();
        }
    }
    private String getId(int row, int col) {
        return String.valueOf(row) + ',' + String.valueOf(col);
    }

    private void reverse(Robot robot) {
        robot.turnRight();
        robot.turnRight();
        robot.move();
        robot.turnRight();
        robot.turnRight();
    }
}
```

经过 AI 小修改 确实精简了不少
AI 的 backtracking 突出了三段
1. 做事情，标记 2. 回溯 3. 回退一步

```java
class Solution {
    // 这个题有更多的思考，
    // 第一：我们建立的坐标系 其实和我们的 directions 有最大的关系
    // 比如 int[][] directions = new int[][]{{1, 0}, {0, -1}, {-1, 0}, {0, 1}};
    // 相当于是认为这个坐标系中机器人初始方向朝下。但是初始方向重要吗，不重要。唯一重要的是，我们尊重这个建立的坐标系不变。
    // 我们使用顺时针或者逆时针的策略是比较容易
    // 第二：做DFS 或者 BFS 的时候，由于我们不能通过指针ptr “瞬移”
    // 那么BFS就被排除了, 太难写！
    // DFS 如果用 iterative 的方法 必须保留path然后手动复位 还是不好写！
    // 因此写代码来说 DFS 的递归 backtracking 几乎是唯一的办法
    public void cleanRoom(Robot robot) {
        Set<String> visited = new HashSet<>();
        int[][] directions = new int[][]{{1, 0}, {0, -1}, {-1, 0}, {0, 1}};
        int direction = 0;
        robot.clean();
        visited.add(getId(0, 0));
        backtracking(robot, visited, directions, direction, 0, 0);
    }
    private void backtracking(
        Robot robot, 
        Set<String> visited, 
        int[][] directions,
        int direction,
        int row, 
        int col
    ) {
        for (int i = 0; i < 4; i++) {
            int nextDirection = (direction + i) % 4;
            int nextRow = row + directions[nextDirection][0];
            int nextCol = col + directions[nextDirection][1];
            if (!visited.contains(getId(nextRow, nextCol)) && robot.move()){
                robot.clean();
                visited.add(getId(nextRow, nextCol));
                backtracking(robot, visited, directions, nextDirection, nextRow, nextCol);
                reverse(robot); // reverse 的是 robot.move 那一步 因为已经挪出去了
            }
            robot.turnRight();
        }
    }
    private String getId(int row, int col) {
        return String.valueOf(row) + ',' + String.valueOf(col);
    }

    private void reverse(Robot robot) {
        robot.turnRight();
        robot.turnRight();
        robot.move();
        robot.turnRight();
        robot.turnRight();
    }
}
```