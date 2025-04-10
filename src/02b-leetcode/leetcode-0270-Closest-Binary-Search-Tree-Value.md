# 270 Closest-Binary-Search-Tree-Value

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
<div><p>Given a non-empty binary search tree and a target value, find the value in the BST that is closest to the target.</p>
<p><b>Note:</b></p>
<ul>
	<li>Given target value is a floating point.</li>
	<li>You are guaranteed to have only one unique value in the BST that is closest to the target.</li>
</ul>
<p><strong>Example:</strong></p>
<pre><strong>Input:</strong> root = [4,2,5,1,3], target = 3.714286
    4
   / \
  2   5
 / \
1   3
<strong>Output:</strong> 4
</pre>
</div></section>
 
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
    public int closestValue(TreeNode root, double target) {
        TreeNode cur = root;
​
        int closest = root.val; 
        while(cur != null) {
            closest =  Math.abs(target - cur.val) < Math.abs(target - closest) ?  cur.val : closest;
            if( target < cur.val ) {
                cur = cur.left;
            }else if( target > cur.val) {
                cur = cur.right;
            }else{
                return cur.val;
            }
        }
        return closest;
    }
}
​
```


2023 写的 应该没看答案至少

```java
class Solution {
    public int closestValue(TreeNode root, double target) {
        double min = Integer.MAX_VALUE + 0.1;
        int minVal = 0;
        TreeNode p = root;
        while (p != null) {
            if (min == Math.abs(p.val - target)) {
                minVal = Math.min(minVal, p.val);
            }
            if (min >  Math.abs(p.val - target)) {
                min = Math.abs(p.val - target);
                minVal = p.val;
            }
            if (p.val < target ) {
                p = p.right;
            } else {
                p = p.left;
            }
        }
        return minVal;
    }
}
```

这个题可能写的太久了，当时觉得很理所当然的事儿，其实需要一些说明。
这个题最大的困惑就是，最终binary search 的结果很可能不是离得最近的值。
但是注意到，如果你做一个 binary search, 你一定会经过所有的可能的 candidate.
因此你只需要始终维护双指针就好了。