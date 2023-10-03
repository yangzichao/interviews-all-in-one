
这个是最好的 1d union find 的题了

```java
class Solution {
    private int numOfPieces;
    public int earliestAcq(int[][] logs, int n) {
        Arrays.sort(logs, (a, b) -> (a[0] - b[0]));
        int[] roots = new int[n];
        this.numOfPieces = n;
        for (int i = 0; i < n; i++) {
            roots[i] = i;
        }
        for (int[] log : logs) {
            union(roots, log[1], log[2]);
            if (numOfPieces == 1) {
                return log[0];
            }
        }
        return -1;
    }

    private void union(int[] roots, int i, int j) {
        int rootA = findRoot(roots, i);
        int rootB = findRoot(roots, j);
        if (rootA != rootB) {
            this.numOfPieces -= 1;
        }
        roots[rootB] = rootA;
        findRoot(roots, j);
    }

    private int findRoot(int[] roots, int i) {
        if (roots[i] == i) return i;
        roots[i] = findRoot(roots, roots[i]);
        return roots[i];
    }
}
```