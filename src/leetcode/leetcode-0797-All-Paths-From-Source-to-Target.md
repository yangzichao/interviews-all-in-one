# 797 All-Paths-From-Source-to-Target 
 
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
<div><p>Given a directed&nbsp;acyclic graph of <code>N</code> nodes.&nbsp;Find all possible paths from node <code>0</code> to node <code>N-1</code>, and return them in any order.</p>
<p>The graph is given as follows:&nbsp; the nodes are 0, 1, ..., graph.length - 1.&nbsp; graph[i] is a list of all nodes j for which the edge (i, j) exists.</p>
<pre><strong>Example:</strong>
<strong>Input:</strong> [[1,2],[3],[3],[]]
<strong>Output:</strong> [[0,1,3],[0,2,3]]
<strong>Explanation:</strong> The graph looks like this:
0---&gt;1
|    |
v    v
2---&gt;3
There are two paths: 0 -&gt; 1 -&gt; 3 and 0 -&gt; 2 -&gt; 3.
</pre>
<p>&nbsp;</p>
<p><strong>Constraints:</strong></p>
<ul>
	<li>The number of nodes in the graph will be in the range <code>[2, 15]</code>.</li>
	<li>You can print different paths in any order, but you should keep the order of nodes inside one path.</li>
</ul>
</div></section>
 
 ## Method One 
 
 这个题目的难点其实在于对时间复杂度的分析。  
 一个切入点是，计算总的可能的路径数量，然后每条路径都是 O(N), 相乘就行。   
 我们从
 两个node开始计算，共有1条。
 三个node，有2条。
 每增加一个新的node,我们都可以把新的node加入之前的路径构成一组新的路径，
 因此会增加一倍的新路径。 
 所以总共的路径数是 1 + 2 + 4 + ... 2^{n - 1} = 2^{n} - 1 条。
 因此时间复杂度是 O(2^N * N);
 
``` Java
class Solution {
    public List<List<Integer>> allPathsSourceTarget(int[][] graph) {
        /*
        这个题注意由于我们是要 all possible path, 我们不能 mark vertex
        由于又是 acyclic graph 所以不用担心有环，因此不需要写 marked。
        如果是有环的情况，我们要 Mark edge 的
        */
        LinkedList< List<Integer> > stack = new LinkedList<>();
        List<List<Integer>> ans = new LinkedList<>();
        List<Integer> temp = new ArrayList<>();
        temp.add(0);
        stack.push(temp);
        while(!stack.isEmpty()){
            List<Integer> pathSoFar = stack.pop();
            int cur = pathSoFar.get( pathSoFar.size() - 1);
            if( cur == graph.length - 1){
                ans.add(pathSoFar);
            }
            for(int next : graph[cur] ) {
                List<Integer> nextPath = new ArrayList<Integer>(pathSoFar);
                nextPath.add(next);
                stack.push(nextPath);
            }
        }
        return ans;
    }
}
​
```