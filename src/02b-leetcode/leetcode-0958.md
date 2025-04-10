




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
    public boolean isCompleteTree(TreeNode root) {
        // my thought: bfs should not meet null until have only null.
        LinkedList<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        boolean metNull = false;
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                TreeNode cur = queue.poll();
                if (cur != null) {
                    if (metNull) return false;
                    queue.offer(cur.left);
                    queue.offer(cur.right);
                } else {
                    metNull = true;
                }
            }
        }
        return true;
    }
}
```