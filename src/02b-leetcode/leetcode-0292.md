这个还是应该证明一下

```java
class Solution {
    public boolean canWinNim(int n) {
        return n % 4 != 0;
        // canWinNim(int n) check n - 1, n - 2, n - 3  如果后面三个全是能赢，那么就是对手能赢
        // boolean[] dp = new boolean[n + 1];
        // for (int i = 1; i <= n; i++) {
        //     boolean canWin = true;
        //     for (int j = 1; j <= 3; j++) {
        //         if (i - j < 0) continue;
        //         canWin = canWin && dp[i - j];
        //     }
        //     dp[i] = !canWin;
        // }
        // return dp[n];
    }
}

// 0 false
// 1 true
// 2 true
// 3 true
// 4 false
// 5 true
// 6 true
// 7 true
// 8 false
// 9 true
```