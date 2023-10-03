# 189J. Rotate Array

观察一个例子就好了

```java
/**
    1 2 3 4 5 6 7 k = 3
    5 6 7 1 2 3 4
*/
```

那么等于先反转数组，然后分别反转 0,k-1 和 k, end。
当然还可以更高级的 不停换位，比如 index:0 -> index:3, index:3 -> index: 6, index:6 -> (6 + k = 3)%7 = index:2;

```java
class Solution {
    public void rotate(int[] nums, int k) {
        int realK = k%nums.length;
        reverse(nums, 0, nums.length - 1);
        reverse(nums, 0, realK - 1);
        reverse(nums, realK, nums.length - 1);
    }
    public void reverse(int[] nums, int start, int end) {
        while( start < end ) {
            swap(nums, start++, end--);
        }
    }
    public void swap(int[] nums, int i , int j ) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }
}
```

错误的cyclic replacement.   
这是因为，很可能数组会分成好几组。    
比如 \[1 2 3 4 5 6\] k = 2, 
调换的话 1 3 5 自成一组循环。    2 4 6 永远不会被调换。
```java
class Solution {
    //这是错误的答案
    public void rotate(int[] nums, int k) {
        if(k == 0) return;
        int candidate = nums[0];
        int index = 0;
        for(int i = 0; i < nums.length; i++){
            int newIndex = (index + k ) % nums.length;
            int temp = nums[newIndex];
            nums[newIndex] = candidate;
            candidate = temp;
            index = newIndex;
        }
    }
}
```

好的答案：
这里用 do while 是因为需要先执行一步，再用 curIndex != i 判断。用 while 直接就无法开始。   我想这就是 do while 的一个意义所在。   
```java
class Solution {
    public void rotate(int[] nums, int k) {
        if(k == 0) return;
        int count = 0;
        for(int i = 0; count < nums.length; i++){
            int candidate = nums[i];
            int curIndex = i;
            do{
                int newIndex = (curIndex + k ) % nums.length;
                int temp = nums[newIndex];
                nums[newIndex] = candidate;
                candidate = temp;
                curIndex = newIndex;
                count++;
            }while(curIndex != i);
        }
    }
}
```

## 2023 用一个 buffer array 一次 AC

```java
class Solution {
    public void rotate(int[] nums, int k) {
        if (nums.length == 0) return;
        int N = nums.length;
        k = k % N;
        if (k == 0) return;
        int[] buffer = new int[k];
        for (int i = N - 1; i >= 0; i--) {
            if (i + k >= N) {
                buffer[(i + k) % N] = nums[i];
            } else {
                nums[i + k] = nums[i];
            }
        }
        for (int i = 0; i < buffer.length; i++) {
            nums[i] = buffer[i];
        }
        return;
    }
}
```