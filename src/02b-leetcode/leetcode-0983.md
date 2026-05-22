
这个题很好我很喜欢，比较朴素的一种DP的写法就是下面这么写的。


```java
class Solution {
    public int mincostTickets(int[] days, int[] costs) {
        int lastTravelDay = days[days.length - 1];
        int[] dp = new int[lastTravelDay + 1];
        for (int day : days) {
            dp[day] = 1;
        }
        for (int i = 1; i <= lastTravelDay; i++) {
            if (dp[i] == 0) {
                dp[i] = dp[i - 1];
            } else {
                dp[i] = dp[i - 1] + costs[0];
                int min7 = i - 7 >= 0 ? dp[i - 7] + costs[1] : costs[1];
                int min30 = i - 30 >= 0 ? dp[i - 30] + costs[2] : costs[2];
                dp[i] = Math.min(dp[i], min7);
                dp[i] = Math.min(dp[i], min30);
            } 
        }
        return dp[lastTravelDay];
    }
}
```

## 2025 
似乎这样写的更严谨一点。
```java
class Solution {
    public int mincostTickets(int[] days, int[] costs) {
        int lastTravelDay = days[days.length - 1];
        int[] minCostTickets = new int[lastTravelDay + 1];
        for (int i = 0; i < days.length; i++) {
            minCostTickets[days[i]] = 1; // 用1标记当前为旅行日
        }

        for (int i = 1; i <= lastTravelDay; i++) {
            if (minCostTickets[i] == 0) {
                // 今天不是旅行日，没有额外开销
                minCostTickets[i] = minCostTickets[i - 1];
                continue;
            }
            // 今天是旅行日，检查三种策略。
            int buy1 = minCostTickets[i - 1] + costs[0];
            int buy7 = (i >= 7 ? minCostTickets[i - 7] : 0) + costs[1];
            int buy30 = (i >= 30 ? minCostTickets[i - 30] : 0) + costs[2];
            // 为什么不检查 buy2？ 因为 x - 2日的 minimalcost 一定是 >= x - 7日的 同理 buy8 ...
            minCostTickets[i] = Math.min(buy1, Math.min(buy7, buy30));
        }
        return minCostTickets[lastTravelDay];
    }
}
```