# 122J. Best Time to Buy and Sell Stock II

```java
class Solution {
    public int maxProfit(int[] prices) {
        int totalProfit = 0;
        for(int i = 1; i < prices.length; i++){
            int curProfit = prices[i] - prices[i - 1];
            if(curProfit > 0){
                totalProfit += curProfit;
            }
        }
        return totalProfit;
    }
}
```
