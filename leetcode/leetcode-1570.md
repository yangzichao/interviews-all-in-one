稀疏向量问题

```java
class SparseVector {
    Map<Integer, Integer> nonZeroMap;
    SparseVector(int[] nums) {
        this.nonZeroMap = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            nonZeroMap.put(i, nums[i]);
        }
    }
    
	// Return the dotProduct of two sparse vectors
    public int dotProduct(SparseVector vec) {
        int product = 0;
        for (int key : nonZeroMap.keySet()) {
            int v1 = nonZeroMap.get(key);
            int v2 = vec.nonZeroMap.getOrDefault(key, 0);
            product += v1 * v2;
        }
        return product;
    }
}

// Your SparseVector object will be instantiated and called as such:
// SparseVector v1 = new SparseVector(nums1);
// SparseVector v2 = new SparseVector(nums2);
// int ans = v1.dotProduct(v2);
```