没啥特别的

```java
/**
 * // This is the interface that allows for creating nested lists.
 * // You should not implement it, or speculate about its implementation
 * public interface NestedInteger {
 *     // Constructor initializes an empty nested list.
 *     public NestedInteger();
 *
 *     // Constructor initializes a single integer.
 *     public NestedInteger(int value);
 *
 *     // @return true if this NestedInteger holds a single integer, rather than a nested list.
 *     public boolean isInteger();
 *
 *     // @return the single integer that this NestedInteger holds, if it holds a single integer
 *     // Return null if this NestedInteger holds a nested list
 *     public Integer getInteger();
 *
 *     // Set this NestedInteger to hold a single integer.
 *     public void setInteger(int value);
 *
 *     // Set this NestedInteger to hold a nested list and adds a nested integer to it.
 *     public void add(NestedInteger ni);
 *
 *     // @return the nested list that this NestedInteger holds, if it holds a nested list
 *     // Return empty list if this NestedInteger holds a single integer
 *     public List<NestedInteger> getList();
 * }
 */
class Solution {
    public int depthSum(List<NestedInteger> nestedList) {
        return depthSumHelper(nestedList, 1);
    }

    public int depthSumHelper(List<NestedInteger> nestedList, int depth) {
        int curSum = 0;
        for (NestedInteger nestedInteger : nestedList) {
            if (nestedInteger.isInteger()) {
                curSum += nestedInteger.getInteger() * depth;
                continue;
            }
            curSum += depthSumHelper(nestedInteger.getList(), depth + 1);
        }
        return curSum;
    }
}

```



# 2025 
2025 傻了 写的不如上面的好
```java
class Solution {
    public int depthSum(List<NestedInteger> nestedList) {
        return depthSumHelper(nestedList, 1);
    }

    private int depthSumHelper(List<NestedInteger> nestedList, int depth) {
        int intSum = 0;
        int subSum = 0;
        for (NestedInteger nestedInt : nestedList) {
            if (nestedInt.isInteger()) {
                intSum += nestedInt.getInteger();
                continue;
            }
            subSum += depthSumHelper(nestedInt.getList(), depth + 1);
        }
        return intSum * depth + subSum;
    }
}
```