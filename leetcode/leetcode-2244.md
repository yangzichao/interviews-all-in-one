


这个题还是挺有意思的
如果说学到了什么，就是应当尽快想到取余分情况讨论。

```java
class Solution {
    public int minimumRounds(int[] tasks) {
        Map<Integer, Integer> count = new HashMap<>();
        for (int task : tasks) {
            count.put(task, count.getOrDefault(task, 0) + 1);
        }
        int totalRounds = 0;
        for (int key : count.keySet()) {
            int cur = count.get(key);
            if (cur == 1) return -1;
            if (cur % 3 == 0) {
                totalRounds += cur / 3;
            } else if (cur % 3 == 1) {
                totalRounds += (cur - 3) / 3 + 2;
            } else {
                totalRounds += cur / 3 + 1;
            }
        }
        return totalRounds;
    }
}
```