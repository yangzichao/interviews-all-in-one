# 1. Two Sum

Given an array of integers, return indices of the two numbers such that they add up to a specific target.

You may assume that each input would have exactly one solution, and you may not use the same element twice.

Example:

Given nums = [2, 7, 11, 15], target = 9,

Because nums[0] + nums[1] = 2 + 7 = 9,
return [0, 1].

## Method Best: HashMap

方法就是将数组都添加入 HashSet 中，速度比暴力法快的多了。

```Java
class Solution {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for(int i = 0; i < nums.length; i+=1){
          int complement = target - nums[i];
          if(map.containsKey(complement) && map.get(complement) != i){
                  return new int[]{i, map.get(complement)};
          }
          map.put(nums[i],i);
        }
        // return null;
        throw new IllegalArgumentException("No two sum solution");
    }
}
```

时间复杂度 Time Complexity O(N)
空间复杂度 Space Complexity O(N)

变形 1010.
变形 523
变形 560
