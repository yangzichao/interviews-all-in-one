
```java
class Solution {
    public int maxProfit(int[] prices) {
        /**
            我们有三个状态，buyAndHold, sell, rest.
            0: buyAndHold 就是我这轮买股票，或者之前某轮买了股票。下一轮我们只能去到卖或者继续Hold.即 -> 0, 1
            1: sell 我卖了股票。下一轮我们只能去往 rest. 即 -> 2
            2: rest 上一轮卖了股票，所以我要休息。下一轮可以继续休息，或者买股票。即 -> 0, 2
            这里面的一个技巧就是，我们在买股票的时候，立刻把当下的price当作cost给计算到总的利润当中去。
            0 : 上一轮是 0或者2. 选择 0 即意味着 hold, 选择 2 意味着我做的事buy的动作， 买就要立刻扣掉成本。
            1 : 只能由 0 转换来。由于成本已经扣掉了，那么立刻加上price就是利润了。
            2 : 只能由 1 或者 2 转换来。比较简单。
         */
         int bought = Integer.MIN_VALUE;
         int sell = Integer.MIN_VALUE;
         int rest = 0;
         for(int price : prices) {
             int preBought = bought;
             int preSell = sell;
             int preRest = rest;

             sell = preBought + price;
             bought = Math.max(preRest - price, preBought);
             rest = Math.max(preRest, preSell);
         }

         return Math.max(bought, Math.max(rest, sell));
    }
}
```



```java
class Solution {
    public int maxProfit(int[] prices) {
        /**
        * 这里采用有限状态机的标准表格，左侧一列代表操作，上面一行代表状态
        * cell代表状态经过左侧的操作进入的新状态。
        * 其中三个状态 held意味着买入了并持有股票中，sold是股票刚刚卖出，此轮不能买入只能休息，cool意味着可以此轮可以买入股票。
        * 这个题的难点在于抓住这三个状态
        * action\state:     held sold cool
        * buy                 \   \   held
        * sell              sold  \    \
        * rest              held cool cool
        */
        if (prices.length < 2) return 0;
        int held = Integer.MIN_VALUE;
        int sold = Integer.MIN_VALUE;
        int cool = 0;
        for (int i = 0; i < prices.length; i++) {
            int preHeld = held;
            int preSold = sold;
            int preCool = cool;
            held = Math.max(preHeld, preCool - prices[i]);
            sold = held + prices[i];
            cool = Math.max(preCool, preSold);
        }
        return Math.max(held, Math.max(sold, cool));
    }
}
```

