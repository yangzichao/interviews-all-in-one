这个题的一个简单思想就是排序 
比如

```java
class Solution {
    // [3,2,1,2,1,7], sort 先排序
    // 1 1 2 2 3 7 思想就是改变原数组，始终比前面的多1
    // 1 2 2 2 3 7
    // 1 2 3 2 3 7
    // 1 2 3 4 3 7
    // 1 2 3 4 5 7 差不多这样，其实也不用改变原数组，只需要维持一个数记录当前的最大就行
    public int minIncrementForUnique(int[] nums) {
        // 1 1 2 2 3 7
        //   2 3 4 5 7 
        Arrays.sort(nums);
        int total = 0;
        for (int i = 1; i < nums.length; i++) {
          if (nums[i] <= nums[i - 1]) {
            total += nums[i - 1] + 1 - nums[i];
            nums[i] = nums[i - 1] + 1;
          }
        }
        return total;
    }
}
```