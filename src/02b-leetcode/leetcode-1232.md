
这个题又是 atan2 的运用，但是似乎不这么写更好。

```java
class Solution {
    public boolean checkStraightLine(int[][] coordinates) { 
        if (coordinates.length < 2) return true;
        int[] p1 = coordinates[0];
        int[] p2 = coordinates[1];
        double atan = Math.atan2(p1[0] - p2[0], p1[1] - p2[1]);
        for (int i = 2; i < coordinates.length; i++) {
            int[] curP = coordinates[i];
            double atan1 = Math.atan2(p1[0] - curP[0], p1[1] - curP[1]);
            double atan2 = Math.atan2(curP[0] - p1[0], curP[1] - p1[1]);
            // 注意，我们这样就避免了斜率是相反数的情况了。
            System.out.println(atan);
            if (atan1 != atan && atan2 != atan) return false;
        }
        return true;
    }
}
```

推荐的写法可以是这个
具体可以想想啊，挺不错的。
```java
class Solution {
    // Returns the delta Y.
    int getYDiff(int[] a, int[] b) {
        return a[1] - b[1];
    }
    
    // Returns the delta X.
    int getXDiff(int[] a, int[] b) {
        return a[0] - b[0];
    }
    
    public boolean checkStraightLine(int[][] coordinates) {
        int deltaY = getYDiff(coordinates[1], coordinates[0]);
        int deltaX = getXDiff(coordinates[1], coordinates[0]);
        
        for (int i = 2; i < coordinates.length; i++) {
            // Check if the slope between points 0 and i, is the same as between 0 and 1.
            if (deltaY * getXDiff(coordinates[i], coordinates[0])
                != deltaX * getYDiff(coordinates[i], coordinates[0])) {
                return false;
            }
        }
        return true;
    }
}
```