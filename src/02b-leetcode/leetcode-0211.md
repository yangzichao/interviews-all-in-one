


```java
class TrieNode {
    char c;
    boolean isWord;
    TrieNode[] children;
    public TrieNode(){};
    public TrieNode(char c) {
        this.c = c;
        this.isWord = false;
        this.children = new TrieNode[26];
    }
}
class WordDictionary {
    private TrieNode root;

    public WordDictionary() {
        this.root = new TrieNode('@');
    }
    
    public void addWord(String word) {
        TrieNode p = root;
        for (char c : word.toCharArray()) {
            if (p.children[c - 'a'] == null) {
                p.children[c - 'a'] = new TrieNode(c);
            }
            p = p.children[c - 'a'];
        }
        p.isWord = true;
    }
    public boolean search(String word) {
        return searchHelper(word, root);
    }
    
    private boolean searchHelper(String word, TrieNode node) {
        TrieNode p = node;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c == '.') {
                boolean isFound = false;
                for (int j = 0; j < 26; j++) {
                    if (p.children[j] != null) {
                        isFound = isFound || searchHelper(word.substring(i + 1), p.children[j]);
                    }
                }
                return isFound;
            }
            if (p.children[c - 'a'] == null) {
                return false;
            }
            p = p.children[c - 'a'];
        }
        return p.isWord;
    }
}

/**
 * Your WordDictionary object will be instantiated and called as such:
 * WordDictionary obj = new WordDictionary();
 * obj.addWord(word);
 * boolean param_2 = obj.search(word);
 */
 ```




好像Trie 写的确实不太够

```java
class WordDictionary {
    class Node {
       boolean isWord;
       char c;
       Map<Character, Node> map;
       Node(char c) {
        this.c = c;
        this.map = new HashMap<>();
       } 
    }
    Node root;
    public WordDictionary() {
        this.root = new Node('n');
    }
    
    public void addWord(String word) {
        Node cur = root;
        for (char c : word.toCharArray()) {
            if (!cur.map.containsKey(c)) {
                cur.map.put(c, new Node(c));
            }
            cur = cur.map.get(c);
        }
        cur.isWord = true;
    }
    
    public boolean search(String word) {
        return searchHelper(word, 0, root);
    }

    private boolean searchHelper(String word, int index, Node node) {
        if (index == word.length()) return node.isWord;
        char cur = word.charAt(index);
        if (cur == '.') {
            boolean isFound = false;
            for (Node next : node.map.values()) {
                isFound = searchHelper(word, index + 1, next);
                if (isFound) return true;
            }
            return false;
        } 
        if (!node.map.containsKey(cur)) return false;
        return searchHelper(word, index + 1, node.map.get(cur));
    }
}

/**
 * Your WordDictionary object will be instantiated and called as such:
 * WordDictionary obj = new WordDictionary();
 * obj.addWord(word);
 * boolean param_2 = obj.search(word);
 */
```