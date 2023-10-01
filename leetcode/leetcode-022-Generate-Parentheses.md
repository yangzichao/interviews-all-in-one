# 022J. Generate Parentheses

这个题竟然没有存过一版吗？

```java
class Solution {
    public List<String> generateParenthesis(int n) {
        StringBuilder sb = new StringBuilder();
        List<String> ans = new ArrayList<>();
        dfs(ans, sb, 2 * n, 0, 0);
        return ans;
    }

    private void dfs(List<String> ans, StringBuilder sb, int maxDepth, int depth, int netVal) {
        if (netVal < 0) return;
        if (depth == maxDepth) {
            if (netVal == 0) {
                ans.add(sb.toString());
            }
            return;
        }
        sb.append('(');
        dfs(ans, sb, maxDepth, depth + 1, netVal + 1);
        sb.deleteCharAt(sb.length() - 1);

        sb.append(')');
        dfs(ans, sb, maxDepth, depth + 1, netVal - 1);
        sb.deleteCharAt(sb.length() - 1);
    }
}

```