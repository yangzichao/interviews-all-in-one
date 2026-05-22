# 128J. Longest Consecutive Sequence
https://leetcode.com/problems/longest-consecutive-sequence/

是一道水hard.
## Method Best: O(N) O(N)
<pre>
其原理是我们先把所有元素都存到Set之中。
然后我们iterate这个数组，如果该位-1的元素已经在set中了，就跳过它，
避免重复的循环。借此达到O(N).
</pre>
```Java
class Solution {
    public int longestConsecutive(int[] nums) {
        Set<Integer> set = new HashSet<>();
        for(int n : nums){
            set.add(n);
        }
        int ans = 0;

        for(int i = 0; i < nums.length; i++){
            // 这本是一个 O(N^2)的解法，但是由于加上了这句判断，变成了O(N)
            // 这句保证了我们从一个 sequence 的最小的一位开始查找。
            if(set.contains(nums[i] - 1)){
                continue;
            }
            int count = 1;
            int temp = nums[i];
            while( set.contains(temp + 1)){
                count++;
                temp+=1;
            }
            ans = Math.max(ans, count);
        }
        return ans;
    }
}
```

上面的答案现在 TLE 了 


```java
class Solution {
    public int longestConsecutive(int[] nums) {
        if (nums.length < 1) return 0;
        int longest = 1;
        Set<Integer> set = new HashSet<>();
        for (int num : nums) set.add(num);

        for (int num : set) { // 区别在这里 是 iterate set 因为 set去除了重复的元素，所以快。
            if (!set.contains(num - 1)) {
                int count = 1;
                int cur = num;
                while (set.contains(cur + 1)) {
                    count++;
                    cur = cur + 1;
                }
                longest = Math.max(longest, count);
            }
        }
        return longest;

    }
}
```
