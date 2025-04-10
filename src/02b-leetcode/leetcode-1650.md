这个题其实很简单，但是思维可能会卡住。
首先这个题由于提供了父节点，大大简化了问题。挑战在于，没有提供根节点。
解法1就是从一个节点网上找，全部记录，再从另一个往上找，第一个遇到的节点就是lca.
```java
class Solution {
    public Node lowestCommonAncestor(Node p, Node q) {
        Set<Node> pPath = new HashSet<>();
        Node a = p, b = q;
        while (a != null) {
            pPath.add(a);
            a = a.parent;
        }
        while (b != null) {
            if (pPath.contains(b)) return b;
            b = b.parent;
        }
        return null;
    }
}
```

解法 2 可以参考两个linkedlist找交叉点。

即 160 https://leetcode.com/problems/intersection-of-two-linked-lists/description/
非常的awesome


```java
class Solution {
    public Node lowestCommonAncestor(Node p, Node q) {
        Node a = p;
        Node b = q;
        while (a != b) {
            a = a == null? q : a.parent;
            b = b == null? p : b.parent;  
        }
        return a;
    }
}
```