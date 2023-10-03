

这个题模拟stack 顺序 但是还可以更好

```java
class Solution {
    public boolean validateStackSequences(int[] pushed, int[] popped) {
        // Try to Simulate stack
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        int popIndex = 0;
        for (int i = 0; i < pushed.length; i++) {
            stack.push(pushed[i]);
            while (!stack.isEmpty() && stack.peek() == popped[popIndex]) {
                stack.pop();
                popIndex++;
            }
        }
        return popIndex == popped.length;
    }
}
```