# 099J. Recover Binary Search Tree

这个题还真不好写。
思想就是，一个排好序的数组里，如果你只发现了一个 peak, 
那么说明 这个 peak 和 下一位的位置被调换了。
如果你发现了两个peak, 说明 第一个peak 和 第二个 peak 的 next 被调换了。

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
    ArrayList<TreeNode> inorder;
    public void recoverTree(TreeNode root) {
        this.inorder = new ArrayList<>();
        getInorder(root);
   
        TreeNode prev = null;
        TreeNode next = null;
        for (int i = 0; i < inorder.size() - 1; i++) {
            if (inorder.get(i).val > inorder.get(i + 1).val) {  
                if (prev == null) {
                    prev = inorder.get(i);
                    next = inorder.get(i + 1);
                } else {
                    next = inorder.get(i + 1);
                }
            }
        }
        int temp = prev.val;
        prev.val = next.val;
        next.val = temp;
    }

    private void getInorder(TreeNode node) {
        if (node == null) return;
        getInorder(node.left);
        inorder.add(node);
        getInorder(node.right);
    }
}

```


运用上述的思想，也可以轻松的写出递归的解法。
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
    private TreeNode next;
    private TreeNode parent;
    public void recoverTree(TreeNode root) {
        prev = null;
        next = null;
        parent = null;
        helper(root);
        int temp = prev.val;
        prev.val = next.val;
        next.val = temp;
    }
    private void helper(TreeNode node) {
        if (node == null) return;
        helper(node.left);
        if (parent != null) {
            if (parent.val > node.val) {
                if (prev == null) {
                    prev = parent;
                    next = node;
                } else {
                    next = node;
                }
            }
        }
        parent = node;
        helper(node.right);
    }
}
```