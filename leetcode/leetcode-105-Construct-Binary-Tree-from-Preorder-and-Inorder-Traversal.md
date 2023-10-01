# 105J. Construct Binary Tree from Preorder and Inorder Traversal

https://leetcode.com/problems/construct-binary-tree-from-preorder-and-inorder-traversal/

## Method 1: 朴素

搞明白 preorder 的第一个是 root,
然后 inorder 数组中找到 root 的位置， 那么
root 左侧的子数组，都在 root 的左侧的子树，而 root 右侧的子数组都在 root 右侧的子树。

```java
class Solution {
    public TreeNode buildTree(int[] preorder, int[] inorder) {
        if(preorder.length == 0 || inorder.length == 0) return null;
        int rootVal = preorder[0];
        TreeNode root = new TreeNode(rootVal);

        int i = 0;
        for(i = 0; i < inorder.length; i++){
            if(inorder[i] == rootVal){
                break;
            }
        }

        root.left = buildTree(Arrays.copyOfRange(preorder,1,1+i),
                          Arrays.copyOfRange(inorder,0,i));
        root.right = buildTree(Arrays.copyOfRange(preorder,1+i,preorder.length),
                           Arrays.copyOfRange(inorder,i+1,inorder.length));
        return root;
    }
}
```

## 提升

方法是用 HashMap 缓存住，增加查找的速度

```java
class Solution {
    // Map of InOrder
    private HashMap<Integer, Integer> inMap = new HashMap<>();

    public TreeNode buildTree(int[] preorder, int[] inorder) {
        for(int i = 0; i < inorder.length; i++){
            inMap.put(inorder[i],i);
        }
        return helper(preorder, 0, preorder.length - 1, inorder, 0, inorder.length - 1);
    }

    public TreeNode helper(int[] preorder, int preA, int preB, int[] inorder,int inA, int inB){
        if(preA > preB || inA > inB) return null;
        TreeNode root = new TreeNode(preorder[preA]);
        int i = inMap.get(root.val);
        int inILeft = i - inA;
        int inIRight = inB - i;

        root.left =
            helper(preorder,preA + 1, preA + inILeft,inorder, inA , i - 1);
        root.right =
            helper(preorder,preA + 1 + inILeft, preA + 1 + inILeft + inIRight,inorder, i + 1, inB);
        return root;
    }
}
```

以上的代码中，inIleft + inIRight = inB - inA, 可见有冗余的参数。

下面的代码是我后面一次写的 更便于理解；
原理是 从 preorder 里找到 root 并在 inorder 中定位它。从 inorder 里可以知道, 这个节点左侧和右侧各有多少个节点，因此可以反过来在 preorder 中定位左子树的节点们和右子树的节点们对应的 preorder subarray.

```java
class Solution {
    private int[] preorder;
    private int[] inorder;
    private Map<Integer, Integer> map;
    public TreeNode buildTree(int[] preorder, int[] inorder) {
        this.map = new HashMap<>();
        this.preorder = preorder;
        this.inorder = inorder;
        for(int i = 0; i < inorder.length; i++){
            map.put(inorder[i], i);
        }
        return helper(0, preorder.length - 1, 0, inorder.length - 1);
    }
    private TreeNode helper(int preL, int preR, int inL, int inR){
        if(preL > preR || inL > inR) return null;
        TreeNode curRoot = new TreeNode(preorder[preL]);
        int inPivot = map.get(preorder[preL]);
        int totalNodesInLeftSubTree = inPivot - inL;
        int totalNodesInRightSubTree = inR - inPivot;
        int leftSubTreePreorderLeftIndex = preL + 1;
        int leftSubTreePreorderRightIndex = preL + totalNodesInLeftSubTree;
        int rightSubTreePreorderLeftIndex = leftSubTreePreorderRightIndex + 1;
        int rightSubTreePreorderRightIndex = leftSubTreePreorderRightIndex + totalNodesInRightSubTree;
        curRoot.left = helper(leftSubTreePreorderLeftIndex, leftSubTreePreorderRightIndex, inL, inPivot - 1);
        curRoot.right = helper(rightSubTreePreorderLeftIndex, rightSubTreePreorderRightIndex, inPivot + 1, inR);

        return curRoot;
    }
}
```
