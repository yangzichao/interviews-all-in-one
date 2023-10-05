
平平无奇的类Trie解法。这个题反而是那个暴力 hashmap让我震惊了。

```java
class FileSystem {
    class Node {
        int val;
        Map<String, Node> children;
        public Node (int val) {
            this.val = val;
            this.children = new HashMap<>();
        };
    }
    Node root;
    public FileSystem() {
        this.root = new Node(-1);
    }
    
    public boolean createPath(String path, int value) {
        String[] paths = path.split("/");
        Node p = root;
        for (int i = 1; i < paths.length - 1; i++) {
            if (p.children.get(paths[i]) == null) return false;
            p = p.children.get(paths[i]);
        }
        if (p.children.containsKey(paths[paths.length - 1])) return false;
        p.children.putIfAbsent(paths[paths.length - 1], new Node(value));
        return true;
    }
    
    public int get(String path) {
        String[] paths = path.split("/");
        Node p = root;
        for (int i = 1; i < paths.length; i++) {
            if (p.children.get(paths[i]) == null) return -1;
            p = p.children.get(paths[i]);
        }
        return p.val;
    }
}

/**
 * Your FileSystem object will be instantiated and called as such:
 * FileSystem obj = new FileSystem();
 * boolean param_1 = obj.createPath(path,value);
 * int param_2 = obj.get(path);
 */
```



暴力hashmap法。
这个是不是有点儿看出 redis 这种 key value store 和 document store 的区别了
