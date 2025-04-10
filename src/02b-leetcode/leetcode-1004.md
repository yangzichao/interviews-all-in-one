

这题有点儿意思 竟然写错了一下

思路：用queue当个滑窗 

```java
class Solution {
    public int longestOnes(int[] nums, int k) {
        // Method 2: sliding window scan the array
        int max = 0;
        int negative = k;
        Queue<Integer> queue = new ArrayDeque<>();
        for (int num : nums) {
            if (num == 1) {
                queue.offer(num);
                max = Math.max(max, queue.size());
                continue;
            }
            while (!queue.isEmpty() && negative == 0) {
                int element = queue.poll();
                if (element == 0) {
                    negative += 1;
                }
            }
            if (negative > 0) {
                queue.offer(num);
                negative -= 1;
                max = Math.max(max, queue.size()); 
                continue;
            }
        }

        return max;
    }
}
```