

```java
class Solution {
    public int getMaxLen(int[] nums) {
        // + + + + - + + - + - + 0
        // 0 把这个问题分成了子问题
        // 只考虑子问题的话 就是只需要考虑最左侧和最右侧负号位置
        // 那么左右扫一遍即可 我知道解释的不清楚 以后再细细论证
        int maxLen = 0;
        int curSign = 1;
        int curLeft = -1;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == 0) {
                curSign = 1;
                curLeft = i;
            } else {
                curSign *= nums[i] > 0 ? 1 : -1;
                if (curSign > 0) {
                    maxLen = Math.max(maxLen, i - curLeft);
                }
            }
        }
        curSign = 1;
        int curRight = nums.length;
        for (int i = nums.length - 1; i >= 0; i--) {
            if (nums[i] == 0) {
                curSign = 1;
                curRight = i;
            } else {
                curSign *= nums[i] > 0 ? 1 : -1;
                if (curSign > 0) {
                    maxLen = Math.max(maxLen, curRight - i);
                }
            }
        }
        return maxLen;
    }
}




```