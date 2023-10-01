# 029 Divide-Two-Integers

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
<div><p>Given two integers <code>dividend</code> and <code>divisor</code>, divide two integers without using multiplication, division, and mod operator.</p>
<p>Return the quotient after dividing <code>dividend</code> by <code>divisor</code>.</p>
<p>The integer division should truncate toward zero, which means losing its fractional part. For example, <code>truncate(8.345) = 8</code> and <code>truncate(-2.7335) = -2</code>.</p>
<p><strong>Note:</strong></p>
<ul>
	<li>Assume we are dealing with an environment that could only store integers within the 32-bit signed integer range: [−2<sup>31</sup>, &nbsp;2<sup>31</sup> − 1]. For this problem, assume that your function <strong>returns 2<sup>31</sup> − 1 when the division result&nbsp;overflows</strong>.</li>
</ul>
<p>&nbsp;</p>
<p><strong>Example 1:</strong></p>
<pre><strong>Input:</strong> dividend = 10, divisor = 3
<strong>Output:</strong> 3
<strong>Explanation:</strong> 10/3 = truncate(3.33333..) = 3.
</pre>
<p><strong>Example 2:</strong></p>
<pre><strong>Input:</strong> dividend = 7, divisor = -3
<strong>Output:</strong> -2
<strong>Explanation:</strong> 7/-3 = truncate(-2.33333..) = -2.
</pre>
<p><strong>Example 3:</strong></p>
<pre><strong>Input:</strong> dividend = 0, divisor = 1
<strong>Output:</strong> 0
</pre>
<p><strong>Example 4:</strong></p>
<pre><strong>Input:</strong> dividend = 1, divisor = 1
<strong>Output:</strong> 1
</pre>
<p>&nbsp;</p>
<p><strong>Constraints:</strong></p>
<ul>
	<li><code>-2<sup>31</sup> &lt;= dividend,&nbsp;divisor &lt;= 2<sup>31</sup> - 1</code></li>
	<li><code>divisor != 0</code></li>
</ul>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public int divide(int dividend, int divisor) {
        // 这个题有诸多方法，但是这个方法是最好的应该
        // 想像一下十进制的除法怎么做的？
        // 比如 34 / 3  = 11;
        // 我们 3*10 / 3 = 1*10
        // 然后 （34 - 30）/ 3 = 4 / 3 = 1
        // 答案 是 1*10 + 1 = 11;
        // 再来一个 74/3 = 24；
        // 7*10/3 = 2*10
        // (74 - 3*2*10)/3 = 14/3
        // 1*10 / 3 = 0*10
        // 14/3 = 4;
        
        // 再来一个例子 740/23 = 32;
        // 7*100/23 = 3*10;
        // 740 - 23*3*10 = 50;
        // 50/23 = 2*1;

// 我们把 dividend 和 divisor 全部用 二进制表示 做除法实际上是一样的步骤
// 只是二进制情况少一点，每一次我们得到的不是 0 就是 1x10...0;
// 10101010/100001
//

if( dividend == Integer.MIN_VALUE && divisor == -1){
           return Integer.MAX_VALUE;
      }
       boolean isNegative = ( dividend > 0 ) ^ ( divisor > 0);
       dividend = Math.abs(dividend);
       divisor = Math.abs(divisor);
       int quotient = 0;
       for(int i = 31; i >= 0; i--){
           if( (dividend >>> i) - divisor >= 0 ){
               quotient += 1 << i;
               dividend -= divisor << i;
          }
      }

return isNegative ? -quotient : quotient;
  }
}
​

```

```
