

## 2055
2055 这个题就是关于优化，直球的话很容易，但是也必然TLE了。

```java
class Solution {
    public int[] platesBetweenCandles(String s, int[][] queries) {
        int[] ans = new int[queries.length];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = getQueryResult(s, queries[i]);
        }
        return ans;
    }

    public int getQueryResult(String s, int[] query) {
        int plateCount = 0;
        int leftPlate = -1;
        int rightPlate = -1;
        for (int i = query[0]; i <= query[1]; i++) {
            if (s.charAt(i) == '*') continue;
            if (leftPlate == -1) {
                leftPlate = i;
            } else {
                rightPlate = i;
            }
            plateCount++;
        }
        if (plateCount < 2) return 0; 
        return rightPlate - leftPlate + 1 - plateCount;
    }
}
```


## 优化解答 1: Binary Search + prefix Sum

上面的题当中，我们已经注意到了最关键的技巧。
即 计算 candle 数量只需要找到 最左plate和最右plate的坐标，然后知道这两个之间的总plate数量即可。
那么我们都有办法解决这个问题，最左plate 最右plate使用 binary search 即可。
而总plate数量可以用 prefixSum 轻松得知。
prefixSum 需要一个 O(N) 的时间复杂度。
而每一次 query 只需要 O(log N) 的时间复杂度。
总的复杂度是 O(N) + k O(log N);

具体到 BinarySearch，也不好执行，我们得自建一个只有 candle 坐标的 list 来 binary search.
