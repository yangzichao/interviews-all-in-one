785 是一道经典的图的题
这个题就和 886 一模一样 https://leetcode.com/problems/possible-bipartition/ 

二分图要求每个边分属于两个图，我们可以用染色的方法。
我们用一个全局变量来标记当前的进度下是否还能保持二分图。
如果一个点我们没有访问过，我们就把它染相对的颜色。如果访问过，我们就看他的颜色是不是符合要求。
如果不符合要求，立刻将这个flag给标记为 false, 全局的递归都要立刻停止。

```java
class Solution {
    private boolean[] marked;
    private boolean[] color;
    private boolean isBiGraph;
    public boolean isBipartite(int[][] graph) {
        this.marked = new boolean[graph.length];
        this.color = new boolean[graph.length];
        this.isBiGraph = true;
        for (int i = 0; i < graph.length; i++) {
            if (marked[i]) continue;
            dfs(graph, i);
        }
        return isBiGraph;
    }

    private void dfs(int[][] graph, int cur) {
        if (!isBiGraph) return;
        for (int next : graph[cur]) {
            if (marked[next]) {
                if (color[next] == color[cur]) {
                    isBiGraph = false;
                    return;
                }
                continue;
            }
            marked[next] = true;
            color[next] = !color[cur];
            dfs(graph, next);
        }
    }
}
```