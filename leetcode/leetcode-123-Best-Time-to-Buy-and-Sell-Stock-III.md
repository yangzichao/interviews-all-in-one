# 123J. Best Time to Buy and Sell Stock III

卖股票两次这个题目，我们其实可以把它当成两个买股票一次来看待。
buyOneCurLow 一直是此前的最低价格。
sellOneMaxProfit 是卖一次的最大收益。
以上和 Best Time to Buy and Sell Stock I 是一样的。关键的地方来了。
我们要把此刻卖了一次的利润，加到当下的价格之中，这样 buyTwoCurLow 就是 考虑了卖一次的利润之后的最低价格。
此时 sellTwoMaxProfit 就是卖两次的最大利润。

注意 不能使用下面的语句

```java
    buyTwoCurLow = Math.min(buyTwoCurLow, price );
    sellTwoMaxProfit = Math.max(sellTwoMaxProfit, price - buyTwoCurLow + sellOneMaxProfit);
```

```java
class Solution {
    public int maxProfit(int[] prices) {
        /**
            max profit in the following states
            state 0 : we haven't buy the first stock yet, so we track the lowest price
            state 1 : we are going to sell the first stock, so we have a profit here. Here we have the all time high profit.
            state 2 : we haven't buy the second stock, so the lowest price would be current price - first stock profit.
            state 3 : we are going to sell the second stock.
         */

         int buyOneCurLow = Integer.MAX_VALUE;
         int buyTwoCurLow = Integer.MAX_VALUE;
         int sellOneMaxProfit = 0;
         int sellTwoMaxProfit = 0;
         for(int price : prices) {
             buyOneCurLow = Math.min(buyOneCurLow, price);
             sellOneMaxProfit = Math.max(sellOneMaxProfit, price - buyOneCurLow);
             buyTwoCurLow = Math.min(buyTwoCurLow, price - sellOneMaxProfit);
             sellTwoMaxProfit = Math.max(sellTwoMaxProfit, price - buyTwoCurLow);
         }
         return sellTwoMaxProfit;
    }
}
```
