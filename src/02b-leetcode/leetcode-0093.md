
这个题的难点在于处理这个 dots...

```java
class Solution {
    public List<String> restoreIpAddresses(String s) {
        List<String> ans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        backtrack(ans, s, sb, 0, 3);
        return ans;
    }
    private void backtrack(List<String> ans, String s, StringBuilder sb, int cur, int dots) {
        if (cur == s.length() && dots < 0) {
            ans.add(sb.toString());
            return;
        }
        if (cur >= s.length() || dots < 0) {
            return;
        }
        for (int i = 1; i <= 3; i++) {
            if (cur + i > s.length()) continue;
            if (!isValid(Integer.valueOf(s.substring(cur, cur + i)), i)) continue;
            int curLength = sb.length();
            sb.append(Integer.valueOf(s.substring(cur, cur + i)));
            if (dots > 0) sb.append('.');
            backtrack(ans, s, sb, cur + i, dots - 1);
            sb.setLength(curLength);
        }
    }

    private boolean isValid(int num, int length) {
        if (length == 1) return true;
        if (length == 2) return num > 9;
        return num < 256 && num > 99;
    }
}
```