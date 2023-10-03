

```java
class Solution {
    public void setZeroes(int[][] matrix) {
        // 用第一行和第一列来标注需要标0的列和行 标注的方式无所谓
        // 注意这里不是乱选的，不是说随便选一列和一行就可以
        // 选取第一行和第一列来标注的原因是他们被标记的时候肯定已经访问过了。
        // 然后针对第一行和第一列，我们再用两个 boolean 分别标记。
        int m = matrix.length;
        int n = matrix[0].length;
        boolean shouldFirstRow = false;
        boolean shouldFirstCol = false;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] == 0) {
                    if (i == 0) shouldFirstRow = true;
                    if (j == 0) shouldFirstCol = true;
                    matrix[i][0] = 0;
                    matrix[0][j] = 0;
                }
            }
        }

        for (int i = 1; i < m; i++) {
            if (matrix[i][0] == 0) changeRowOrCol(matrix, i, true);
        }
        for (int j = 1; j < n; j++) {
            if (matrix[0][j] == 0) changeRowOrCol(matrix, j, false);
        }
        if (shouldFirstRow) {
            changeRowOrCol(matrix, 0, true);
        } 
        if (shouldFirstCol) {
            changeRowOrCol(matrix, 0, false);
        } 
    }

    private void changeRowOrCol(int[][] matrix, int index, boolean isRow) {
        if (isRow) {
            for (int j = 0; j < matrix[index].length; j++) {
                matrix[index][j] = 0;
            }
        } else {
            for (int i = 0; i < matrix.length; i++) {
                matrix[i][index] = 0;
            }
        }
    }
}

```