# 028J?. Implement strStr()
https://leetcode.com/problems/implement-strstr/

KMP 需要理解
[阮一峰的KMP](http://www.ruanyifeng.com/blog/2013/05/Knuth%E2%80%93Morris%E2%80%93Pratt_algorithm.html)

## Method: Not Bad
<pre>
唯一需要注意的是
i < haystack.length() - needle.length() + 1;
的+1，想想为什么。也没啥为什么其实。
</pre>
```Java
class Solution {
    public int strStr(String haystack, String needle) {
        if(needle.length() == 0 ) return 0; // edge case
        for(int i = 0; i < haystack.length() - needle.length() + 1; i++){
            if(haystack.charAt(i) == needle.charAt(0)){
                int n = needle.length() - 1;
                while(n > 0){
                    if(haystack.charAt(i+n) == needle.charAt(n)){
                        n--;
                    }else{
                        break;
                    }
                }
                if(n==0){
                    return i;
                }
            }
        }
        return -1;
    }
}
```
上面好多年前写的了，应该是20年的。下面是23年9月写的，可见还是有进步。

```java
class Solution {
    public int strStr(String haystack, String needle) {
        for (int i = 0; i < haystack.length() - needle.length() + 1; i++) {
            boolean isMatch = true;
            for (int j = 0; j < needle.length(); j++) {
                if (haystack.charAt(i + j) != needle.charAt(j)) {
                    isMatch = false;
                    break;
                }
            }
            if (isMatch) return i;
        }
        return -1;
    }
}

```