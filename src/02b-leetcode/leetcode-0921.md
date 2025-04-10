




```java
class Solution {
    public int minAddToMakeValid(String s) {
        int net = 0;
        int add = 0;
        for (char c : s.toCharArray()) {
            if (c == '(') {
                net += 1;
                continue;
            }
            net -= 1;
            if (net < 0) {
                add += 1;
                net += 1;
            }
        }
        add += net;
        return add;
    }
}
```