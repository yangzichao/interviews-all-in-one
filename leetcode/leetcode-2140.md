
这个题 dp 还是代表着

```java
class Solution {
    public long mostPoints(int[][] questions) {
        long[] dp = new long[questions.length];
        for (int i = questions.length - 1; i >= 0; i--) {
            int points = questions[i][0];
            int brainpower = questions[i][1];
            long solve = Long.valueOf(points);
            if (i + brainpower + 1 < questions.length) {
                solve += dp[i + brainpower + 1];
            }
            long skip = i == questions.length - 1 ? 0 : dp[i + 1];
            dp[i] = Math.max(skip, solve);
        }
        return dp[0];
    }
}
``