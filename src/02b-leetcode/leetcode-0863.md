

# 2025 
这个题代码量还挺大的

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
    public List<Integer> distanceK(TreeNode root, TreeNode target, int k) {
        // step 1 do a traversal, link parent nodes
        // we choose bfs for no reason
        Map<TreeNode, TreeNode> nodeToParentMap = new HashMap<>();
        Deque<TreeNode> queue = new ArrayDeque<>();
        TreeNode cur = root;
        nodeToParentMap.put(root, null);
        queue.offer(cur);
        while (!queue.isEmpty()) {
            int size = queue.size();
            while (size > 0) {
                cur = queue.poll();
                if (cur.left != null) {
                    nodeToParentMap.put(cur.left, cur);
                    queue.offer(cur.left);
                }
                if (cur.right != null) {
                    nodeToParentMap.put(cur.right, cur);
                    queue.offer(cur.right);
                }
                size--;
            }
        }
        // step 2, dfs or bfs start from target
        // we choose dfs just try to be different, while bfs is much easier!!!
        List<Integer> ans = new ArrayList<>();
        Deque<TreeNode> nodeStack = new ArrayDeque<>();
        Deque<Integer> depthStack = new ArrayDeque<>();
        Set<TreeNode> marked = new HashSet<>();
        cur = target;
        nodeStack.push(cur);
        depthStack.push(0);
        while (!nodeStack.isEmpty()) {
            cur = nodeStack.pop();
            marked.add(cur);
            int depth = depthStack.pop();
            if (depth == k) {
                ans.add(cur.val);
            }
            if (cur.left != null && !marked.contains(cur.left)) {
                nodeStack.push(cur.left);
                depthStack.push(depth + 1);
            }
            if (cur.right != null && !marked.contains(cur.right)) {
                nodeStack.push(cur.right);
                depthStack.push(depth + 1);
            }
            if (nodeToParentMap.get(cur) != null && !marked.contains(nodeToParentMap.get(cur))) {
                nodeStack.push(nodeToParentMap.get(cur));
                depthStack.push(depth + 1);
            }
        }
        return ans;
    }
}
```