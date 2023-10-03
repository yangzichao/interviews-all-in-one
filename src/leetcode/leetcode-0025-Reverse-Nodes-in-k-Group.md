# 025J. Reverse Nodes in k-Group

才发现这个题竟然这么久以来是没有写过的，我至少写一次递归的解法好了。

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
class Solution {
    public ListNode reverseKGroup(ListNode head, int k) {
        ListNode cur = head;
        int counter = 0;
        while (cur != null && counter < k) { // find k + 1 node
            cur = cur.next;
            counter++;
        }
        if (counter != k) return head;
        cur = reverseKGroup(cur, k);
        while (counter > 0) {
            counter--;
            ListNode nextHead = head.next;
            head.next = cur;
            cur = head;
            head = nextHead;
        }
        head = cur;
        return head;
    }
}
```