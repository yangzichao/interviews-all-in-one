
## 2025

far from ideal

```java
class Solution {
    public boolean isToeplitzMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            if (!checkOneDiagonal(matrix, i, 0)) return false;
        }
        for (int j = 0; j < matrix[0].length; j++) {
            if (!checkOneDiagonal(matrix, 0, j)) return false;
        }
        return true;
    }

    private boolean checkOneDiagonal(int[][] matrix, int i, int j) {
        int cur = matrix[i][j];
        while (i < matrix.length && j < matrix[0].length) {
            if (matrix[i][j] != cur) return false;
            i += 1;
            j += 1;
        }
        return true;
    }
}
```