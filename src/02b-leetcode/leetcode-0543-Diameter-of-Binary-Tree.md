# 543 Diameter-of-Binary-Tree 
 
difficulty: Easy 
 
<style>
        section pre{
          background-color: #eee;
          border: 1px solid #ddd;
          padding:10px;
          border-radius: 5px;
        }
      </style>
<section>
<div><p>
Given a binary tree, you need to compute the length of the diameter of the tree. The diameter of a binary tree is the length of the <b>longest</b> path between any two nodes in a tree. This path may or may not pass through the root.
</p>
<p>
<b>Example:</b><br>
Given a binary tree <br>
</p><pre>          1
         / \
        2   3
       / \     
      4   5    
</pre>
<p></p>
<p>
Return <b>3</b>, which is the length of the path [4,2,1,3] or [5,2,1,3].
</p>
<p><b>Note:</b>
The length of path between two nodes is represented by the number of edges between them.
</p></div></section>
 
 ## Method One 
 
``` Java
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode() {}
 *     TreeNode(int val) { this.val = val; }
 *     TreeNode(int val, TreeNode left, TreeNode right) {
 *         this.val = val;
 *         this.left = left;
 *         this.right = right;
 *     }
 * }
 */
class Solution {
    private int maxLength;
    public int diameterOfBinaryTree(TreeNode root) {
        // 我们在每个节点，都统计它左右最深的深度，加一起就是直径了。一直比较这个直径就可以。
        if(root == null) {
            return 0;
        }
        this.maxLength = Integer.MIN_VALUE;
        dfs(root);
        return maxLength;
    }
    
    public int dfs(TreeNode root) {
        if(root == null) {
            return 0;
        }
        int rightDepth = dfs(root.right);
        int leftDepth = dfs(root.left);
        maxLength = Math.max(maxLength, leftDepth + rightDepth);
        return Math.max(leftDepth, rightDepth) + 1;
    }
    
}
​
```


2024 把一个 method 包起来还是更好看的。

```java
class Solution {
    // Analysis, this equals to find the Maximum depthLeft + depthRight of among all nodes.
    // Needs to do the tree travelsal 
    public int diameterOfBinaryTree(TreeNode root) {
        int[] diameter = new int[1];
        depthOfTree(root, diameter);
        return diameter[0];
    }

    private int depthOfTree(TreeNode node, int[] diameter) {
        if (node == null) return 0;
        int depthLeft = depthOfTree(node.left, diameter);
        int depthRight = depthOfTree(node.right, diameter);
        int maxDepth = Math.max(depthLeft, depthRight) + 1;
        diameter[0] = Math.max(diameter[0], depthLeft + depthRight);
        return maxDepth;
    }
}
```