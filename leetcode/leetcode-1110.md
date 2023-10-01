

思想就是，只有在一个Node，不被删除并且是root的情况下，加入它。

这个题的 iteration 不好写，因为 postOrder 是比较直觉的想法。
```java
class Solution {
    private Set<Integer> toDeleteSet;
    private List<TreeNode> ans;
    public List<TreeNode> delNodes(TreeNode root, int[] to_delete) {
        this.toDeleteSet = new HashSet<>();
        for (int n : to_delete) {
            toDeleteSet.add(n);
        }
        this.ans = new ArrayList<>();
        helper(root, true);
        return ans;
    }

    private TreeNode helper(TreeNode node, boolean isRoot) {
        if (node == null) return null;
        if (isRoot && !toDeleteSet.contains(node.val)) {
            ans.add(node);
        }
        boolean isDeleted = false;
        if (toDeleteSet.contains(node.val)) {
            isDeleted = true;
        }
        node.left = helper(node.left, isDeleted);
        node.right = helper(node.right, isDeleted);
        return isDeleted ? null : node;
    }
}
```