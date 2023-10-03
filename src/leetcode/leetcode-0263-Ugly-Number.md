# 263 Ugly-Number 
 
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
<div><p>Write a program to check whether a given number is an ugly number.</p>
<p>Ugly numbers are <strong>positive numbers</strong> whose prime factors only include <code>2, 3, 5</code>.</p>
<p><strong>Example 1:</strong></p>
<pre><strong>Input:</strong> 6
<strong>Output:</strong> true
<strong>Explanation: </strong>6 = 2 ×&nbsp;3</pre>
<p><strong>Example 2:</strong></p>
<pre><strong>Input:</strong> 8
<strong>Output:</strong> true
<strong>Explanation: </strong>8 = 2 × 2 ×&nbsp;2
</pre>
<p><strong>Example 3:</strong></p>
<pre><strong>Input:</strong> 14
<strong>Output:</strong> false 
<strong>Explanation: </strong><code>14</code> is not ugly since it includes another prime factor <code>7</code>.
</pre>
<p><strong>Note:</strong></p>
<ol>
	<li><code>1</code> is typically treated as an ugly number.</li>
	<li>Input is within the 32-bit signed integer range:&nbsp;[−2<sup>31</sup>,&nbsp; 2<sup>31&nbsp;</sup>− 1].</li>
</ol></div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public boolean isUgly(int num) {
        if( num == 0){
            return false;
        }
        int n = num;
        while( n != 1 ){
            if( n %2 == 0){
                n /=2;
            }else if( n % 3 == 0){
                n  /=3;
            }else if( n % 5 == 0){
                n /=5;
            }else{
                return false;
            }
        }
        return true;
    }
}
​
```