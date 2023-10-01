
不错我竟然第一次就想到了这个wigglesort的解法。
关联：
https://leetcode.com/problems/wiggle-sort/

```java
class Solution {
    public int[] rearrangeArray(int[] nums) {
        // 我觉得这个题等于 wiggle sort
        for (int i = 0; i < nums.length - 1; i++) {
            if (i % 2 == 0) {
                if (nums[i] > nums[i + 1]) {
                    swap(nums, i, i + 1);
                }
            }
            if (i % 2 == 1) {
                if (nums[i] < nums[i + 1]) {
                    swap(nums, i, i + 1);
                }
            }
        }
        return nums;
    }

    private void swap(int[] nums, int i, int j) {
        nums[i] ^= nums[j];
        nums[j] ^= nums[i];
        nums[i] ^= nums[j];
    }
}
```