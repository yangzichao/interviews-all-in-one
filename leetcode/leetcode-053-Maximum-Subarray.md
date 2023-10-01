# 053J. Maximum Subarray

https://leetcode.com/problems/maximum-subarray/

## Method: Best 1, Dynamic Programming.

<pre>
思想大致类似于
如果前面部分的和是负数，那么对我就是负影响，直接丢弃，
如果是正数，可以加入子数组。

</pre>

```Java
class Solution {
    public int maxSubArray(int[] nums) {
        int ans = nums[0];
        for(int i = 1; i < nums.length; i+= 1){
            if(nums[i-1] > 0) nums[i] += nums[i-1];
            ans = Math.max(ans,nums[i]);
        }
        return ans;
    }
}
```

## Method: Best 2, Greedy.

<pre>
类似于，我们保存一个局部最大，和全局最大。
局部最大就是，如果局部最大加上当前nums[i]，还不如nums[i]大，
说明局部最大目前是负数，而nums[i]是正数，直接取nums[i]。反之，如果nums[i]是负数，
则局部最大加nums[i]比较大，存下局部最大加nums[i].
全局最大一直做比较，所有的局部最大中最大的一定是全局最大，这就是贪心法。
</pre>

```Java
class Solution {
    public int maxSubArray(int[] nums) {
        int curSum = nums[0];
        int maxSum = nums[0];
        for(int i = 1; i < nums.length; i++){ //从1开始是防止edge case 以免写开头的 if return
            curSum = Math.max(nums[i],curSum+nums[i]);
            maxSum = Math.max(maxSum,curSum);
        }
        return maxSum;
    }
}
```

53 放一个 2023 年写的写法，一个思路到是。这个题的解法有个名字叫 Kadane's algo

```java
class Solution {
    public int maxSubArray(int[] nums) {
        int max = Integer.MIN_VALUE;
        int[] prefixSum = new int[nums.length + 1];
        for (int i = 0; i < nums.length; i++) {
            prefixSum[i + 1] = prefixSum[i] + nums[i];
        }
        int left = 0;
        for (int right = 1; right < prefixSum.length; right++) {
            int curSum = prefixSum[right] - prefixSum[left];
            max = Math.max(curSum, max);
            if (curSum < 0) {
                left = right;
            }
        }
        return max;
    }
}
```

2272 也是用到了 Kadane's algo
