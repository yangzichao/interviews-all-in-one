非常的 easy 没啥意思啊 打卡的题 530 和这个一样 用的 iterative 解法

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
    private TreeNode prev;
    private int min;
    public int minDiffInBST(TreeNode root) {
        this.min = Integer.MAX_VALUE;
        this.prev = null;
        inorder(root);
        return min;
    }
    private void inorder(TreeNode node) {
        if (node == null) return;
        inorder(node.left);
        if (prev != null) {
           min = Math.min(min, node.val - prev.val);
        }
        prev = node;
        inorder(node.right);
    }
}
```