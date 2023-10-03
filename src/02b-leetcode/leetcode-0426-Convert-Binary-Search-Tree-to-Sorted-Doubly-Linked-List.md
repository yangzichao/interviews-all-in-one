# 426 Convert-Binary-Search-Tree-to-Sorted-Doubly-Linked-List 
 
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
<div><p>Convert a <strong>Binary Search Tree</strong>&nbsp;to a sorted <strong>Circular Doubly-Linked List</strong>&nbsp;in place.</p>
<p>You can think of the left and right pointers as synonymous to the predecessor and successor pointers in a doubly-linked list. For a circular doubly linked list, the predecessor of the first element is the last element, and the successor of the last element is the first element.</p>
<p>We want to do the transformation <strong>in place</strong>. After the transformation, the left pointer of the tree node should point to its predecessor, and the right pointer should point to its successor. You should return the pointer to the smallest element of the linked list.</p>
<p>&nbsp;</p>
<p><strong>Example 1:</strong></p>
<p><img src="https://assets.leetcode.com/uploads/2018/10/12/bstdlloriginalbst.png" style="width: 100%; max-width: 300px;"></p>
<pre><strong>Input:</strong> root = [4,2,5,1,3]
<img src="https://assets.leetcode.com/uploads/2018/10/12/bstdllreturndll.png" style="width: 100%; max-width: 450px;">
<strong>Output:</strong> [1,2,3,4,5]
<strong>Explanation:</strong> The figure below shows the transformed BST. The solid line indicates the successor relationship, while the dashed line means the predecessor relationship.
<img src="https://assets.leetcode.com/uploads/2018/10/12/bstdllreturnbst.png" style="width: 100%; max-width: 450px;">
</pre>
<p><strong>Example 2:</strong></p>
<pre><strong>Input:</strong> root = [2,1,3]
<strong>Output:</strong> [1,2,3]
</pre>
<p><strong>Example 3:</strong></p>
<pre><strong>Input:</strong> root = []
<strong>Output:</strong> []
<strong>Explanation:</strong> Input is an empty tree. Output is also an empty Linked List.
</pre>
<p><strong>Example 4:</strong></p>
<pre><strong>Input:</strong> root = [1]
<strong>Output:</strong> [1]
</pre>
<p>&nbsp;</p>
<p><strong>Constraints:</strong></p>
<ul>
	<li><code>-1000 &lt;= Node.val &lt;= 1000</code></li>
	<li><code>Node.left.val &lt; Node.val &lt; Node.right.val</code></li>
	<li>All values of <code>Node.val</code> are unique.</li>
	<li><code>0 &lt;= Number of Nodes &lt;= 2000</code></li>
</ul>
</div></section>
 
 ## Method One 
 
``` Java
/*
// Definition for a Node.
class Node {
    public int val;
    public Node left;
    public Node right;
​
    public Node() {}
​
    public Node(int _val) {
        val = _val;
    }
​
    public Node(int _val,Node _left,Node _right) {
        val = _val;
        left = _left;
        right = _right;
    }
};
*/
class Solution {
    private Node head = null;
    private Node last = null;
    
    public Node treeToDoublyList(Node root) {
        if(root == null){
            return null;
        }
        helper(root);
        last.right = head;
        head.left = last;
        return head;
    }
    
    public void helper(Node node){
        if(node == null){
            return;
        }
        
        helper(node.left);
        if(last != null){
            last.right = node;
            node.left = last;
        }else{
            head = node;
        }
        last = node;
        helper(node.right);
    }
}
​
```

## Method 2

```java
class Solution {
    public Node treeToDoublyList(Node root) {
        if(root == null){
            return root;
        }
        Node head = null;
        Node prev = null;
        Node cur = root;
        ArrayDeque<Node> stack = new ArrayDeque<Node>();
        while(!stack.isEmpty() || cur != null ){
            while(cur != null){
                stack.push(cur);
                cur = cur.left;
            }
            cur = stack.pop();

            if(prev == null){
                head = cur;
            }else{
                prev.right = cur;
                cur.left = prev;
            }
            prev = cur;
            cur = cur.right;
        }
        head.left = prev;
        prev.right = head;
        return head;
    }
}
```

## Method 3

真正的 O(1) 还包括 morris traversal.