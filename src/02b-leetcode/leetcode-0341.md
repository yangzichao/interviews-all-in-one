
下面的是我第一次写的解法，很快，也很好写，也很对。
但是受到了一点启发：In my opinion an iterator shouldn't copy the entire data (which some solutions have done) but just iterate over the original data structure.

```java
/**
 * // This is the interface that allows for creating nested lists.
 * // You should not implement it, or speculate about its implementation
 * public interface NestedInteger {
 *
 *     // @return true if this NestedInteger holds a single integer, rather than a nested list.
 *     public boolean isInteger();
 *
 *     // @return the single integer that this NestedInteger holds, if it holds a single integer
 *     // Return null if this NestedInteger holds a nested list
 *     public Integer getInteger();
 *
 *     // @return the nested list that this NestedInteger holds, if it holds a nested list
 *     // Return empty list if this NestedInteger holds a single integer
 *     public List<NestedInteger> getList();
 * }
 */
public class NestedIterator implements Iterator<Integer> {
    private List<Integer> list;
    private int cur;
    public NestedIterator(List<NestedInteger> nestedList) {
        this.list = new ArrayList<>();
        for (NestedInteger nestedInteger : nestedList) {
            flatten(nestedInteger);
        }
        this.cur = -1;
    }
    
    @Override
    public Integer next() {
        if (hasNext()) {
            return list.get(++cur);
        }
        return -1;
    }

    @Override
    public boolean hasNext() {
        return cur + 1 < list.size();
    }

    private void flatten (NestedInteger nestedList) {
        if (nestedList.isInteger()) {
            list.add(nestedList.getInteger());
            return;
        }
        for (NestedInteger nestedInteger : nestedList.getList()) {
            flatten(nestedInteger);
        }
    }
}

/**
 * Your NestedIterator object will be instantiated and called as such:
 * NestedIterator i = new NestedIterator(nestedList);
 * while (i.hasNext()) v[f()] = i.next();
 */
```