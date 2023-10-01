# 322 Coin-Change 
 
difficulty: Medium 
 
<style>
        section pre{
          background-color: #eee;
          border: 1px solid #ddd;
          padding:10px;
          border-radius: 5px;
        }
      </style>
<section>
<div><p>You are given coins of different denominations and a total amount of money <i>amount</i>. Write a function to compute the fewest number of coins that you need to make up that amount. If that amount of money cannot be made up by any combination of the coins, return <code>-1</code>.</p>
<p><b>Example 1:</b></p>
<pre><strong>Input: </strong>coins = <code>[1, 2, 5]</code>, amount = <code>11</code>
<strong>Output: </strong><code>3</code> 
<strong>Explanation:</strong> 11 = 5 + 5 + 1</pre>
<p><b>Example 2:</b></p>
<pre><strong>Input: </strong>coins = <code>[2]</code>, amount = <code>3</code>
<strong>Output: </strong>-1
</pre>
<p><b>Note</b>:<br>
You may assume that you have an infinite number of each kind of coin.</p>
</div></section>
 


 这个题很经典，时间复杂度是 O( S * N). 空间复杂度是 O(N)。  
 其实可以把空间复杂度降低到 O( max(coin value) )。 因为你只需最近的 1 - coin-value 的cache.
 ## Method One 
 
``` Java
class Solution {
    // top-down method
    // dp(N) = min( dp(N - a), dp(N - b), ... all possibilities) + 1;
    // dp(0) = 0;
    // dp(negative values) = -1, 不考虑它
    public int coinChange(int[] coins, int amount) {
        if(amount == 0){
            return 0;
        }
        return helper(coins, amount, new int[amount]);
    }
    public int helper(int[] coins, int amount, int[] counts ){
        if( amount < 0 ){
            return -1;
        }
        if( amount == 0 ){
            return 0;
        }
        // 这个 early return 是 dp 的关键之处
        if( counts[amount - 1] != 0){
            return counts[amount - 1];
        }
        int min = Integer.MAX_VALUE;
        for(int coin : coins ){
            int cur = helper( coins, amount - coin, counts);
            if( cur >= 0){
                min = Math.min(min, cur);
            }
        }
        counts[amount - 1] = min == Integer.MAX_VALUE ? -1 : min + 1;
        return counts[amount - 1];
    }
}
​
```

感受一下TLE的代码。
```java
class Solution {
    public int coinChange(int[] coins, int amount) {
        if( amount < 0){
            return -1;
        }
        if( amount == 0){
            return 0;
        }
        int coinCount = Integer.MAX_VALUE;
        for(int coin : coins){
            int curCount = coinChange(coins, amount - coin);
            if( curCount >= 0){
                coinCount = Math.min(coinCount, curCount);
            }
        }
        return coinCount == Integer.MAX_VALUE ? -1 : coinCount + 1;
    }
}
```


不使用递归当然也可以写！
```java
class Solution {
    public int coinChange(int[] coins, int amount) {
        int[] numberOfCoins = new int[amount + 1];
        Arrays.fill(numberOfCoins, -1);
        numberOfCoins[0] = 0;
        for(int i = 1; i < amount + 1; i++){
            int cur = Integer.MAX_VALUE;
            for(int coin : coins){
                if(i - coin < 0){
                    continue;
                }
                if( numberOfCoins[i - coin] < 0){
                    continue;
                }
                cur = Math.min(cur, numberOfCoins[i - coin]);
            }
            if( cur < Integer.MAX_VALUE){
                numberOfCoins[i] = cur + 1;
            }
        }
        return numberOfCoins[amount];
    }
}
```


其实怎么写都可以，但是 leetCode 给了一个 [1,2147483647] 的例子，也太坑了卧槽。

```java
class Solution {
    public int coinChange(int[] coins, int amount) {
        if (amount == 0) return 0;
        int[] dp = new int[amount + 1];
        for (int i = 0; i <= amount; i++) {
            if (i > 0 && dp[i] == 0) continue;
            for (int coin : coins) {
                if (i + coin > amount) continue;
                if (i >= Integer.MAX_VALUE - coin) continue;
                if (dp[i + coin] == 0) {
                    dp[i + coin] = dp[i] + 1;
                } else {
                    dp[i + coin] = Math.min(dp[i + coin], dp[i] + 1);
                }
            }
        }
        return dp[amount] == 0 ? -1 : dp[amount];
    }
}
```

Follow up : 如何把上面的答案改一点，就得到 coin 的组合呢？


```java
class Solution {
    public int coinChange(int[] coins, int amount) {
        if (amount == 0) return 0;
        ArrayList<Integer>[] dp = new ArrayList[amount + 1];
        dp[0] = new ArrayList<>();
        for (int i = 0; i <= amount; i++) {
            if (i > 0 && dp[i] == null) continue;
            for (int coin : coins) {
                if (i + coin > amount) continue;
                if (i > Integer.MAX_VALUE - coin) continue;
                if (dp[i + coin] == null) {
                    dp[i + coin] = new ArrayList<>(dp[i]);
                    dp[i + coin].add(coin);
                } else {
                    if (dp[i + coin].size() > dp[i].size() + 1) {
                        dp[i + coin] = new ArrayList<>(dp[i]);
                        dp[i + coin].add(coin);
                    }
                }
            }
        }
        return dp[amount] == null ? -1 : dp[amount].size();
    }
}
```