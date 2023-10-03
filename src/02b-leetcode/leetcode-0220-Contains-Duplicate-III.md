# 220J. Contains Duplicate III


TLE method 
这个题还确实有点难度。以后再写。

```java
class Solution {
    public boolean containsNearbyAlmostDuplicate(int[] nums, int indexDiff, int valueDiff) {
        Set<Integer> window = new HashSet<>();
        for (int i = 0; i < nums.length; i++) {
            for (int vdiff = 0; vdiff <= valueDiff; vdiff++) {
                if (window.contains(nums[i] - vdiff) || window.contains(nums[i] + vdiff)) {
                    return true;
                }
            }
            if (window.size() >= indexDiff) {
                window.remove(nums[i - indexDiff]);
            }
            window.add(nums[i]);
        }
        return false;
    }
}
```