
这个题比 235 要好一点。

```java
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 */
class Solution {
    Set<TreeNode> set;
    private TreeNode lca;
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode[] nodes) {
        this.set = new HashSet<>();
        this.lca = null;
        for (TreeNode node : nodes) set.add(node);
        lcaHelper(root);
        return lca;
    }
    private int lcaHelper(TreeNode node) {
        if (node == null) return 0;
        int foundFromLeft = lcaHelper(node.left);
        int foundFromRight = lcaHelper(node.right);
        int total = foundFromLeft + foundFromRight;
        total += set.contains(node) ? 1 : 0;
        if (total == set.size() && lca == null) {
            lca = node;
        }
        return total;
    }
}
```