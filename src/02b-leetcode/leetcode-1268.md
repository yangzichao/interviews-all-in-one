# 1268. Search Suggestions System

difficulty: Medium

Given an array of strings `products` and a string `searchWord`. We want to design a system that suggests at most three product names from `products` after each character of `searchWord` is typed. Suggested products should have common prefix with the searchWord. If there are more than three products with a common prefix return the three lexicographically minimums products.

Return *list of lists* of the suggested `products` after each character of `searchWord` is typed.

**Example 1:**

```
Input: products = ["mobile","mouse","moneypot","monitor","mousepad"], searchWord = "mouse"
Output: [
  ["mobile","moneypot","monitor"],
  ["mobile","moneypot","monitor"],
  ["mouse","mousepad"],
  ["mouse","mousepad"],
  ["mouse","mousepad"]
]
Explanation: products sorted lexicographically = ["mobile","moneypot","monitor","mouse","mousepad"]
After typing m and mo all products match and we show user ["mobile","moneypot","monitor"]
After typing mou, mous and mouse the system suggests ["mouse","mousepad"]
```

**Example 2:**

```
Input: products = ["havana"], searchWord = "havana"
Output: [["havana"],["havana"],["havana"],["havana"],["havana"],["havana"]]
```

**Constraints:**

- `1 <= products.length <= 1000`
- `1 <= products[i].length <= 3000`
- `1 <= sum(products[i].length) <= 2 * 10^4`
- All characters of `products[i]` are lower-case English letters.
- `1 <= searchWord.length <= 1000`
- All characters of `searchWord` are lower-case English letters.

## 真实工程视角

In real world people prefer trie + cache (cache top results in each TrieNode) to achieve O(1) query. You build/update trie once in a few hours but will be queried a few million times. It is more important to have low query latency than have most up-to-date suggestion.

## Method One: Trie + DFS

每个 `TrieNode` 在自己是某个 product 的结尾时记录完整的单词。查询时沿着 `searchWord` 一个字符一个字符地往下走，到达每一层后从当前子树做 DFS（按字典序 `a` → `z`），收集前 3 个单词。

字符串按字典序加入 trie 时不需要预先排序：DFS 按 `a` → `z` 的固定顺序遍历子节点，自然就拿到了字典序最小的前 3 个。

```Java
class Solution {
    class Node {
        char val;
        String word;
        Node[] children;
        Node () {
            this.children = new Node[26];
        }
        Node (char val) {
            this.val = val;
            this.children = new Node[26];
        }
        Node get (char c) {
            return children[c - 'a'];
        }
        void set (char c, Node node) {
            children[c - 'a'] = node;
        }
    }

    public List<List<String>> suggestedProducts(String[] products, String searchWord) {
        List<List<String>> ans = new ArrayList<>();
        Node root = new Node();
        for (String product : products) {
            addString(root, product);
        }

        Node node = root;
        for (char c : searchWord.toCharArray()) {
            if (node != null) {
                node = node.get(c);
            }
            List<String> top3 = new ArrayList<>();
            if (node != null) {
                dfs(node, top3);
            }
            ans.add(top3);
        }
        return ans;
    }

    private void dfs (Node node, List<String> top3) {
        if (top3.size() == 3) return;
        if (node.word != null) top3.add(node.word);
        for (char c = 'a'; c <= 'z'; c++) {
            if (node.get(c) == null) continue;
            dfs(node.get(c), top3);
        }
    }

    private void addString(Node root, String string) {
        Node node = root;
        for (char c : string.toCharArray()) {
            if (node.get(c) == null) {
                node.set(c, new Node(c));
            }
            node = node.get(c);
        }
        node.word = string;
    }
}
```

时间复杂度 Time Complexity:
- 建树 O(∑|products[i]|)
- 查询时每一层 DFS 最坏情况要扫整棵子树，但因为只取前 3 个，可以视作每层 O(M)（M 为最长 product 长度），总查询 O(|searchWord| · M)

空间复杂度 Space Complexity O(∑|products[i]| · 26)（trie 节点定长数组）

### Refactored: 更干净的版本

对上面的写法做了几处清理：

1. **删掉 `Node.val`**：trie 节点是哪个字符，由它在父节点 `children[]` 里的下标决定，不需要再存一份。连带 `Node(char val)` 构造器一起删掉。
2. **`Node` 改成 `static`**：避免每个节点隐式持有 `Solution.this` 的引用。
3. **DFS 把 `top3.size() < 3` 放进 `for` 条件**：凑齐 3 个之后剩下的字母不再进递归再立刻 return。
4. **`dfs` → `collectTop3`，`addString` → `insert`**：名字更贴合用途，符合 trie 的常见命名。

```Java
class Solution {
    private static class Node {
        String word;
        Node[] children = new Node[26];
        Node get(char c) { return children[c - 'a']; }
        void set(char c, Node n) { children[c - 'a'] = n; }
    }

    public List<List<String>> suggestedProducts(String[] products, String searchWord) {
        Node root = new Node();
        for (String p : products) insert(root, p);

        List<List<String>> ans = new ArrayList<>();
        Node node = root;
        for (char c : searchWord.toCharArray()) {
            if (node != null) node = node.get(c);
            List<String> top3 = new ArrayList<>();
            if (node != null) collectTop3(node, top3);
            ans.add(top3);
        }
        return ans;
    }

    private void collectTop3(Node node, List<String> top3) {
        if (node.word != null) top3.add(node.word);
        for (char c = 'a'; c <= 'z' && top3.size() < 3; c++) {
            Node next = node.get(c);
            if (next != null) collectTop3(next, top3);
        }
    }

    private void insert(Node root, String word) {
        Node node = root;
        for (char c : word.toCharArray()) {
            if (node.get(c) == null) node.set(c, new Node());
            node = node.get(c);
        }
        node.word = word;
    }
}
```

## Method Two: Sort + Binary Search

把 `products` 先按字典序排序，对 `searchWord` 的每个前缀用二分找到第一个 ≥ 该前缀的位置，从那里往后取最多 3 个仍然以该前缀开头的字符串即可。代码更短，但查询时每个前缀都要做一次二分 + 字符串比较，常数比 trie + cache 那套高。
