# 300 Longest-Increasing-Subsequence 
 
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
<div><p>Given an unsorted array of integers, find the length of longest increasing subsequence.</p>
<p><b>Example:</b></p>
<pre><b>Input:</b> <code>[10,9,2,5,3,7,101,18]
</code><b>Output: </b>4 
<strong>Explanation: </strong>The longest increasing subsequence is <code>[2,3,7,101]</code>, therefore the length is <code>4</code>. </pre>
<p><strong>Note: </strong></p>
<ul>
	<li>There may be more than one LIS combination, it is only necessary for you to return the length.</li>
	<li>Your algorithm should run in O(<i>n<sup>2</sup></i>) complexity.</li>
</ul>
<p><b>Follow up:</b> Could you improve it to O(<i>n</i> log <i>n</i>) time complexity?</p>
</div></section>
 

 Subsequence 有多少个呢？ 其实subsequence和subset一样，有2^N个。
 如果 1 个元素， 有 subs(1) = 1 = 2^1 - 1;
 如果新加1个元素，它自己是一个新的subsequence, 也可以和之前的每个元素组成一个新的subsequence, 因此新增加的元素是 subs(1) + 1, 总数是 2*subs(1) + 1 = 2^2 - 1;
 再新加一个，新产生的元素是 subs(2) + 1.
 所以递推公式是 subs(n) = 2 subs(n - 1) + 1;
 所以是 O(2^N); 
 
 ## Method One 
 
``` Java
class Solution {
    public int lengthOfLIS(int[] nums) {
        // 我们 dp[i] 存的是 以 nums[i] 结尾的最长长度。
        // 注意 这样 dp[ dp.length - 1] 并非是 最长的。
        // 比如  1 4 10 4 这个，dp[3] = 2。 
        // 因为重复定义： dp[3] 存的是 以 4 结尾的最长长度。
        // 最长的长度在 dp[2] = 3.
        
        if( nums.length < 1) {
            return 0;
        }
        int[] dp = new int[nums.length]; 
        Arrays.fill(dp, 1);
        int max = 1;
        for(int i = 1; i < nums.length; i++ ) {
            for(int j = 0; j < i ; j++ ) {
                if ( nums[i] > nums[j] ){
                    dp[i] = Math.max( dp[i] , dp[j] + 1);
                    max = Math.max(max, dp[i]);
                }
            }
        }
        return max;
    }
}
​
```


300 题试听重要的，做完了可以练练673.