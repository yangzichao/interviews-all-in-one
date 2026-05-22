
这个题先写个暴力法挂这里

思路是 计算每个房子距离最近的一个heater的距离。
然后上述那个值的全局最大值就是最小的 minRadius. 

```java
class Solution {
    public int findRadius(int[] houses, int[] heaters) {
        int minRadius = 0;
        for (int house : houses) {
            int minDis = Integer.MAX_VALUE;
            for (int heater : heaters) {
                int dis = Math.abs(heater - house);
                minDis = Math.min(minDis, dis);
            }
            minRadius = Math.max(minRadius, minDis);
        }
        return minRadius;
    }
}
```

可以想像，如果 heaters 是排序好的，那么第二个 for loop 替换为 binary search 就很快了。


```java
class Solution {
    public int findRadius(int[] houses, int[] heaters) {
        Arrays.sort(heaters);
        int minR = 0;
        for (int house : houses) {
            int minDis = Integer.MAX_VALUE;
            int index = Arrays.binarySearch(heaters, house);
            if (index < 0) {
                index = - （index + 1）; // 等价于 index = ~index; 位运算 极快。
                int dist1 = index - 1 >= 0 ? house - heaters[index - 1] : Integer.MAX_VALUE;
                int dist2 = index < heaters.length ? heaters[index] - house : Integer.MAX_VALUE;
                
                minR = Math.max(minR, Math.min(dist1, dist2));
            }
        }
        return minR;
    }
}
```