# 089 Gray-Code 
 
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
<div><p>The gray code is a binary numeral system where two successive values differ in only one bit.</p>
<p>Given a non-negative integer <em>n</em> representing the total number of bits in the code, print the sequence of gray code. A gray code sequence must begin with 0.</p>
<p><strong>Example 1:</strong></p>
<pre><strong>Input:</strong>&nbsp;2
<strong>Output:</strong>&nbsp;<code>[0,1,3,2]</code>
<strong>Explanation:</strong>
00 - 0
01 - 1
11 - 3
10 - 2
For a given&nbsp;<em>n</em>, a gray code sequence may not be uniquely defined.
For example, [0,2,3,1] is also a valid gray code sequence.
00 - 0
10 - 2
11 - 3
01 - 1
</pre>
<p><strong>Example 2:</strong></p>
<pre><strong>Input:</strong>&nbsp;0
<strong>Output:</strong>&nbsp;<code>[0]
<strong>Explanation:</strong> We define the gray code sequence to begin with 0.
&nbsp;            A gray code sequence of <em>n</em> has size = 2<sup>n</sup>, which for <em>n</em> = 0 the size is 2<sup>0</sup> = 1.
&nbsp;            Therefore, for <em>n</em> = 0 the gray code sequence is [0].</code>
</pre>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public List<Integer> grayCode(int n) {
        // 这个题的第一个难点是理解题意
        // n 就是说有多少个二进制位 binary digits
        // 第二个难点就是如何实现这个每次只变一位的操作
        // 我们以 n = 3 为例，
        // n = 2 的时候的一个解是 0 1 3 2 
        // 即 00 01 11 10, 到 n = 3 的时候 二进制是 000 001 011 010, 即每个前面添了 0
        // 由于要确保只变化一个，我们可以从当前的最后一个数 010, 将它的第一位变成1 得到110,
        // 以此类推把 n = 2 的解逆序加 1 就能得到一组 新的数 110 111 101 100 并且保证符合题目的要求 以此类推
        
        List<Integer> ans = new ArrayList<>();
        ans.add(0);
​
        for(int i = 0; i < n; i++){
            int jMax = ans.size();
            for(int j = jMax - 1; j >= 0; j--){
                ans.add( ans.get(j) | (1 << i) );
            }
        }
        return ans;
    }
}
​
```