```java
class Solution {
    public int minimumSwaps(int[] nums) {
        int[] smallest = new int[2];
        int[] largest = new int[2];
        smallest[0] = Integer.MAX_VALUE;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] < smallest[0]) {
                smallest[0] = nums[i];
                smallest[1] = i;
            }
            if (nums[i] >= largest[0]) {
                largest[0] = nums[i];
                largest[1] = i;
            }
        }
        if (largest[1] < smallest[1]) {
            return smallest[1] + (nums.length - 1 - largest[1]) - 1;
        }
        return smallest[1] + (nums.length - 1 - largest[1]);
    }
}
```
