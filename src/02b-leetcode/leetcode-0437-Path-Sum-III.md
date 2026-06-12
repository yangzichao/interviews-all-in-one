# 437 Path Sum III

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
<div><p>Given the root of a binary tree and an integer <code>targetSum</code>, return the number of paths where the sum of the values along the path equals <code>targetSum</code>.</p>
<p>The path does not need to start or end at the root or a leaf, but it must go downwards (traveling only from parent nodes to child nodes).</p>
<p>&nbsp;</p>
<p><b>Example 1:</b></p>
<pre><b>Input:</b> root = [10,5,-3,3,2,null,11,3,-2,null,1], targetSum = 8
<b>Output:</b> 3
<b>Explanation:</b> The paths that sum to 8 are shown.
</pre>
<p><b>Example 2:</b></p>
<pre><b>Input:</b> root = [5,4,8,11,null,13,4,7,2,null,null,5,1], targetSum = 22
<b>Output:</b> 3
</pre>
<p>&nbsp;</p>
<p><b>Constraints:</b></p>
<ul>
	<li>The number of nodes in the tree is in the range <code>[0, 1000]</code>.</li>
	<li><code>-10<sup>9</sup> &lt;= Node.val &lt;= 10<sup>9</sup></code></li>
	<li><code>-1000 &lt;= targetSum &lt;= 1000</code></li>
</ul>
</div></section>

## Method One — Prefix Sum + HashMap O(n)

**思路：** 把从 root 到当前节点的路径看成一个数组，在数组中寻找和为 targetSum 的子数组，等价于前缀和问题：
- 设 `prefixSum[i]` 为根到节点 i 的路径和
- 若存在祖先节点 j 使得 `prefixSum[i] - prefixSum[j] == targetSum`，则 j+1 到 i 这段路径满足条件
- 用 HashMap 记录每个前缀和出现的次数，每到一个节点就查 `prefixSum - targetSum` 是否存在
- DFS 回溯时撤销当前节点的计数，确保 map 只记录当前路径上的祖先节点

TikTok 一面原题。

``` Java
class Solution {
    public int pathSum(TreeNode root, int targetSum) {
        Map<Long, Integer> map = new HashMap<>();
        map.put(0L, 1);
        return dfs(map, root, 0L, targetSum);
    }

    private int dfs(Map<Long, Integer> map, TreeNode node, long prevSum, int targetSum) {
        if (node == null) return 0;

        long prefixSum = node.val + prevSum;

        int count = map.getOrDefault(prefixSum - targetSum, 0);

        map.put(prefixSum, map.getOrDefault(prefixSum, 0) + 1);

        count += dfs(map, node.left, prefixSum, targetSum);
        count += dfs(map, node.right, prefixSum, targetSum);

        map.put(prefixSum, map.get(prefixSum) - 1);

        return count;
    }
}
```
