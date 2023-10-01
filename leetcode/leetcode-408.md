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