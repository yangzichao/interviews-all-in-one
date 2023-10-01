# 095J. Unique Binary Search Trees II
https://leetcode.com/problems/unique-binary-search-trees-ii/


问题的分析可以参考上一题 [096](leetCode-096-Unique-Binary-Search-Trees.md)
需要补上memorization的优化版本解法。
https://www.cnblogs.com/grandyang/p/4301096.html
## Method 1: 容易但是不是真的最优。
问题的分析虽然和上一题一样，但是实现其实不太相同，但是也差不多。
用recursion的方法其实不是最优的解法。
思路就是把对应总数为 n 的所有的不同的BST的root 都存在一个List里。
```java
class Solution {

    public List<TreeNode> generateTrees(int n) {
        if(n == 0){
            return new LinkedList<TreeNode>();
        }
        return helper(1,n);
    }
    public LinkedList<TreeNode> helper(int start, int end){
        LinkedList<TreeNode> ans = new LinkedList<>();
        if(start > end){
            ans.add(null);
            return ans;
        }
        
        for(int i = start; i <= end; i++){
            LinkedList<TreeNode> left = helper(start, i - 1);
            LinkedList<TreeNode> right = helper(i+1, end);
            
            for(TreeNode l : left){
                for(TreeNode r : right){
                    TreeNode cur = new TreeNode(i);
                    cur.left = l;
                    cur.right = r;
                    ans.add(cur);
                }
            }
        }
        return ans;
    }
}
```