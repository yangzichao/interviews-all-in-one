# 230J Kth Smallest Element in a BST

https://leetcode.com/problems/kth-smallest-element-in-a-bst/

## Method Recursion

秒杀

```java
class Solution {
    private int count;
    private int kth;
    public int kthSmallest(TreeNode root, int k) {
        this.count = k;
        this.kth = 0;
        inorder(root);
        return kth;
    }
    private void inorder(TreeNode root){
        if(root == null) return;
        inorder(root.left);
        count--;
        if(count == 0) {
            kth = root.val;
            return;
        }
        inorder(root.right);
    }
}
```

## Method Iteration

没有意义 还是秒杀

```java
class Solution {
    public int kthSmallest(TreeNode root, int k) {
        Stack<TreeNode> stack = new Stack<>();
        while(true){
            while(root!=null){
                stack.push(root);
                root = root.left;
            }
            root = stack.pop();
            if(--k == 0){
                return root.val;
            }
            root = root.right;
        }
    }
}
```

## 2025

```java
class Solution {
    public int kthSmallest(TreeNode root, int k) {
        Deque<TreeNode> stack = new ArrayDeque();
        stack.push(root);
        TreeNode cur = root;
        int counter = k;
        while (cur != null || !stack.isEmpty()) {
            while (cur != null) {
                stack.push(cur);
                cur = cur.left;
            }
            cur = stack.pop();
            counter -= 1;
            while (counter == 0) return cur.val;
            cur = cur.right;
         }
         return -1;
    }
}
```


Follow up: If the BST is modified often (i.e., we can do insert and delete operations) and you need to find the kth smallest frequently, how would you optimize?

I can save the full in-order sequence so that insert and delete are O(log N), and find kth is O(1);
好像和官方解答不一样，但是也无所谓了