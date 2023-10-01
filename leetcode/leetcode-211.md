


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