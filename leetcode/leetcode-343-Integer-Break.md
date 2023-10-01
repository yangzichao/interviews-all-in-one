# 343 Integer-Break 
 
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
<div><p>Given a positive integer <i>n</i>, break it into the sum of <b>at least</b> two positive integers and maximize the product of those integers. Return the maximum product you can get.</p>
<p><strong>Example 1:</strong></p>
<div>
<pre><strong>Input: </strong><span id="example-input-1-1">2</span>
<strong>Output: </strong><span id="example-output-1">1</span>
<strong>Explanation: </strong>2 = 1 + 1, 1 × 1 = 1.</pre>
<div>
<p><strong>Example 2:</strong></p>
<pre><strong>Input: </strong><span id="example-input-2-1">10</span>
<strong>Output: </strong><span id="example-output-2">36</span>
<strong>Explanation: </strong>10 = 3 + 3 + 4, 3 ×&nbsp;3 ×&nbsp;4 = 36.</pre>
<p><b>Note</b>: You may assume that <i>n</i> is not less than 2 and not larger than 58.</p>
</div>
</div></div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public int integerBreak(int n) {
        // 这个解法其实值得好好想一想
        // 和Knapsack的问题一起想想
        // 我们的朴素的想法就是， n 可以取出 1 到 n - 1 所有的数, 用 i 表示。
        // 那么剩下的部分就是 n - i, 假设此时 dp[n - i] 是已知的最大的结果。
        // 那么我们比较 dp[n] 和 dp[n - i] * i 的大小，就是在比较，拆出一个 i 会不会得到更大的数。
        // 那么我们不断更新这个 dp 数组，就能得到最大的值了。
        // dp[0] = 1 是为了方便 j = n 的情况能得到 n * 1 而不是 0.
        // int i = n - 1; i > 0; i--, 还是 i = 1; i < n; i++ 都一样。
        // j 只能从小往大尝试。
​
        int[] dp = new int[ n + 1];
        dp[0] = 1;
        for(int i = n - 1; i > 0; i-- ){
            for(int j = i; j <= n; j++){
                dp[j] = Math.max( dp[j], dp[j - i] * i);
            }
        }
        return dp[n];
    }
}
​
​
​
```

## Method Best

```java
class Solution {
    public int integerBreak(int n) {
        // 首先亮一个结论，应当尽可能多的用3，除非最后有一个4剩下，此时应当用 2*2。
        // 证明: suppose we have a factor called f, and f is larger or equal to 4.
        // f >= 4. Let us split f into (f - 2) and 2. (f - 2)*2 = 2f - 4 > = 4.
        // This proves that no factor larger than 4 is needed.
        // Now we do the same for (f - 3)*3 = 3f - 9, for f = 4, it is smaller than f.
        // while for f > 4, it is always good to split a 3 out.
        // 现在问题变成了，n = 5 + 3*k, n = 4+ 3*k, n = 3 + 3*k 三种情况。
        // 对于第一种，2*3^(k + 1). 第二种 4*3^k, 第三种 3^(k + 1).
        // 为什么用 pow 计算呢？ 因为Java中 pow是 O(1)的，这样这个解法就是 O(1) 了。
        // 如果手动进行 pow 计算也是 log(n) 的时间复杂度！
        if( n == 2){
            return 1;
        }
        if( n == 3){
            return 2;
        }
        int ans = 1;
        if( n % 3 == 0 ){
            return (int) Math.pow(3, n/3);
        }else if( n % 3 == 1 ){
            return (int) ( 4*Math.pow(3, (n - 4)/3) );
        }else{
            return (int) ( 2*Math.pow(3, n/3) );
        }
        
    }
}
```