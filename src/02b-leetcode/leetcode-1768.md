
精髓在于 p <= q 代替了额外的 boolean flag 还是很妙的
```java
class Solution {
    public String mergeAlternately(String word1, String word2) {
        int p = 0, q = 0;
        StringBuilder sb = new StringBuilder();
        while (p < word1.length() || q < word2.length()) {
            if (p == word1.length()) {
                sb.append(word2.charAt(q++));
                continue;
            }
            if (q == word2.length()) {
                sb.append(word1.charAt(p++));
                continue;
            }
            if (p <= q) {
                sb.append(word1.charAt(p++));
            } else {
                sb.append(word2.charAt(q++));
            }
        }
        return sb.toString();
    }
}
```