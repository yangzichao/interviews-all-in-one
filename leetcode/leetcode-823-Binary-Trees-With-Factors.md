# 823 Binary-Trees-With-Factors 
 
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
<div><p>Given an array of unique integers, <code>arr</code>, where each integer <code>arr[i]</code> is strictly greater than <code>1</code>.</p>
<p>We make a binary tree using these integers, and each number may be used for any number of times. Each non-leaf node's value should be equal to the product of the values of its children.</p>
<p>Return <em>the number of binary trees we can make</em>. The answer may be too large so return the answer <strong>modulo</strong> <code>10<sup>9</sup> + 7</code>.</p>
<p>&nbsp;</p>
<p><strong>Example 1:</strong></p>
<pre><strong>Input:</strong> arr = [2,4]
<strong>Output:</strong> 3
<strong>Explanation:</strong> We can make these trees: <code>[2], [4], [4, 2, 2]</code></pre>
<p><strong>Example 2:</strong></p>
<pre><strong>Input:</strong> arr = [2,4,5,10]
<strong>Output:</strong> 7
<strong>Explanation:</strong> We can make these trees: <code>[2], [4], [5], [10], [4, 2, 2], [10, 2, 5], [10, 5, 2]</code>.</pre>
<p>&nbsp;</p>
<p><strong>Constraints:</strong></p>
<ul>
	<li><code>1 &lt;= arr.length &lt;= 1000</code></li>
	<li><code>2 &lt;= arr[i] &lt;= 10<sup>9</sup></code></li>
</ul>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public int numFactoredBinaryTrees(int[] A) {
        // 这道题还是很有意思的
        // 可以好好分析分析
        int MOD = 1_000_000_007;
        int N = A.length;
        Arrays.sort(A);
        long[] dp = new long[N];
        Arrays.fill(dp, 1);
​
        Map<Integer, Integer> index = new HashMap();
        for (int i = 0; i < N; ++i)
            index.put(A[i], i);
​
        for (int i = 0; i < N; ++i)
            for (int j = 0; j < i; ++j) {
                if (A[i] % A[j] == 0) { // A[j] is left child
                    int right = A[i] / A[j];
                    if (index.containsKey(right)) {
                        dp[i] = (dp[i] + dp[j] * dp[index.get(right)]) % MOD;
                    }
                }
            }
​
        long ans = 0;
        for (long x: dp) ans += x;
        return (int) (ans % MOD);
    }
}
​
```