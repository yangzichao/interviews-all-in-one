# 015J. Three Sum

https://leetcode.com/problems/3sum/

## Method: O(N^2)

<pre>
Two Sum 因为可以达到比 O(NlogN)更好的O(N),所以不用Sort.
Three Sum 应当先Sort 比较合算. 剩下的比较平庸。
</pre>

这里用到了[167 题](leetCode-167-Two-Sum-II.md)的代码。

```Java
class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);

        List<List<Integer>> ans = new ArrayList<>();

        int target = 0;

        for(int i = 0; i < nums.length - 2; i++){

            target = 0 - nums[i];
            int left = i + 1;
            int right = nums.length - 1;
            // 这个是为了避免重复的 i
            if(i == 0 || (i> 0 && nums[i]!=nums[i-1] )){
                while(left < right){
                    if( nums[left] + nums[right] < target){
                        left++;
                    }else if(nums[left] + nums[right] > target){
                        right--;
                    }else{
                        List<Integer> temp = new ArrayList<>();
                        temp.add(nums[i]);
                        temp.add(nums[left]);
                        temp.add(nums[right]);
                        ans.add(temp);
                        //以下两句是为了避免重复的 left 和 right
                        while (left < right && nums[left] == nums[left+1]) left++;
                        while (left < right && nums[right] == nums[right-1]) right--;
                        left++;
                        right--;
                    }
                }
            }
        }
        return ans;
    }
}
```

又一次做

```java
class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        // get zero;
        // need solution set.
        Arrays.sort(nums);
        List<List<Integer>> ans = new ArrayList<>();
        for(int i = 0; i < nums.length - 2; i ++ ) {
            // 减 2 是为了给 left 和 righ 留位置
            int target = 0 - nums[i];
            int left = i + 1;
            int right = nums.length - 1;
            // 这个题的问题是存在重复的数字，所以需要跳过。 但是是因为不能有 triplet 相同，而不是因为
            // 有重复的数字。重复的数字是允许的。i取的是一串重复数字中的第一个。
            if( i == 0 || (i > 0 && nums[i] != nums[i - 1] )) {
                while(left < right) {
                    int sum = nums[left] + nums[right];
                    if(sum < target) {
                        left ++;
                    }else if(sum > target) {
                        right --;
                    }else {
                        Integer[] tempAns = new Integer[]{nums[i], nums[left], nums[right]};
                        List<Integer> temp = Arrays.asList(tempAns);
                        ans.add(temp );
                        // 以下是为了排除重复的数字。
                        while( (left < right) && nums[left] == nums[left + 1] ) {
                            left ++;
                        }
                        while( (left < right) && nums[right] == nums[right - 1] ) {
                            right--;
                        }
                        //因为要全部的答案，所以不能停
                        left++;
                        right--;
                    }
                }
            }

        }

        return ans;
    }
}
```

最好是这样：

```java
class Solution {
    public List<List<Integer>> threeSum(int[] nums) {

        Arrays.sort(nums);
        List<List<Integer>> ans = new ArrayList<>();
        for(int i = 0; i < nums.length - 2; i ++ ) {
            // 减 2 是为了给 left 和 right 留位置
            // 这个题的问题是存在重复的数字，所以需要跳过。 但是是因为不能有 triplet 相同，而不是因为
            // 有重复的数字。重复的数字是允许的。i取的是一串重复数字中的第一个。
            if( i > 0 && nums[i] == nums[i - 1] ){
                continue;
                // 注意 这里只能是 continue; 而不能是 i++ 或者 while() i++
                // continue 也是起到了 i++ 的作用 但是它相当于把 for loop 的
                // conditional statement 一直带上了。
                // 所以 if continue  的效果是对 for loop 起到了增加一个额外的 conditional statement 的作用
            }

            int target = 0 - nums[i];
            int left = i + 1;
            int right = nums.length - 1;

            while(left < right) {
                int sum = nums[left] + nums[right];
                if(sum < target) {
                    left ++;
                }else if(sum > target) {
                    right --;
                }else {
                    Integer[] tempAns = new Integer[]{nums[i], nums[left], nums[right]};
                    List<Integer> temp = Arrays.asList(tempAns);
                    ans.add(temp );
                    // 以下是为了排除重复的数字。
                    while( (left < right) && nums[left] == nums[left + 1] ) {
                        left ++;
                    }
                    while( (left < right) && nums[right] == nums[right - 1] ) {
                        right--;
                    } // 注：2022年再看发现这一行可以去掉，因为left一侧的重复数字被排除了之后，一定不会再选择重复的right了。
                    //因为要全部的答案，所以不能停
                    left++;
                    right--;
                }
            }


        }

        return ans;
    }
}
```


## 2023 又写：
```java
class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> ans = new ArrayList<>();
        for (int i = 0; i < nums.length - 2; i++) {
            if (i > 0 && nums[i] == nums[i - 1]) continue;
            int lo = i + 1;
            int hi = nums.length - 1;
            while (lo < hi) {
                if (nums[lo] + nums[hi] == -nums[i]) {
                    List<Integer> triplet = new ArrayList<>();
                    triplet.add(nums[i]);
                    triplet.add(nums[lo]);
                    triplet.add(nums[hi]);
                    ans.add(triplet);
                    while (lo < hi && nums[lo] == nums[lo + 1]) lo++;
                    while (lo < hi && nums[hi] == nums[hi - 1]) hi--;
                    lo++;
                    hi--;
                } else if (nums[lo] + nums[hi] < -nums[i]) {
                    lo++;
                } else {
                    hi--;
                }
            }
        }
        return ans;
    }
}
```


2023 09 

```java
class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> ans = new ArrayList<>();
        for (int i = 0; i < nums.length - 2; i++) {
            if (i > 0 && nums[i] == nums[i - 1]) continue;
            int target = - nums[i];
            int left = i + 1;
            int right = nums.length - 1;
            while (left < right) {
                int sum = nums[left] + nums[right];
                if (sum < target) {
                    left++;
                } else if (sum > target) {
                    right--;
                } else {
                    ans.add(Arrays.asList(new Integer[]{nums[i], nums[left], nums[right]}));
                    while (left < right && nums[left] == nums[left + 1]) left++;
                    while (left < right && nums[right] == nums[right - 1]) right--;
                    left++;
                    right--;
                }
            } 
        }
        return ans;
    }
}
```