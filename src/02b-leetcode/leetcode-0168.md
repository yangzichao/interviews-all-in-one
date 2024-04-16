
这个题是一个 base62算法的基础。

```java
class Solution {
    public String convertToTitle(int columnNumber) {
        String dict = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        while (columnNumber > 0) {
            columnNumber -= 1;
            sb.append(dict.charAt( columnNumber % 26));
            columnNumber /= 26;
        }
        return sb.reverse().toString();
    }
}
```