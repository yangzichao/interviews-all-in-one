

是有点儿意思只能说 普通很容易写一个TLE

```java
class Solution {
    public int numFriendRequests(int[] ages) {
        int[] uniqueAge = new int[121];
        for (int age : ages) {
            uniqueAge[age]++;
        }
        int count = 0;
        for (int x = 0; x < uniqueAge.length; x++) {
            for (int y = 0; y < uniqueAge.length; y++) {
                if (uniqueAge[x] == 0 || uniqueAge[y] == 0) continue;
                if (y > x) continue;
                if (y > 100 && x < 100) continue;
                if (y <= (double) (0.5 * x + 7.0)) continue;
                count += uniqueAge[x] * uniqueAge[y];
                if (x == y) {
                    count -= uniqueAge[x]; // remove self send
                }
            }
        }
        return count;
    }
}
```