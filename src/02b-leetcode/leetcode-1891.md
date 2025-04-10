
这个题是 Binary Search 非常好的一个题 


```java
class Solution {
    public int maxLength(int[] ribbons, int k) {
        // long sum = 0;
        int maxRibbon = 0;
        for (int ribbon : ribbons) {
            // sum += ribbon;
            maxRibbon = Math.max(maxRibbon, ribbon);
        }
        // if (sum < k) return 0;
        int lo = 1;
        int hi = maxRibbon;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int sumK = 0;
            for (int ribbon : ribbons) {
                sumK += (ribbon / mid);
            }
            if (sumK == k) {
                // note: we must not return mid here
                // we have many true mid value here
                // this is like we are finding the largest among them
                lo = mid + 1; 
            } else if (sumK < k) {
                // means mid is too large, make mid smaller
                hi = mid - 1;
            } else {
                lo = mid + 1;
            }
        }
        return hi;
    }
}

// example: 9 7 5， if k = 3, x is 5; if k = 6, x is 3
// Define: ribbon = ribbons[i]
// for each ribbon / x = y
// sum over y should be >= 10
// binary search is perfect for looking max x
// Time Complexity is O(N log X ) where X is the largest ribbon[i]
```