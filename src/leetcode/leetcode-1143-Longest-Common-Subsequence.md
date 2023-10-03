# 1143 Longest-Common-Subsequence

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
<div><p>Given two strings <code>text1</code> and <code>text2</code>, return the length of their longest common subsequence.</p>
<p>A <em>subsequence</em> of a string is a new string generated from the original string with some characters(can be none) deleted without changing the relative order of the remaining characters. (eg, "ace" is a subsequence of "abcde" while "aec" is not).&nbsp;A <em>common subsequence</em>&nbsp;of two strings is a subsequence that is common to both strings.</p>
<p>&nbsp;</p>
<p>If there is no common subsequence, return 0.</p>
<p>&nbsp;</p>
<p><strong>Example 1:</strong></p>
<pre><strong>Input:</strong> text1 = "abcde", text2 = "ace" 
<strong>Output:</strong> 3  
<strong>Explanation:</strong> The longest common subsequence is "ace" and its length is 3.
</pre>
<p><strong>Example 2:</strong></p>
<pre><strong>Input:</strong> text1 = "abc", text2 = "abc"
<strong>Output:</strong> 3
<strong>Explanation:</strong> The longest common subsequence is "abc" and its length is 3.
</pre>
<p><strong>Example 3:</strong></p>
<pre><strong>Input:</strong> text1 = "abc", text2 = "def"
<strong>Output:</strong> 0
<strong>Explanation:</strong> There is no such common subsequence, so the result is 0.
</pre>
<p>&nbsp;</p>
<p><strong>Constraints:</strong></p>
<ul>
	<li><code>1 &lt;= text1.length &lt;= 1000</code></li>
	<li><code>1 &lt;= text2.length &lt;= 1000</code></li>
	<li>The input strings consist of lowercase English characters only.</li>
</ul>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public int longestCommonSubsequence(String text1, String text2) {
        // 这个题得好好看看
        // 首先搞明白题意，这是 subsequence 不是 substring. 
        // 比如 abc 和 adc 的 longest common subsequence 是 ac. 即，只要在string中的相对顺序不变就行。
        // 这个办法是建一个 2D dp 数组，代表啥呢？
    //   index 0 1 2 3
    //   index / a b c 
        // 0 / 0 0 0 0    
        // 1 a 0 1 1 1
        // 2 d 0 1 1 1
        // 3 c 0 1 1 2
        // 比如 (1,1) 是 1, a 和 a 相等，最长子数组是 1.
        // (1, 2)  位置，是 1，代表着是 ab 和 a 的最长子数组, 由于 b /= a, 所以我们保持之前的最长记录。
        // 我们到了 (1,3) 位置是 abc 和 a, c /= a, 同样保持之前的最长纪录。
        // 而 (2,2) 位置是 ab 和 ad 的最长子数组。
        // (3,3) 是 abc 和 adc 有 c == c, 那么我们就在 ab 和 ad 的记录上面再增加 1. 
        // 可以看这个 一看就明白 https://www.youtube.com/watch?v=Dumq-rceuac
        // 但是他解释的很差
        int m = text1.length();
        int n = text2.length();
        int[][] dp = new int[ m + 1][ n + 1];
        for(int i = 0; i < m; i++ ) {
            for( int j = 0; j < n; j++ ) {
                if( text1.charAt(i) == text2.charAt(j) ) {
                    dp[i + 1][j + 1] = dp[i][j] + 1;
                }else{
                    dp[i + 1][j + 1] = Math.max(dp[i][j + 1],dp[i + 1][j] );
                }
            }
        }    
        return dp[m][n];
    }
}
```

需要补充一些分析：
这个题当然是要用动态规划的。 问题是如果给你了子问题的答案，你怎么处理呢？  
比如两个 String, 用 strA, strB 来表示，strA1, strB1 代表 strA 和 strB 去掉最后一个字母的 String。
其实如何自然的得出这个分类，我也没想好，还是直接跳跃的来解释吧！  
现在知道的子问题的结果是：  
strA1, strB : l1,
strA, strB1 : l2,
strA1, strB1: l3,
对于以上每种情况，为了推导到 strA, strB 的答案， 我们都需要做一个运算， 就是对比 strA, strB 最后一个字母是否相同。  
第一种情况，这两个字母如果不相同，很明显，我们取 l1, l2, l3 的最大值即可，但是显然 l1, l2 >= l3, 而且 l1, l2 最多比 l3 大 1, 即 l3 + 1 >= l1, l2 >= l3 . 因此这种情况，我们只比较 l1, l2.
另一种情况是 strA, strB 的最后一个字母相同。 那么我们要的结果至少是 l3 + 1。至于 l1, l2, 其实我们没法说。但是由于 l3 + 1 >= l1, l2 >= l3. 所以我们直接取 l3 + 1 就好了。
