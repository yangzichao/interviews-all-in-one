
DFS 或者 BFS 都可以做。应该 DFS 性能好一点。

```java
class Solution {
    public boolean canReach(int[] arr, int start) {
        int N = arr.length;
        boolean[] visited = new boolean[N];
        Deque<Integer> stack = new ArrayDeque<>();
        if (arr[start] == 0) return true;
        stack.push(start);
        visited[start] = true;
        while (!stack.isEmpty()) {
            int cur = stack.pop();
            int next1 = cur - arr[cur];
            int next2 = cur + arr[cur];
            if (next1 >= 0 && next1 < N && !visited[next1]) {
                if (arr[next1] == 0) return true;
                visited[next1] = true;
                stack.push(next1);
            }
            if (next2 >= 0 && next2 < N && !visited[next2]) {
                if (arr[next2] == 0) return true;
                visited[next2] = true;
                stack.push(next2);
            }
        }
        return false;
    }
}
```