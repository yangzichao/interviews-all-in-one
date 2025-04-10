


```java
class Solution {
    public int maximumSwap(int num) {
        // 思路是，如果我们有每个数字的右侧最大的值这个信息
        // 那么如果这个数字和它本身右侧最大值相同，那么说明不用交换它
        // 我们从头开始，找到的第一个“德不配位”的数，就是第一个swap的candidate: swap1 
        // 然后我们就把 swap1 和 它最右侧的右侧最大值 swap2 交换位置 就可以了
        // 注意一个 edge case 右侧最大值可能存在于多个 index 那么我们找最靠右的
        char[] chars = String.valueOf(num).toCharArray();
        char[] maxRight = new char[chars.length];
        for (int i = chars.length - 1; i >= 0; i--) {
            if (i == chars.length - 1 ) {
                maxRight[i] = chars[i];
                continue;
            }
            maxRight[i] = maxRight[i + 1] > chars[i] ? maxRight[i + 1] : chars[i];
        }
        int swap1 = 0;
        while (swap1 < chars.length && chars[swap1] == maxRight[swap1]) {
            swap1++;
        }
        if (swap1 < chars.length) {
            int swap2 = chars.length - 1;
            while (swap1 < swap2 && chars[swap2] != maxRight[swap1]) {
                swap2--;
            }
            char temp = chars[swap1];
            chars[swap1] = chars[swap2];
            chars[swap2] = temp;
        }

        // parse result
        int sum = 0;
        for (char c : chars) {
            sum = sum * 10 + (int) (c - '0');
        }
        return sum;
    }

}

```