# 261J.  Graph Valid Tree
https://leetcode.com/problems/graph-valid-tree/

## Method Best
一个无向图是不是树有两个要素：
一是只能有一部分，就是全部连通。
二是不能有环。

检测无环的方式是什么呢？
如果做DFS，到下一层，发现该节点已经被访问过了，由于是
无向图，除非这个节点就是自己的上一层，否则说明该无向图
必然有环。

```java
class Solution {
    // Graph是Tree，首先要只有一个部分，其次不能有环。
    // count 数有几个部分
    // hasCycle 判断是否有环
    private int count;
    private boolean hasCycle;
    private boolean[] visited;
    
    public boolean validTree(int n, int[][] edges) {
        // Initialize Global Variables
        this.count = 0;
        this.visited = new boolean[n];
        this.hasCycle = false;
        // Initialize Graph
        List<Integer>[] adj = new ArrayList[n];
        for(int i = 0; i < n; i++) adj[i] = new ArrayList<>();
        for(int[] pairs : edges){
            adj[pairs[0]].add(pairs[1]);
            adj[pairs[1]].add(pairs[0]);
        }
        // DFS
        for(int i = 0; i < n; i++){
            if(!this.visited[i]){
                dfs(adj,i,i);
                count++;
            }
        }
        // Graph是Tree，首先要只有一个部分，其次不能有环。
        return (count < 2) && (!hasCycle) ;
    }
    
    private void dfs(List<Integer>[] adj, int cur, int last ){
        visited[cur] = true;
        for(int next : adj[cur]){
            if(!visited[next]){
                dfs(adj, next, cur);
            }else if(next != last){
                this.hasCycle = true;
            }
        }
    } 
}
```



202309
写了一个 early return 多一点的

```java
class Solution {
    boolean hasCycle;
    boolean[] marked;
    public boolean validTree(int n, int[][] edges) {
        this.hasCycle = false;
        this.marked = new boolean[n];
        ArrayList<Integer>[] adj = new ArrayList[n];
        for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();
        for (int[] edge : edges) {
            adj[edge[0]].add(edge[1]);
            adj[edge[1]].add(edge[0]);
        }
        int island = 0;
        for (int i = 0; i < n; i++) {
            if (hasCycle || island > 1) return false;
            if (marked[i]) continue;
            island++;
            dfs(adj, i, i);
        }
        return !hasCycle && island == 1;
    }

    private void dfs(ArrayList<Integer>[] adj, int cur, int prev) {
        if (hasCycle) return;
        marked[cur] = true;
        for (int next : adj[cur]) {
            if (marked[next]) {
                if (next != prev) {
                    this.hasCycle = true;
                    return;
                }
                continue;
            }
            dfs(adj, next, cur);
        }
    }
}

```


这个题用 bfs 的 indegrees (kahn's algorithm) 也可以写，但是比较麻烦的地方是需要判断是不是所有的点都是联通的。