# 075J. Sort Colors

https://leetcode.com/problems/sort-colors/

这个题是三切分快排的核心算法。非常重要。
核心是，随便找一个数用来切分数组，把比他小的都挪到他左边，比他大的都挪到他右边。
这里我们知道要找 1，让 0 都到 1 左侧，让 2 到 1 右侧。所以我们直接用 1 来切分。

## Method Best:

不明白的话这个题可以参考 QuickSort3Way。
以下就是三切分快排的核心算法。由于只有三个不重复的元素，所以不需要切分子数组。
唯一可以提升的是，可以用位操作来 swap.

但是位操作的快，但是如果遇到 nums 里 a 和 b 是一样的情况，那就翻车了。
a = a^b;
b = a^b;
a = a^b;

```java
class Solution {
    public void sortColors(int[] nums) {
        //这个题相关的有 三向快排 remove duplicates.
        int N = nums.length;
        int loT = 0;
        int hiT = N - 1;
        int i = 0;
        int v = 1;
        while( i <= hiT){
            if(nums[i] < v){
                int temp = nums[i];
                nums[i]   = nums[loT];
                nums[loT] = temp;
                i++;
                loT++;
            }else if(nums[i] > v){
                int temp = nums[i];
                nums[i] = nums[hiT];
                nums[hiT] = temp;
                hiT--;
            }else{
                i++;
            }
        }
        return;
    }
}
```

## 2023
这是2023年写的，可读性确实好一点。


```java
class Solution {
    public void sortColors(int[] nums) {
        int WHITE = 1;
        int lo = 0;
        int cur = 0;
        int hi = nums.length - 1;
        while (cur <= hi) {
            if (nums[cur] == WHITE) {
                cur++;
            } else if (nums[cur] < WHITE) {
                // 这里面需要唯一想明白的是，为什么这里需要另一个 cur++;
                // 因为我们的操作保证了 lo 总是处在一串 0 之前，并且 cur 之前不存在 大于 white 的值
                // 因此 cur 一定也要++ 
                // 你可以把 < white 看做是 <= white 的一种特殊情况，<= 的情况下，需要 cur ++
                // 只是在 < 的情况下，还需要 swap 和 lo++
                swap(nums, lo, cur);
                cur++;
                lo++;
            } else {
                swap(nums, cur, hi);
                hi--;
            }
        }
    }

    private void swap(int[] nums, int i, int j) {
        if (i == j) return;
        nums[i] ^= nums[j];
        nums[j] ^= nums[i];
        nums[i] ^= nums[j];
    }
}
```

