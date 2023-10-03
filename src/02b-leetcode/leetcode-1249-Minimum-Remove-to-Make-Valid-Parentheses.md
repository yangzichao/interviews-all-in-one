# 1249 Minimum-Remove-to-Make-Valid-Parentheses

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
<div><p>Given a string <font face="monospace">s</font>&nbsp;of&nbsp;<code>'('</code>&nbsp;,&nbsp;<code>')'</code>&nbsp;and lowercase English characters.&nbsp;</p>
<p>Your task is to remove the minimum number of parentheses (&nbsp;<code>'('</code>&nbsp;or&nbsp;<code>')'</code>,&nbsp;in any positions ) so that the resulting <em>parentheses string</em> is valid and return <strong>any</strong> valid string.</p>
<p>Formally, a <em>parentheses string</em> is valid if and only if:</p>
<ul>
	<li>It is the empty string, contains only lowercase characters, or</li>
	<li>It can be written as&nbsp;<code>AB</code>&nbsp;(<code>A</code>&nbsp;concatenated with&nbsp;<code>B</code>), where&nbsp;<code>A</code>&nbsp;and&nbsp;<code>B</code>&nbsp;are valid strings, or</li>
	<li>It can be written as&nbsp;<code>(A)</code>, where&nbsp;<code>A</code>&nbsp;is a valid string.</li>
</ul>
<p>&nbsp;</p>
<p><strong>Example 1:</strong></p>
<pre><strong>Input:</strong> s = "lee(t(c)o)de)"
<strong>Output:</strong> "lee(t(c)o)de"
<strong>Explanation:</strong> "lee(t(co)de)" , "lee(t(c)ode)" would also be accepted.
</pre>
<p><strong>Example 2:</strong></p>
<pre><strong>Input:</strong> s = "a)b(c)d"
<strong>Output:</strong> "ab(c)d"
</pre>
<p><strong>Example 3:</strong></p>
<pre><strong>Input:</strong> s = "))(("
<strong>Output:</strong> ""
<strong>Explanation:</strong> An empty string is also valid.
</pre>
<p><strong>Example 4:</strong></p>
<pre><strong>Input:</strong> s = "(a(b(c)d)"
<strong>Output:</strong> "a(b(c)d)"
</pre>
<p>&nbsp;</p>
<p><strong>Constraints:</strong></p>
<ul>
	<li><code>1 &lt;= s.length &lt;= 10^5</code></li>
	<li><code>s[i]</code>&nbsp;is one&nbsp;of&nbsp;&nbsp;<code>'('</code> , <code>')'</code> and&nbsp;lowercase English letters<code>.</code></li>
</ul></div></section>
 

这个题要注意和301的区别。
301 要求的是所有的 minRemove 的结果
这个只需要找到其中的一个结果就好了。
区别在于，找到其中的一个结果，可以用好几种不同的算法。


 ## Method One 
 
```java
class Solution {
    public String minRemoveToMakeValid(String s) {
        int netCount = 0;
        LinkedList<Integer> indices = new LinkedList<>();

for(int i = 0; i < s.length(); i++) {
           char c = s.charAt(i);
           if( c == '(' ) {
               indices.push(i);
               netCount += 1;
          }
           if( c == ')' ) {
               if( netCount > 0) {
                   netCount -= 1;
                   indices.pop();
              }else {
                   indices.push(i);
              }
          }
      }
       StringBuilder ans = new StringBuilder();
       Set<Integer> indicesSet = new HashSet<Integer>();
       int sizeOfIndices = indices.size();

for( int i = 0; i < sizeOfIndices; i++ ) {
           indicesSet.add(indices.pop());
      }
       for( int i = 0; i < s.length(); i++) {
           char c = s.charAt(i);
           if(indicesSet.contains(i)) {

continue;
          }
           ans.append(c);
      }
       return String.valueOf(ans);
  }

//不用以下的办法是因为 substring 是 O(N);
   // public String removeIth(String s, int i) {
   //     return s.substring( 0 , i ) + s.substring( i + 1 );
   // }
}

```


## Method 
以下是一个自己想的方法。原理大致相同。
这个题，O(N) 的解法，扫两遍数组是必须的。

```java
class Solution {
    public String minRemoveToMakeValid(String s) {
        int counter = 0;
        StringBuilder sb = new StringBuilder();
        LinkedList<Integer> leftParenthesesIndices = new LinkedList<>();
        int i = 0;
        while( i < s.length() ){
            if(s.charAt(i) == '('){
                counter += 1;
                leftParenthesesIndices.push(sb.length());
            }
            if(s.charAt(i) == ')'){
                counter -= 1;
                if( counter < 0){
                    i++;
                    counter = 0;
                    continue;
                }
            }
            sb.append(s.charAt(i));
            i++;
        }

        while( counter > 0 ){
            sb.deleteCharAt(leftParenthesesIndices.pop());
            counter--;
        }
        return sb.toString();
    }
}
```

## Method Best!

```java

class Solution {

    public String minRemoveToMakeValid(String s) {

        // Parse 1: Remove all invalid ")"
        StringBuilder sb = new StringBuilder();
        int openSeen = 0;
        int balance = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') {
                openSeen++;
                balance++;
            } if (c == ')') {
                if (balance == 0) continue;
                balance--;
            }
            sb.append(c);
        }

        // Parse 2: Remove the rightmost "("
        StringBuilder result = new StringBuilder();
        int openToKeep = openSeen - balance;
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (c == '(') {
                openToKeep--;
                if (openToKeep < 0) continue;
            }
            result.append(c);
        }

        return result.toString();
    }
}
```