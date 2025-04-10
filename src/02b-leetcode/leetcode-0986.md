
这个代码质量一般 但是竟然是一遍过。

```java
class Solution {
    public int[][] intervalIntersection(int[][] firstList, int[][] secondList) {
        // two pointed pointing both list 
        // when comparing 2 segments, easy to calculate intersection
        // we move the pointer which points to the segment that has smaller end value. 

        List<int[]> intersections = new ArrayList<>();
        int i = 0, j = 0;
        while (i < firstList.length && j < secondList.length) {
            int[] firstSec = firstList[i];
            int[] secondSec = secondList[j];
            // intersection is calculated by take max start and min end, then compare.
            int intersecStart = Math.max(firstSec[0], secondSec[0]);
            int intersecEnd = Math.min(firstSec[1], secondSec[1]);
            if (intersecStart <= intersecEnd) {
                intersections.add(new int[]{intersecStart, intersecEnd});
            }
            if (firstSec[1] < secondSec[1]) {
                i++;
            } else if (firstSec[1] > secondSec[1]) {
                j++;
            } else {
                i++;
                j++;
            }
        }
        int[][] ans = new int[intersections.size()][2];
        for (int k = 0; k < ans.length; k++) {
            ans[k] = intersections.get(k);
        }
        return ans;
    }
}
```