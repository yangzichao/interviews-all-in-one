# 209 Minimum-Size-Subarray-Sum

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
<div><p>Given an array of <strong>n</strong> positive integers and a positive integer <strong>s</strong>, find the minimal length of a <b>contiguous</b> subarray of which the sum ≥ <strong>s</strong>. If there isn't one, return 0 instead.</p>
<p><strong>Example:&nbsp;</strong></p>
<pre><strong>Input:</strong> <code>s = 7, nums = [2,3,1,2,4,3]</code>
<strong>Output:</strong> 2
<strong>Explanation: </strong>the subarray <code>[4,3]</code> has the minimal length under the problem constraint.</pre>
<div class="spoilers"><b>Follow up:</b></div>
<div class="spoilers">If you have figured out the <i>O</i>(<i>n</i>) solution, try coding another solution of which the time complexity is <i>O</i>(<i>n</i> log <i>n</i>).&nbsp;</div>
</div></section>


注意，这个题目的一个重要假设是，所有的元素都是正数，因此 prefixSumArray 是单调递增的。

## Method One

```Java
class Solution {
    public int minSubArrayLen(int s, int[] nums) {
        int minimum = Integer.MAX_VALUE;
        int sum = 0;
        int left = 0;
​
        for(int i = 0; i < nums.length; i++ ) {
            int right = i;
            sum += nums[i];
            while(sum >= s) {
                minimum = Math.min( minimum, (right - left + 1) );
                sum -= nums[left];
                left += 1;
            }
        }
        
        return minimum == Integer.MAX_VALUE  ?  0 : minimum;
    }
}
​
```

第二次写，先写的是这样的

```java
class Solution {
    public int minSubArrayLen(int s, int[] nums) {
        int minSize = Integer.MAX_VALUE;
        int sum = 0;
        int left = 0;
        for(int right = 0; right < nums.length; right++) {
            if(sum < s) {
                sum += nums[right];
            }
            while(sum >= s) {
                minSize = Math.min(minSize, right - left + 1);
                sum -= nums[left];
                left +=1;
            }
        }

        return minSize == Integer.MAX_VALUE ? 0 : minSize;
    }
}
```

后面发现可以把 if(sum < s ) 拿掉，因为 while 保证了下一次一定 sum < s

## 为什么滑窗在这里是对的（几句话直觉）

1. **元素全正这个条件是命门**：right 往右走，sum 只会变大；left 往右走，sum 只会变小。两个方向都是单调的，不会"忽大忽小"。

2. **对每个 right，最优答案是"还能让 sum ≥ s 的最靠右的 left"**：因为 left 越靠右，窗口越短。而全局最优解，必然是某个 right 配上它对应的这个"最靠右的 left"——再多缩一格就不够 s 了。

3. **关键结论：这个"最靠右的 left"随 right 单调右移，永远不用回头**。因为 right 前进时 sum 又涨了一截，原来合法的 left 现在更合法，最优 left 只会更靠右、不会更靠左。所以左右指针都只前进、不回退，每个元素最多进出窗口一次，O(n) 就够了，而且不会错过任何最优窗口。

一句话浓缩：**因为元素全正，"扩 right、缩 left" 的方向是单向的，最优左端点随 right 单调右移，所以双指针不回头也不会错过最优解。**
