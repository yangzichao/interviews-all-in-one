# 525 Contiguous-Array 
 
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
<div><p>Given a binary array <code>nums</code>, return <em>the maximum length of a contiguous subarray with an equal number of </em><code>0</code><em> and </em><code>1</code>.</p>
<p>&nbsp;</p>
<p><strong class="example">Example 1:</strong></p>
<pre><strong>Input:</strong> nums = [0,1]
<strong>Output:</strong> 2
<strong>Explanation:</strong> [0, 1] is the longest contiguous subarray with an equal number of 0 and 1.
</pre>
<p><strong class="example">Example 2:</strong></p>
<pre><strong>Input:</strong> nums = [0,1,0]
<strong>Output:</strong> 2
<strong>Explanation:</strong> [0, 1] (or [1, 0]) is a longest contiguous subarray with equal number of 0 and 1.
</pre>
<p>&nbsp;</p>
<p><strong>Constraints:</strong></p>
<ul>
	<li><code>1 &lt;= nums.length &lt;= 10<sup>5</sup></code></li>
	<li><code>nums[i]</code> is either <code>0</code> or <code>1</code>.</li>
</ul>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public int findMaxLength(int[] nums) {
            // 记分法则，如果是 1 则 加 1 分 如果是 0 则 减一分
            // 如果经历过一个分数，并且又回到了这个分数，说明这个区间之内是有等量的1 0
            // 我们用map记下每个score第一次出现的index
        Map<Integer, Integer> scoreIndexMap = new HashMap<>();
        scoreIndexMap.put(0, -1);
        int score = 0;
        int maxLength = 0;
        for(int i = 0; i < nums.length; i++){
            score += nums[i] == 1 ? 1 : -1;
            if(scoreIndexMap.containsKey(score)){
                maxLength = Math.max(maxLength, i - scoreIndexMap.get(score) );
            }else{
                scoreIndexMap.put(score, i);
            }
        }
        return maxLength;
    }
}
        return maxLength;
​
```