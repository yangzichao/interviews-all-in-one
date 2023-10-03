# 038 Count-and-Say 
 
difficulty: Easy 
 
<style>
        section pre{
          background-color: #eee;
          border: 1px solid #ddd;
          padding:10px;
          border-radius: 5px;
        }
      </style>
<section>
<div><p>The <strong>count-and-say</strong> sequence is a sequence of digit strings defined by the recursive formula:</p>
<ul>
	<li><code>countAndSay(1) = "1"</code></li>
	<li><code>countAndSay(n)</code> is the way you would "say" the digit string from <code>countAndSay(n-1)</code>, which is then converted into a different digit string.</li>
</ul>
<p>To determine how you "say" a digit string, split it into the <strong>minimal</strong> number of groups so that each group is a contiguous section all of the <strong>same character.</strong> Then for each group, say the number of characters, then say the character. To convert the saying into a digit string, replace the counts with a number and concatenate every saying.</p>
<p>For example, the saying and conversion for digit string <code>"3322251"</code>:</p>
<img alt="" src="https://assets.leetcode.com/uploads/2020/10/23/countandsay.jpg" style="width: 581px; height: 172px;">
<p>Given a positive integer <code>n</code>, return <em>the </em><code>n<sup>th</sup></code><em> term of the <strong>count-and-say</strong> sequence</em>.</p>
<p>&nbsp;</p>
<p><strong>Example 1:</strong></p>
<pre><strong>Input:</strong> n = 1
<strong>Output:</strong> "1"
<strong>Explanation:</strong> This is the base case.
</pre>
<p><strong>Example 2:</strong></p>
<pre><strong>Input:</strong> n = 4
<strong>Output:</strong> "1211"
<strong>Explanation:</strong>
countAndSay(1) = "1"
countAndSay(2) = say "1" = one 1 = "11"
countAndSay(3) = say "11" = two 1's = "21"
countAndSay(4) = say "21" = one 2 + one 1 = "12" + "11" = "1211"
</pre>
<p>&nbsp;</p>
<p><strong>Constraints:</strong></p>
<ul>
	<li><code>1 &lt;= n &lt;= 30</code></li>
</ul>
</div></section>
 
 ## Method One 
 
 这个题麻烦在于弄懂题意。看起来好像可以递归，实际上不太能。
 其实就是重复 n - 1 次 **把一个函数的输出当成下一次的输入**。
 而这个函数本身干了什么事，虽然是这个题目中比较重要的部分，但是却是这个题中得到的最不重要的知识。
 我觉得这个题目考察的最重要的就是，**能写出下面这种去耦的代码**。

``` Java
class Solution {
    public String countAndSay(int n) {
        if( n == 1){
            return "1";
        }
        String val = "1"; 
        for(int i = 1; i < n; i++){
            val = getNextVal(val);
        }
        return val;
    }
    
    public String getNextVal(String val){
        StringBuilder sb = new StringBuilder();
        char[] input = val.toCharArray();
        char cur = input[0];
        int count = 0;
        for(char c : input){
            if( cur != c ){
                sb.append(String.valueOf(count)).append(cur);
                cur = c;
                count = 1;
            }else{
                count++;
            }  
        }
        sb.append(String.valueOf(count)).append(cur);
        return sb.toString();
    }
}
​
```