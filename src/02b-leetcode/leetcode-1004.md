

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

这个题真是不太好写，很容易写错，第一次也不知道怎么写的。

核心思想是：滑窗滑到里面有至多 k - 1 个 0 为止。

```java
class Solution {
    public int longestOnes(int[] nums, int k) {
        Queue<Integer> queue = new ArrayDeque<>();
        int count = k;
        int max = 0;
        for (int num : nums) {
            if (num == 1) {
                queue.offer(num);
            } else {
                while (!queue.isEmpty() && count == 0) {
                    int cur = queue.poll();
                    if (cur == 0) {
                        count += 1;
                    }
                }
                if (count > 0) {
                    count--;
                    queue.offer(num);
                }
            }

            max = Math.max(max, queue.size());
        }
        return max;
    }
}
```