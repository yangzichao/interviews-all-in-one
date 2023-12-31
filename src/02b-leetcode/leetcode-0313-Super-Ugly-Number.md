# 313 Super-Ugly-Number 
 
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
<div><p>Write a program to find the <code>n<sup>th</sup></code> super ugly number.</p>
<p>Super ugly numbers are positive numbers whose all prime factors are in the given prime list <code>primes</code> of size <code>k</code>.</p>
<p><b>Example:</b></p>
<pre><b>Input:</b> n = 12, <code>primes</code> = <code>[2,7,13,19]</code>
<b>Output:</b> 32 
<strong>Explanation: </strong><code>[1,2,4,7,8,13,14,16,19,26,28,32] </code>is the sequence of the first 12 
             super ugly numbers given <code>primes</code> = <code>[2,7,13,19]</code> of size 4.</pre>
<p><b>Note:</b></p>
<ul>
	<li><code>1</code> is a super ugly number for any given <code>primes</code>.</li>
	<li>The given numbers in <code>primes</code> are in ascending order.</li>
	<li>0 &lt; <code>k</code> ≤ 100, 0 &lt; <code>n</code> ≤ 10<sup>6</sup>, 0 &lt; <code>primes[i]</code> &lt; 1000.</li>
	<li>The n<sup>th</sup> super ugly number is guaranteed to fit in a 32-bit signed integer.</li>
</ul>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public int nthSuperUglyNumber(int n, int[] primes) {
        if( n < 2 ){
            return n;
        } 
        int[] pointers = new int[primes.length];
        int[] uglyNumbers = new int[n];
        uglyNumbers[0] = 1;
        for(int i = 1; i < n; i++){
            int min = Integer.MAX_VALUE;
            for(int j = 0; j < primes.length; j++ ){
                min = Math.min( min, primes[j]*uglyNumbers[ pointers[j] ] );
            }
            uglyNumbers[i] = min;
            for(int j = 0; j < primes.length; j++ ){
                if( min == primes[j]*uglyNumbers[ pointers[j] ] ){
                    pointers[j]++;
                }
            }
        }
        return uglyNumbers[ n - 1];
    }
}
​
```