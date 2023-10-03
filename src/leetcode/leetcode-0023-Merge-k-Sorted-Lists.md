# 023J. Merge k Sorted Lists

https://leetcode.com/problems/merge-k-sorted-lists/

优先队列的方法也是值得试一试的。

## Method Best, merge with divide and conquer.

<pre>
思路是分治合并。即先实现两个列表的合并，然后合并剩下的。
N是总的元素个数，k是链表的个数，时间复杂度是O(Nlogk),空间 O(1)
</pre>

```java
class Solution {
    public ListNode mergeTwoLists(ListNode l1, ListNode l2){
        ListNode dummyhead = new ListNode(-1);
        ListNode p = dummyhead;
        ListNode p1 = l1, p2 = l2;
        while(p1!=null||p2!=null){
            if(p1 == null){
                p.next = p2;
                break;
            }
            if(p2 == null){
                p.next = p1;
                break;
            }
            if(p1.val < p2.val){
                p.next = p1;
                p1 = p1.next;
            }else{
                p.next = p2;
                p2 = p2.next;
            }
            p = p.next;
            p.next = null;
        }
        return dummyhead.next;
    }
    public ListNode mergeKLists(ListNode[] lists) {
        if(lists.length < 1){
            return null;
        }
        int interval = 1;
        while(interval < lists.length){
            for(int i = 0; i + interval < lists.length; i += interval*2){
                lists[i] = mergeTwoLists(lists[i],lists[i+interval]);
            }
            interval*=2;
        }
        return lists[0];
    }
}
```

## 递归

递归的方法实际是 Top-down method.
需要注意的是，此时 left[0] 不再保证是最终合并的全部 List 了。

```java
class Solution {
    public ListNode mergeKLists(ListNode[] lists) {
        return  mergeHelper(lists, 0, lists.length - 1);
    }
    private ListNode mergeHelper(ListNode[] lists, int left, int right){
        if(left == right){
            return lists[left];
        }
        if(left > right){
            return null;
        }

        int mid = left + (right - left)/2;
        return mergeTwoLists( mergeHelper(lists, left, mid) , mergeHelper(lists, mid + 1, right) );
    }
    private ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        if(list1 == null ){
            return list2;
        }
        if(list2 == null){
            return list1;
        }
        if(list1.val < list2.val){
            list1.next = mergeTwoLists(list1.next, list2);
            return list1;
        }
        list2.next = mergeTwoLists(list1, list2.next);
        return list2;
    }
}
```

## 2023

```java

class Solution {
    public ListNode mergeKLists(ListNode[] lists) {
        if (lists.length < 1) return null;
        for (int step = 1; step < lists.length; step *= 2) {
            for (int i = 0; i + step < lists.length; i += 2 * step) {
                lists[i] = mergeTwoLists(lists[i], lists[i + step]);
            }
        }
        return lists[0];
    }
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        ListNode dummyHead = new ListNode();
        ListNode p = dummyHead;
        ListNode l1 = list1;
        ListNode l2 = list2;
        while (l1 != null && l2 != null) {
            if (l1.val > l2.val) {
                p.next = l2;
                l2 = l2.next;
            } else {
                p.next = l1;
                l1 = l1.next;
            }
            p = p.next;
        }
        p.next = l1 == null ? l2 : l1;
        return dummyHead.next;
    }
}


```