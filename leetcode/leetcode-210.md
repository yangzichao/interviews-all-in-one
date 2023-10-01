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