# 092J(1/1). Reverse Linked List II

https://leetcode.com/problems/reverse-linked-list-ii/





## 方法1 不一定好，比较好解释。
<pre>
首先实现反转前N个的方法，但是不能和后面断掉，所以我们需要定义一个successor,
指向不需要被翻转的头部。
然后我们将指针挪到需要被翻转的node的前一位，使用
      p.next = reverseFirstN(p.next,n);
这样做的道理是因为我们无法把dummyhead传到迭代method里面。

</pre>
```Java
class Solution {
    ListNode successor = null;
    public ListNode reverseFirstN(ListNode l, int n){
        if( n == 1) {
            successor = l.next;
            return l;
        }
        ListNode p = reverseFirstN(l.next,n - 1);
        l.next.next = l;
        l.next = successor;
        return p;
    }
    public ListNode reverseBetween(ListNode head, int m, int n) {
        ListNode dummyhead = new ListNode(0);
        dummyhead.next = head;
        ListNode p = dummyhead;
        while(m > 1){
            p = p.next;
            m -=1;
            n -=1;
        }
        p.next = reverseFirstN(p.next,n);
        return dummyhead.next;
    }
}

```

## 2025

请参见一下 leetcode 206 的解释，有两种翻转的方式。

第一种 删节点插入到队列前来翻转的方法。
```java
class Solution {
    public ListNode reverseBetween(ListNode head, int left, int right) {
        ListNode dummyHead = new ListNode(-1);
        dummyHead.next = head;
        ListNode cur = dummyHead;
        int count = 1;
        while (count < left) {
            cur = cur.next;
            count++;
        }
        ListNode tempHead = cur;
        cur = cur.next;
        for (int i = 0; i < right - left; i++) {
            ListNode nodeToPromote = cur.next;
            if (nodeToPromote == null) break;
            cur.next = nodeToPromote.next;
            nodeToPromote.next = tempHead.next;
            tempHead.next = nodeToPromote;
        }

        return dummyHead.next;
    }
}
```

第二种 三指针翻转的方法

```java
class Solution {
    public ListNode reverseBetween(ListNode head, int left, int right) {
        ListNode dummyHead = new ListNode(-1);
        dummyHead.next = head;
        ListNode cur = dummyHead;
        int count = 1;
        while (count < left) {
            cur = cur.next;
            count++;
        }
        ListNode tempHead = cur;
        ListNode prev = tempHead;
        cur = cur.next;
        for (int i = 0; i <= right - left; i++) {
            ListNode tempNext = cur.next;
            cur.next = prev;
            prev = cur;
            cur = tempNext;
        }
        tempHead.next.next = cur;
        tempHead.next = prev;
        return dummyHead.next;
    }
}
```