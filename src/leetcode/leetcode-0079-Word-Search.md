# 079J Word Search

https://leetcode.com/problems/word-search/

这是一个很不一样的回溯。我直觉是 dfs。但是发现应当回溯。
我的答案逻辑是，首先找到一个开始的点。然后 dfs。如果 dfs
每前进一层，能够匹配下一个字符。那么我们就 return true.
反之就 return false. 现在有问题了: dfs 是需要毁灭性的
标记访问路径的。如果一个起点 return false，那么我们需要
想办法退回去的。朴素的思路就是，如果四个方向皆碰壁，那么就
意味着没有一条路是通顺的。此时我们应当回退。

这个题之所以写的比起典型的回溯要不太一样。是因为典型的回溯的图一定需要遍历
全局。而这个题并不需要。我们只需要尝试全部的起点就可以了。

```java
class Solution {
    private int L; // word.length()
    private int M;
    private int N;
    public boolean exist(char[][] board, String word) {
        this.L = word.length();
        // if(word.length == 0) return true;
        this.M = board.length;
        if(M < 1) return false;
        this.N = board[0].length;
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                if(dfs(board, word, i, j, 0)){
                    return true;
                }
            }
        }
        return false;
    }
    private boolean dfs(char[][] board, String word, int row, int column, int index){
        if(index == L) return true;
        if( (row < 0) || (row > M - 1) || (column < 0) || (column > N - 1 )) {
            return false;
        }
        if( board[row][column] != word.charAt(index) ) return false;
        char curChar = board[row][column];
        board[row][column] = '.';
        if ( dfs(board,word, row + 1, column, index+1) ||
            dfs(board,word, row - 1, column, index+1) ||
            dfs(board,word, row, column + 1, index+1) ||
            dfs(board,word, row, column - 1, index+1) ){
            return true;
        }
        board[row][column] = curChar;
        return false;
    }
}
```

注意，上面的写法会有更好的时间复杂度，因为下面的写法，虽然更符合直觉，但是无论什么情况都会 dfs 到最后一层，所以会超时。

```java
class Solution {
    private final int[][] DIRECTIONS = new int[][]{{0,1},{0,-1},{1,0},{-1,0}};
    public boolean exist(char[][] board, String word) {
         for(int i = 0; i < board.length; i++){
             for(int j = 0; j < board[0].length; j++){
                 if(backtrack(board, new boolean[board.length][ board[0].length], word, new StringBuilder(), i, j, 0)){
                     return true;
                 }
             }
         }
        return false;
    }

    private boolean backtrack(char[][] board, boolean[][] marked, String word, StringBuilder sb, int row, int col, int level){
        if(level >= word.length() ) {
            return word.equals(sb.toString());
        }
        if(row < 0 || col < 0 || row >= board.length || col >= board[0].length) return false;
        if(marked[row][col]) return false;

        marked[row][col] = true;
        sb.append(board[row][col]);
        for(int[] direction : DIRECTIONS){
            if(backtrack(board, marked, word, sb, row + direction[0], col + direction[1], level + 1)) return true;
        }
        sb.deleteCharAt(sb.length() - 1);
        marked[row][col] = false;
        return false;
    }
}
```

综合起来给出下面的写法。

```java
class Solution {
    private int L; // word.length()
    private int M;
    private int N;
    private final int[][] DIRECTIONS = new int[][]{{0,1},{0,-1},{1,0},{-1,0}};
    public boolean exist(char[][] board, String word) {
        this.L = word.length();
        // if(word.length == 0) return true;
        this.M = board.length;
        if(M < 1) return false;
        this.N = board[0].length;
        for(int i = 0; i < M; i++){
            for(int j = 0; j < N; j++){
                if(backtrack(board, word, i, j, 0)){
                    return true;
                }
            }
        }
        return false;
    }
    private boolean backtrack(char[][] board, String word, int row, int column, int index){
        if(index == L) return true;
        if( (row < 0) || (row > M - 1) || (column < 0) || (column > N - 1 )) {
            return false;
        }
        if( board[row][column] != word.charAt(index) ) return false;
        char curChar = board[row][column];
        board[row][column] = '.';
        for(int[] direction : DIRECTIONS){
            if(backtrack(board, word, row + direction[0], column + direction[1], index + 1)) return true;
        }
        board[row][column] = curChar;
        return false;
    }
}
```

下面的解法基于212的Trie的解法，果然很快。。。比起上面的是 (117ms < 170ms>)

```java
class Solution {
    class TrieNode {
        TrieNode[] children;
        boolean isWord;
        String word;
        public TrieNode() {
            this.children = new TrieNode[128];
            this.isWord = false;
            this.word = "";
        }
    }
    private TrieNode root;
    private char[][] board;
    private int M;
    private int N;
    private boolean[][] marked;
    private boolean hasWord;
    private int[] directions = new int[]{1, 0, -1, 0, 1};
    public boolean exist(char[][] board, String word) {
        this.board = board;
        this.M = board.length;
        this.N = board[0].length;
        this.hasWord = false;
        this.root = new TrieNode();
        buildTrie(word);

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                if (hasWord) return hasWord;
                if (root.children[board[i][j] - 'A'] == null) continue;
                TrieNode p = root.children[board[i][j] - 'A'];
                marked = new boolean[M][N];
                marked[i][j] = true;
                dfs(i, j, p);
            }
        }
        return hasWord;
    }

    private void dfs(int row, int col, TrieNode p) {
        if (hasWord) return;
        if (p.isWord) hasWord = true;
        for (int i = 0; i < 4; i++) {
            int nextRow = row + directions[i];
            int nextCol = col + directions[i + 1];
            if (nextRow < 0 || nextCol < 0 || nextRow >= M || nextCol >= N) continue;
            if (marked[nextRow][nextCol]) continue;
            char targetChar = board[nextRow][nextCol];
            if (p.children[targetChar - 'A'] == null) continue;
            marked[nextRow][nextCol] = true;
            dfs(nextRow, nextCol, p.children[targetChar - 'A']);
            marked[nextRow][nextCol] = false;
        }
    }

    private void buildTrie(String word) {
        TrieNode p = root;
        for (int i = 0; i < word.length(); i++) {
            char cur = word.charAt(i);
            if (p.children[cur - 'A'] == null) {
                p.children[cur - 'A'] = new TrieNode();
            }
            p = p.children[cur - 'A'];
            if (i == word.length() - 1) {
                p.isWord = true;
                p.word = word;
            }
        }
    }
}
```