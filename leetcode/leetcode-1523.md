

```java
class Solution {
    public int countOdds(int low, int high) {
        // a\b.  odd        even
        // odd.  (b - a + 2)/2 (b-a-1)/2
        // even  (b-a-1)/2  (b - a)/2
        int interval = 0;
        interval += low % 2 == 1 ? 1 : 0;
        interval += high % 2 == 1 ? 1 : 0;
        return (interval + high - low) / 2;
    }
}
```