# 162J. Find Peak Element

https://leetcode.com/problems/find-peak-element/

```
峰值这个题最关键的一点就是证明 binary search 在这里是可行的。
由于 nums[-1] = nums[nums.length] = - \infity 
我们只要发现  nums[i] < nums[i + 1] 就有 峰值在 i 的右侧
只要发现 nums[i] > nums[i + 1] 就有峰值在 i + 1 左侧 
```
## Method 普通

这是完全普通按照定义来的，
由于只需要返回任意一个峰值，所以很简单。

```Java
class Solution {
    public int findPeakElement(int[] nums) {
        if(nums.length < 2){
            return 0;
        }
        List<Integer> list = new ArrayList<>();
        for(int i = 1; i < nums.length - 1; i++){
            if(nums[i] > nums[i-1] && nums[i] > nums[i+1]){
                return i;
            }
        }
        return nums[0] > nums[1] ? 0:nums.length - 1;
    }
}
```

## Method Best: 二分法查找

下面这个为什么不行？
因为取 pivot - 1, pivot + 1。edge case 会太多。

```Java
class Solution {
    public int findPeakElement(int[] nums) {
        if(nums.length < 2){
            return 0;
        }
        int left = 0;
        int right = nums.length - 1;
        int pivot = 0;
        while(left <= right){
            pivot = left + (right - left)/ 2;
            if( nums[pivot] > nums[pivot - 1] && nums[pivot] > nums[pivot + 1]){
                return pivot;
            }else if( nums[pivot] > nums[pivot - 1] && nums[pivot] < nums[pivot + 1] ){
                left = pivot + 1;
            }else{
                right = pivot - 1;
            }
        }

        return left;
    }
}
```

可行的代码如下

```Java
class Solution {
    public int findPeakElement(int[] nums) {
        if(nums.length < 2){
            return 0;
        }
        int left = 0;
        int right = nums.length - 1;
        int pivot = 0;
        while(left < right){
            pivot = left + (right - left)/ 2;

            if(nums[pivot] > nums[pivot + 1]){
                 // 如果 pivot 比 pivot + 1 大，说明左侧有极大
                // 分割区域，包含 pivot
                right = pivot;
            }else{
                // 反之说明右侧有极大，
                // 分割区域，不包含 pivot.
                left = pivot + 1;
            }
        }
        //用 left < right作为判据 最终 left = right
        // 这里由于是找极大，和一般的找特定值不同。
        return right;
    }
}
```

同样的思想，也可以这么写

```Java
class Solution {
    public int findPeakElement(int[] nums) {
        if(nums.length < 2){
            return 0;
        }
        int left = 0;
        int right = nums.length - 1;
        int pivot = 0;
        while(left < right){
            pivot = left + (right - left)/ 2;

            if(nums[pivot] <  nums[pivot + 1]){
                left = pivot + 1;
            }else{
                right = pivot;
            }
        }

        return right;
    }
}
```

看看 2022 最新总结的思想：

```java
class Solution {
    public int findPeakElement(int[] nums) {
        int lo = 0;
        int hi = nums.length - 1;
        while(lo <= hi){
            int mid = (hi + lo) >>> 1;
            boolean whatWeWantToBeTrue = mid + 1 == nums.length ? false : nums[mid] < nums[mid + 1];
            if(whatWeWantToBeTrue){
                lo = mid + 1;
            }else{
                hi = mid - 1;
            }
        }
        return lo;
    }
}
```

## 2023 
基于上述的总结：

峰值这个题最关键的一点就是证明 binary search 在这里是可行的。
由于 nums[-1] = nums[nums.length] = - \infity 
我们只要发现  nums[i] < nums[i + 1] 就有 峰值在 i 的右侧
只要发现 nums[i] > nums[i + 1] 就有峰值在 i + 1 左侧 

既可以 令 nums[mid] < nums[mid + 1] 然后 left = mid + 1


```java
class Solution {
    public int findPeakElement(int[] nums) {
        int left = 0;
        int right = nums.length - 1;
        while (left <= right) {
            int mid = (left + right) >>> 1;
            // [T,T,T,T,F,F,F,F], T 是 上升。找到的是第一个 F, return left
            boolean whatWeWantToBeTrue = mid == nums.length - 1 ? false : nums[mid] < nums[mid + 1];
            if (whatWeWantToBeTrue) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return left;
    }
}
```

也可以 令 nums[mid - 1] > nums[mid] 然后 right = mid - 1

```java
class Solution {
    public int findPeakElement(int[] nums) {
        int left = 0;
        int right = nums.length - 1;
        while (left <= right) {
            int mid = (left + right) >>> 1;
            // [F,F,F,F,T,T,T,T], T 是 下降。找到的是第一个 T 之前的 F, return right
            boolean whatWeWantToBeTrue = mid == 0 ? false : nums[mid - 1] > nums[mid];
            if (whatWeWantToBeTrue) {
                right = mid - 1;
            } else {
                left = mid + 1;     
            }
        }
        return right;
    }
}
```

你看上面两个写法都是对的。