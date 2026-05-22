这不是最优的实现 但是思路都一样的。

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
    public int longestConsecutive(TreeNode root) {
        // you need to know the parent node
        Map<TreeNode, Integer> nodelcs = new HashMap<>();
        helper(root, null, nodelcs);
        int max = 1;
        for (Map.Entry<TreeNode, Integer> entry : nodelcs.entrySet()) {
            max = Math.max(max, entry.getValue());
        }
        return max;
    }
    private void helper(TreeNode node, TreeNode parent, Map<TreeNode, Integer> nodelcs) {
        if (node == null) return;
        nodelcs.put(node, 1);
        if (parent != null) {
            int lcs = node.val - parent.val == 1 ? nodelcs.get(parent) + 1 : 1;
            nodelcs.put(node, lcs);
        }
        helper(node.left, node, nodelcs);
        helper(node.right, node, nodelcs);
    }
}
```