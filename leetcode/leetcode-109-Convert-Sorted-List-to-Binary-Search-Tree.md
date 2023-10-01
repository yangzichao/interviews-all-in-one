# 109J. Convert Sorted List to Binary Search Tree
https://leetcode.com/problems/convert-sorted-list-to-binary-search-tree/


## Method 1
[复制108](leetCode-108-Convert-Sorted-Array-to-Binary-Search-Tree.md)
先把LinkedList都加到数组里，再做108一样的。
## Method 2 模拟 inorder
真的好机智
先取这个数组的size，这个还是不能避免的。

```java
class Solution {
    private ListNode head;
    public TreeNode sortedListToBST(ListNode head) {
        int size = getsize(head);
        this.head = head;
        return convertListToBST(0,size -1);
    }
    
    public int getsize(ListNode head){
        ListNode p = head;
        int size  = 0;
        while(p!=null){
            p = p.next;
            size++;
        }
        return size;
    }
    public TreeNode convertListToBST(int l, int r){
        if(l>r) return null;
        int mid = l + (r - l)/2;
        TreeNode left = convertListToBST(l, mid-1);
        TreeNode node = new TreeNode(this.head.val);
        this.head = this.head.next;
        
        node.left  = left;
        node.right = convertListToBST(mid + 1,r);
        return node;
    }
}
```


## 2023 写的 还是有进步吧

```java
/**
 * Definition for singly-linked list.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 * }
 */
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
    private int size;
    private ListNode head;
    public TreeNode sortedListToBST(ListNode head) {
        this.head = head;
        this.size = getSize(head);
        return inOrderHelper(0, size - 1);
    }
    private int getSize(ListNode node) {
        int count = 0;
        ListNode p = node;
        while (p != null) {
            count++;
            p = p.next;
        }
        return count;
    }

    private TreeNode inOrderHelper(int lo, int hi) {
        if (lo > hi) return null;
        int mid = (lo + hi) >>> 1;
        TreeNode leftChild = inOrderHelper(lo, mid - 1);
        TreeNode node = new TreeNode(head.val);
        head = head.next;
        TreeNode rightChild = inOrderHelper(mid + 1, hi);
        node.left = leftChild;
        node.right = rightChild;
        return node;
    }
}
```