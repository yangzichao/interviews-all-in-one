


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
    private int sum;
    public int rangeSumBST(TreeNode root, int low, int high) {
        this.sum = 0;
        helper(root, low, high);
        return sum;
    }
    private void helper(TreeNode node, int lo, int hi) {
        if (node == null) return;
        if (node.val > lo) {
            helper(node.left, lo, hi);
        }
        if (lo <= node.val && node.val <= hi) {
            this.sum += node.val;
        }
        if (node.val < hi) {
            helper(node.right, lo, hi);
        }

    }
}
```

不用helper更好一点。

```java
class Solution {
    public int rangeSumBST(TreeNode root, int low, int high) {
        if (root == null) return 0;
        int total = 0;
        if (root.val >= low) {
            total += rangeSumBST(root.left, low, high);
        }
        if (root.val <= high) {
            total += rangeSumBST(root.right, low, high);
        }

        if (root.val >= low && root.val <= high) {
            total += root.val;
        }
        return total;
    }
}
```