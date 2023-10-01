这个题的第二个解法有点好啊……


```java
class Solution {
    public int[] getModifiedArray(int length, int[][] updates) {
        int[] ans = new int[length];
        for (int[] update : updates) {
            int start = update[0];
            int end = update[1];
            int val = update[2];
            ans[start] += val;
            if (end + 1 < length) {
                ans[end + 1] -= val;
            }
        }
        for (int i = 1; i < length; i++) {
            ans[i] += ans[i - 1];
        }
        return ans;
    }
}
```