# 018J. 4Sum

```java
class Solution {
    public List<List<Integer>> fourSum(int[] nums, int target) {
        Arrays.sort(nums);
        return kSum(nums, target, 0, 4);
    }
    public List<List<Integer>> kSum(int[] nums, int target, int start, int k) {
        List<List<Integer>> res = new ArrayList<>();
        if (start == nums.length || nums[start] * k > target || target > nums[nums.length - 1] * k){
            // start == nums.length 是边界条件
            // nums[start] * k > target || target > nums[nums.length - 1] * k 是early return, 可以加速
            // 从这两个条件轻易就能看出不存在合理的解。因此可以直接return.
            return res;
        }
        if (k == 2){
            return twoSum(nums, target, start);
        }

        for (int i = start; i < nums.length; ++i){
            // 跳过重复的数字
            if( i > start && nums[i] == nums[i - 1] ){
                continue;
            }
            for (List<Integer> set : kSum(nums, target - nums[i], i + 1, k - 1) ) {
                res.add(new ArrayList<>(Arrays.asList(nums[i])));
                res.get(res.size() - 1).addAll(set);
            }
        }
        return res;
    }

    public List<List<Integer>> twoSum(int[] nums, int target, int start) {
        List<List<Integer>> res = new ArrayList<>();
        int lo = start, hi = nums.length - 1;
        while (lo < hi) {
            int sum = nums[lo] + nums[hi];
            if (sum < target || (lo > start && nums[lo] == nums[lo - 1]))
                ++lo;
            else if (sum > target || (hi < nums.length - 1 && nums[hi] == nums[hi + 1]))
                --hi;
            else
                res.add(Arrays.asList(nums[lo++], nums[hi--]));
        }
        return res;
    }
}
```

下面是我写的一种方法

```java
class Solution {

    public List<List<Integer>> fourSum(int[] nums, int target) {
        // Try to solve the NSum problem;
        Arrays.sort(nums);
        return kSum(4, nums, 0, target);

    }
    private List<List<Integer>> kSum(int nSum, int[] nums, int start, int target){
        List<List<Integer>> solutionSet = new ArrayList<>();
        if(start == nums.length){
            return solutionSet;
        }
        if(nSum == 2){
            return twoSum(nums, start, target);
        }

        for(int i = start; i < nums.length - nSum + 1; i++){
            if(i > start && nums[i] == nums[i - 1]){
                continue;
            }
            int newTarget = target - nums[i];
            for(List<Integer> solution : kSum(nSum - 1, nums, i + 1, newTarget) ){
                List<Integer> curSolution = new ArrayList<>(solution);
                curSolution.add(nums[i]);
                solutionSet.add(curSolution);
            }
        }
        return solutionSet;
    }

    private List<List<Integer>> twoSum(int[] nums, int start, int target){
        List<List<Integer>> solutionSet = new ArrayList<>();
        int left = start;
        int right = nums.length - 1;

        while(left < right){
            int sum = nums[left] + nums[right];
            if( sum < target){
                left++;
            }else if (sum > target){
                right--;
            }else{
                solutionSet.add( Arrays.asList( nums[left], nums[right]) );
                while( left < right && nums[left] == nums[left + 1]){
                    left++;
                }
                while( left < right && nums[right] == nums[right - 1] ){
                    right--;
                }
                left++;
                right--;
            }
        }
        return solutionSet;
    }
}
```
