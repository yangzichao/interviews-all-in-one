
这个就是一个比较好的类似于 coin change 之类的。

```java
class Solution {
    public int countGoodStrings(int low, int high, int zero, int one) {
        int[] dp = new int[high + 1];
        int mod = 1000000007;
        dp[0] = 1;
        for (int i = 1; i <= high; i++) {
            if (i >= zero) {
                dp[i] += dp[i - zero];
            }
            if (i >= one) {
                dp[i] += dp[i - one];
            }
            dp[i] %= mod;
        }
        int sum = 0;
        for (int i = low; i <= high; i++) {
            sum += dp[i];
            sum %= mod;
        }
        return sum;
    }
}
```