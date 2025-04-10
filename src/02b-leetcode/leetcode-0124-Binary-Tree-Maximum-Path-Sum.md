# 124J. Binary Tree Maximum Path Sum

https://leetcode.com/problems/binary-tree-maximum-path-sum/

这个题其实有点像贪心，不太好证明解的有效性。
某种意义这个题和 53. Maximum Subarray 的一个思想是一个意思：
即不论前一个数是不是负数，只要前一段数的总贡献为正数就可以把他们都加进来。

对于这个题，假设我们有一个最长的路径，它既不通过根节点，也不连接任何一个叶节点，这种情形足够一般了。
这个路径必然会由三段组成，一个左子路径，一个右子路径，和一个根节点。注意，左右侧子路径不一定要和叶节点连接。
或者反过来理解，从这个根节点起，分别找左、右子节点出发的最大的 pathSum。
那么我们可以进行一个 dfs, 这个 dfs 向上一级返回值是：从当前节点出发，能获取最大 pathSum 的路径。
左，右子路径任一的总贡献如果不为正的话，我们就舍弃掉它。
同时我们维持一个全局指针，一直比较最大的 path sum.
时间复杂度是 O(N) 我们只遍历节点一遍。

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
    private int globalMax;
    public int maxPathSum(TreeNode root) {
        if(root == null) return Integer.MIN_VALUE;
        this.globalMax = root.val;
        findNodeContribution(root);
        return globalMax;
    }
    private int findNodeContribution(TreeNode root){
        //关键的观察是，一个树当中的道路，只能至多是两条从同一节点拼成的路
        //不能拐弯抹角的走
        if(root == null) return 0;
        int left = Math.max( findNodeContribution(root.left), 0 ); // 这个 Math.max 很关键，如果是负数我们就不要他们的贡献了。u
        int right = Math.max( findNodeContribution(root.right), 0);
        int ss = left + right + root.val;
        globalMax = Math.max(ss, globalMax);
        return root.val + Math.max(left,right);
    }
}
```
