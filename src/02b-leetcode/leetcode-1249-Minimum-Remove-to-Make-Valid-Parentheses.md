# 1249 Minimum-Remove-to-Make-Valid-Parentheses


这个题要注意和301的区别。
301 要求的是所有的 minRemove 的结果
这个只需要找到其中的一个结果就好了。
区别在于，找到其中的一个结果，可以用好几种不同的算法。

注意：这个题移除多余的右括号很容易，但是要注意多余的左括号也要被移除。

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


2023 年写的
这个思路就是，入栈的时候去除多余的 右括号，出栈的时候去除多余的左括号。
```java
class Solution {
    public String minRemoveToMakeValid(String s) {
        StringBuilder sb = new StringBuilder();
        ArrayDeque<Character> stack = new ArrayDeque<>();
        int net = 0;
        for (char c : s.toCharArray()) {
            if (c == '(') net++;
            if (c == ')') {
                if (net == 0) continue;
                net--;
            }
            stack.push(c);
        }
        while (!stack.isEmpty()) {
            while (!stack.isEmpty() && stack.peek() == '(' && net > 0) {
                stack.pop();
                net--;
            }
            if (!stack.isEmpty()) sb.append(stack.pop());
        }
        return sb.reverse().toString();
    }
}

//不用以下的办法是因为 substring 是 O(N);
   // public String removeIth(String s, int i) {
   //     return s.substring( 0 , i ) + s.substring( i + 1 );
   // }

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



# 2025

我太牛了 竟然自己写出了和双扫不一样的解法，这个时间复杂度比leetcode官方解法还要好一点。

```java
class Solution {
    public String minRemoveToMakeValid(String s) {
        int net = 0;
        StringBuilder sb = new StringBuilder();
        ArrayList<Integer> leftIndices = new ArrayList<>();
        for (char c : s.toCharArray()) {
            if (c == '(') {
                net += 1;
                leftIndices.addLast(sb.length());
            } else if (c == ')') {
                if (net == 0) {
                    continue;
                }
                net -= 1;
                leftIndices.removeLast();
            } 
            sb.append(c);
        }
        // remove from the last so that original order won't be changed
        for (int i = leftIndices.size() - 1; i >= 0; i--) {
            int indexToRemove = leftIndices.get(i);
            sb.deleteCharAt(indexToRemove);
        }
        return sb.toString();
    }
}
```