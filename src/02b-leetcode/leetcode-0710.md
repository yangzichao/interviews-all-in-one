

这个题是个hard，果然思路太过于直白就会 TLE 或者 MLE
如下的解答，MLE了。
这个思路其实叫白名单，即反黑名单而行之。
```java
class Solution {
    int[] nums;
    public Solution(int n, int[] blacklist) {
        this.nums = new int[n - blacklist.length];
        Set<Integer> set = new HashSet<>();
        for (int b : blacklist) set.add(b);
        int index = 0;
        for (int i = 0; i < n; i++) {
            if (set.contains(i)) continue;
            nums[index++] = i;
        }
    }
    
    public int pick() {
        return nums[(int) (Math.floor(Math.random() * nums.length))];
    }
}

/**
 * Your Solution object will be instantiated and called as such:
 * Solution obj = new Solution(n, blacklist);
 * int param_1 = obj.pick();
 */
```

图省事儿，不行再来，这会是 TLE 了。

```java
class Solution {
    Set<Integer> bSet;
    int n;
    public Solution(int n, int[] blacklist) {
        this.n = n;
        this.bSet = new HashSet<>();
        for (int b : blacklist) bSet.add(b);
    }
    
    public int pick() {
        int candidate = (int) (Math.floor(Math.random() * n));
        if (bSet.contains(candidate)) return pick();
        return candidate;
    }
}

/**
 * Your Solution object will be instantiated and called as such:
 * Solution obj = new Solution(n, blacklist);
 * int param_1 = obj.pick();
 */
```