# 077J Combinations
https://leetcode.com/problems/combinations/


乞丐版BackTracking

## Backtracking

```java
class Solution {
    private List<List<Integer>> ans;
    private List<Integer> temp;
    private int N;
    public List<List<Integer>> combine(int n, int k) {
        this.ans = new LinkedList<>();
        this.temp = new LinkedList<>();
        this.N = n;
        thisIteration(1, k);
        return ans;
    }
    private void thisIteration(int idx, int k ){
        if( k == 0){
            ans.add(new LinkedList<>(temp));
            return;
        }
        for(int i = idx; i < N + 1; i++){
            temp.add(i);
            thisIteration(i + 1, k - 1);
            temp.remove(temp.size() - 1);
        }
    }
}
```


```java
class Solution {
    private List<List<Integer>> ans;
    private List<Integer> cur;
    public List<List<Integer>> combine(int n, int k) {
        this.ans = new ArrayList<>();
        this.cur = new ArrayList<>();
        helper(1, n, k);
        return ans;
    }
    private void helper(int start, int end, int k) {
        if (k == 0) {
            ans.add(new ArrayList<>(cur));
            return;
        }
        for (int i = start; i <= end; i++) {
            cur.add(i);
            helper(i + 1, end, k - 1);
            cur.remove(cur.size() - 1);
        }
    }
}
```