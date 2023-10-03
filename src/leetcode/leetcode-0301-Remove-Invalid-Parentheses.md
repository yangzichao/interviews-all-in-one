# 301J. Remove Invalid Parentheses
https://leetcode.com/problems/remove-invalid-parentheses/

这个题非常的磨人，令人感到烦躁。
## Method: Best？ 
<pre>

</pre>
```java
class Solution {
    public boolean isValid(String s){
        int count = 0;
        for(int i = 0; i < s.length();i++){
            char c = s.charAt(i);
            if(c == '(') count++;
            if(c == ')') count--;
            if(count < 0) return false; 
            // means the parentheses are already not closed
            // return false immediately
        }
        return count == 0;
    }
    
    public List<String> removeInvalidParentheses(String s) {
        List<String> ans = new ArrayList<>(); // answer list
        
        // represent how many left/right parentheses need to be removed
        int l = 0, r = 0;
        
        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            l += c == '(' ? 1: 0;
            if(l == 0){
                //如果左右括号配好对了，第一个出现的是),那么这个是要移除的
                r += c == ')' ? 1:0;
            }else{
                //如果没配好对，如果有有括号，那么就减少一个未配对的。
                l-= c == ')' ? 1:0;
            }
        }
        
        dfs(s,0,l,r,ans);
        return ans;
    }
    
    public void dfs(String s, int start, int l, int r, List<String> ans){
        // 如果合法 就加入答案
        if( l == 0 && r == 0){
            if(isValid(s)){
                ans.add(s);
            }
            return;
        }
        
        //
        for(int i = start; i < s.length(); i++){
            char c = s.charAt(i);
            //这个判断是后面加上的，我们对于重复的部分只删一个且只删第一个。
            if(i != start && c == s.charAt(i-1)) continue;
            //其他字符自动跳过
            //先删右括号，再删左括号，为什么呢？
            // 考虑这么一件事，如果左括号比右括号多，而且只需要删左括号，说明
            // 是这种情况 (((((((()
            // 在需要删左括号的情况下 仍然需要删右括号，说明什么？
            // 说明有多的右括号出现在一串左括号前面了 即 ) (((((((()
            if(r > 0 && c == ')'){
                String temp = i < s.length() ? s.substring(0,i) + s.substring(i+1) : s.substring(0,i);
                dfs(temp, i, l, r-1, ans);
            }else if(l > 0 && c == '('){
                String temp = i < s.length() ? s.substring(0,i) + s.substring(i+1) : s.substring(0,i);
                dfs(temp, i, l-1, r, ans);
            }
            // if(c == '(' || c ==')'){
            //     //这是用来删除第i个字符。
            //     String temp = i < s.length() ? s.substring(0,i) + s.substring(i+1) : s.substring(0,i);
            //     if(r > 0 && c ==')'){
            //         dfs(temp, i, l, r-1, ans);
            //     }else if(l > 0 && c == '('){
            //         dfs(temp, i, l-1, r, ans);
            //     }
            // }
        }
    }
}
```


## 2023 的写法 BFS 
这个题目确实BFS 比上面的 DFS 更自然，因为它天然的控制了层数
DFS我已经看不懂了
这里技术上要注意的点是，这个图，不保证是一个树：
因此，如果不使用入列之前就加入seen（visited）的话，是会重复入列一个节点的。
如果这样的话，答案中就会有重复的元素，需要用一个 Set 去重。
当然，为了最佳的时间复杂度，应当入列之前就加入 seenSet.

除此之外，对于题目的分析，应该对比和1249的分析。
这两个题乍一看很像，但是之所以这个题这么难，而1249题容易，就是因为本题要求给出所有的 valid 结果。
```java
class Solution {
    /**
    都想不到是个暴力解法 这个题思路就是把这个问题转化成一个图的搜索
    BFS，根节点是s, 子节点是 s 去除任意一个括号 以此类推
    直到暴力搜索了 h 层之后 找到了第一个 valid parenthese 
     */
    public List<String> removeInvalidParentheses(String s) {
        List<String> ans = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        boolean isValidFound = false;
        queue.offer(s);
        seen.add(s);
        while (!queue.isEmpty() && !isValidFound) {
            int size = queue.size();
            while (size > 0) {
                String cur = queue.poll();
                if (isValidParentheses(cur)) {
                    isValidFound = true;
                    ans.add(cur);
                } else {
                    for (int i = 0; i < cur.length(); i++) {
                        char c = cur.charAt(i);
                        String next = "";
                        if (c == '(' || c == ')') {
                            next = cur.substring(0, i) + cur.substring(i + 1, cur.length());
                            if (!seen.contains(next)) {
                                seen.add(next);
                                queue.offer(next);
                            }
                        }
                    }
                }
                size--;
            }
        }
        return ans;
    }

    private boolean isValidParentheses(String str) {
        int net = 0;
        for (char c : str.toCharArray()) {
            if (c == '(') net += 1;
            if (c == ')') {
                net -= 1;
                if (net < 0) return false;
            }
        } 
        return net == 0;
    }
}
```
