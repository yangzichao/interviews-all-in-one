

二分搜索的办法:

二分法的直觉很像875.

时间复杂度差不多是 N log(W), W 是区间的差值。
```java
class Solution {
    public double minmaxGasDist(int[] stations, int k) {
        double left = 0;
        double right = stations[stations.length - 1] - stations[0];
        while (left + 1e-6 < right) {
            double penalty = left + (right - left) / 2;
            int totalGapLargerThanPenalty = 0;
            for (int i = 0; i < stations.length - 1; i++) {
                double gap = stations[i + 1] - stations[i];
                totalGapLargerThanPenalty += Math.floor(gap/penalty);
            }
            if (totalGapLargerThanPenalty > k) {
                left = penalty + 1e-6;
            } else {
                right = penalty - 1e-6;
            }
        }
        return left;
    }
}
```

下面是用 heap 的办法，很符合直觉虽然 TLE。
即存入两个值，a[0] 是原区间， a[1] 是 原区间被分成多少份。
永远把最大的区间多分一份。当然了 确实慢一点。
N + k Log(N) 的时间复杂度。

```java
class Solution {
    public double minmaxGasDist(int[] stations, int k) {
        PriorityQueue<double[]> gaps = new PriorityQueue<>( (a, b) -> 
            a[0] / a[1] < b[0] / b[1] ? 1 : -1
        );
        for (int i = 0; i < stations.length - 1; i++) {
            gaps.offer(new double[]{stations[i + 1] - stations[i], 1});
        }
        while (k > 0) {
            double[] cur = gaps.poll();
            cur[1] += 1;
            gaps.offer(cur);
            k--;
        }
        double[] top = gaps.peek();
        return top[0] / top[1];
    }
}
```