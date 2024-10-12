# 242 Valid-Anagram

difficulty: Easy

<style>
        section pre{
          background-color: #eee;
          border: 1px solid #ddd;
          padding:10px;
          border-radius: 5px;
        }
      </style>
<section>
<div><p>Given two strings <em>s</em> and <em>t&nbsp;</em>, write a function to determine if <em>t</em> is an anagram of <em>s</em>.</p>
<p><b>Example 1:</b></p>
<pre><b>Input:</b> <em>s</em> = "anagram", <em>t</em> = "nagaram"
<b>Output:</b> true
</pre>
<p><b>Example 2:</b></p>
<pre><b>Input:</b> <em>s</em> = "rat", <em>t</em> = "car"
<b>Output: </b>false
</pre>
<p><strong>Note:</strong><br>
You may assume the string contains only lowercase alphabets.</p>
<p><strong>Follow up:</strong><br>
What if the inputs contain unicode characters? How would you adapt your solution to such case?</p>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public boolean isAnagram(String s, String t) {
        int[] map = new int[26];
        for(char c : s.toCharArray()) {
            map[ c - 'a' ]++;
        }
        for(char c : t.toCharArray()) {
            map[ c - 'a' ]--;
        }
        for(int n : map) {
            if(n != 0) {
                return false;
            }
        }
        return true;
    }
}
​
```


神奇的一幕：

```java
class Solution {

    public boolean isAnagram(String s, String t) {
        return areCharFrequencyCountTheSame(getCharFrequencyCount(s), getCharFrequencyCount(t));
    }

    private Map<Character, Integer> getCharFrequencyCount(String s) {
        Map<Character, Integer> charFrequencyCount = new HashMap<>();
        for (char c : s.toCharArray()) {
            charFrequencyCount.put(c, charFrequencyCount.getOrDefault(c, 0) + 1);
        }
        return charFrequencyCount;
    }

    private boolean areCharFrequencyCountTheSame(Map<Character, Integer> map1, Map<Character, Integer> map2) {
        if (map1.size() != map2.size()) return false;
        for (char key : map1.keySet()) {
            if (!map2.containsKey(key)) return false;
            // if (map2.get(key) != map1.get(key)) return false; // 这是错误的
            // if (map2.get(key) - map1.get(key) != 0) return false; // 这是正确的 因为 - 把 Integer unbox了。所以avoid use != 比较 Integer.
            if (map2.get(key).compareTo(map1.get(key)) != 0) return false; // Best practice
        }
        return true;
    }
}
```


or just this

```java
class Solution {

    public boolean isAnagram(String s, String t) {
        return getCharFrequencyCount(s).equals(getCharFrequencyCount(t));
    }

    private Map<Character, Integer> getCharFrequencyCount(String s) {
        Map<Character, Integer> charFrequencyCount = new HashMap<>();
        for (char c : s.toCharArray()) {
            charFrequencyCount.put(c, charFrequencyCount.getOrDefault(c, 0) + 1);
        }
        return charFrequencyCount;
    }

}
```