# 234J. Palindrome Linked List
https://leetcode.com/problems/palindrome-linked-list/







## 方法1 还行
结合 [876](leetCode-876-Middle-of-the-Linked-List.md)
先找中位，再翻转，再比较。
```java
class Solution {
    public boolean isPalindrome(ListNode head) {
        ListNode slow = head, fast = head;

        while(fast!=null && fast.next !=null){
            slow = slow.next;
            fast = fast.next.next;
        }
        if(fast != null){
            slow = slow.next;
        }
        slow = reverse(slow);
        while(slow!=null){
            if(head.val != slow.val){
                return false;
            }
            head = head.next;
            slow = slow.next;
        }
        return true;

    }
    public ListNode reverse(ListNode head){
        if(head == null || head.next == null){
            return head;
        }
        ListNode p = reverse(head.next);
        head.next.next = head;
        head.next = null;
        return p;
    }
}
```
## 方法2 空间是 O(N) 但是优雅

```Java
Explanation
..........................................................................................
Example :
1-> 2-> 3-> 4-> 2-> 1

ref points 1 initially.
Make recursive calls until you reach the last element - 1.
On returning from each recurssion, check if it is equal to ref values.
ref values are updated to on each recurssion.
So first check is ref 1 -  end 1
Second ref 2 - second last 2 ...and so on.
..........................................................................................

 class Solution {
    ListNode ref;
    public boolean isPalindrome(ListNode head) {
        ref = head;        
        return check(head);
    }

    public boolean check(ListNode node){
        if(node == null) return true;
        boolean ans = check(node.next);
        boolean isEqual = (ref.val == node.val)? true : false;
        ref = ref.next;
        return ans && isEqual;
    }
}
```


## 2023 
写的还有进步吧

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
    public boolean isPalindrome(ListNode head) {
        ListNode dummyHead = new ListNode();
        dummyHead.next = head;
        ListNode slow = dummyHead;
        ListNode fast = dummyHead;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        // x -> 1 -> 2 return slow.next
        // x -> 1 -> 2 -> 3 return slow.next;
        slow.next = reverseList(slow.next); // reverse secondhalf
        ListNode p1 = head;
        ListNode p2 = slow.next;
        while (p1 != null && p2 != null) {
            if (p1.val != p2.val) return false;
            p1 = p1.next;
            p2 = p2.next;
        }
        slow.next = reverseList(slow.next); // recover secondhalf
        return true;

    }
    private ListNode reverseList(ListNode node) {
        if (node == null || node.next == null) return node;
        ListNode newHead = reverseList(node.next);
        node.next.next = node;
        node.next = null;
        return newHead;
    }
}
```