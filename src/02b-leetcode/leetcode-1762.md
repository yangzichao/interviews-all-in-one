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

实在简单 不值一提

```java
class Solution {
    public int[] findBuildings(int[] heights) {
        ArrayList<Integer> oceanViews = new ArrayList<>();
        int curMax = -1;
        for (int i = heights.length - 1; i >= 0; i--) {
            if (heights[i] > curMax) {
                oceanViews.add(i);
            }
            curMax = Math.max(curMax, heights[i]);
        }
        int N = oceanViews.size();
        int[] ans = new int[N];
        for (int i = 0; i < N; i++) {
            ans[i] = oceanViews.get(N - 1 - i);
        }
        return ans;
    }
}

```


主要是这个题想让单调栈，但是设计的不好。

```java
class Solution {
    public int[] findBuildings(int[] heights) {
        Deque<int[]> monotonicStack = new ArrayDeque<>();

        for (int i = 0; i < heights.length; i++) {
            while (!monotonicStack.isEmpty() && monotonicStack.peekLast()[0] <= heights[i]) {
                    monotonicStack.removeLast();
            }
            monotonicStack.addLast(new int[]{heights[i], i});
        }
        int[] ans = new int[monotonicStack.size()];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = monotonicStack.removeFirst()[1];
        }
        return ans;
    }
}
```