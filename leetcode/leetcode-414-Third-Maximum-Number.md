# 414 Third-Maximum-Number 
 
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
<div><p>Given a <b>non-empty</b> array of integers, return the <b>third</b> maximum number in this array. If it does not exist, return the maximum number. The time complexity must be in O(n).</p>
<p><b>Example 1:</b><br>
</p><pre><b>Input:</b> [3, 2, 1]
<b>Output:</b> 1
<b>Explanation:</b> The third maximum is 1.
</pre>
<p></p>
<p><b>Example 2:</b><br>
</p><pre><b>Input:</b> [1, 2]
<b>Output:</b> 2
<b>Explanation:</b> The third maximum does not exist, so the maximum (2) is returned instead.
</pre>
<p></p>
<p><b>Example 3:</b><br>
</p><pre><b>Input:</b> [2, 2, 3, 1]
<b>Output:</b> 1
<b>Explanation:</b> Note that the third maximum here means the third maximum distinct number.
Both numbers with value 2 are both considered as second maximum.
</pre>
<p></p></div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public int thirdMax(int[] nums) {
        Set<Integer> set = new HashSet<>();
        
        int first = Integer.MIN_VALUE;
        int second = Integer.MIN_VALUE;
        int third = Integer.MIN_VALUE;
        for(int n : nums){
            if( set.contains(n)){
                continue;
            }
            if( n > first){
                third = second;
                second = first;
                first = n;
            }else if ( n > second ){
                third = second;
                second = n;
            }else if ( n > third ){
                third = n;
            }
            set.add(n);
        }
        return set.size() >= 3 ? third : first;
    }
}
​
```