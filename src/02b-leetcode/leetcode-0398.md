
没啥意思的题，过！

```java
class Solution {
    Map<Integer, List<Integer>> valToIndex;
    public Solution(int[] nums) {
        this.valToIndex = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            valToIndex.putIfAbsent(nums[i], new ArrayList<>());
            valToIndex.get(nums[i]).add(i);
        }
    }
    
    public int pick(int target) {
        if (!valToIndex.containsKey(target)) return -1;
        List<Integer> list = valToIndex.get(target);
        int random = (int) Math.floor(Math.random() * list.size());
        return list.get(random);
    }

}

/**
 * Your Solution object will be instantiated and called as such:
 * Solution obj = new Solution(nums);
 * int param_1 = obj.pick(target);
 */
```