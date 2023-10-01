这个题edge case 挺多的，不过需要记住一点就是他有个设定是 
lower < nums_i < higher
228 是一个和他相似的题。

```java
class Solution {
    public List<List<Integer>> findMissingRanges(int[] nums, int lower, int upper) {
        List<List<Integer>> ans = new ArrayList<>();
        int lowest = lower;
        for (int num : nums) {
            if (lowest < num) {
                List<Integer> temp = new ArrayList<>();
                temp.add(lowest);
                temp.add(num - 1 > lowest ? num - 1 : lowest);
                ans.add(temp);
            }
            if (num == upper) {
                return ans;
            }
            lowest = num + 1;
        }
        if (lowest <= upper) {
            List<Integer> temp = new ArrayList<>();
            temp.add(lowest);
            temp.add(upper > lowest ? upper : lowest);
            ans.add(temp);
        }
        return ans;
    }
}
```

2023 年又一次写的，要流畅的多了看起来。

```java
class Solution {
    public List<List<Integer>> findMissingRanges(int[] nums, int lower, int upper) {
        List<List<Integer>> ans = new ArrayList<>();
        for (int num : nums) {
            if (num - lower >= 1) {
                ans.add(getRange(lower, num - 1));
            }
            lower = num + 1;
        }
        if (upper >= lower) {
            ans.add(getRange(lower, upper));
        }
        return ans;
    }

    private List<Integer> getRange(int a, int b) {
        return Arrays.asList(new Integer[]{a, b});
    }
}
```