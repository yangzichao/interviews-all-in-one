# 264 Ugly-Number-II 
 
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
<div><p>Write a program to find the <code>n</code>-th ugly number.</p>
<p>Ugly numbers are<strong> positive numbers</strong> whose prime factors only include <code>2, 3, 5</code>.&nbsp;</p>
<p><strong>Example:</strong></p>
<pre><strong>Input:</strong> n = 10
<strong>Output:</strong> 12
<strong>Explanation: </strong><code>1, 2, 3, 4, 5, 6, 8, 9, 10, 12</code> is the sequence of the first <code>10</code> ugly numbers.</pre>
<p><strong>Note: </strong>&nbsp;</p>
<ol>
	<li><code>1</code> is typically treated as an ugly number.</li>
	<li><code>n</code> <b>does not exceed 1690</b>.</li>
</ol></div></section>
 
 ## Method One 
 
``` Java

    public int nthUglyNumber(int n) {
        if( n < 2){
            return n;
        }
        // 核心是 一个丑数肯定是一个更小的丑数乘以2 3 5 得到，或者说是乘以另一个丑数得到
        // 我们考虑下面的组数，分别是第 i 个丑数乘以 2 3 5 得到。
        // 比如我们已经知道前10个丑数是 1 2 3 4 5 6 8 9 10 12
        // 1 1*2 2*2 3*2 4*2 5*2 6*2 8*2
        // 1 1*3 2*3 3*3 4*4 5*3 6*3 8*3
        // 1 1*5 2*5 3*5 4*5 5*5 6*5 8*5
​
        // 问题是我们如何知道新的丑数是什么呢？
        // 我们知道第一个丑数是1，因此新的 candidates 是 2 3 5 ，
        // 我们发现当下最小的丑数是 2， 因此我们第二列是 4 6 10
        // 那么下一个丑数是什么呢 是从 3 5 4 6 10 里面选
        // 由于 3 和 6， 5 和 10 分别是在同一个数组里面，因此我们不用考虑 6 和 10
        // 显然 3， 4， 5 当中 3 是下一个丑数。
        // 因此我们发现，我们应当维护三个指针，分别指向丑数数组当前的位置。
        // 如果某个指针所指的数被选为了下一个丑数，那么这个指针往后移一位。
        
        int pA = 0;
        int pB = 0;
        int pC = 0;
        int[] uglyNumbers = new int[n];
        
        uglyNumbers[0] = 1;
        
        for(int i = 1; i < n; i++){
            int nA = uglyNumbers[pA]*A;
            int nB = uglyNumbers[pB]*B;
            int nC = uglyNumbers[pC]*C;
            int min = Math.min( Math.min(nA, nB), nC);
            uglyNumbers[i] = min;
            // 这里用 if if if 是为了排除 nA == nB == 6 这种有重复的情况。
            if( nA == min ){
                pA++;
            }
            if( nB == min ){
                pB++;
            }
            if( nC == min ){
                pC++;
            }
        }
        return uglyNumbers[n - 1];
        
        // // 除此之外用 heap 也是一个最直接的方法
        // // 注意要防止 integer overflow 和 重复的元素
        // PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        // minHeap.offer(1);
        // int ugly = 1;
        // while( n > 0){
        //     n--;
        //     ugly = minHeap.poll();
        //     while( !minHeap.isEmpty() && ugly == minHeap.peek()){
        //         minHeap.poll();
        //     }
        //     if( ugly < Integer.MAX_VALUE/A){
        //         minHeap.offer(ugly * A);
        //     }
        //     if( ugly < Integer.MAX_VALUE/B){
        //         minHeap.offer(ugly * B);
        //     }
        //     if( ugly < Integer.MAX_VALUE/C){
        //         minHeap.offer(ugly * C);
        //     }
        // }
        // return ugly;
    }
}
​
```