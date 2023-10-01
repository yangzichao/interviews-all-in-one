# 065 Valid-Number

difficulty: Hard

<style>
        section pre{
          background-color: #eee;
          border: 1px solid #ddd;
          padding:10px;
          border-radius: 5px;
        }
      </style>
<section>
<div><p>Validate if a given string can be interpreted as&nbsp;a decimal number.</p>
<p>Some examples:<br>
<code>"0"</code> =&gt; <code>true</code><br>
<code>" 0.1 "</code> =&gt; <code>true</code><br>
<code>"abc"</code> =&gt; <code>false</code><br>
<code>"1 a"</code> =&gt; <code>false</code><br>
<code>"2e10"</code> =&gt; <code>true</code><br>
<code>" -90e3&nbsp; &nbsp;"</code> =&gt; <code>true</code><br>
<code>" 1e"</code> =&gt; <code>false</code><br>
<code>"e3"</code> =&gt; <code>false</code><br>
<code>" 6e-1"</code> =&gt; <code>true</code><br>
<code>" 99e2.5&nbsp;"</code> =&gt; <code>false</code><br>
<code>"53.5e93"</code> =&gt; <code>true</code><br>
<code>" --6 "</code> =&gt; <code>false</code><br>
<code>"-+3"</code> =&gt; <code>false</code><br>
<code>"95a54e53"</code> =&gt; <code>false</code></p>
<p><strong>Note:</strong> It is intended for the problem statement to be ambiguous. You should gather all requirements up front before implementing one. However, here is a list of characters that can be in a valid decimal number:</p>
<ul>
	<li>Numbers 0-9</li>
	<li>Exponent - "e"</li>
	<li>Positive/negative sign - "+"/"-"</li>
	<li>Decimal point - "."</li>
</ul>
<p>Of course, the context of these characters also matters in the input.</p>
<p><strong>Update (2015-02-10):</strong><br>
The signature of the <code>C++</code> function had been updated. If you still see your function signature accepts a <code>const char *</code> argument, please click the reload button to reset your code definition.</p>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public boolean isNumber(String s) {
        //这个问题需要讨论的是四个符号如何合法。
        // e 和. 都只能出现一次。
        // e 的合法是需要 e 前后都有合法数字。
        // 并且 e 之前可以出现小数点，e之后只能是纯数字和首位正负号
        // . 的合法是，只能出现在e之前，并且小数点前或者后面至少有数字
        // + - 的合法是只能出现在字符最开始和 e 之后, 因此我们需要index的信息
        // 这个字符串被e分成了两部分，当e出现之后进行第二次检查。
        s = s.trim();
        boolean decimalSeen = false;
        boolean eSeen = false;
        boolean numberSeen = false;
        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if( c <= '9' && c >= '0'){
                numberSeen = true;
            }else if( c == 'e') {
                if(!numberSeen || eSeen) {
                    return false;
                }
                eSeen = true;
                numberSeen = false;
            }else if ( c == '.'){
                if(eSeen || decimalSeen){
                    return false;
                }
                decimalSeen = true;
            }else if( c == '+' || c == '-'){
                // !(i == 0 || s.charAt(i - 1 ) != 'e')
            }else if( c == '+' || c == '-'){
                if( i != 0 && s.charAt(i - 1) != 'e'){
                    return false;
                }
            }else {
                return false;
            }
        }
        return numberSeen;
    }
}
​
```

上面的代码写的有点不太易懂，我们需要把算法搞清楚，下面的可读性会强一点。


```java
class Solution {
    // 学英语 一个小数 decimal number
    // 整数部分 integer part, 小数部分 decimal part
    public boolean isNumber(String s) {
        /**
        // 先由e或者E分成两部分
        // e 或者 E, PartA, E之前是个小数，PartB, E 之后是个整数
        // partB: 整数的字符的要求，除了第一位可以是 + -, 别的必须是数字，而且不能只是一个 + -
        // partA: 
                小数的要求，只能第一个字符是 + -，所以我们先检查第一位，之后就不允许出现+-了
                dot 最多出现一次
                0 - 9 至少出现一次，即不能是 empty number
                进入 partB 之前，立刻检查partA是不是合法的 
        */

        if (s.length() == 0) return false;

        Queue<Character> charQueue = new ArrayDeque<>();
        for ( char c : s.toCharArray() ) {
            charQueue.offer(c);
        }
        // 先检查符号
        if (charQueue.peek() == '+' || charQueue.peek() == '-') {
            charQueue.poll();
        }
        if (charQueue.isEmpty()) return false;

        boolean hasDecimal = false;
        boolean isEmptyNumber = true;
        // 现在我们只关心 . 数字 和 e，别的字符都不允许
        // . 最多出现一次
        // 数字至少出现一次
        // e出现了之后 我们立刻检查前面是不是满足了 数字至少出现一次
        while (!charQueue.isEmpty()) {
            char c = charQueue.poll();
            if (c == '.') {
                if (hasDecimal) return false;
                hasDecimal = true;
                continue;
            }
            if (c == 'e' || c == 'E') {
                if (isEmptyNumber) return false;
                return isInteger(charQueue);
            }
            if (!Character.isDigit(c)) {
                return false;
            }
            isEmptyNumber = false;
        }
        return !isEmptyNumber;
    }

    // isInteger 用来检测 partB 是不是一个整数
    private boolean isInteger(Queue<Character> charQueue) {
        if (charQueue.isEmpty()) return false;
        if (charQueue.peek() == '+' || charQueue.peek() == '-') {
            charQueue.poll();
        }
        if (charQueue.isEmpty()) return false;

        while (!charQueue.isEmpty()) {
            if (!Character.isDigit(charQueue.poll())) {
                return false;
            }
        }
        return true;
    }
}
```