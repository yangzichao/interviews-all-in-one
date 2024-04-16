
这个是 decode base62 算法
```java
class Solution {
    public int titleToNumber(String columnTitle) {
        String dict = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int sum = 0;
        for (int i = 0; i < columnTitle.length(); i++) {
            sum = sum * dict.length() + (dict.indexOf(columnTitle.charAt(i)) + 1);
        }
        return sum;
    }
}
```