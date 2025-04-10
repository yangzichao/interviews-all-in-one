我都震惊了这个题没有笔记？可能是都放到207了吧。

```java
class Solution {
    public int[] findOrder(int numCourses, int[][] prerequisites) {
        ArrayList<Integer>[] adj = new ArrayList[numCourses];
        for (int i = 0; i < numCourses; i++) {
            adj[i] = new ArrayList<>();
        }
        int[] indegrees = new int[numCourses];
        for (int[] prerequisite : prerequisites) {
            adj[prerequisite[1]].add(prerequisite[0]);
            indegrees[prerequisite[0]]++;
        } 

        ArrayList<Integer> topo = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) {
            if (indegrees[i] == 0) topo.add(i);
        }
        for (int i = 0; i < topo.size(); i++) {
            int cur = topo.get(i);
            for (int next : adj[cur]) {
                indegrees[next]--;
                if (indegrees[next] == 0) {
                    topo.add(next);
                }
            }
        }
        if (topo.size() < numCourses) return new int[]{};

        int[] ans = new int[numCourses];
        for (int i = 0 ; i < numCourses; i++) {
            ans[i] = topo.get(i);
        }
        return ans;
    }
}
```


## 2025
这里我们放一个 2025年的笔记，代码是以前的老代码。
onStack其实本质上是一个路径记录，我们只是用一个set来实现更快的查找当前节点是否在过去的路径上。

```java
class Solution {
    private boolean[] marked;
    private boolean[] onStack;
    private boolean hasCycle;
    private ArrayList<Integer>[] graph;

    private Stack<Integer> reversePost;
    private ArrayList<Integer> postList;
    private LinkedList<Integer> reversePostList;

    public int[] findOrder(int numCourses, int[][] prerequisites) {

        this.marked = new boolean[numCourses];
        this.onStack = new boolean[numCourses];
        this.hasCycle = false;
        this.graph = new ArrayList[numCourses];

        this.reversePost = new Stack<>();
        this.postList = new ArrayList<>();
        this.reversePostList = new LinkedList<>();

        for(int i = 0; i < numCourses; i++ ) {
            graph[i] = new ArrayList<Integer>();
        }

        for(int[] edge : prerequisites) {
            graph[edge[1]].add(edge[0]);
        }

        for(int i = 0; i < numCourses; i++ ) {
            if(!marked[i]) {
                dfs(i);
            }
        }
        if(hasCycle) return new int[0];

        int[] ansRP = new int[numCourses];
        int[] ansPList = new int[numCourses];
        int[] ansRPList = new int[numCourses];

        for(int i = 0; i < numCourses; i++) {
            ansRP[i] = reversePost.pop();
            ansPList[i] = postList.get(numCourses - i - 1);
            ansRPList[i] = reversePostList.get(i);
        }

        return ansRPList;
    }

    public void dfs(int vertex) {
        marked[vertex] = true;
        onStack[vertex] = true;
        for(int neighbor : graph[vertex] ) {
            if( hasCycle ) {
                return;
            }

            if(!marked[neighbor]) {
                dfs(neighbor);
            }else if( onStack[neighbor] ) {
                hasCycle = true;
                return;
            }
        }
        onStack[vertex] = false;


        reversePost.push(vertex);
        postList.add(vertex);
        reversePostList.addFirst(vertex);
    }
}

```


下面是这次新憋出来的代码，上面代码的是算法第四版中的写法。



```java
class Solution {
    public int[] findOrder(int numCourses, int[][] prerequisites) {
        // build graph
        Map<Integer, Set<Integer>> courseMap = new HashMap<>();
        for (int[] prerequisite : prerequisites) {
            int from = prerequisite[1];
            int to = prerequisite[0];
            courseMap.putIfAbsent(from, new HashSet<>());
            courseMap.putIfAbsent(to, new HashSet<>());
            courseMap.get(from).add(to);
        }

        // dfs
        boolean[] hasCycle = new boolean[]{false};
        Set<Integer> marked = new HashSet<>();
        List<Integer> postOrder = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) {
            // do a real dfs
            if (hasCycle[0]) return new int[]{};
            if (marked.contains(i)) continue;
            Set<Integer> onStack = new HashSet<>();
            dfs(courseMap, marked, onStack, postOrder, i, hasCycle);
        }
        int[] ans = new int[postOrder.size()];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = postOrder.get(ans.length - 1 - i);
        }
        return ans;
    }
    private void dfs(Map<Integer, Set<Integer>> courseMap, 
    Set<Integer> marked, 
    Set<Integer> onStack,
    List<Integer> postOrder,
    int cur,
    boolean[] hasCycle) {
        marked.add(cur);
        onStack.add(cur);
        for (int next : courseMap.getOrDefault(cur, new HashSet<>())) {
            if (hasCycle[0]) return;
            if (marked.contains(next)) {
                if (onStack.contains(next)) {
                    hasCycle[0] = true;
                    return;
                }
                continue;
            }
            dfs(courseMap, marked, onStack, postOrder, next, hasCycle);
        }
        onStack.remove(cur);
        postOrder.add(cur);
    }
}
```