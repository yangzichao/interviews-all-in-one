

rotate 排序了的数组不会有两个反常的下降。
```java
class Solution {
    public boolean check(int[] nums) {
        boolean isFound = false;
        for (int i = 0; i < nums.length; i++) {
            int nextIndex = (i + 1) % nums.length;
            if (nums[i] > nums[nextIndex]) {
                if (isFound) return false;
                isFound = true;
            }
        }
        return true;
    }
}
```