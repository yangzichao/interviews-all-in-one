# 003J. Longest Substring Without Repeating Characters

https://leetcode.com/problems/longest-substring-without-repeating-characters/


# 总结 

这个题有两个思路，
1. 直觉就是简单的用一个Set做滑窗。
2. 滑窗但是优化

请使用方法2

```java
class Solution {
    public int lengthOfLongestSubstring(String s) {
        Map<Character, Integer> charToCurrKnownRightmostIndex = new HashMap<>();
        int left = 0;
        int maxLength = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (charToCurrKnownRightmostIndex.containsKey(c)) {
                int currKnownRightmostIndex = charToCurrKnownRightmostIndex.get(c);
                if (currKnownRightmostIndex >= left) {
                    left = currKnownRightmostIndex + 1;
                }
            }
            charToCurrKnownRightmostIndex.put(c, i);
            int currWindowWidth = i - left + 1;
            maxLength = Math.max(maxLength, currWindowWidth);
        }
        return maxLength;
    }
}
```


## Method: Sliding Window almost best

原理是用双指针加 HashSet 保存元素。i j 一慢一快。
j 指针快，用来检测指向的位置是否已经存在于 Set 之中。
如果没有的话就移动 j，保持 i 不变。如果 j 指向的元素
已经存在于 set 之中，那么就移出 i 指向的元素，i++，
继续下一次检测，直到 i 移除了和 j 指向的相同的元素。

改进方案是，用 HashMap 直接记录位置，可以较大的减少循环的次数。

```Java
class Solution {
    public int lengthOfLongestSubstring(String s) {
        int n = s.length();
        Set<Character> set = new HashSet<>();
        int ans = 0;
        int i = 0, j = 0;
        while(j < n){
            if( !set.contains(s.charAt(j)) ){
                set.add(s.charAt(j++));
                ans = Math.max(ans, j - i);
            }else{
                set.remove(s.charAt(i++));
            }

        }
        return ans;

    }
}
```

用 for 的写法:

```java
class Solution {
    public int lengthOfLongestSubstring(String s) {
        Set<Character> window = new HashSet<>();
        int max = 0;
        int left = 0;
        for(char c : s.toCharArray()){
            if( window.size() == 0 || !window.contains(c)) {
                window.add(c);
                max = Math.max(max, window.size());
                continue;
            }
            while(window.contains(c)){
                window.remove(s.charAt(left));
                left++;
            }
            window.add(c);
            max = Math.max(max, window.size());
        }
        return max;
    }
}
```

发现上面 if 语句可以直接优化掉

```java
class Solution {
    public int lengthOfLongestSubstring(String s) {
        Set<Character> window = new HashSet<>();
        int max = 0;
        int left = 0;
        for(char c : s.toCharArray()){
            while(window.contains(c)){
                window.remove(s.charAt(left));
                left++;
            }
            window.add(c);
            max = Math.max(max, window.size());
        }
        return max;
    }
}
```
