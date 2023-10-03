
这个题和 two sum 几乎一模一样有木有。

这个题的重点是，加起来的和，取模modulus 60，所以我们可以直接只记录模60的之后的值的数量。

唯一的例外是，如果这个数本身就 是60的整数倍，我们需要查找的是 0 的数量。

```java
class Solution {
    public int numPairsDivisibleBy60(int[] time) {
        Map<Integer, Integer> map = new HashMap<>();
        int count = 0;
        for(int i = 0; i < time.length; i++){
            if(time[i] % 60 == 0){
                count += map.getOrDefault(0, 0);
            }else{
                count += map.getOrDefault(60 - time[i] % 60, 0);
            }
            map.put(time[i] % 60, map.getOrDefault(time[i] % 60, 0) + 1);
        }
        return count;
    }
}
```