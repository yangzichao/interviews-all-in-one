

这个题很不好写对 乍一看好像可以写到一个扫描里，但是其实不好处理，以下已经是不错的写法了
```java
class Solution {
    public double findMaxAverage(int[] nums, int k) {
        double sum = 0;
        for (int i = 0; i < k; i++) {
            sum += nums[i];
        }
        double maxSum = sum;
        for (int i = k; i < nums.length; i++) {
            sum += nums[i] - nums[i - k];
            maxSum = Math.max(maxSum, sum);
        }
        return maxSum / k;
    }
}
```


chatgpt 写的 挺好的：

```java
class Solution {
    public double findMaxAverage(int[] nums, int k) {
        double sum = 0.0;
        double maxSum = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < nums.length; i++) {
            sum += nums[i];

            // Once we've hit the k-th element, set or update maxSum
            if (i == k - 1) {
                maxSum = sum;
            } else if (i >= k) {
                // Slide the window: remove the element that's no longer in the window
                sum -= nums[i - k];
                maxSum = Math.max(maxSum, sum);
            }
        }
        return maxSum / k;
    }
}
```