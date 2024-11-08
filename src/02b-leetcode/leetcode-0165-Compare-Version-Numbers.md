# 165 Compare-Version-Numbers

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
<div><p>Compare two version numbers <em>version1</em> and <em>version2</em>.<br>
If <code><em>version1</em> &gt; <em>version2</em></code> return <code>1;</code>&nbsp;if <code><em>version1</em> &lt; <em>version2</em></code> return <code>-1;</code>otherwise return <code>0</code>.</p>
<p>You may assume that the version strings are non-empty and contain only digits and the <code>.</code> character.</p>
<p>The <code>.</code> character does not represent a decimal point and is used to separate number sequences.</p>
<p>For instance, <code>2.5</code> is not "two and a half" or "half way to version three", it is the fifth second-level revision of the second first-level revision.</p>
<p>You may assume the default revision number for each level of a version number to be <code>0</code>. For example, version number <code>3.4</code> has a revision number of <code>3</code> and <code>4</code> for its first and second level revision number. Its third and fourth level revision number are both <code>0</code>.</p>
<p>&nbsp;</p>
<p><strong>Example 1:</strong></p>
<pre><strong>Input:</strong> <code><em>version1</em></code> = "0.1", <code><em>version2</em></code> = "1.1"
<strong>Output:</strong> -1</pre>
<p><strong>Example 2:</strong></p>
<pre><strong>Input: </strong><code><em>version1</em></code> = "1.0.1", <code><em>version2</em></code> = "1"
<strong>Output:</strong> 1</pre>
<p><strong>Example 3:</strong></p>
<pre><strong>Input:</strong> <code><em>version1</em></code> = "7.5.2.4", <code><em>version2</em></code> = "7.5.3"
<strong>Output:</strong> -1</pre>
<p><strong>Example 4:</strong></p>
<pre><strong>Input:</strong> <code><em>version1</em></code> = "1.01", <code><em>version2</em></code> = "1.001"
<strong>Output:</strong> 0
<strong>Explanation:</strong> Ignoring leading zeroes, both “01” and “001" represent the same number “1”</pre>
<p><strong>Example 5:</strong></p>
<pre><strong>Input:</strong> <code><em>version1</em></code> = "1.0", <code><em>version2</em></code> = "1.0.0"
<strong>Output:</strong> 0
<strong>Explanation:</strong> The first version number does not have a third level revision number, which means its third level revision number is default to "0"</pre>
<p>&nbsp;</p>
<p><strong>Note:</strong></p>
<ol>
<li>Version strings are composed of numeric strings separated by dots <code>.</code> and this numeric strings <strong>may</strong> have leading zeroes. </li>
<li>Version strings do not start or end with dots, and they will not be two consecutive dots.</li>
</ol></div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public int compareVersion(String version1, String version2) {
        String[] v1 = version1.split("\\.");
        String[] v2 = version2.split("\\.");
        
        int size1 = v1.length;
        int size2 = v2.length;
        int maxSize = Math.max(size1, size2);
        
        for(int i = 0; i < maxSize; i++) {
            int i1 = i < size1 ? Integer.parseInt(v1[i]) : 0;
            int i2 = i < size2 ? Integer.parseInt(v2[i]) : 0;
            if( i1 != i2 ) {
                return i1 > i2 ? 1 : -1;
            } 
        }
        return 0;
    }
    
​
}
​
```

又一种写法

```java
class Solution {
    public int compareVersion(String version1, String version2) {
        String[] v1 = version1.split("\\.");
        String[] v2 = version2.split("\\.");
        int p = 0;
        while( p < v1.length || p < v2.length ) {
            int curV1 = p <  v1.length ? Integer.parseInt( v1[p]) : 0;
            int curV2 = p <  v2.length ? Integer.parseInt( v2[p]) : 0;
            if(curV1 < curV2) {
                return -1;
            }
            if(curV1 > curV2) {
                return 1;
            }
            p++;
        }
        return 0;
    }
}
```


2024: 好久没写了，都忘记了 \\.

```java
class Solution {
    public int compareVersion(String version1, String version2) {
        String[] v1 = version1.split("\\.");
        String[] v2 = version2.split("\\.");
        int maxLength = Math.max(v1.length, v2.length);
        for (int i = 0; i < maxLength; i++) {
            int curV1 = i >= v1.length ? 0 : strToInt(v1[i]);
            int curV2 = i >= v2.length ? 0 :strToInt(v2[i]);
            if (curV1 != curV2) {
                return curV1 < curV2 ? -1 : 1;
            }
        }
        return 0;      
    }

    private int strToInt(String str) {
        int num = 0;
        for (char c : str.toCharArray()) {
            num = (num * 10) + (int) (c - '0');
        }
        return num;
    }
}
```