280 这个题看似很傻逼，其实我觉得是一个很好的基础题。

这两个方法都有必要比较熟悉：

第一种就是先排序，再交换相邻的两个。
```java
class Solution {
    public void wiggleSort(int[] nums) {
        Arrays.sort(nums);
        for (int i = 1; i < nums.length - 1; i += 2) {
            swap(nums, i, i + 1);
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

另一种方法不需要排序。看起来非常简单，实则需要想一想。


```java
class Solution {
// 对于 a > b  < c 的情况, 我们需要 swap a, b
// b < a ? c, 然后判断 a 和 c，假如 a < c
// 那么需要变成 b < c > a, 由于 b < c 就是我们的初始条件，所以可以如下这么写而不加额外的判断。

    public void wiggleSort(int[] nums) {
        for (int i = 0; i < nums.length - 1; i++) {
            if (i % 2 == 0 && nums[i] > nums[i + 1]) {
                swap(nums, i, i + 1);
            }
            if (i % 2 == 1 && nums[i] < nums[i + 1]) {
                swap(nums, i, i + 1);
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

还可以参考一下变种：1968题。