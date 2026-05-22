这个题2025年才第一次写，写的有点儿过于混乱了。

思路是模拟这个pre-order的过程。

hasVal 是为了处理连续的括号


```java
class Solution {
    public TreeNode str2tree(String s) {
        Deque<TreeNode> nodeStack = new ArrayDeque<>();
        int val = 0;
        int sign = 1;
        boolean hasVal = false;
        for (char c : s.toCharArray()) {
            if (c == '-') {
                sign = -1;
                continue;
            }
            if (Character.isDigit(c)) {
                hasVal = true;
                val = val * 10 + (int) (c - '0');
                continue;
            }
            if (c == '(') {
                if (hasVal) {
                    TreeNode newNode = new TreeNode(sign * val);
                    sign = 1;
                    val = 0;
                    hasVal = false;

                    nodeStack.push(newNode);
                } else {

                }
            }
            if (c == ')') {
                if (hasVal) {
                    TreeNode curNode = new TreeNode(sign * val);
                    sign = 1;
                    val = 0;
                    hasVal = false;

                    TreeNode parent = nodeStack.pop();
                    if (parent.left == null) {
                        parent.left = curNode;
                    } else {
                        parent.right = curNode;
                    }
                    nodeStack.push(parent);
                } else {
                    TreeNode curNode = nodeStack.pop();
                    TreeNode parent = nodeStack.pop();
                    if (parent.left == null) {
                        parent.left = curNode;
                    } else {
                        parent.right = curNode;
                    }
                    nodeStack.push(parent);
                }
            }
        }
        if (hasVal) {
            return new TreeNode(sign * val);
        }
        return nodeStack.isEmpty() ? null : nodeStack.pop();
    }
}



```
