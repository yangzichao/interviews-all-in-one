# 045 Jump-Game-II

difficulty: Hard

<style>
        section pre{
          background-color: #eee;
          border: 1px solid #ddd;
          padding:10px;
          border-radius: 5px;
        }
      </style>
<section>
<div><p>Given an array of non-negative integers, you are initially positioned at the first index of the array.</p>
<p>Each element in the array represents your maximum jump length at that position.</p>
<p>Your goal is to reach the last index in the minimum number of jumps.</p>
<p><strong>Example:</strong></p>
<pre><strong>Input:</strong> [2,3,1,1,4]
<strong>Output:</strong> 2
<strong>Explanation:</strong> The minimum number of jumps to reach the last index is 2.
    Jump 1 step from index 0 to 1, then 3 steps to the last index.</pre>
<p><strong>Note:</strong></p>
<p>You can assume that you can always reach the last index.</p>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    /*
    https://leetcode.com/problems/jump-game-ii/discuss/18014/Concise-O(n)-one-loop-JAVA-solution-based-on-Greedy
    最高赞下面确实好多魔鬼
    为何 i < nums.length - 1 ?
    因为我们不关心到达最后一个位置之后再怎么跳，如果 nums.length, 我们很多时候会多跳一次才停止。
    这个解法本身是一个 BFS。
    我们 i 到 curEnd 之间搜寻最大可能的向右跳的范围 curFartherest,
    一旦 i == curEnd，说明我们搜索完毕，然后我们假设跳到了 curFatherest, step+=1 。
    但是这不意味着全局来看，跳到 curFatherest 就是最好的解
    我们继续在 i 和 新的 curEnd 之间继续搜寻下一跳的最大的 curFatherest。
    */
    public int jump(int[] nums) {    
        int curEnd = 0;
        int curFartherest = 0;
        int steps = 0;
        for(int i = 0; i < nums.length - 1; i++){
            curFartherest = Math.max(curFartherest, i+nums[i]);
            if( curFartherest >= nums.length - 1){
                return steps + 1;
            }
            if( i == curEnd ){
                steps += 1;
                curEnd  = curFartherest;
            }
        }
        return steps;
    }
}
​
2025 年写了一个基于 bfs 原则的代码

这个题的思路是这样的 你可以想象如果我们储存一个数组 然后数组上的数字都是需要多少步到这一个位置 那么这个数组的形态肯定是就是单调递增的 它肯定是类似于0多少个1 然后多少个2 然后多少个连续多少个3 然后连续多少个4这个样子 那么我们就可以想象 这个BFS相当于就是我们每一层都是同样步数到达的 index.

```java
class Solution {
    public int jump(int[] nums) {
        if (nums.length == 0) return 0;
        int N = nums.length;
        int steps = 0;
        int curFurtherest = 0;
        int lo = 0;
        while (curFurtherest < N - 1) {
            int hi = curFurtherest;
            while (lo <= hi) {
                curFurtherest = Math.max(curFurtherest, lo + nums[lo]);
                lo++;
            }
            steps++;
        }
        return steps;
    }
}
```
​
/**
下面的是一个 O(N^2)的解法，我觉得还挺好的，然而太慢
    public int jump(int[] nums) {
        int[] dp = new int[nums.length];
        Arrays.fill(dp, -1);
        dp[0] = 0;
        for(int i = 0; i < nums.length; i++ ){
            int maxStep = nums[i];
            for(int next = maxStep; next >= 1; next--){
                if( i + next >= nums.length ){
                    continue;
                }
                if( dp[ i + next ]  < 0 ){
                    dp[ i + next ] = dp[i] + 1;
                }else{
                    dp[ i + next ] = Math.min( dp[ i + next ], dp[i] + 1);
                }
                if( i + next == nums.length ){
                    return dp[nums.length - 1];
                }
            }
        }
        return dp[nums.length - 1];
    }
*/
​
```

2022 的 O(N^2) 解法。

```java
class Solution {
    public int jump(int[] nums) {

        int[] jumps = new int[nums.length];
        for(int i = 0; i < nums.length; i++){
            for(int j = nums[i]; j > 0; j--){
                if(i + j > nums.length - 1) continue;
                if(i + j == nums.length - 1) {
                    return jumps[i] + 1;
                }
                jumps[i + j] = jumps[i + j] == 0 ? jumps[i] + 1 : Math.min(jumps[i + j], jumps[i] + 1);
            }
        }
        return 0;
    }
}
```

2023 的 O(N^2)

```java
class Solution {
    public int jump(int[] nums) {
       int[] dp = new int[nums.length];
       for(int i = 0; i < nums.length; i++) {
           for(int j = nums[i]; j > 0; j--) {
                if(i + j >= nums.length) continue;
                if(i + j == nums.length - 1) {
                    return dp[i] + 1;
                }
                if(dp[i + j] == 0) {
                    dp[i + j] = dp[i] + 1;
                }
           }
       }
       return dp[nums.length - 1];
    }
}
```

2024 还是 O(N^2) 笑死

```java
class Solution {
    public int jump(int[] nums) {
        int[] minSteps = new int[nums.length];
        Arrays.fill(minSteps, Integer.MAX_VALUE);
        minSteps[0] = 0;
        for (int i = 0; i < nums.length; i++) {
            for (int j = 1; j <= nums[i] && i + j < nums.length; j++) {
                minSteps[i + j] = Math.min(minSteps[i] + 1, minSteps[i + j]);
            }
        }
        return minSteps[nums.length - 1];
    }
}
```
