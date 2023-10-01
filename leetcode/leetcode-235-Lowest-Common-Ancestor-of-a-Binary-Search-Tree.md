# 235J. Lowest Common Ancestor of a Binary Search Tree
https://leetcode.com/problems/lowest-common-ancestor-of-a-binary-search-tree/

## Method 1 Recursion
这是个easy题没啥好说的

```java
class Solution {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if(root == null) return root;

        if(p.val < root.val && q.val < root.val){
            return lowestCommonAncestor(root.left, p, q);
        }else if(p.val > root.val && q.val > root.val){
            return lowestCommonAncestor(root.right, p, q);
        }else if(p.val == root.val || q.val == root.val){
            return root;
        }else{
            return root;
        }
    }
}
```
明显后面两种可以合并，简化代码
```java
class Solution {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if(root == null) return root;

        if(p.val < root.val && q.val < root.val){
            return lowestCommonAncestor(root.left, p, q);
        }else if(p.val > root.val && q.val > root.val){
            return lowestCommonAncestor(root.right, p, q);
        }else{
            return root;
        }
    }
}
```

2023 写的 供参考

```java
class Solution {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if (p.val > q.val) return lowestCommonAncestor(root, q, p);
        if (root == null) return null;
        if (root.val < p.val) {
            return lowestCommonAncestor(root.right, p, q);
        }
        if (root.val > q.val) {
            return lowestCommonAncestor(root.left, p, q);
        }
        return root;
    }
}
```

## Method 2 Iterative
利用了 BST 的特性，非常机智
```java
class Solution {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if( q.val < p.val ){
            return lowestCommonAncestor(root, q, p);
        }
        TreeNode cur = root;
        while( cur != null ){
            if( cur.val < p.val){
                cur = cur.right;
            }else if (cur.val > q.val){
                cur = cur.left;
            }else{
                return cur;
            }
        }
        return cur;
    }
}
```

```java
class Solution {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        
        TreeNode node = root;

        while(p!=null){
            if(p.val < node.val && q.val < node.val){
                node = node.left;
            }else if( p.val > node.val && q.val > node.val ){
                node = node.right;
            }else{
                return node;
            }
        }
        return node;
    }
}
```

```java
class Solution {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        TreeNode node = root;
        while( true ) {
            if( p.val < node.val && q.val < node.val ) {
                node = node.left;
            }else if( p.val > node.val && q.val > node.val ) {
                node = node.right;
            }else{
                return node;
            }
        }
        // return node;
    }
}
```