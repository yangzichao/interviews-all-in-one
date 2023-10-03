# 111J. Minimum Depth of Binary Tree
https://leetcode.com/problems/minimum-depth-of-binary-tree/


## BFS
秒杀
```java
class Solution {
    public int minDepth(TreeNode root) {
        if(root ==  null) return 0;
        Queue<TreeNode> q = new LinkedList<>();
        int depth = 1;
        q.offer(root);
        while(!q.isEmpty()){
            int size = q.size();
            while(size > 0){
                TreeNode node = q.poll();
                if(node.left == null && node.right == null){
                    return depth;
                }
                if( node.left!=null){
                    q.offer(node.left);
                }
                if(node.right !=null){
                    q.offer(node.right);
                }
                size--;
            }
            depth++;
        }
        return depth;
    }
}
```

## DFS 也可以 但没必要

2023 写了一个很奇怪的东西

```java
class Solution {
    public int minDepth(TreeNode root) {
        if (root == null) return 0;
        return minDepthWrapper(root);
    }
    private int minDepthWrapper(TreeNode root) {
        if (root == null) return Integer.MAX_VALUE;
        if (root.left == null && root.right == null) return 1;
        return Math.min(minDepthWrapper(root.left), minDepthWrapper(root.right)) + 1;
    }
}
```