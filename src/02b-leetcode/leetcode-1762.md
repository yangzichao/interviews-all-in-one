我觉得有点简单的


```java
class Solution {
    public int[] findBuildings(int[] heights) {
        int[] maxHeightsR = new int[heights.length];
        int curMax = -1;
        for (int i = heights.length - 2; i >= 0; i--) {
            curMax = Math.max(curMax, heights[i + 1]);
            maxHeightsR[i] = curMax;
        }
        ArrayList<Integer> oceanViews = new ArrayList<>();
        for (int i = 0; i < heights.length; i++) {
            if (heights[i] > maxHeightsR[i]) {
                oceanViews.add(i);
            }
        }
        int[] ans = new int[oceanViews.size()];
        for (int i = 0; i < oceanViews.size(); i++) {
            ans[i] = oceanViews.get(i);
        }
        return ans;
    }
}
```