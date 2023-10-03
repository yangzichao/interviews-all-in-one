# 400 Nth-Digit 
 
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
<div><p>Find the <i>n</i><sup>th</sup> digit of the infinite integer sequence 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, ... </p>
<p><b>Note:</b><br>
<i>n</i> is positive and will fit within the range of a 32-bit signed integer (<i>n</i> &lt; 2<sup>31</sup>).
</p>
<p><b>Example 1:</b>
</p><pre><b>Input:</b>
3
<b>Output:</b>
3
</pre>
<p></p>
<p><b>Example 2:</b>
</p><pre><b>Input:</b>
11
<b>Output:</b>
0
<b>Explanation:</b>
The 11th digit of the sequence 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, ... is a 0, which is part of the number 10.
</pre>
<p></p></div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public int findNthDigit(int n) {
        long totalDigits = 9;
        long count = 1; 
        int digit = 1;
        // 先找到所在的数字有多少位，所有的1位数共占据9位，2位数共占据180位，以此类推
        while(n > totalDigits){
            n -= totalDigits;
            digit += 1;
            count *= 10; // count 和 totalDigits 用 long 是因为这俩是可能overflow的
            totalDigits = 9*count*digit;
        }
        // 此时 n 是剩下的总位数，而且我们也知道该数字是多少位一个的， 因此比较容易推算是哪个数字了
        // int number = n/digit + ( n%digit == 0 ? 0 : 1) + count - 1;
        long number = (n - 1)/digit + count;
        return (int) (String.valueOf(number).charAt( (n - 1) %digit ) -'0');
    }
}
​
​
​
```