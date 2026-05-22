

我突然意识到这个题其实还不错
核心就是让你去trim原本的 dfs，参考下面的dfs: 
时间复杂度更好一些，虽然 Big O 不变

```java
class Solution {
    public int rangeSumBST(TreeNode root, int low, int high) {
        if (root == null) return 0;
        int total = 0;
        if (root.val >= low) {
            total += rangeSumBST(root.left, low, high);
        }

        if (root.val >= low && root.val <= high) {
            total += root.val;
        }

        if (root.val <= high) {
            total += rangeSumBST(root.right, low, high);
        }

        return total;
    }
}
```


iterative 的方法

```java
class Solution {
    public int rangeSumBST(TreeNode root, int low, int high) {
        Deque<TreeNode> stack = new ArrayDeque<>();
        int sum = 0;
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode cur = stack.pop();
            if (cur.val <= high && cur.val >= low) {
                sum += cur.val;
            }
            if (cur.left != null && cur.val > low) {
                stack.offer(cur.left);
            }
            if (cur.right != null && cur.val < high) {
                stack.offer(cur.right);
            }
        }
        return sum;
    }
}
```