
这个题是 binary search 的一个好问题啊 
面试别人可以问这个
```java
class Solution {
    public int findKthPositive(int[] arr, int k) {
        // analysis:
        // at index i: should 1 to i + 1 
        // nums[i] == i + 1, means no missing element
        // nums[i] == i + 1 + j, means we missed j element
        // nums[i] == i + i + k means we missed k element, we want the i - 1 element
        // in above case, lo will be at i, hi will be at i - 1
        // the kth element will be hi + 1 + k
        
        int lo = 0;
        int hi = arr.length - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (arr[mid] < mid + 1 + k) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return hi + 1 + k;
    }
}

```