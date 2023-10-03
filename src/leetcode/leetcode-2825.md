写的一般，大致是那个意思。

```java
class Solution {
    public boolean canMakeSubsequence(String str1, String str2) {
        if (str1.length() < str2.length()) return false;
        int p1 = 0;
        for (int i = 0; i < str2.length(); i++) {
            char candidate = str2.charAt(i);
            boolean isFound = false;
            while (p1 < str1.length()) {
                int diff = (int) candidate - str1.charAt(p1);
                if (diff == 0 || diff == 1 || diff == -25) {
                    p1++;
                    isFound = true;
                    break;
                }
                p1++;
            }
            if (!isFound) return false;
        }
        return true;
    }
}
```