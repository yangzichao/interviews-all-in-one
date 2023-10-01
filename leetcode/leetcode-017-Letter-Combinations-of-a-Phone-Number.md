# 017J. Letter Combinations of a Phone Number

https://leetcode.com/problems/letter-combinations-of-a-phone-number/

## Method Best

这个题没啥意思啊。

```java
class Solution {
    public List<String> letterCombinations(String digits) {
        List<String> ans = new ArrayList<String>();
        if(digits==null||digits.length()==0) return ans;
        char[][] map = new char[10][];
        map[2]="abc".toCharArray();
        map[3]="def".toCharArray();
        map[4]="ghi".toCharArray();
        map[5]="jkl".toCharArray();
        map[6]="mno".toCharArray();
        map[7]="pqrs".toCharArray();
        map[8]="tuv".toCharArray();
        map[9]="wxyz".toCharArray();

        char[] input = digits.toCharArray();


        ans.add("");
        for(char c : input){
            ans = add(ans, map[ c - '0']);
        }
        return ans;
    }

    // 因为需要把旧的String更新一下，与其删掉旧的
    // 不如把旧的List of String 拿出来然后更新，并且加入到一个新的答案。返回给ans.
    public List<String> add(List<String> old, char[] set){
        List<String> update = new ArrayList<String>();
        for(String s : old){
            for(char c : set){
                update.add( s + c);
            }
        }
        return update;
    }
}
```

这个题用回溯 backtracking 似乎代码确实更好看。
即把每个数字看成一层树，每个字母看成数字的子节点。
如果我们发现树到底了，那么我们就把这个路径加到答案集合里。

这里放出参考答案：

```java
class Solution {
    private List<String> combinations = new ArrayList<>();
    private Map<Character, String> letters = Map.of(
        '2', "abc", '3', "def", '4', "ghi", '5', "jkl",
        '6', "mno", '7', "pqrs", '8', "tuv", '9', "wxyz");
    private String phoneDigits;

    public List<String> letterCombinations(String digits) {
        // If the input is empty, immediately return an empty answer array
        if (digits.length() == 0) {
            return combinations;
        }

        // Initiate backtracking with an empty path and starting index of 0
        phoneDigits = digits;
        backtrack(0, new StringBuilder());
        return combinations;
    }

    private void backtrack(int index, StringBuilder path) {
        // If the path is the same length as digits, we have a complete combination
        if (path.length() == phoneDigits.length()) {
            combinations.add(path.toString());
            return; // Backtrack
        }

        // Get the letters that the current digit maps to, and loop through them
        String possibleLetters = letters.get(phoneDigits.charAt(index));
        for (char letter: possibleLetters.toCharArray()) {
            // Add the letter to our current path
            path.append(letter);
            // Move on to the next digit
            backtrack(index + 1, path);
            // Backtrack by removing the letter before moving onto the next
            path.deleteCharAt(path.length() - 1);
        }
    }
}
```

上面的参考答案是 leetCode 写的？geez
用 map.of 初始化 java 9 才有，而且immutable. 
如果直接一堆 {{put(a,b)}} 容易有memory leak 不推荐使用。
```java
class Solution {
    private Map<Character, String> map;
    private List<String> ans;
    public List<String> letterCombinations(String digits) {
        if (digits.length() == 0) return new ArrayList<>();
        this.map = new HashMap<>();
        this.ans = new ArrayList<>();
        map.put('2', "abc");
        map.put('3', "def");
        map.put('4', "ghi");
        map.put('5', "jkl");
        map.put('6', "mno");
        map.put('7', "pqrs");
        map.put('8', "tuv");
        map.put('9', "wxyz");
        helper(new StringBuilder(), digits, 0);
        return ans;
    }
    private void helper(StringBuilder sb, String digits, int index) {
        if (index >= digits.length()) {
            ans.add(sb.toString());
            return;
        }
        String candidates = map.get(digits.charAt(index));
        for (char candidate : candidates.toCharArray()) {
            sb.append(candidate);
            helper(sb, digits, index + 1);
            sb.deleteCharAt(sb.length() - 1);
        }
    }
}
```