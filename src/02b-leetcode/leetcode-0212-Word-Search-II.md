# 212 Word-Search-II 
 
difficulty: Hard 
 
<style>
        section pre{
          background-color: #eee;
          border: 1px solid #ddd;
          padding:10px;
          border-radius: 5px;
        }
      </style>
<section>
<div><p>Given a 2D board and a list of words from the dictionary, find all words in the board.</p>
<p>Each word must be constructed from letters of sequentially adjacent cell, where "adjacent" cells are those horizontally or vertically neighboring. The same letter cell may not be used more than once in a word.</p>
<p>&nbsp;</p>
<p><strong>Example:</strong></p>
<pre><strong>Input:</strong> 
<b>board </b>= [
  ['<span style="color:#d70">o</span>','<span style="color:#d70">a</span>','a','n'],
  ['e','<span style="color:#d30">t</span>','<span style="color:#d00">a</span>','<span style="color:#d00">e</span>'],
  ['i','<span style="color:#d70">h</span>','k','r'],
  ['i','f','l','v']
]
<b>words</b> = <code>["oath","pea","eat","rain"]</code>
<strong>Output:&nbsp;</strong><code>["eat","oath"]</code>
</pre>
<p>&nbsp;</p>
<p><b>Note:</b></p>
<ol>
	<li>All inputs are consist of lowercase letters <code>a-z</code>.</li>
	<li>The values of&nbsp;<code>words</code> are distinct.</li>
</ol>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    private class Node{
        Map<Character, Node> children;
        boolean isWord;
        String str;
        public Node(boolean isWord){
            this.isWord = isWord;
            this.children = new HashMap<>();
            this.str = "";
        }
    }
    private Node root;
    private char[][] board;
    private Set<String> ans;
    private final int[][] directions = new int[][]{{0,1}, {1,0}, {0, -1}, {-1, 0}};
    
    public List<String> findWords(char[][] board, String[] words) {
        this.board = board;
        this.root = new Node(false);
        this.ans = new HashSet<>();
        
        for( String word : words ){
            Node cur = root;
            for(char c : word.toCharArray()){
                if( ! cur.children.containsKey(c) ){
                    cur.children.put(c, new Node(false));
                }
                cur = cur.children.get(c);
            }
            cur.isWord = true;
            cur.str = word;
        }
        

        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board[0].length; j++){
                if(!root.children.containsKey(board[i][j])){
                    continue;
                }
                boolean[][] marked = new boolean[board.length][board[0].length];
                marked[i][j] = true;
                Node cur = root.children.get(board[i][j]);
                dfs(cur, marked, i, j);
            }
        }
        return new ArrayList<>(ans);
    }
    
    private void dfs(Node cur, boolean[][] marked, int row, int col){
        if( cur.isWord == true ){
            ans.add(cur.str);
        }
        for(int[] direction : directions ){
            int r = row + direction[0];
            int c = col + direction[1];
            if( r < 0 || c < 0 || r >= marked.length || c >= marked[0].length ){
                continue;
            }
            if( marked[r][c] ){
                continue;
            }
            if( !cur.children.containsKey( board[r][c] ) ){
                continue;
            }
            marked[r][c] = true;
            Node next = cur.children.get( board[r][c] );
            dfs(next, marked, r, c);
            marked[r][c] = false;
        }
    }
}
/*
这个问题 我在这个例子上翻车过。
Input:
[["a","b"],["a","a"]]
["aba","baa","bab","aaab","aaa","aaaa","aaba"]
Output:
["aaa","aba","aaba","baa"]
Expected:
["aaa","aaab","aaba","aba","baa"]
 
原因就在于这个，如果找到了某个word, 还是应当继续搜索的！
if( cur.isWord == true ){
    ans.add(cur.str);
    return;
}
        
*/
```


上面的方法优化了哪里呢？
其实是利用了 Trie， 但是又没有用好。举个例子，本来我们需要搜索完整个图才能断定是不是没有别的词儿了, 
但是由于利用 Trie 我们可以在 prefix 不存在 match的时候就 early return. 上面的代码，没有利用 prefix的性能。
加上了prefix不match就 early return的限制之后，下面的代码在leetcode中快了一倍 (400 ms -> 200 ms);



```java
class Solution {
    class TrieNode {
        TrieNode[] children;
        boolean isWord;
        String word;
        public TrieNode() {
            this.children = new TrieNode[26];
            this.isWord = false;
            this.word = "";
        }
    }
    private TrieNode root;
    private char[][] board;
    private int M;
    private int N;
    private boolean[][] marked;
    private Set<String> ans;
    private int[] directions = new int[]{1, 0, -1, 0, 1};

    public List<String> findWords(char[][] board, String[] words) {
        this.board = board;
        this.M = board.length;
        this.N = board[0].length;
        this.ans = new HashSet<>();
        this.root = new TrieNode();
        buildTrie(words);

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                if (root.children[board[i][j] - 'a'] == null) continue;
                TrieNode p = root.children[board[i][j] - 'a'];
                marked = new boolean[M][N];
                marked[i][j] = true;
                dfs(i, j, p);
            }
        }
        return new ArrayList<>(ans);
    }
    private void dfs(int row, int col, TrieNode p) {
        if (p.isWord) ans.add(p.word);
        for (int i = 0; i < 4; i++) {
            int nextRow = row + directions[i];
            int nextCol = col + directions[i + 1];
            if (nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
            if (marked[nextRow][nextCol]) continue;
            char targetChar = board[nextRow][nextCol];
            if (p.children[targetChar - 'a'] == null) continue;
            marked[nextRow][nextCol] = true;
            dfs(nextRow, nextCol, p.children[targetChar - 'a']);
            marked[nextRow][nextCol] = false;
        }
    }

    private void buildTrie(String[] words) {
        for (String word : words) {
            TrieNode p = root;
            for (int i = 0; i < word.length(); i++) {
                char cur = word.charAt(i);
                if (p.children[cur - 'a'] == null) {
                    p.children[cur - 'a'] = new TrieNode();
                }
                p = p.children[cur - 'a'];
                if (i == word.length() - 1) {
                    p.isWord = true;
                    p.word = word;
                }
            }
        }
    }
}

```