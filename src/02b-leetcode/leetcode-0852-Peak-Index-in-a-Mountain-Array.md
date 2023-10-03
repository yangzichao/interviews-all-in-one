# 852J. Peak Index in a Mountain Array
https://leetcode.com/problems/peak-index-in-a-mountain-array/
Binary Search So easy;
进阶：
162
究极：
1095
## Method Best: Binary Search
这个题的难点在于，如何避免 pivot - 1 overflow 的问题。       
由于这个题目保证了一个特性，即mountain一定存在并且至少有三个元素，我们的当前的模板来说， pivot 永远不会取到 nums.length - 1, 所以  pivot + 1 是无论如何安全的。
那么我们就只利用 pivot + 1, 然后用 else 把 pivot - 1 的各种情况都包括进去。       
这就是巧妙之处。         
不信你可以试着用 pivot - 1 来试试，一定翻车。      
```java
class Solution {
    public int peakIndexInMountainArray(int[] A) {
        int left = 0, right = A.length -1;
        int pivot = 0;
        while(left <= right){
            pivot = left + (right  - left)/2;
            if( (A[pivot] > A[pivot + 1]) &&(A[pivot] > A[pivot - 1])){
                return pivot;
            }else if(A[pivot] < A[pivot + 1]){
                left = pivot + 1;
            }else{
                right = pivot - 1;
            }
        }
        return pivot;
    }
}
```




The comparison A[i] < A[i+1] in a mountain array looks like [True, True, True, ..., True, False, False, ..., False]: 1 or more boolean Trues, followed by 1 or more boolean False. For example, in the mountain array [1, 2, 3, 4, 1], the comparisons A[i] < A[i+1] would be True, True, True, False.

We can binary search over this array of comparisons, to find the largest index i such that A[i] < A[i+1].


2023 年运用自创模板写的：
```java
class Solution {
    public int peakIndexInMountainArray(int[] arr) {
        int left = 0;
        int right = arr.length - 1;
        while (left <= right) {
            int mid = (left + right) >>> 1;
            // [T,T,T,T,F,F,F]
            boolean isTrue = mid == arr.length - 1 ? false : arr[mid] < arr[mid + 1];
            if (isTrue) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return left;
    }
}
```