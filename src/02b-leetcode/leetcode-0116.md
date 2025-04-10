

```java
/*
// Definition for a Node.
class Node {
    public int val;
    public Node left;
    public Node right;
    public Node next;

    public Node() {}
    
    public Node(int _val) {
        val = _val;
    }

    public Node(int _val, Node _left, Node _right, Node _next) {
        val = _val;
        left = _left;
        right = _right;
        next = _next;
    }
};
*/

class Solution {
    public Node connect(Node root) {
        if (root == null) return null;
        Deque<Node> queue = new ArrayDeque<>(); 
        Node cur = root;
        queue.offer(cur);

        while (!queue.isEmpty()) {
            int size = queue.size();
            Node prev = null;
            while (size > 0) {
                cur = queue.poll();
                if (cur.left != null) {
                    queue.offer(cur.left);
                }
                if (cur.right != null) {
                    queue.offer(cur.right);
                }
                if (prev != null) {
                    prev.next = cur;
                }
                prev = cur;
                size--;
            }
        }

        return root;
    }
}
```