



```java
class Solution {
    public int singleNonDuplicate(int[] nums) {
        // 记录下我短暂的思考过程，对以后有帮助
        // 首先：排好序的 array, log N 时间复杂度，显然要用 binary search
        // 那么我们取了 mid 之后 怎么知道 candidate 是在 mid 左边还是右边呢？
        // 注意到每个元素都是重复的 只有一个不是
        // 因此如果 mid = mid + 1 
        // 并且 mid 的 index 是一个偶数，说明 candidate 在 mid + 1 右边, 奇数则在 mid 左边
        // 如果 mid != mid + 1 我们就要看 mid 和 mid - 1, 也是同理
        int left = 0;
        int right = nums.length - 1;
        while (left <= right) {
            int mid = (left + right) >>> 1;
            if (mid < nums.length - 1 && nums[mid] == nums[mid + 1]) {
                if (mid % 2 == 0) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            } else if (mid > 0 && nums[mid] == nums[mid - 1]) {
                if (mid % 2 == 0) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            } else {
                return nums[mid];
            }
        }
        return nums[left];
    }
}
```