# 009. Palindrome Number

Determine whether an integer is a palindrome. An integer is a palindrome when it reads the same backward as forward.

Example 1:  
Input: 121  
Output: true

Example 2:  
Input: -121
Output: false

Explanation: From left to right, it reads -121. From right to left, it becomes 121-. Therefore it is not a palindrome.

Example 3:  
Input: 10
Output: false
Explanation: Reads 01 from right to left. Therefore it is not a palindrome.

Follow up:
Could you solve it without converting the integer to a string?

## Method 1

这个办法是先翻转字符 再对比。
这里有个问题就是 为什么不需要担心翻转产生的 overflow?

```java
class Solution {
    public boolean isPalindrome(int x) {
        int dupx = x;
        if (x > 0 || x == 0){
            int rev = 0;
            while(x != 0){
                rev = 10* rev + x%10;
                x = x/10;
            }
            return rev == dupx;
        }
        else{
            return false;
        }
    }
}
```
