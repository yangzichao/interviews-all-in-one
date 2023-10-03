
一种实现trie 的简单写法。

```java
class Trie {
    class Node {
        private boolean isWord;
        private Node[] children;
        Node (boolean isWord) {
            this.isWord = false;
            this.children = new Node[26];
        }
    }
    private Node root;
    public Trie() {
        this.root = new Node(false);
    }
    
    public void insert(String word) {
        Node p = root;
        for (char c : word.toCharArray()){
            if (p.children[c - 'a'] == null) {
                p.children[c - 'a'] = new Node(false);
            }
            p = p.children[c - 'a'];
        }
        p.isWord = true;
    }
    
    public boolean search(String word) {
        Node p = root;
        for (char c : word.toCharArray()) {
            if (p.children[c - 'a'] == null) return false;
            p = p.children[c - 'a'];
        }
        return p.isWord;
    }
    
    public boolean startsWith(String prefix) {
        Node p = root;
        for (char c : prefix.toCharArray()) {
            if (p.children[c - 'a'] == null) return false;
            p = p.children[c - 'a'];
        }
        return true;
    }
}

/**
 * Your Trie object will be instantiated and called as such:
 * Trie obj = new Trie();
 * obj.insert(word);
 * boolean param_2 = obj.search(word);
 * boolean param_3 = obj.startsWith(prefix);
 */
```