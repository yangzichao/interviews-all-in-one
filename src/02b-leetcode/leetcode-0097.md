这个题目的DFS + memoization 的做法和DP的做法都要会。真是一个好题。
也算是一个经验吧，对于部分题目来说，空字符也要算成一个"字符".




```java
class Solution {
    // 这个题需要一个非常重要的观察
    // 首先我们看 s1 + s2 长度一定要和 s3 相等，否则一定不成立。
    // 其次，s3 的最后一个字符，一定是 s1 和 s2 的最后一个字符之一。
    // 那么倒推，如果 s3 的最后一个字符不等于 s1 的最后一个字符，那么必须要等于s2的最后一个字符。
    // 如果是这样的话，要求 s1 和 s2去掉最后一个字符 与 s3去掉最后一个字符 也符合本题的 isInterleave 的要求。
    // 这样我们就成功找到了一个子问题
    // dp[i][j] 的意义是 s1.substring(0, i) 和 s2.substring(0, j) 是不是 s3.substring(0, i + j - 1) 的 interleave
    public boolean isInterleave(String s1, String s2, String s3) {
        if (s1.length() + s2.length() != s3.length()) return false;
        int M = s1.length();
        int N = s2.length();
        boolean[][] dp = new boolean[M + 1][N + 1];
        for (int i = 0; i <= M; i++) {
            for (int j = 0; j <= N; j++) {
                if(i == 0 && j == 0) {
                    dp[i][j] = true;
                } else if (i == 0) {
                    char curTargetS3Char = s3.charAt(i + j - 1);
                    boolean check2 = (s2.charAt(j - 1) == curTargetS3Char) && dp[i][j - 1];
                    dp[i][j] = check2;
                } else if (j == 0) {
                    char curTargetS3Char = s3.charAt(i + j - 1);
                    boolean check1 = (s1.charAt(i - 1) == curTargetS3Char) && dp[i - 1][j];
                    dp[i][j] = check1;
                } else {
                    char curTargetS3Char = s3.charAt(i + j - 1);
                    boolean check1 = (s1.charAt(i - 1) == curTargetS3Char) && dp[i - 1][j];
                    boolean check2 = (s2.charAt(j - 1) == curTargetS3Char) && dp[i][j - 1];
                    dp[i][j] = check1 || check2;
                }

            }
        }
        return dp[M][N];
    }
}
```