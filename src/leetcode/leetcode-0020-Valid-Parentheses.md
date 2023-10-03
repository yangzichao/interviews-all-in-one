# 020J. Valid Parentheses

Given a string containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.

An input string is valid if:

Open brackets must be closed by the same type of brackets.
Open brackets must be closed in the correct order.
Note that an empty string is also considered valid.

Example 1:

Input: "()"
Output: true
Example 2:

Input: "()[]{}"
Output: true
Example 3:

Input: "(]"
Output: false
Example 4:

Input: "([)]"
Output: false
Example 5:

Input: "{[]}"
Output: true

## Best Method: Stack
括号系统就是一个完美的先进后出 FILO.
以下的代码中，用一个dummytail是为了防止peek方法看到空报错。

```Java
class Solution {
    public boolean isValid(String s) {        
        Stack<Character> stack = new Stack<>();
        stack.push('d'); // dummytail;
        for(int i = 0; i < s.length(); i += 1){
           if(s.charAt(i) == ')'&& stack.peek() == '('){
               stack.pop();
           }
           else if(s.charAt(i) == ']'&& stack.peek() == '['){
               stack.pop();
           }
           else if(s.charAt(i) == '}'&& stack.peek() == '{'){
               stack.pop();
           }
           else{
               stack.push(s.charAt(i));
           }
        }
        stack.pop();// pop dummytail
        return stack.empty();
    }
}
```
以上的代码并不好，因为很明显用一个map来代替这么多无谓的if语句是更好的。

```java
class Solution {
    public boolean isValid(String s) {
        ArrayDeque<Character> stack = new ArrayDeque<>();
        Map<Character, Character> parenthesesMap = new HashMap<>();
        parenthesesMap.put(')','(');
        parenthesesMap.put(']','[');
        parenthesesMap.put('}','{');
        for(char c : s.toCharArray()){
            if(c == ')' || c == ']' || c == '}'){
                if(stack.isEmpty()) return false;
                if(stack.peekFirst() == parenthesesMap.get(c)){
                    stack.pop();
                    continue;
                }else{
                    return false;
                }
            }
            stack.push(c);
        }
        return stack.isEmpty();
    }
}
```