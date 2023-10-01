这个题目和163 有点像吧，就是纯array的题，想好怎么写吧。

思路类似于双指针。用快指针探路，探一串路当中最后的一位。然后处理字符。双指针一起更新。
```java
class Solution {
    public List<String> summaryRanges(int[] nums) {
        List<String> ans = new ArrayList<>();
        int lo = 0;
        int hi = 0;
        while (hi < nums.length) {
            while (hi < nums.length - 1 && nums[hi] == nums[hi + 1] - 1) {
                hi++;
            }
            String temp = nums[hi] == nums[lo] 
                            ? String.valueOf(nums[lo]) 
                            : String.valueOf(nums[lo]) + "->" + String.valueOf(nums[hi]);
            ans.add(temp);
            hi++;
            lo = hi;
        }
        return ans;
    }
}
```