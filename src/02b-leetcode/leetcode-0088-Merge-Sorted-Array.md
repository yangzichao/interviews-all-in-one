# 088J. Merge Sorted Array

https://leetcode.com/problems/merge-sorted-array/

## Method Best:

这个题有点垃圾。
总之就是从尾巴开始比，这样对于这个题比较有用，因为数组不像
LinkedList 比较容易插入。除此之外没啥特别之处。

```java
class Solution {
    public void merge(int[] nums1, int m, int[] nums2, int n) {
        int p = m + n - 1;
        int p1 = m - 1;
        int p2 = n - 1;
        while(p1 > -1 || p2 > -1){
            int n1 = p1 > -1? nums1[p1] : Integer.MIN_VALUE;
            int n2 = p2 > -1? nums2[p2] : Integer.MIN_VALUE;
            if(n1 > n2){
                nums1[p--] = n1;
                p1--;
            }else{
                nums1[p--] = n2;
                p2--;
            }
        }
    }
}
```

确实有点烦

```java
class Solution {
    public void merge(int[] nums1, int m, int[] nums2, int n) {
        int p1 = m - 1;
        int p2 = n - 1;
        for (int cur = m + n - 1; cur >= 0; cur--) {
            if (p1 < 0 || (p2 >= 0 && nums1[p1] < nums2[p2]) ) {
                nums1[cur] = nums2[p2];
                p2--;
            } else {
                nums1[cur] = nums1[p1];
                p1--;
            }
        }
    }

```

## 2025 写的
```java
class Solution {
    public void merge(int[] nums1, int m, int[] nums2, int n) {
        int p = m - 1;
        int q = n - 1;
        for (int i = nums1.length - 1; i >= 0; i--) {
            if (p < 0) {
                nums1[i] = nums2[q--];
                continue;
            }
            if (q < 0) {
                swap(nums1, i, p--);
                continue;
            }
            if (nums1[p] < nums2[q]) {
                nums1[i] = nums2[q--];
            } else {
                swap(nums1, i, p--);
            }
        }
    }

    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
```