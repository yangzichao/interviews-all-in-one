


```java
class Solution {
/**
左右横扫
左扫保证每一个人如果比左边人评分高 得到的candy多
右扫保证每一个人如果比右边人评分高 得到的candy多
 */
    public int candy(int[] ratings) {
        int N = ratings.length;
        int[] candys = new int[N];
        Arrays.fill(candys, 1);

        for (int i = 1; i < N; i++) {
            if (ratings[i] > ratings[i - 1]) {
                candys[i] = candys[i - 1] + 1;
            }
        }

        for (int i = N - 2; i >= 0; i--) {
            if (ratings[i] > ratings[i + 1]) {
                candys[i] = Math.max(candys[i], candys[i + 1] + 1);
            }
        }

        int sum = 0;
        for (int can : candys) sum += can;
        return sum;
    }
}
```