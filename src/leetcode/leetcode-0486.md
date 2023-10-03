还可以看看这个题解 https://leetcode.cn/problems/predict-the-winner/solution/shou-hua-tu-jie-san-chong-xie-fa-di-gui-ji-yi-hua-/

以下是一个评论区的分析，非常精彩。区间型 dp 的例子。

```
相对分数 说成 净胜分 ，语义会更强一些。

甲乙比赛，甲先手面对区间[i...j]时，dp[i][j]表示甲对乙的净胜分。

最终求的就是，甲先手面对区间[0...n-1]时，甲对乙的净胜分dp[0][n-1]是否>=0。

甲先手面对区间[i...j]时，

如果甲拿nums[i]，那么变成乙先手面对区间[i+1...j]，这段区间内乙对甲的净胜分为dp[i+1][j]；那么甲对乙的净胜分就应该是nums[i] - dp[i+1][j]。
如果甲拿nums[j]，同理可得甲对乙的净胜分为是nums[j] - dp[i][j-1]。
以上两种情况二者取大即可。
```

## method recursion:

这是 O(2^N) 的时间复杂度，但是思路比较直接。

```java
class Solution {
    // 我们先写一个递归的思路，不快，但是有助于思考。
    // 我们把问题转化一下，问题中优胜的意义变成是先手玩家P1相对于后手玩家P2的净得分大于0.
    // 这样我们就把子问题转化为，某个子数组的先手玩家的最大净得分是多少。
    // 我们假设 p1MaxNetWinInSubarray 返回的是某个子数组的先手玩家的最大净得分

    public boolean PredictTheWinner(int[] nums) {
        return p1MaxNetWinInSubarray(nums, 0, nums.length - 1) >= 0;
    }

    private int p1MaxNetWinInSubarray(int[] nums, int i, int j) {
        if(i == j) { // 此时数组只有一个元素，先手直接取胜
            return nums[i];
        }
        // 子数组是从 i 到 j, 先手玩家只能选择 i 或者 j.
        // 为什么这里用减法呢？因为此时后手玩家P2 一定会选剩余子数组的最大净的分的战略
        int maxHead = nums[i] - p1MaxNetWinInSubarray(nums, i + 1, j);
        int maxTail = nums[j] - p1MaxNetWinInSubarray(nums, i, j - 1);
        return Math.max(maxHead, maxTail);
    }
}
```

基于上面的思想，这个题目被优化到了 O(N^2) 的时间复杂度。

```java

class Solution {
    // 那么我们典型的优化就是加一个数组记忆下这个数值, 果然速度直接变成 0 ms beat 100%了。
    // 注意这里有一个问题就是不能够假设 p1 net win 一定大于0
    private Integer[][] p1MaxNetWinInSubarrayIJ;
    public boolean PredictTheWinner(int[] nums) {
        p1MaxNetWinInSubarrayIJ = new Integer[nums.length][nums.length];
        return p1MaxNetWinInSubarray(nums, 0, nums.length - 1) >= 0;
    }

    private int p1MaxNetWinInSubarray(int[] nums, int i, int j) {
        if(i == j) { // 此时数组只有一个元素，先手直接取胜
            p1MaxNetWinInSubarrayIJ[i][j] = nums[i];
            return nums[i];
        }
        if(p1MaxNetWinInSubarrayIJ[i][j] != null) return p1MaxNetWinInSubarrayIJ[i][j];
        // 子数组是从 i 到 j, 先手玩家只能选择 i 或者 j.
        // 为什么这里用减法呢？因为此时后手玩家P2 一定会选剩余子数组的最大净的分的战略
        int maxHead = nums[i] - p1MaxNetWinInSubarray(nums, i + 1, j);
        int maxTail = nums[j] - p1MaxNetWinInSubarray(nums, i, j - 1);
        p1MaxNetWinInSubarrayIJ[i][j] = Math.max(maxHead, maxTail);
        return p1MaxNetWinInSubarrayIJ[i][j];
    }
}
```

## method other:
486 这个题也太好了
只需要知道这个题有个 tag 叫 minimax.

```java
class Solution {
    // 这个题目是一个新的类型的叫minimax
    // 相关的题目可以看一下877 stone game
    // 这里的思路就是 dp[i][j][0], dp[i][j][1] 表示的是 nums[i] 到 nums[j] 的subarray 的先手0 和后手1的最大分数
    // 其中dp[i][j][0]是先手的最大分数，而dp[i][j][1]是后手的最小的最大值。这就是题目标minimax的原因。至于为什么，我其实没想明白。
    public boolean predictTheWinner(int[] nums) {
        // [0,...n - 1], [0,... n -2], [1,... n-1];
        // [1, ... n]
        // dp[i][j] represent [i,...j]; check dp[i + 1][j] or dp[i][j - 1];
        // dp represent netscore of player 1
        int N = nums.length;
        int[][] dp = new int[N][N];
        for (int i = 0; i < N; i++) {
            dp[i][i] = nums[i];
        }
        // dp[0][0], dp[1][1], -> dp[0][1];
        // dp[i + 1][j] or dp[i][j - 1]
        // pick dp[i + 1][j] means you take nums[i] this round
        // pick dp[i][j - 1] means you take nums[j] this round
        // assume player 2 is very smart and knows global optimized results
        // you net score will be nums[i] - dp[i + 1][j] or nums[j] - dp[i][j - 1], pick the larger one
        // pick upper triangle 上三角
        for (int len = 1; len < N; len++) {
            for (int row = 0; row + len < N; row++) {
                int col = row + len;
                dp[row][col] = Math.max(nums[row] - dp[row + 1][col], nums[col] - dp[row][col - 1]);
            }
        }
        return dp[0][N - 1] >= 0;
    }
}
```