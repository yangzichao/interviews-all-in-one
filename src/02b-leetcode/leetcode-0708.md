

这个题很好的题！！好好想想！！

```java
/*
// Definition for a Node.
class Node {
    public int val;
    public Node next;

    public Node() {}

    public Node(int _val) {
        val = _val;
    }

    public Node(int _val, Node _next) {
        val = _val;
        next = _next;
    }
};
*/

class Solution {
    public Node insert(Node head, int insertVal) {
        // 这个题是个很不错的题 需要好好分析 其实不难
        // 一个排好序的list 由于不用担心next空指针的问题 我们可以一直疯狂比较 curNode 与 curNode.next
        // curNode 和 curNode.next 只有两种情况
        // 1. 上升。这是绝大多数的情形。那么 insertVal 必须要在两者val之间才能 insert。
        // 2. 下降。这意味着排序从最大掉到最小。那么 insertVal 必须要要么更大，要么更小，才能 insert。
        Node newNode = new Node(insertVal);
        if (head == null) {
            newNode.next = newNode;
            return newNode;
        }

        Node p = head;
        while (p.next != head) {
            if (p.val <= p.next.val) { // increasing
                if (p.val <= insertVal && insertVal <= p.next.val) {
                    break; // we do it outside
                }
            } else { // big change
                if (p.val <= insertVal || insertVal <= p.next.val) {
                    break;
                }
            }
            p = p.next;
        }
        newNode.next = p.next;
        p.next = newNode;
        return head;
    }
}

```