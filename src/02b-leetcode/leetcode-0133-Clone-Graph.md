# 133J. Clone Graph

https://leetcode.com/problems/clone-graph/

这个题和 138 有关

## Method DFS

关键的思想就在于，用 HashMap
在原位就复制一个节点，并且将它固定住。

以下是一个分解步骤

```java
class Node {
    public int val;
    public List<Node> neighbors;

    public Node(int _val) {
        val = _val;
        neighbors = new ArrayList<Node>();
    }
}
*/
class Solution {
    // HashMap 既可以建立新老节点的映射，也可以帮助判断图的遍历。
    private HashMap<Node,Node> oldToNew;

    public Node cloneGraph(Node node) {
        if(node == null) return null;
        oldToNew = new HashMap<Node,Node>();
        // DFS 帮助在每个节点处复制一个节点
        dfs(node);
        // 对于每个节点，我们把它的邻居的关系都复制一遍。
        for(Node oldNode : oldToNew.keySet()){
            //这是这个节点对应的复制节点
            Node newNode = oldToNew.get(oldNode);
            //把它的邻居的复制节点都加到它的邻居列表里
            for(Node nei : oldNode.neighbors){
                newNode.neighbors.add(oldToNew.get(nei));
            }
        }
        return oldToNew.get(node);
    }
    private void dfs(Node node){
        //复制新node
        Node nNode = new Node(node.val);
        //标记原node
        oldToNew.put(node,nNode);
        for(Node n : node.neighbors){
            if(!oldToNew.containsKey(n)){
                dfs(n);
            }
        }
    }
}
```

精简版的代码可以这样

```java
/*
// Definition for a Node.
class Node {
    public int val;
    public List<Node> neighbors;

    public Node(int _val) {
        val = _val;
        neighbors = new ArrayList<Node>();
    }
}
*/
class Solution {
    // HashMap 既可以建立新老节点的映射，也可以帮助判断图的遍历。
    private HashMap<Node,Node> oldToNew;

    public Node cloneGraph(Node node) {
        if(node == null) return null;
        oldToNew = new HashMap<Node,Node>();
        return  dfs(node);
    }
    private Node dfs(Node node){
        // 边界条件，如果访问过，那么就把这个
        // 节点对应的克隆节点返回
        if(oldToNew.containsKey(node)){
            return oldToNew.get(node);
        }
        //复制新node
        Node nNode = new Node(node.val);
        //标记原node
        oldToNew.put(node,nNode);

        // 由于已经有了边界条件，所以直接把邻居都加一遍。
        for(Node n : node.neighbors){
            nNode.neighbors.add(dfs(n)) ;
        }
        return nNode;
    }
}
```

## BFS

```java
class Solution {
    public Node cloneGraph(Node node) {
        if( node == null){
            return null;
        }
        Map<Node, Node> marked = new HashMap<>();
        Queue<Node> bfs = new LinkedList<>();
        bfs.offer(node);
        marked.put(node, new Node(node.val) );
        while(!bfs.isEmpty()){
            Node cur = bfs.poll();
            Node copy = marked.get(cur);
            for(Node next : cur.neighbors){
                // 这里，如果没访问过，我们新创建一个 copy
                // 不论访问过或者没有，我们都要创建一个连接
                if( !marked.containsKey(next) ){
                    marked.put(next, new Node(next.val) );
                    bfs.offer(next);
                }
                copy.neighbors.add(marked.get(next));
            }
        }
        return marked.get(node);
    }
}
```

下面是2022年写的一个答案
```java
class Solution {
    private Map<Node, Node> map;
    public Node cloneGraph(Node node) {
        if(node == null) return null;
        map = new HashMap<>();
        Node newNode = new Node(node.val, new ArrayList<>());
        map.put(node, newNode);
        dfs(node, newNode);
        return newNode;
    }
    private void dfs(Node node, Node newNode){
        if(node == null) return;
        for(Node next : node.neighbors){
             if(map.containsKey(next)) {
                 newNode.neighbors.add(map.get(next));
             }else{
                Node newNext = new Node(next.val, new ArrayList<>());
                newNode.neighbors.add(newNext);
                map.put(next, newNext);
                dfs(next, newNext);
             }
        }
    }
}
```

## 2023 写的 和 2022 的就很像了。


```java

先分析，这个图是无向图，可以有环，所以得先用 map 的 keySet 马克住了，入栈前就马克。
每次进入一个节点，建立的是单向的边，所以这里和传统的 DFS 写法有一点不一样。
首先就是，不管当前节点的子节点有没有被访问过，都得遍历一遍子节点。只是之前被访问过的子节点就不入栈了。

class Solution {
    Map<Node, Node> map;
    public Node cloneGraph(Node node) {
        if (node == null) return null;
        this.map = new HashMap<>();
        map.put(node, new Node(node.val));
        helper(node);
        return map.get(node);
    }
    private void helper(Node node) {
        if (node == null) return;
        Node copied = map.get(node);
        for (Node next : node.neighbors) {
            if (map.containsKey(next)) {
                copied.neighbors.add(map.get(next));
                continue;
            }
            Node nextCopied = new Node(next.val);
            copied.neighbors.add(nextCopied);
            map.put(next, nextCopied);
            helper(next);
        }
    }
}
```



## 2025 年写的 

2025 年的思路最清晰啊。 非常便于理解，很直观。
这么写是利用了题目中的 “connected" 图。联通图。
* 如果不是联通图，那么很显然这么写是不行的。
* 还有就是这个题中的 Map 其实起到了一个图防止成环的作用。

```java
/*
// Definition for a Node.
class Node {
    public int val;
    public List<Node> neighbors;
    public Node() {
        val = 0;
        neighbors = new ArrayList<Node>();
    }
    public Node(int _val) {
        val = _val;
        neighbors = new ArrayList<Node>();
    }
    public Node(int _val, ArrayList<Node> _neighbors) {
        val = _val;
        neighbors = _neighbors;
    }
}
*/

class Solution {
    public Node cloneGraph(Node node) {
        if (node == null) return null;
        Map<Node, Node> oldToNewMap = new HashMap<>();
        helper(oldToNewMap, node);
        return oldToNewMap.get(node);
    }

    private void helper(Map<Node, Node> oldToNewMap, Node node) {
        Node newNode = new Node(node.val);
        oldToNewMap.put(node, newNode);
        for (Node neighbor : node.neighbors) {
            if (!oldToNewMap.containsKey(neighbor)) {
                helper(oldToNewMap, neighbor);
            }
            newNode.neighbors.add(oldToNewMap.get(neighbor));
        }
    }
}

```