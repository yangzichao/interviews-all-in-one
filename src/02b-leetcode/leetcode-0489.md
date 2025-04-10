
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