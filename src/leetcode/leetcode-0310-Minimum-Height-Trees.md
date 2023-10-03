# 310 Minimum-Height-Trees 
 
difficulty: Medium 
 
<style>
        section pre{
          background-color: #eee;
          border: 1px solid #ddd;
          padding:10px;
          border-radius: 5px;
        }
      </style>
<section>
<div><p>A tree is an undirected graph in which any two vertices are connected by&nbsp;<i>exactly</i>&nbsp;one path. In other words, any connected graph without simple cycles is a tree.</p>
<p>Given a tree of <code>n</code> nodes&nbsp;labelled from <code>0</code> to <code>n - 1</code>, and an array of&nbsp;<code>n - 1</code>&nbsp;<code>edges</code> where <code>edges[i] = [a<sub>i</sub>, b<sub>i</sub>]</code> indicates that there is an undirected edge between the two nodes&nbsp;<code>a<sub>i</sub></code> and&nbsp;<code>b<sub>i</sub></code> in the tree,&nbsp;you can choose any node of the tree as the root. When you select a node <code>x</code> as the root, the result tree has height <code>h</code>. Among all possible rooted trees, those with minimum height (i.e. <code>min(h)</code>)&nbsp; are called <strong>minimum height trees</strong> (MHTs).</p>
<p>Return <em>a list of all <strong>MHTs'</strong> root labels</em>.&nbsp;You can return the answer in <strong>any order</strong>.</p>
<p>The <strong>height</strong> of a rooted tree is the number of edges on the longest downward path between the root and a leaf.</p>
<p>&nbsp;</p>
<p><strong>Example 1:</strong></p>
<img alt="" src="https://assets.leetcode.com/uploads/2020/09/01/e1.jpg" style="width: 800px; height: 213px;">
<pre><strong>Input:</strong> n = 4, edges = [[1,0],[1,2],[1,3]]
<strong>Output:</strong> [1]
<strong>Explanation:</strong> As shown, the height of the tree is 1 when the root is the node with label 1 which is the only MHT.
</pre>
<p><strong>Example 2:</strong></p>
<img alt="" src="https://assets.leetcode.com/uploads/2020/09/01/e2.jpg" style="width: 800px; height: 321px;">
<pre><strong>Input:</strong> n = 6, edges = [[3,0],[3,1],[3,2],[3,4],[5,4]]
<strong>Output:</strong> [3,4]
</pre>
<p><strong>Example 3:</strong></p>
<pre><strong>Input:</strong> n = 1, edges = []
<strong>Output:</strong> [0]
</pre>
<p><strong>Example 4:</strong></p>
<pre><strong>Input:</strong> n = 2, edges = [[0,1]]
<strong>Output:</strong> [0,1]
</pre>
<p>&nbsp;</p>
<p><strong>Constraints:</strong></p>
<ul>
	<li><code>1 &lt;= n &lt;= 2 * 10<sup>4</sup></code></li>
	<li><code>edges.length == n - 1</code></li>
	<li><code>0 &lt;= a<sub>i</sub>, b<sub>i</sub> &lt; n</code></li>
	<li><code>a<sub>i</sub> != b<sub>i</sub></code></li>
	<li>All the pairs <code>(a<sub>i</sub>, b<sub>i</sub>)</code> are distinct.</li>
	<li>The given input is <strong>guaranteed</strong> to be a tree and there will be <strong>no repeated</strong> edges.</li>
</ul>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    /*
    这个讨论的最高评论直接杀死比赛，解释了一切。
    https://leetcode.com/problems/minimum-height-trees/discuss/76055/Share-some-thoughts
    我们用 indegrees 找到所有的 leaf, 然后同时开始BFS。
    我们每次记录上一层BFS 的 leaves, 这样我们最后一层 BFS 的 leaves 就是所有的正确答案了。
    */
    public List<Integer> findMinHeightTrees(int n, int[][] edges) {
        if( n == 0){
            return new ArrayList<Integer>();
        }
        if( n == 1 ){
            ArrayList<Integer> temp = new ArrayList<Integer>();
            temp.add(0);
            return temp;
        }
        int[] degrees = new int[n];
        ArrayList<Integer>[] graph = new ArrayList[n];
        for(int i = 0; i < n; i++ ){
            graph[i] = new ArrayList<>();
        }
        
        for(int[] edge : edges){
            int a = edge[0];
            int b = edge[1];
            graph[a].add(b);
            graph[b].add(a);
            degrees[a] += 1;
            degrees[b] += 1;
        }
        LinkedList<Integer> bfs = new LinkedList<>();
        boolean[] marked = new boolean[n];
        
        for(int i = 0; i < n; i++ ){
            if( degrees[i] == 1 ){
                bfs.offer(i);
            }
        }
        
        List<Integer> prevLeaves = new ArrayList<>();
        while( !bfs.isEmpty() ){
            int size = bfs.size();
            prevLeaves = new ArrayList<>(bfs);
            while( size > 0 ){
                size -= 1;
                int cur = bfs.poll();
                marked[cur] = true;
                for( int next  : graph[cur] ){
                    if( marked[next] ){
                        continue;
                    }
                    degrees[next] -= 1;
                    if( degrees[next] == 1){
                        bfs.offer(next);
                    }
                }
            }            
        }
        return prevLeaves;
    }
}
​
```


上面的答案在2023年被证实有缺陷。因为没有考虑 indegrees == 1 的节点。

```java
class Solution {
    public List<Integer> findMinHeightTrees(int n, int[][] edges) {
        List<Integer>[] adjList = new ArrayList[n];
        int[] indegrees = new int[n];
        for (int i = 0; i < n; i++) {
            adjList[i] = new ArrayList<>();
        }
        for (int[] edge : edges) {
            adjList[edge[0]].add(edge[1]);
            adjList[edge[1]].add(edge[0]);
            indegrees[edge[0]]++;
            indegrees[edge[1]]++;
        }
        List<Integer> ans = new ArrayList<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (indegrees[i] == 1) {
                queue.offer(i);
            }
            if (indegrees[i] == 0) {
                ans.add(i);
            }
        }
        ArrayList<Integer> prevLevel = new ArrayList<>();
        while (!queue.isEmpty()) {
            int size = queue.size();
            prevLevel = new ArrayList<>(queue);
            while (size > 0) {
                int cur = queue.poll();
                for (int next : adjList[cur]) {
                    indegrees[next]--;
                    if (indegrees[next] == 1) {
                        queue.offer(next);
                    }
                }
                size--;
            }
        }
        ans.addAll(prevLevel);
        return ans;
    }
}
```
