# 004J. Median of Two Sorted Arrays

https://leetcode.com/problems/median-of-two-sorted-arrays/

## Method Best

<pre>
基本的原理是，我们对短数组随便切一刀，对长数组也切一刀分成左右。目前我们就对短数组做BinarySearch. l r p 都是在对短数组。
由于作出合格的切分时，短数组和长数组总共的左侧元素数量已知，
因此，当短数组的切割位置确定的时候，会导致
长数组分割的位置也确定了。        
我们先标记该分割位置左侧和右侧的
元素分别为{l1,r1},{l2,r2}. 由于两个数组都是排好序的，
所以当完美的分割了中位数的时候，理应有左侧的数都小于等于右侧。
但是 l1 <= r1, l2 <= r2 是天然保证的，所以我们还要要求，
l1 <= r2 and l2 <= r1。如果 l1 > r2, 意味着我们在短数组
切的太偏右，r = p - 1, 反之如果 l2 > r1, 太偏左，应当
l = p + 1.

到此我们就可以搭好这个代码的框架了：
</pre>

```java
class Solution {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        // Always make nums1 the shorter array.
        if(nums1.length > nums2.length) return findMedianSortedArrays(nums2, nums1);
        // the total number of points to the left.
        int n = (nums1.length + nums2.length)/2;
        // pointers
        int l1 = 0, r1 = 0, l2 = 0, r2 = 0;
        // Binary Search Pointers
        int l = 0, r = nums1.length - 1, p= 0;
        // Begin Binary Search
        while( l <= r){
            // Pivot
            p = l + (r - l)/2;
            // Initial proper positions of pointers.
            //但是怎么处理超出数组的问题呢？
            l1 = p - 1; r1 = p; l2 = n - r1 - 1; r2 = n - r1 ;

            //regular rule
            if(nums1[l1] > nums2[r2]){
                r = p - 1;
            }else if( nums2[l2] > nums1[r1]){
                l = p + 1;
            }else{
               // found and judge
            }
        }

        return something.
    }
}
```

<pre>
但是怎么处理 edge case 呢，比如我们超出了数组边界的情况？
左侧l1,l2 只能是 -1的情形，我们将它的值设为 MIN，
右侧r1, r2 只能是数组长度 n.length，我们把它设为 MAX

那么怎么处理中位数呢？
如果是偶数长度，那么左侧Max(l1,l2)和右侧 Min(r1,r2)的平均值就是了。
如果是奇数长度，那么Min(r1,r2)就是了。
</pre>

- 其实一开始我还遇到了一个问题，就是 pivot 到底是指向 l1 还是 r1
- 最终证明只能指向 r1. Why? 因为 l 从 0 开始增加，值永远大于等于 0，最大为 n。而
  r 从 n - 1 开始减少，值永远小于等于 n - 1， 最小为 -1.以上运用了极限情况，r 比 l 小一。
  考虑 p = l +(r - l)/2, 当 l 为 0 时，r 最多为 -1, 因此 p 最小为 0，最大为 n.
  而 l1 需要是 -1 到 n-1，r1 是 0 到 n.因此 pivot 只能代表 r1.

```Java
class Solution {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        // Always make nums1 the shorter array.
        if(nums1.length > nums2.length) return findMedianSortedArrays(nums2, nums1);
        // the total number of points to the left.
        int n = (nums1.length + nums2.length)/2;
        // pointers
        int l1 = 0, r1 = 0, l2 = 0, r2 = 0;
        // Binary Search Pointers
        int l = 0, r = nums1.length - 1, p= 0;

        double ans = -1;
        // Begin Binary Search
        while( true ){
            // Pivot
            p = l + (r - l)/2;
            l1 = p - 1; r1 = p; l2 = n - r1 - 1; r2 = n - r1 ;

            double L1 = l1 < 0 ? Integer.MIN_VALUE : nums1[l1];
            double L2 = l2 < 0 ? Integer.MIN_VALUE : nums2[l2];
            double R1 = r1 > nums1.length - 1 ? Integer.MAX_VALUE : nums1[r1];
            double R2 = r2 > nums2.length - 1 ? Integer.MAX_VALUE : nums2[r2];

            if( L1 > R2){
                r = p - 1;
            }else if( L2 > R1 ){
                l = p + 1;
            }else{
                // if find proper position to cut arrays
                if( (nums1.length + nums2.length) %2 == 0) {  // even
                    return ans = (Math.max(L1,L2) + Math.min(R1,R2))/2;
                }else{ // odd
                    return ans = Math.min(R1,R2);
                }
            }
        }
    }
}
```



附上一个并不是最佳的解法

```java
class Solution {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        // if (nums1.length > nums2.length) return findMedianSortedArrays(nums2, nums1);
        int N = nums1.length + nums2.length;
        // odd: (N - 1) / 2 == N / 2
        // even: (N - 1) / 2 and (N / 2)
        // int lowcount = (N - 1) / 2;
        // int hiCount = N / 2;
        int loVal = 0;
        int hiVal = 0;
        int p1 = 0;
        int p2 = 0;
        for (int i = 0; i <= N / 2; i++) {
            int val1 = p1 == nums1.length ? Integer.MAX_VALUE : nums1[p1];
            int val2 = p2 == nums2.length ? Integer.MAX_VALUE : nums2[p2];
            int curVal = 0;
            if (val1 < val2) {
                p1++;
                curVal = val1;
            } else {
                p2++;
                curVal = val2;
            }
            if (i == (N - 1) / 2) loVal = curVal;
            if (i == N / 2) hiVal = curVal;
        }
        return (double) (hiVal + loVal) / 2.;
    }
}
```