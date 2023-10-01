# 905 Sort-Array-By-Parity 
 
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
<div><p>Given an array <code>A</code> of non-negative integers, return an array consisting of all the even elements of <code>A</code>, followed by all the odd elements of <code>A</code>.</p>
<p>You may return any answer array that satisfies this condition.</p>
<p>&nbsp;</p>
<div>
<p><strong>Example 1:</strong></p>
<pre><strong>Input: </strong><span id="example-input-1-1">[3,1,2,4]</span>
<strong>Output: </strong><span id="example-output-1">[2,4,3,1]</span>
The outputs [4,2,3,1], [2,4,1,3], and [4,2,1,3] would also be accepted.
</pre>
<p>&nbsp;</p>
<p><strong>Note:</strong></p>
<ol>
	<li><code>1 &lt;= A.length &lt;= 5000</code></li>
	<li><code>0 &lt;= A[i] &lt;= 5000</code></li>
</ol>
</div>
</div></section>
 
 ## Method One 
 
 这是之前写的，其实已经很好了，但是还是不如最好的好。可以看出来，有一定重复的代码。
``` Java
class Solution {
    public int[] sortArrayByParity(int[] A) {
        int index = 0;
        for(int i = 0; i < A.length; i++){
            if( A[i]%2 == 0){
                swap(A, index++, i);
            }
        }
        return A;
    }
    public void swap(int[] arr, int i, int j){
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
​
```

改进之后的：  
其实核心都是快速排序的partition代码。
```java
class Solution {
    public int[] sortArrayByParity(int[] A) {
        int left = 0;
        int right = A.length - 1;
        while( left <= right ){
            if( A[right] % 2 == 0 ){
                swap(A, left++, right);
            }else{
                right--;
            }
        }
        return A;
    }
    public void swap(int[] arr, int i, int j){
        if( i == j){
            return;
        }
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
```