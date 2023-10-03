这个题真的好难，我受不了了。
如果有机会研究一下他的二分法题解：
https://leetcode.cn/problems/split-array-largest-sum/solution/er-fen-cha-zhao-by-liweiwei1419-4/

这个题的dp的想法应该还是挺直接的，是非常经典的问题，必须要会！   
一个变形就是分配读书的问题   
https://www.geeksforgeeks.org/allocate-minimum-number-pages/#   
又一个变形就是     
1011. Capacity To Ship Packages Within D Days   

可以参见这个评论找到更多的类似的二分法题解。


# Method dp:

```java
class Solution {

    public int splitArray(int[] nums, int K) {
        int N = nums.length;
        int[][] dp = new int[K + 1][N + 1];
        int[] prefixSum = new int[N + 1]; // 我们这里是为了阅读方便，可以不建这个数组的
        for (int i = 0; i < N; i++) {
            dp[1][i + 1] = dp[1][i] + nums[i];
            prefixSum[i + 1] = prefixSum[i] + nums[i];
        }
        if (K == 1) return dp[1][N];
        for (int k = 2; k <= K; k++) {
            for (int i = k - 1; i < N; i++) {
                int curMinMax = Integer.MAX_VALUE;
                for (int j = k - 2; j < i; j++) {
                    int maxSubArraySum = Math.max(dp[k - 1][j + 1], prefixSum[i + 1] - prefixSum[j + 1]);
                    curMinMax = Math.min(curMinMax, maxSubArraySum);
                }
                dp[k][i + 1] = curMinMax;
            }
        }
        return dp[K][N];
    }

}
/** 
    这个题是非常有名并且经典的题目
    dp[k][i + 1] 定义即 0 -> i 的subarray的分成k份的时候的 minimal largest sum
    k = 0 就忽略了
    我们从 k = 1 开始，可知因为只分割一份，这就是一个 prefixSum 
    现在看 k = 2, 考虑dp[2][i + 1], 
        我们选择相对于dp[1][i + 1]在 j 处再切一刀，即把 j 和之前的元素分出去 其中 0 <= j < i, 注意这里 0 来自于 k - 2
        则 dp[1][j + 1] 包含了之前的最小的最大值，
        我们再拿剩下的数之和（用 prefixSum, 即dp[1][i + 1] - dp[1][j + 1] 立得）与 dp[1][j + 1] 相比，取更大的一个
        对于所有的 j 遍历我们可以取使得dp[2][i + 1]最小的分割
    因此对于一般的 k 我们有
    考虑如下数组：
        7 2  5 10  8

    k\i 0 1  2  3  4
    1   7 9 14 24 32  
    2   \ 7  7 14 18
    3   \ \  7  7 14 


*/
```