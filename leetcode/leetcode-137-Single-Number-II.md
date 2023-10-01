# 137J. Single Number II
https://leetcode.com/problems/single-number-ii/

这个题和137一起学习位运算

## 位运算法最佳
记那个单独的数为 x。
核心的想法是，计算二进制每一位的和。如果某一位能被三除尽而没有余数，那么 x 的二进制表示的对应的位一定是 0.
如果被三除余一，那么 x 的二进制表示的对应的位一定是 1.
这样就能得到 x 的二进制表示。

```java
class Solution {
    public int singleNumber(int[] nums) {
        int[] counts = new int[32];
        for(int n : nums){
            for(int i = 0; i < 32; i++){
                if( ( (n >>> i) & 1) == 1 ){
                    counts[i]++;
                }
            }
        }
        int res = 0;
        for(int i = 31; i >= 0; i--) {
            res <<= 1; // 注意这里必须要放置于 if 之前，否则是错的，因为最后一位计算完之后不应当移位了
            if( counts[i] % 3 == 1){
                res |= 1;
            }
        }
        return res;
    }
}
```

## 136 137 260 通用方法 HashMap 不错的

```Java
class Solution {
    public int singleNumber(int[] nums) {
        Map<Integer,Integer> myMap = new HashMap<>();

        for(int n : nums){
            if(myMap.containsKey(n)){
                int temp = myMap.get(n) + 1;
                myMap.put(n,temp);
            }else{
                myMap.put(n,1);
            }
        }

        for(int n:nums){
            if(myMap.get(n) == 1){
                return n;
            }
        }
        return 0;
    }
}
```
