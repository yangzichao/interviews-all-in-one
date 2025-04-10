# 076J. Minimum Window Substring
https://leetcode.com/problems/minimum-window-substring/

## Method Sliding Window

```java
class Solution {
    //全局变量
    //tmap用来储存和记录
    private Map<Character, Integer> smap;
    private Map<Character, Integer> tmap;
    
    public String minWindow(String s, String t) {
        this.smap = new HashMap<>();
        this.tmap = new HashMap<>();
        initTmap(t);
        int minSize = Integer.MAX_VALUE;
        int slow = 0, fast = 0, start = 0, end = 0;
        while(fast < s.length() ){
            if(!isContains()){
                smap.put(s.charAt(fast), smap.getOrDefault(s.charAt(fast),0) + 1);
                fast++;
            }else{
                if(minSize > fast - slow){
                    start = slow;
                    end   = fast;
                    minSize = fast - slow;
                }
                smap.put(s.charAt(slow), smap.get(s.charAt(slow)) - 1);
                slow++;
            }     
        }
        while(slow < s.length() && isContains()){
            if( minSize > fast - slow){
                start = slow;
                end   = fast;
                minSize = fast - slow;  
            }
            smap.put(s.charAt(slow), smap.get(s.charAt(slow)) - 1);
            slow++; 
        }
        return s.substring(start, end); 
        
    }
    private boolean isContains(){
        for(char c : tmap.keySet()){
            if(smap.getOrDefault(c,0) < tmap.get(c)){
                return false;
            }
        }
        return true;
    }
    
    private void initTmap(String t){
        for(char c: t.toCharArray()){
            tmap.put(c, tmap.getOrDefault(c,0) + 1);
        }
    }
}
```




## 2025

这个题每次好像都把最佳方法跳过了，那么这次就好好写一下。

这个题其实思路也不难，假如没重复元素，这就是个 medium。
虚拟的题目：
如果 s 和 t 都是 unique 的思路就是 原