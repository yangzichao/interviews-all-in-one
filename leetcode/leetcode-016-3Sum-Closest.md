# 016J. 3Sum Closest

真的没写过这个题吗，太神奇了。
```java
class Solution {
    public int threeSumClosest(int[] nums, int target) {
        Arrays.sort(nums);
        int closest = Integer.MAX_VALUE;
        int candidate = Integer.MAX_VALUE;

        for (int i = 0; i < nums.length - 2; i++) {
            int left = i + 1;
            int right = nums.length - 1;
            while (left < right) {
                int sum = nums[i] + nums[left] + nums[right];
                int diff = target - sum;
                if (Math.abs(diff) < closest) {
                    closest = Math.abs(diff);
                    candidate = sum;
                }
                if (diff > 0) {
                    left++;
                } else if (diff < 0) {
                    right--;
                } else {
                   return target;
                }
            } 
        }
        return candidate;
    }
}
```