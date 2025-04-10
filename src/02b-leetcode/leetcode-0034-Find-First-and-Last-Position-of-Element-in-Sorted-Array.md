# 034J. Find First and Last Position of Element in Sorted Array

https://leetcode.com/problems/find-first-and-last-position-of-element-in-sorted-array/

## Method Good: Traditional Binary Search

日后慢慢分析一下。一定要好好学啊，这可是二项搜索之王一通百通。

```java
class Solution {
    public int[] searchRange(int[] nums, int target) {
        if(nums.length == 0) return new int[]{-1,-1};
        int[] range = new int[2];

        int totalNumber = nums.length;
        int left = 0;
        int right = totalNumber - 1;
        while(left < right) {
            int pivot = left + (right - left)/2;
            int num = nums[pivot];
            if(num < target) {
                left = pivot + 1;
            }else{
                right = pivot;
            }
        }

        range[0] = nums[left] == target ? left : -1;

        left = 0;
        right = totalNumber - 1;
        while(left < right) {
            int pivot = left + (right - left + 1)/2;
            int num = nums[pivot];
            if(num > target){
                right = pivot - 1;
            }else{
                left = pivot;
            }
        }
        range[1] = nums[right] == target ? right : -1;

        return range;
    }
}
```



## 2025

竟然没有 update 上面那个笔记吗 ？ 神奇。
```java
class Solution {
    public int[] searchRange(int[] nums, int target) {
        int[] ans = new int[2];
        ans[0] = find(nums, target, true);
        ans[1] = find(nums, target, false);
        return ans;
    }
    private int find(int[] nums, int target, boolean findFirst) {
        boolean isFound = false;
        int lo = 0;
        int hi = nums.length - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (nums[mid] < target) {
                lo = mid + 1;
            } else if (nums[mid] > target) {
                hi = mid - 1;
            } else {
                isFound = true;
                if (findFirst) {
                    hi = mid - 1;
                } else {
                    lo = mid + 1;
                }
            }
        }
        if (!isFound) return -1;
        return findFirst ? lo : hi;
    }
}
```