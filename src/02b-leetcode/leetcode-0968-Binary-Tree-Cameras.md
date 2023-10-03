# 968J. Binary Tree Cameras

https://leetcode.com/problems/binary-tree-cameras/

这个题很好！

这个几乎想到正确的方法了。
事实上这个题是一个图的题而不是树的题。当然都差不多的。
一个观察就是，从底向上安摄像机是更优秀的。比如一个最底的节点，在
它身上安摄像机，当然不如安在它的父节点上。
那么就要 DFS 进入到最深层先。
为了从保证始终从最下层开始处理，要用 postorder.
用 inorder 为什么不行呢？可以考虑树是一个链表的情况。

直接看代码吧。

```java
class Solution {
    private Set<TreeNode> visited;
    private int ans;
    public int minCameraCover(TreeNode root) {
        this.ans = 0;
        this.visited = new HashSet<>();
        visited.add(null);
        dfs(root, null);
        return ans;
    }
    private void dfs(TreeNode node, TreeNode par){
        if(node == null) return;
        dfs(node.left, node);
        dfs(node.right, node);
        //一个节点的除非自己是root，否则不判断它自己。因为Set中有null,所以所有的叶节点都被排除了。
        //层层往上。其实代码还是能看懂的，也不难。这个题很好。
        if(par==null && !visited.contains(node) || !visited.contains(node.left) || !visited.contains(node.right) ){
            ans++;
            visited.add(node);
            visited.add(par);
            visited.add(node.left);
            visited.add(node.right);
        }
    }
}
```


2022

其实是贪心法。从底向上。
camera 放到 root, leaf, 中间的 node 分别只能覆盖 3, 2, 4 个节点。  
我们倾向于总是放到 leaf 节点的父节点上，并把覆盖到的节点拿掉。  
由于是出节点的之后再操作，因此写法上是 post order. 

helper的判断语句是，如果子节点有没有被capture，那么就需要在这一层放 camera. 
如果都被访问了，但是我是 root, 并且还没有被capture，那么在我这儿多加一个。

```java
class Solution {
    private int totalCameras;
    public int minCameraCover(TreeNode root) {
        this.totalCameras = 0;
        if(root == null) return 0;
        Set<TreeNode> captured = new HashSet<>();
        captured.add(null);
        helper(root, null, captured);
        return totalCameras;
    }

    private void helper(TreeNode node, TreeNode parent, Set<TreeNode> captured){
        if(node == null) return;
        helper(node.left, node, captured);
        helper(node.right, node, captured);
        if( !captured.contains(node.left) || !captured.contains(node.right) || (parent == null && !captured.contains(node))){
            totalCameras++;
            captured.add(node.left);
            captured.add(node.right);
            captured.add(node);
            captured.add(parent);
        }
    }

    private void helper2(TreeNode node, TreeNode parent, Set<TreeNode> captured){ 
        // 这也能行 速度稍微快了一点。
        if(node == null) return;
        helper(node.left, node, captured);
        helper(node.right, node, captured);
        if( !captured.contains(node.left) || !captured.contains(node.right) ){
            totalCameras++;
            captured.add(node.left);
            captured.add(node.right);
            captured.add(node);
            captured.add(parent);
            return;
        }
        if( (parent == null && !captured.contains(node)) ){
            totalCameras++;
            captured.add(node);
            return;
        }
    }
}
```