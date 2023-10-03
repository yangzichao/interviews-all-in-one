



线性解法：

```java
class Solution {
    public int missingElement(int[] nums, int k) {
        int cur = nums[0];
        for (int i = 1; i < nums.length; i++) {
            int gap = nums[i] - cur - 1;
            if (gap >= k) return cur + k;
            cur = nums[i];
            k -= gap;
        }
        return cur + k;
    }
}
```

以上非常的 trivial;
这个题是一个二分查找的牛题

下面是我自己写的，用的还是我最爱的 binary search 的模板。
计算两个index之间有多少空元素，推出的式子如下：nums[j] - nums[i] - (j - i);
我们的判定条件是要求最后k一定是要大于这个区间的，因此当binary search 停下的时候，left 指的位置
是第一个满足 true 的位置。但是从这里我们是没法推出 kth 的位置，因此我们用 right 指针来计算。

nums[right] + (k - (nums[right] - nums[0] - right));
= nums[0] + right + k;

```java
class Solution {
    public int missingElement(int[] nums, int k) {
        int left = 0;
        int right = nums.length - 1;
        while (left <= right) {
            int mid = (left + right) >>> 1;
            if (nums[mid] - nums[0] - mid < k) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return nums[0] + right + k;
    }
}

```