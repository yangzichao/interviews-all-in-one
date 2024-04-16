使用 prefix array 是一个非常常见的技巧

我们先来分析一下这个技巧, 以053的Kadane's algo为例。 
使用prefix array 的好处是可以O(1)时间计算一个subarray的信息（比如求和 求积）。   
我们往往有两个选择建立 dp 的数组:
* 原数组长度相同
* 原数组长度 + 1

如果使用原数组长度相同，那么：
```java
int[] prefix = new int[nums.length];
prefix[i] = prefix[i - 1] + nums[i];
```
prefix[i] 储存的是 包含了 nums[i] 之前的所有和
但是如果想要计算nums在 i j 之间的和 需要取prefix的 i - 1, j. 
注意到 这里经常需要处理 i - 1, 对于java来说容易 array index error. 

所以我们往往采取 ** 原数组长度 + 1 **  的技巧
```java
int[] prefix = new int[nums.length + 1];
prefix[i] = prefix[i - 1] + nums[i];
```
此时，prefix[i + 1] 储存的是 包含了 nums[i] 之前的所有和. 
但是如果想要计算 nums 在 i j 之间的和 需要取prefix的 i, j + 1.
其计算过程是 (i - 1 + 1), (j + 1) 即所有的index shift一位。
这样我们不必处理特殊的 i - 1 的 edge case. 

以上并不是什么很高级的分析，但是是为了抢时间我们才这么写。


```java
class Solution {
    public int maxSubArray(int[] nums) {
        int max = Integer.MIN_VALUE;
        int[] prefixSum = new int[nums.length + 1];
        for (int i = 0; i < nums.length; i++) {
            prefixSum[i + 1] = prefixSum[i] + nums[i];
        }
        int left = 0;
        for (int right = 1; right < prefixSum.length; right++) {
            int curSum = prefixSum[right] - prefixSum[left];
            max = Math.max(curSum, max);
            if (curSum < 0) {
                left = right;
            }
        }
        return max;
    }
}

```