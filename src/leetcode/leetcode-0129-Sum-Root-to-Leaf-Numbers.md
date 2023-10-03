# 129 Sum-Root-to-Leaf-Numbers 
 
difficulty: Medium 
 
<style>
        section pre{
          background-color: #eee;
          border: 1px solid #ddd;
          padding:10px;
          border-radius: 5px;
        }
      </style>
<section>
<div><p>Given a binary tree containing digits from <code>0-9</code> only, each root-to-leaf path could represent a number.</p>
<p>An example is the root-to-leaf path <code>1-&gt;2-&gt;3</code> which represents the number <code>123</code>.</p>
<p>Find the total sum of all root-to-leaf numbers.</p>
<p><strong>Note:</strong>&nbsp;A leaf is a node with no children.</p>
<p><strong>Example:</strong></p>
<pre><strong>Input:</strong> [1,2,3]
    1
   / \
  2   3
<strong>Output:</strong> 25
<strong>Explanation:</strong>
The root-to-leaf path <code>1-&gt;2</code> represents the number <code>12</code>.
The root-to-leaf path <code>1-&gt;3</code> represents the number <code>13</code>.
Therefore, sum = 12 + 13 = <code>25</code>.</pre>
<p><strong>Example 2:</strong></p>
<pre><strong>Input:</strong> [4,9,0,5,1]
    4
   / \
  9   0
&nbsp;/ \
5   1
<strong>Output:</strong> 1026
<strong>Explanation:</strong>
The root-to-leaf path <code>4-&gt;9-&gt;5</code> represents the number 495.
The root-to-leaf path <code>4-&gt;9-&gt;1</code> represents the number 491.
The root-to-leaf path <code>4-&gt;0</code> represents the number 40.
Therefore, sum = 495 + 491 + 40 = <code>1026</code>.</pre>
</div></section>
 
 ## Method One 
 
可以归到112 path sum 一类
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
    public int sumNumbers(TreeNode root) {
        if( root == null ){
            return 0;
        }
        int sum = 0;
        ArrayDeque<TreeNode> stack = new ArrayDeque<>();
        ArrayDeque<Integer> vals = new ArrayDeque<>();
        
        stack.push(root);
        vals.push(root.val);
        while(!stack.isEmpty()){
            TreeNode curNode = stack.pop();
            int curVal = vals.pop();
            
            if( curNode.left != null ){
                stack.push(curNode.left);
                vals.push(curVal*10 + curNode.left.val);
            }
            if( curNode.right !=  null ){
                stack.push(curNode.right);
                vals.push(curVal*10 + curNode.right.val);
            }
            
            if( curNode.left == null && curNode.right == null){
                sum += curVal;
            }
        }
        return sum;
    }
}
​
```