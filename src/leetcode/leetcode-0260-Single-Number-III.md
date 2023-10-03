# 260J. Single Number III
https://leetcode.com/problems/single-number-iii/

## Method: Best

```java
class Solution {
    public int[] singleNumber(int[] nums) {
        // 设 这两个 single number 是 x1, x2
        // diff = x1 ^ x2
        // 找 x1 和 x2 binary 的第一个不一样的位，以此把数分成两组
        int diff = 0;
        for(int n : nums){
            diff ^= n;
        }
        int digit = 0;
        while( (diff & 1) == 0 ){
            digit++;
            diff >>>= 1;
        }
        int x1 = 0;
        int x2 = 0;
        for(int n : nums){
            if( ( (n >>> digit) & 1 ) == 0 ){
                x1 ^= n;
            }else{
                x2 ^= n;
            }
        }
        return new int[]{x1, x2};
    }
}
```

## Method: HashMap
```Java
class Solution {
    public int[] singleNumber(int[] nums) {
      Map<Integer,Integer> myMap = new HashMap<>();
        for(int n : nums){
            myMap.put(n,myMap.getOrDefault(n,0) + 1);
        }
        int[] output = new int[2];
        int id = 0;
        for(int item: myMap.keySet()){
            if(myMap.get(item) == 1){
                output[id++] = item;
            }
        }
        return output;
    }
}
```

