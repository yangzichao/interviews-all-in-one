# LeetCode 题目, leetcode 复习组合拳
总的来说，复习一下 162, 33, 81, 153, 154, 4, 658 就差不多了。

#### 基础练习
* [035: Search-Insert-Position](https://leetcode.com/problems/search-insert-position/)      
* [704: Binary-Search](https://leetcode.com/problems/binary-search/)     
* [374: Guess-Number-Higher-or-Lower](https://leetcode.com/problems/guess-number-higher-or-lower/)     
* [278: First-Bad-Version](https://leetcode.com/problems/first-bad-version/)     

#### 仍然是基础练习
* [069: Sqrt(x)](https://leetcode.com/problems/sqrtx/)           
    这个题的特点是，sqrt(8) 需要返回 2. 按照我们的模板，left 是应当存在的位置，是 3，所以我们返回 right。   
    * [367: Valid-Perfect-Square](https://leetcode.com/problems/valid-perfect-square/)         
    367 和 69 一样，更没有难度了      
* [050: Pow(x,-n)](https://leetcode.com/problems/powx-n/)     
    50 其实都不算是个 binary search 了。 
* [074: Search-a-2D-Matrix](https://leetcode.com/problems/search-a-2d-matrix/)    
* [240: Search-a-2D-Matrix-II](https://leetcode.com/problems/search-a-2d-matrix-ii/)
    240用binary search 不是最佳，但是和074放一起比较好。它是一个非常好的题！
    
#### 有点难度
* [034: Find-First-and-Last-Position-of-Element-in-Sorted-Array](https://leetcode.com/problems/find-first-and-last-position-of-element-in-sorted-array/)
* [744: Find-Smallest-Letter-Greater-Than-Target](https://leetcode.com/problems/find-smallest-letter-greater-than-target/)   
    744 是简化版的 34 题
* [702: Search-in-a-Sorted-Array-of-Unknown-Size](https://leetcode.com/problems/search-in-a-sorted-array-of-unknown-size/)         
    702 也是简化版的 34 题 但是702很没意思
    
* [875. Koko Eating Bananas](https://leetcode.com/problems/koko-eating-bananas/description/)
 这个题很棒 很有意思 相似的还有774
* [162: Find-Peak-Element](https://leetcode.com/problems/find-peak-element/)   
* [852: Peak-Index-in-a-Mountain-Array](https://leetcode.com/problems/peak-index-in-a-mountain-array/)    
* [540]() leetcode 540 也是一道非常好的题
* [1539]() 虽然是一个 easy 但是也是一个非常好的 binary search 的题目
* [1891](https://leetcode.com/problems/cutting-ribbons/description/) 就是大名鼎鼎的 cut ribbon 了 一定要做

#### Rotated Sorted Array:
* [033: Search-in-Rotated-Sorted-Array](https://leetcode.com/problems/search-in-rotated-sorted-array/)    
* [081: Search-in-Rotated-Sorted-Array-II](https://leetcode.com/problems/search-in-rotated-sorted-array-ii/)  
* [153: Find-Minimum-in-Rotated-Sorted-Array](https://leetcode.com/problems/find-minimum-in-rotated-sorted-array/)
* [154: Find-Minimum-in-Rotated-Sorted-Array-II](https://leetcode.com/problems/find-minimum-in-rotated-sorted-array-ii/)

[中文Leetcode 上对旋转排序数组的总结](https://leetcode-cn.com/problems/find-minimum-in-rotated-sorted-array-ii/solution/zong-jie-xuan-zhuan-shu-zu-ni-hui-fa-xian-dai-ma-d/)


#### 超级难
* [004: Median-of-Two-Sorted-Arrays](https://leetcode.com/problems/median-of-two-sorted-arrays/)
* [658: Find-K-Closest-Elements](https://leetcode.com/problems/find-k-closest-elements/)    
* [378: Kth-Smallest-Element-in-a-Sorted-Matrix](https://leetcode.com/problems/kth-smallest-element-in-a-sorted-matrix/)      
378 绝对是一道神题！太屌了！
这都能binary search?    
https://leetcode.com/problems/kth-smallest-element-in-a-sorted-matrix/discuss/85173/Share-my-thoughts-and-Clean-Java-Code

#### 跟Binary Search沾边的 dp
* [410: Split-Array-Largest-Sum](https://leetcode.com/problems/split-array-largest-sum/)

#### 峰值查找的讲座 
Have a look at MIT 6006's 1st lecture on 'Algorithmic thinking and Peak Finding'(timestamp for divide and conquer strategy for peak finding : 27.40 min). You will understand the correctness of the algorithm(binary search in peak finding). Lecturer also showed the master theorem for this also.Never miss the 2D version of peak finding in the lecture.
https://www.youtube.com/watch?v=HtSuA80QTyo&list=PLUl4u3cNGP61Oq3tWYp6V_F-5jb5L2iHb&index=2&t=0s

### 面经中收集的题目
* [Cut Ribbon](https://leetcode.com/discuss/interview-question/388503/Google-or-Phone-Screen-or-Cut-Ribbon/350993)   
 (Given an array of integers with elements representing lengths of ribbons. Your goal is to obtain k ribbons of equal length cutting the ribbons into as many pieces as you want. Find the maximum integer length L to obtain at least k ribbons of length L)

> Input: arr = [1, 2, 3, 4, 9], k = 5     
> Output: 3      
> Explanation: cut ribbon of length 9 into 3 pieces of length 3, length 4 into two pieces one of which is length 3 and the other length 1,     
> and one piece is already is of length 3. So you get 5 total pieces (satisfying k) and the greatest length L possible which would be 3.   

用Binary Search可以得到一个 N Log N 的解法。
```java
  public static int greatestLength(int[] arr, int k) {
    int max = 0;
    for (int a : arr) {
      max = Math.max(max, a);
    }
    int lo = 1, hi = max;
    while (lo <= hi) {
      int mid = lo + (hi - lo) / 2;
      int len = getLen(arr, mid);
      if (len >= k) { lo = mid + 1; }
      else { hi = mid - 1; }
    }
    return hi;
  }
  private static int getLen(int[] arr, int target) {
    int res = 0;
    for (int a : arr) {
      res += (a / target);
    }
    return res;
  }
```


# 二分查找 
深入理解二分查找的本质，以及对二分查找模板的解析。     

## 二分查找的模板
二分查找有非常多的模板，常见的有三种。但是我们这里只使用一种模板。即 Java Arrays.binarySearch 的模板。
### Java Arrays.binarySearch 的模板
Java 的源代码肯定是最有说服力的。
```java
    private static int binarySearch0(int[] a, int fromIndex, int toIndex,
                                     int key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }
```
如果找的到key,我们就返回它的index。如果没找到，我们返回的index是负数，其绝对值是应该插入的数组的位置，注意，该位置是从1开始计算的，而不是0。  
这样的原因是，0已经被找到的范围所占用了，如果没找到只能从-1开始计数。


Q: 为什么用 (lo + hi) >>> 1 而不是 (lo + hi) / 2;
A: 因为前者避免了 integer overflow. 因为 >>> 是 unsigned bit shift. 具体见：
https://stackoverflow.com/questions/24617747/how-does-using-1-prevent-overflow-when-adding-two-numbers-than-dividing-by-2


## 二分查找的本质 

二分查找，找的是什么？最简化的版本，是给了一个条件，然后根据这个条件得出了一个 boolean 数组，\[true, ..., true, false ... false\]; 
然后我们根据情况返回最后一个true 或者 第一个false。
即找到这个分界之处。（即 二分）  
对这点最典型的问题就是 [278: First-Bad-Version](https://leetcode.com/problems/first-bad-version/)    
所有类似的问题都可以化为这个模式。  
以我们使用的模板为例 low 指向的是第一个 false;    
所以我们事实上只需要根据需要更改这个 criteria的条件就好了。
    
```java
class BinarySearchRealTemplate implements BinarySearchFunction {
    public void binarySearch(int[] nums, int target) {
        if(nums.length < 1) return;
        boolean isFound = false;
        int low = 0;
        int high = nums.length - 1;
        int mid = 0;
        while(low <= high) {
            mid = low + (high - low)/2; 
            // 注意 low + (high - low)/2 不会overflow 但是 (low + high)/2 会
            // 很神奇的是 (low + high) >>> 1 也不会。
            int num = nums[mid];
            boolean whatWeWantToBeTrue = num < target; // 回到一般的情况，
            if(criteria) {
                low = mid + 1;
            }else {
                high = mid - 1;
            }
        }
        if(low > -1 && low < nums.length && nums[low] == target) isFound = true;
        printBinary(nums, low, high , mid, target, isFound);
    }
}
```

根据这个我们来立刻做一个题：
我们有非常多的办法可以写这个题。
* [852: Peak-Index-in-a-Mountain-Array](https://leetcode.com/problems/peak-index-in-a-mountain-array/)
```java
class Solution {
    public int peakIndexInMountainArray(int[] arr) {
        int lo = 0;
        int hi = arr.length - 1;
        while(lo <= hi){
            int mid = (lo + hi) >>> 1;
            boolean whatWeWantToBeTrue = arr[mid] < arr[mid + 1];
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
同理：
* [162: Find-Peak-Element](https://leetcode.com/problems/find-peak-element/)

理解了本质，这个很难的二分查找也可以比较轻松的写了。
* [658: Find-K-Closest-Elements](https://leetcode.com/problems/find-k-closest-elements/)


补充：可能有些情况要涉及处理 等于 的情况。此时就看需要 == 应当属于 true 还是 false.

# Todo:

有空学习一下这整个章节，尤其是第二个模板；   https://leetcode.com/explore/learn/card/binary-search/

有时间看看这个题能不能二分法
https://www.fastprep.io/problems/amazon-get-minimum-boxes