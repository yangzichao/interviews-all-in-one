# 680 Valid-Palindrome-II

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
<div><p>
Given a non-empty string <code>s</code>, you may delete <b>at most</b> one character.  Judge whether you can make it a palindrome.
</p>
<p><b>Example 1:</b><br>
</p><pre><b>Input:</b> "aba"
<b>Output:</b> True
</pre>
<p></p>
<p><b>Example 2:</b><br>
</p><pre><b>Input:</b> "abca"
<b>Output:</b> True
<b>Explanation:</b> You could delete the character 'c'.
</pre>
<p></p>
<p><b>Note:</b><br>
</p><ol>
<li>The string will only contain lowercase characters a-z.
The maximum length of the string is 50000.</li>
</ol>
<p></p></div></section>
 
 ## Method One 
 核心就一句话，一旦碰到两边不一样了，那么我们试试，去掉左边或者右边之后，是否仍然是 palindrome 的。

为什么这么说呢？  
 我们反过来思考，假如我们先有一个 palindrome string abcba, 然后插入一个字符破坏它，会是什么样呢？  
 由于 palindrome string 的对称性，我们要找到是哪一位的对称性被破坏了。  
 a b c x b a 等价于 a b x c b a  
 a b c b x a 等价于 a x b c b a  
 a b c b a x 等价于 x a b c b a
所以我们从两头开始对比，一旦发现不一样了，我们就知道这一位的对称性被破坏了。由于我们并不知道是哪个破坏的对称性，
所以我们应该两遍都去掉试试。

```Java
class Solution {
    public boolean validPalindrome(String s) {
        for(int l = 0, r = s.length() - 1; l < r; l++, r-- ) {
            if(s.charAt(l) == s.charAt(r)) continue;
            return isPalindrome( s.substring(l, r) ) || isPalindrome( s.substring(l+1, r+1) );
        }
        return true;
    }
    public boolean isPalindrome(String s) {
        for(int left = 0, right = s.length() - 1; left < right; left++, right-- ) {
            if(s.charAt(left) != s.charAt(right)){
                return false;
            }
        }
        return true;
    }
}
​
```

## Follow Up Questions 好像非常好啊
这个题或许还可以出一个 follow up, 即如果可以允许移除最多两个或者 k 个字符怎么办呢？
isPalindrome 或许可以用一个递归的办法写，即 k 个就是 k 层。


2023写了一个这个
```java
class Solution {
    public boolean validPalindrome(String s) {
        int lo = 0;
        int hi = s.length() - 1;
        while (lo < hi && s.charAt(lo) == s.charAt(hi)) {
            lo++;
            hi--;
        }
        if (hi <= lo) return true;
        boolean found = false;
        found = found || isPalindrome(s.substring(lo + 1, hi + 1));
        found = found || isPalindrome(s.substring(lo, hi));
        return found;
    }

    private boolean isPalindrome(String s) {
        int lo = 0;
        int hi = s.length() - 1;
        while (lo < hi ) {
            if (s.charAt(lo) != s.charAt(hi)) return false;
            lo++;
            hi--;
        }
        return true;
    }
}
```