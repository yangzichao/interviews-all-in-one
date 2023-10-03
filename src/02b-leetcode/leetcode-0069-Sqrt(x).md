# 069 Sqrt(x)

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
<div><p>Given a non-negative integer <code>x</code>,&nbsp;compute and return <em>the square root of</em> <code>x</code>.</p>
<p>Since the return type&nbsp;is an integer, the decimal digits are <strong>truncated</strong>, and only <strong>the integer part</strong> of the result&nbsp;is returned.</p>
<p><strong>Note:&nbsp;</strong>You are not allowed to use any built-in exponent function or operator, such as <code>pow(x, 0.5)</code> or&nbsp;<code>x ** 0.5</code>.</p>
<p>&nbsp;</p>
<p><strong>Example 1:</strong></p>
<pre><strong>Input:</strong> x = 4
<strong>Output:</strong> 2
</pre>
<p><strong>Example 2:</strong></p>
<pre><strong>Input:</strong> x = 8
<strong>Output:</strong> 2
<strong>Explanation:</strong> The square root of 8 is 2.82842..., and since the decimal part is truncated, 2 is returned.</pre>
<p>&nbsp;</p>
<p><strong>Constraints:</strong></p>
<ul>
	<li><code>0 &lt;= x &lt;= 2<sup>31</sup> - 1</code></li>
</ul>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public int mySqrt(int x) {
        if( x < 2){
            return x;
        }
        int left = 0;
        int right = x;
        while( left <= right ){
            int pivot = left + (right - left)/2;
            long power = (long) pivot * pivot;
            if( power == x){
                return pivot;
            }else if ( power < x){
                left = pivot + 1;
            }else{
                right = pivot - 1;
            }
        }
        return left - 1;
    }
}
​
```

这个方法也可以用来求更精确的数值

```java
public class Main {
    public static double mySqrt(int x, double precision) {
        if( x < 2){
            return x;
        }
        double left = 0;
        double right = x;
        while( left <= right ){
            double pivot = left + (right - left)/2;
            double power = (double) pivot * pivot;
            if( power == x){
                return pivot;
            }else if ( power < x){
                left = pivot + precision;
            }else{
                right = pivot - precision;
            }
        }
        return left - precision;
    }
    public static void main(String[] args) {
        System.out.println(mySqrt(2,0.0001)); // output 1.41410390625
    }
}
```
