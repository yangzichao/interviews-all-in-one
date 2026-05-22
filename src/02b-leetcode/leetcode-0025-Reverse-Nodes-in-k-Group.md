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



# 2025

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
        ListNode p = head;
        int count = k;
        while (p != null && count > 0) {
            p = p.next;
            count--;
        }
        if (count > 0) return head;
        ListNode tail = reverseKGroup(p, k);
        ListNode prev = tail;
        ListNode cur = head; 
        int counter = 0;
        while (counter < k) {
            counter++;
            ListNode next = cur.next;
            cur.next = prev;
            prev = cur;
            cur = next;
        }
        return prev;
    }
}
```





请参见 206 题的两种翻转链表的方法：

这个题的具体写法可以根据 翻转方法的不同(2种) 递归和迭代的区别 (2种) 
再加上 递归可以有两种 （前序/后序） 一共写出 2 * (2 + 1) = 6 种解法。

删除-插入法(promote节点法)

```java
class Solution {
    public ListNode reverseKGroup(ListNode head, int k) {
        ListNode dummyHead = new ListNode(-1);
        dummyHead.next = head;
        ListNode p = head;
        int length = 0;
        while (p != null) {
            length++;
            p = p.next;
        }
        if (length < k) return head;
        ListNode curDummy = dummyHead;
        ListNode cur = head;
        for (int i = 0; i < length / k; i++) {
            int counter = 1;
            // 注意 insertion 只需要执行 k - 1  次 因为 head 和 cur 不能是同一个节点。
            while (counter < k) {
                counter++;
                ListNode nodeToPromote = cur.next;
                cur.next = nodeToPromote.next;
                nodeToPromote.next = curDummy.next;
                curDummy.next = nodeToPromote;
            }
            curDummy = cur;
            cur = cur.next;
        }
        return dummyHead.next;
    }
}
```

反转指针再头尾颠倒法：

```java
class Solution {
    public ListNode reverseKGroup(ListNode head, int k) {
        ListNode dummyHead = new ListNode(-1);
        dummyHead.next = head;
        ListNode p = head;
        int length = 0;
        while (p != null) {
            length++;
            p = p.next;
        }
        if (length < k) return head;
        ListNode preHead = dummyHead;
        ListNode prev = dummyHead;
        ListNode cur = head;
        for (int i = 0; i < length / k; i++) {
            int counter = 0;
            // 需要执行 k  次 
            while (counter < k) {
                counter++;
                ListNode tempNext = cur.next;
                cur.next = prev;
                prev = cur;
                cur = tempNext;
            }
            // cur is pointing at curTail 大翻转
            ListNode nextPrehead = preHead.next;
            preHead.next.next = cur;
            preHead.next = prev;
            // 更新 preHead, prev, cur
            preHead = nextPrehead;
            prev = preHead;
            cur = prev.next;
        }
        return dummyHead.next;
    }
}
```