# 371 Sum-of-Two-Integers 
 
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
<div><p>Calculate the sum of two integers <i>a</i> and <i>b</i>, but you are <b>not allowed</b> to use the operator <code>+</code> and <code>-</code>.</p>
<div>
<p><strong>Example 1:</strong></p>
<pre><strong>Input: </strong>a = <span id="example-input-1-1">1</span>, b = <span id="example-input-1-2">2</span>
<strong>Output: </strong><span id="example-output-1">3</span>
</pre>
<div>
<p><strong>Example 2:</strong></p>
<pre><strong>Input: </strong>a = -<span id="example-input-2-1">2</span>, b = <span id="example-input-2-2">3</span>
<strong>Output: </strong>1
</pre>
</div>
</div>
</div></section>
 
 ## Method One 
 
 参见 位操作的笔记部分。
``` Java
class Solution {
    public int getSum(int a, int b) {
        while(b != 0){
            int carry = (a&b) << 1;
            a ^= b;
            b = carry;
        }
        return a;
    }
}
​
```