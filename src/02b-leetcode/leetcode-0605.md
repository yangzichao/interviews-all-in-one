
我觉得这是一个蛮不错的 easy 题。



```java
class Solution {
    public boolean canPlaceFlowers(int[] flowerbed, int n) {
        for (int i = 0; i < flowerbed.length; i++) {
            if (flowerbed[i] > 0) continue;
            boolean frontCheck = i == 0 ? true : flowerbed[i - 1] == 0;
            boolean backCheck = i == flowerbed.length - 1 ? true : flowerbed[i + 1] == 0;
            if (frontCheck && backCheck && n > 0) {
                n--;
                flowerbed[i] = 1;
            }
        }
        return n == 0;
    }
}
```