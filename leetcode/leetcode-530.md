
打卡题过于简单，783用的递归
530 用的 iterative 都是秒杀

```java
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode() {}
 *     TreeNode(int val) { this.val = val; }
 *     TreeNode(int val, TreeNode left, TreeNode right) {
 *         this.val = val;
 *         this.left = left;
 *         this.right = right;
 *     }
 * }
 */
class Solution {
    public int getMinimumDifference(TreeNode root) {
        if (root == null) return 0;
        TreeNode prev = null;
        int min = Integer.MAX_VALUE;
        ArrayDeque<TreeNode> stack = new ArrayDeque<>();
        TreeNode cur = root;
        while (!stack.isEmpty() || cur != null) {
            while (cur != null) {
                stack.push(cur);
                cur = cur.left;
            }
            cur = stack.pop();
            if (prev != null) {
                min = Math.min(min, cur.val - prev.val);
            }
            prev = cur;
            cur = cur.right;
        }
        return min;
    }
}
```