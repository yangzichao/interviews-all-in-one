# 031 Next-Permutation

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
<div><p>Implement <strong>next permutation</strong>, which rearranges numbers into the lexicographically next greater permutation of numbers.</p>
<p>If such arrangement is not possible, it must rearrange it as the lowest possible order (ie, sorted in ascending order).</p>
<p>The replacement must be <strong><a href="http://en.wikipedia.org/wiki/In-place_algorithm" target="_blank">in-place</a></strong> and use only constant&nbsp;extra memory.</p>
<p>Here are some examples. Inputs are in the left-hand column and its corresponding outputs are in the right-hand column.</p>
<p><code>1,2,3</code> → <code>1,3,2</code><br>
<code>3,2,1</code> → <code>1,2,3</code><br>
<code>1,1,5</code> → <code>1,5,1</code></p>
</div></section>
 
 ## Method One 
 参考 https://leetcode.wang/leetCode-31-Next-Permutation.html

这个题重在分析题意, next permutation 是按照 lexicographically
字典顺序的下一个。
其实很难讲清楚。随便写点吧，不行就去参考解答。
54321 是 lexicographically 到头了，所以直接翻转。
如果 .....46521 是这么一个结尾，6521 是完全倒序的，是最大的字典顺序，
如果 4 这里是 大于等于 6 的，我们仍然是最大字典序，没什么好说的，但是这里
突然出现了一个 4，也就是说一个山顶出现了，那么我们是可以做 next permutation 的。
相当于我们要进位。那么我们找 6521 里面 smallest element that is greater than 4.
拿它和 4 交换 得到.....56421. 由于是 next permutation, 交换之后，由于 6421
仍然有序并且是最大的字典序，所以我们要翻转 6421.

```Java
class Solution {
    private int[] nums;
    public void nextPermutation(int[] nums) {
        this.nums = nums;
        int right = nums.length - 2;
        while( right >= 0 && nums[right] >= nums[right + 1]) {
            right--;
        }
        if( right < 0 ) {
            reverse(right + 1);
            return;
        }
        int next = nums.length - 1;
        while( nums[next] <= nums[right] ) {
            next--;
        }
        swap(right, next);
        reverse(right + 1);
    }
    public void reverse(int start ) {
        int i = start;
        int j = nums.length - 1;
        while( i < j) {
            swap(i++, j--);
        }
    }
    public void swap( int i , int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }
}

​
```


这里放一个字典序的例子，并贡献一个2023年写的答案。


```java
class Solution {
    public void nextPermutation(int[] nums) {
        // 1 2 3 4
        // 1 2 4 3
        // 1 3 2 4
        // 1 3 4 2 
        // 1 4 2 3
        // 1 4 3 2
        // 2 1 3 4
        // ...
        // 4 3 2 1
        int right = nums.length - 1;
        while (right >= 1 && nums[right] <= nums[right - 1]) {
            right--;
        }
        if (right == 0) {
            reverse(nums, 0, nums.length - 1);
            return;
        }
        int smallestLargerElement = nums.length - 1;
        while (nums[right - 1] >= nums[smallestLargerElement]) {
            smallestLargerElement--;
        }
        swap(nums, right - 1, smallestLargerElement);
        reverse(nums, right, nums.length - 1);
    }

    private void reverse(int[] nums, int start, int end) {
        if (start >= end) return;
        int left = start;
        int right = end;
        while (left <= right) {
            swap(nums, left, right);
            left++;
            right--;
        }
    }

    private void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }
}
```


## 2025

2025年尝试做一个解释啊，比幼年期可能更不好入门。

设想，一个数组如果完全是倒序排列，即一个“斜坡”，例如54321
这样已经是最大字典序了，那么下一个就是最小的字典序，需要完全反转它，即 12345.
而字典序的目的，就是把整个数组从一个完全的“上坡”变成完全的“下坡”。
基于这个思想，我们不管一个数组前面的情况，只考虑它结尾的“斜坡”部分。