# 791 Custom-Sort-String 
 
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
<div><p><code>S</code> and <code>T</code> are strings composed of lowercase letters. In <code>S</code>, no letter occurs more than once.</p>
<p><code>S</code> was sorted in some custom order previously. We want to permute the characters of <code>T</code> so that they match the order that <code>S</code> was sorted. More specifically, if <code>x</code> occurs before <code>y</code> in <code>S</code>, then <code>x</code> should occur before <code>y</code> in the returned string.</p>
<p>Return any permutation of <code>T</code> (as a string) that satisfies this property.</p>
<pre><strong>Example :</strong>
<strong>Input:</strong> 
S = "cba"
T = "abcd"
<strong>Output:</strong> "cbad"
<strong>Explanation:</strong> 
"a", "b", "c" appear in S, so the order of "a", "b", "c" should be "c", "b", and "a". 
Since "d" does not appear in S, it can be at any position in T. "dcba", "cdba", "cbda" are also valid outputs.
</pre>
<p>&nbsp;</p>
<p><strong>Note:</strong></p>
<ul>
	<li><code>S</code> has length at most <code>26</code>, and no character is repeated in <code>S</code>.</li>
	<li><code>T</code> has length at most <code>200</code>.</li>
	<li><code>S</code> and <code>T</code> consist of lowercase letters only.</li>
</ul>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public String customSortString(String S, String T) {
        // 首先不可避免的是要统计T词频。因为很可能有重复的
        int[] count = new int[26];
        for(char c: T.toCharArray()){
            count[c-'a']++;
        }
        String ans = "";
        //然后我们把S中的都加入
        for(char c: S.toCharArray()){
            for(int i = count[c-'a']; i > 0; i--){
                ans += c;
            }
            count[c - 'a'] = 0; 
        }
        
        // 然后我们把剩下的再加入
        
        for(char c = 'a'; c <= 'z'; c++){
            for(int i = count[c-'a']; i > 0; i--){
                ans += c;
            }
        }
        
        return ans;
    }
}
​
```




## 2025 

2025 年憋的这个 虽然时间复杂度是 N log N, 但是也还好。
好好想想上面的那个 O(N) 解法。不难，我尝试简短解释一下，上面的特殊解法的原理是，
我们把 target 的 String 打碎成为一堆 char 供取用，然后按照 order 的顺序来append.

```java
class Solution {
    public String customSortString(String order, String s) {
        // map character to index, index is naturally there priority, smaller the largers
        // we can pass a new comparator and sort 
        Map<Character, Integer> charRanks = new HashMap<>();
        for (int i = 0; i < order.length(); i++) {
            charRanks.put(order.charAt(i), i + 1);
        }
        PriorityQueue minHeap = new PriorityQueue<>((a, b) -> {
            int rankA = charRanks.getOrDefault(a, -1);
            int rankB = charRanks.getOrDefault(b, -1);
            return rankA - rankB;
        });
        for (char c : s.toCharArray()) {
            minHeap.offer(c);
        }
        StringBuilder sb = new StringBuilder();
        while (!minHeap.isEmpty()) {
            sb.append(minHeap.poll());
        }

        return sb.toString();
    }
}

```