
这个题相当的离谱了，类似于 2 sum 的思想。
可能是类似 medium 下最难的 比 560 难




```java
class Solution {
    public boolean checkSubarraySum(int[] nums, int k) {
        // it is easy to think of using prefix prefixSum so that we have O(n^2)
        // This question, since we are calculating mod k, we calculate prefixSum Mod K
        // Just like 2 sum, if we have 2 index has the same Sum Mod K, that means
        // the sum between this 2 index mod K is zero, and we return true immediately

        // Below is correct for most testcases

        // Set<Integer> prefixSumModKSet = new HashSet<>();
        // prefixSumModKSet.add(0);
        // int prefixSumModK = 0;
        // for (int num : nums) {
        //     prefixSumModK = (num + prefixSumModK) % k;
        //     if (prefixSumModKSet.contains(prefixSumModK)) {
        //         return true;
        //     }
        //     prefixSumModKSet.add(prefixSumModK);
        // }
        // return false;

        // However, the issue is that, there is an edge case that:
        // we have element that its mod k is already zero
        // above is only true if we have only positive mod K element.
        // To deal with that we track the index, then goes back to 2 sum

        Map<Integer, Integer> prefixSumModKToIndex = new HashMap<>();
        prefixSumModKToIndex.put(0, -1);
        int prefixSumModK = 0;
        for (int i = 0; i < nums.length; i++) {
            prefixSumModK = (nums[i] + prefixSumModK) % k;
            if (prefixSumModKToIndex.containsKey(prefixSumModK) ) {
                if (i - prefixSumModKToIndex.get(prefixSumModK) > 1) {
                    // > 1 rather than >= 1 since we calculate the subarray length 
                    return true;
                }
                // note if we containsKey we don't update the index, because of modK zero element 
                continue;
            }
            prefixSumModKToIndex.put(prefixSumModK, i);
        }
        return false;
    }
}

```