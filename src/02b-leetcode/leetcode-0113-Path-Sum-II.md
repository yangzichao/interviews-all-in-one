# 113J. Path Sum II

https://leetcode.com/problems/path-sum-ii/

我觉得这个题像 backtracking 啊

写了一个 PostOrder 的 其实比第一次写，写的还好。

```java
class Solution {
    private ArrayList<Integer> temp;
    private List<List<Integer>> ans;
    public List<List<Integer>> pathSum(TreeNode root, int sum) {
        this.temp = new ArrayList<>();
        this.ans = new ArrayList<>();
        dfs(root,sum);
        return ans;
    }
    public void dfs(TreeNode root, int sum){
        if(root == null) return;
        int nSum = sum - root.val;
        temp.add(root.val);
        dfs(root.left, nSum);
        dfs(root.right, nSum);
        if(nSum == 0 && root.left == null && root.right == null){
            ans.add(new ArrayList<Integer>(temp));
        }
        temp.remove(temp.size() - 1);
    }
}
```


我们做DFS的时候有两种风格，一种是进栈之前，就进行处理。一种是出栈的时候再进行处理。
一般来说，出栈的时候再处理代码是很危险的，这对图本身有很高的要求。
有什么要求呢？不仅仅是DAG，而是每个节点只有一个父节点。Binary Tree自然是满足这个要求的，但是普通的图，例如2D array 只能向右向下的情况，就不能出栈的时候再处理代码。

结论：尽量入栈处理

```java
// Recursive, 入栈处理的代码 注意到 都是入栈前处理将要入栈的node
class Solution {
    public List<List<Integer>> pathSum(TreeNode root, int targetSum) {
        List<List<Integer>> ans = new ArrayList<>();
        List<Integer> path = new ArrayList<>();
        if (root == null) return ans;
        path.add(root.val);
        helper(ans, root, targetSum - root.val, path);
        return ans;
    }
    
    private void helper(List<List<Integer>> ans, TreeNode node, int targetSum, List<Integer> path) {
        if (node.left == null && node.right == null) {
            if (targetSum == 0) {
                ans.add(new ArrayList<>(path));
            }
            return;
        }

        if (node.left != null) {
            path.add(node.left.val);
            helper(ans, node.left, targetSum - node.left.val, path);
            path.remove(path.size() - 1);
        }
        if (node.right != null) {
            path.add(node.right.val);
            helper(ans, node.right, targetSum - node.right.val, path);
            path.remove(path.size() - 1);
        }
    }
}
```


如果用 iterative 的方式写 backtrack, 非常难写。
其根本原因是，stack 或者 queue 都不得不保存一整个状态在 stack/queue 里。
也就是说，iterative 多少沾点 BFS，虽然很多时候可以simulate真正的 dfs preorder/postorder，但是并不总是一样的。