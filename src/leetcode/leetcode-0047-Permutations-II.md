# 047J. Permutations II
https://leetcode.com/problems/permutations-ii/

## Backtracking 1
这个方法用的跟上个题一样，
但是为了避免重复的元素造成重复的结果，
用了一个map。而且为了避免重复还排了个序。
```java
class Solution {
    private Map<Integer,Integer> numMap;
    private List<List<Integer>> ans;
    private List<Integer> temp;
    private int[] nums;    
    public List<List<Integer>> permuteUnique(int[] nums) {
        this.ans = new LinkedList<>();
        this.temp = new LinkedList<>();
        this.nums = nums;
        this.numMap = new HashMap<>();
        for(int n : nums){
            numMap.put(n, numMap.getOrDefault(n,0) + 1);
        }
        Arrays.sort(nums);
        backtracker(nums.length);
        return ans;
    }
    
    private void backtracker(int index){
        if(index  == 0){
            ans.add(new LinkedList<Integer>(temp));
            return;
        }
        
        for(int i = 0; i < nums.length; i++){
            if(numMap.getOrDefault(nums[i],0) == 0) continue;
            if(i > 0 && nums[i] == nums[i-1]) continue;
            temp.add(nums[i]);
            numMap.put(nums[i], numMap.get(nums[i]) - 1 );
            
            backtracker(index - 1);
            
            temp.remove(temp.size() - 1);
            numMap.put(nums[i], numMap.get(nums[i]) + 1 );
        }
    } 
}
```


下面这个是建立在 permutation 的基础之上的答案。没有排序
只是在每一级里加上了一个 set. 这样就避免了和已经交换过的元素再交换。
比如 a b c b e, 其中 a 和 第一个 b 还是和 第二个 b 交换没有区别。
为什么呢？看如下：
b a c b e, 所有的permutation之中必有 b c a e 的组合，所以会和下面的重复。
b b c a e

想想permutation的本质。


```java
class Solution {
    private List<List<Integer>> ans;
    public List<List<Integer>> permuteUnique(int[] nums) {
        this.ans = new ArrayList<>();
        helper(nums, 0);
        return ans;
    }
    private void helper(int[] nums, int index) {
        if (index == nums.length) {
            ArrayList<Integer> cur = new ArrayList<>();
            for (int num : nums) cur.add(num);
            ans.add(cur);
        }
        Set<Integer> used = new HashSet<>();
        for (int i = index; i < nums.length; i++) {
            if (used.contains(nums[i])) continue;
            used.add(nums[i]);
            swap(nums, index, i);
            helper(nums, index + 1);
            swap(nums, index, i);
        }
    }
    private void swap(int[] nums, int i, int j) {
        if (i == j) return;
        nums[i] ^= nums[j];
        nums[j] ^= nums[i];
        nums[i] ^= nums[j];
    }
}
```