```java
class Solution {
    public boolean validWordAbbreviation(String word, String abbr) {
        if (word == null || abbr == null) return false;
        int i = 0;
        int j = 0;
        while (i < abbr.length() && j < word.length()) {
            if (abbr.charAt(i) == word.charAt(j)) {
                i++;
                j++;
                continue;
            }
            if (!Character.isDigit(abbr.charAt(i))) return false;
            if ((int) (abbr.charAt(i) - '0') == 0) return false;
            int start = i;
            while (i < abbr.length() && Character.isDigit(abbr.charAt(i))) i++;
            j += Integer.valueOf(abbr.substring(start, i));
        }
        return i == abbr.length() && j == word.length();
    }
}
```

以下的写法比较清晰一点儿。就是如果遇到数字 就 相当于跳过数字那么多位。
edge cases 是：
1. 需要数字没有 leading zero，那么indexJumpOver是0的时候，当前的字符不能是'0'.
2. 需要注意 index overflow 

```java
class Solution {
    public boolean validWordAbbreviation(String word, String abbr) {
        int originalIndex = -1;
        int indexJumpOver = 0;
        for (char c : abbr.toCharArray()) {
            if (Character.isDigit(c)) {
                if (indexJumpOver == 0 && c == '0') return false;
                indexJumpOver = indexJumpOver * 10 + (int) (c - '0');
            } else {
                originalIndex ++;
                originalIndex += indexJumpOver;
                if (originalIndex >= word.length() || word.charAt(originalIndex) != c) {
                    return false;
                }
                indexJumpOver = 0;
            }
        }
        if (indexJumpOver > 0) originalIndex += indexJumpOver;
        return originalIndex == word.length() - 1;
    }
}
```