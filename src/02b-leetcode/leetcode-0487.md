这个题目可以用滑窗解决。以及 flip 这个操作肯定是要的，不论如何都会 + 1，除非全部都是 1.

注意下面的写法，right 总是停在一串 1 之后的位置，即 0 或者数组之后。
left 总是停在一串 1 的最开始的位置。所以 right - left 就得到全部的长度。
这里比较讨巧的地方在于，由于有 flip 的存在，可以保证 index0 的位置永远是 1.

```java
class Solution {
    public int findMaxConsecutiveOnes(int[] nums) {
        if(nums.length < 2) return nums.length;
        int left = 0;
        int right = 0;
        int maxLength = 0;
        boolean flipped = false;
        for(; right < nums.length; right++) {
            if(nums[right] == 1) {
                continue;
            }
            if(!flipped) {
                flipped = true;
                continue;
            }
            int curLength = right - left;
            maxLength = Math.max(curLength, maxLength);
            while(nums[left] == 1) {
                left++;
            }
            left++;
        }
        maxLength = Math.max(maxLength, right - left);
        return maxLength;
    }
}
```
