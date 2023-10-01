
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