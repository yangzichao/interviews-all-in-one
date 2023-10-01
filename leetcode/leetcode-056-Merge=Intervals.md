# 056J. Merge Intervals
https://leetcode.com/problems/merge-intervals/

## Method Best: T O(Nlog(N)) S O(1)
其实很好想，只是没想到先排序就是最优的。
```Java
class Solution {
    public int[][] merge(int[][] intervals) {
      //排除特殊情况，比如空数组
        if(intervals.length < 2){
            return intervals;
        }
        // 排序，但是int[][]是没有interator的，必须定义一个。
        // 括号里是 java的Lambda表达式 a[0] - b[0]，即 a[0] - b[0] < 0
        // 返回负数，所以是正序。反之是逆序。
        Arrays.sort(intervals, (a,b) ->  a[0] - b[0]);
        int l = 0; // l for length;
        for(int i = 0; i < intervals.length - 1; i++){
            // 排好序之后就简单了
            // 如果有重叠的部分。
            if( intervals[l][1] >= intervals[i+1][0] ){
                intervals[l][1] = Math.max(intervals[l][1],intervals[i+1][1]);
            }else{ //如果没有重叠的部分
                l++;
                intervals[l][0] = intervals[i+1][0];
                intervals[l][1] = intervals[i+1][1];
            }
        }
        // 拷贝从0到l个，需要输入 0, l+1;
        return Arrays.copyOfRange(intervals,0,l+1);
    }
}
```


## 2023 写的。

```java
class Solution {
    public int[][] merge(int[][] intervals) {
        if (intervals.length < 1) return new int[][]{};
        Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
        ArrayList<int[]> list = new ArrayList<>();
        int start = intervals[0][0];
        int end =  intervals[0][1];
        for (int i = 1; i < intervals.length; i++) {
            if (end < intervals[i][0]) {
                list.add(new int[]{start, end});
                start = intervals[i][0];
                end = intervals[i][1];
            } else {
                end = Math.max(end, intervals[i][1]);
            }
        }
        list.add(new int[]{start, end});
        int[][] ans = new int[list.size()][2];
        for (int i = 0; i < list.size(); i++) {
            ans[i][0] = list.get(i)[0];
            ans[i][1] = list.get(i)[1];
        }
        return ans;
    }
}

// After sorting, there are two cases
// connected : disconnected
// [   ].      [.  ]
// [  ].             [ ]
// when connected, update the end.
// when disconnected, compare the old end and new start, that is it.
```