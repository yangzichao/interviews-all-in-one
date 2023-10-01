这个题还蛮好的

```java
class Solution {
    public int deleteAndEarn(int[] nums) {
        // 这个题目是很有迷惑性的。
        // 它的表述中 你选了 nums[i] 就得把 nums[i] - 1 和 nums[i] + 1 全部从数组中删掉
        // 其实等价于 你不能同时选取数值上相邻的两个数
        // 另外还有一个观察就是 你选了某个数，就等同于把数组中全部的相同的数都提取了
        // 那么每个数的价值就是 这个数的值乘以它出现的次数，
        // 这样，我们设想把数组中的独特的元素一个一个，有序的加入进来，这样这个问题就变成了 house robber

        int[] value = new int[10001];
        for (int n : nums) {
            value[n] += n;
        }
        int prevTake = 0;
        int prevSkip = 0;
        for (int i = 1; i < 10001; i++) {
            int take = prevSkip + value[i];
            int skip = Math.max(prevTake, prevSkip);
            prevTake = take;
            prevSkip = skip;
        }
        return Math.max(prevTake, prevSkip);
    }
}
```