这个题比较有点意思。
首先比较好想的就是，我们用斜率就可以归类所有的线了。
比较意外的就是 atan2 这个函数的使用，它其实就是 atan(y/x) 但是规避了 x = 0 的隐患。
所以如果你不调用它就比较不好写。

所以出发点就是，以一个点为原点，得到所有可能的线的斜率，归类，然后count.
对所有的点都重复上述的步骤，即可。


```java
class Solution {
    public int maxPoints(int[][] points) {
        if (points.length < 2) return points.length;
        int max = 1;
        for (int i = 0; i < points.length; i++) {
            Map<Double,Integer> counts = new HashMap<>();
            for (int j = 0; j < points.length; j++) {
                if (i == j) continue;
                double atan2 = Math.atan2(points[i][1] - points[j][1], points[i][0] - points[j][0]);
                int count = counts.getOrDefault(atan2, 0) + 1;
                counts.put(atan2, count);
                max = Math.max(max, count);
            }
        }
        return max + 1;
    }
}

```