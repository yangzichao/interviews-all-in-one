# 139 Word-Break

difficulty: Medium

<style>
        section pre{
          background-color: #eee;
          border: 1px solid #ddd;
          padding:10px;
          border-radius: 5px;
        }
      </style>
<section>
<div><p>Given a <strong>non-empty</strong> string <em>s</em> and a dictionary <em>wordDict</em> containing a list of <strong>non-empty</strong> words, determine if <em>s</em> can be segmented into a space-separated sequence of one or more dictionary words.</p>
<p><strong>Note:</strong></p>
<ul>
	<li>The same word in the dictionary may be reused multiple times in the segmentation.</li>
	<li>You may assume the dictionary does not contain duplicate words.</li>
</ul>
<p><strong>Example 1:</strong></p>
<pre><strong>Input:</strong> s = "leetcode", wordDict = ["leet", "code"]
<strong>Output:</strong> true
<strong>Explanation:</strong> Return true because <code>"leetcode"</code> can be segmented as <code>"leet code"</code>.
</pre>
<p><strong>Example 2:</strong></p>
<pre><strong>Input:</strong> s = "applepenapple", wordDict = ["apple", "pen"]
<strong>Output:</strong> true
<strong>Explanation:</strong> Return true because <code>"</code>applepenapple<code>"</code> can be segmented as <code>"</code>apple pen apple<code>"</code>.
&nbsp;            Note that you are allowed to reuse a dictionary word.
</pre>
<p><strong>Example 3:</strong></p>
<pre><strong>Input:</strong> s = "catsandog", wordDict = ["cats", "dog", "sand", "and", "cat"]
<strong>Output:</strong> false
</pre>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    private Set<String> set;
    private boolean[] marked;
    public boolean wordBreak(String s, List<String> wordDict) {
        this.set = new HashSet<>(wordDict);
        marked = new boolean[s.length() + 1];
        Queue<Integer> bfs = new LinkedList<>();
        bfs.offer(0);
        while( !bfs.isEmpty() ) {
            int size = bfs.size();
            while ( size > 0 ) {
                size -= 1;
                int cur = bfs.poll();
                for(int i = cur + 1; i <= s.length(); i++ ) {
                    if( marked[i] ) {
                        continue;
                    }
                    if(set.contains( s.substring(cur, i) ) ) {
                        if( i == s.length() ) {
                            return true;
                        }
                        bfs.offer(i);
                        marked[i] = true;
                    }
                }
            }
        }
        return false;
    }
    
}
​
```


## Method Best: Using DP

这个题其实是个藏的比较深的动态规划的题。只是包装上了 String 就让人容易想歪。
上面的BFS的思路其实差不多，所以时间复杂度都是一样的。
仔细想一下这个题其实和 coin change 之类的一样啊。随便说一句点一下：
如果 s 的一个substring, s.substring(0, i) match上了，那么我们就不考虑之前的是否match了。
dp[i] 标志的就是这个 substring 是否是match的。


```java
class Solution {
    public boolean wordBreak(String s, List<String> wordDict) {
        boolean[] dp = new boolean[s.length() + 1];
        dp[0] = true;
        for (int i = 0; i <= s.length(); i++) {
            if (!dp[i]) continue;
            String curS = s.substring(i, s.length());
            for (String word : wordDict) {
                if (!curS.startsWith(word)) continue;
                dp[i + word.length()] = true;
            }
        }
        return dp[s.length()];
    }
}
```


值得写一下这个题的 state transfer 方程，我也不知道为什么以前没有写。
就是很简单的 dp[i] 代表着当前有没有可能 通过几个字符串组合匹配到此处。
比如 输入如果是`leetcode`, `lee,leet,cod`
那么 dp 是 `tffttfftf` 其中第一个t是dummy元素 代表空字符串。所以说这个题很像 coin change 是颇有道理的。