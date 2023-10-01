# 150 Evaluate-Reverse-Polish-Notation 
 
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
<div><p>Evaluate the value of an arithmetic expression in <a href="http://en.wikipedia.org/wiki/Reverse_Polish_notation" target="_blank">Reverse Polish Notation</a>.</p>
<p>Valid operators are <code>+</code>, <code>-</code>, <code>*</code>, and <code>/</code>. Each operand may be an integer or another expression.</p>
<p><strong>Note</strong> that division between two integers should truncate toward zero.</p>
<p>It is guaranteed that the given RPN expression is always valid. That means the expression would always evaluate to a result, and there will not be any division by zero operation.</p>
<p>&nbsp;</p>
<p><strong class="example">Example 1:</strong></p>
<pre><strong>Input:</strong> tokens = ["2","1","+","3","*"]
<strong>Output:</strong> 9
<strong>Explanation:</strong> ((2 + 1) * 3) = 9
</pre>
<p><strong class="example">Example 2:</strong></p>
<pre><strong>Input:</strong> tokens = ["4","13","5","/","+"]
<strong>Output:</strong> 6
<strong>Explanation:</strong> (4 + (13 / 5)) = 6
</pre>
<p><strong class="example">Example 3:</strong></p>
<pre><strong>Input:</strong> tokens = ["10","6","9","3","+","-11","*","/","*","17","+","5","+"]
<strong>Output:</strong> 22
<strong>Explanation:</strong> ((10 * (6 / ((9 + 3) * -11))) + 17) + 5
= ((10 * (6 / (12 * -11))) + 17) + 5
= ((10 * (6 / -132)) + 17) + 5
= ((10 * 0) + 17) + 5
= (0 + 17) + 5
= 17 + 5
= 22
</pre>
<p>&nbsp;</p>
<p><strong>Constraints:</strong></p>
<ul>
	<li><code>1 &lt;= tokens.length &lt;= 10<sup>4</sup></code></li>
	<li><code>tokens[i]</code> is either an operator: <code>"+"</code>, <code>"-"</code>, <code>"*"</code>, or <code>"/"</code>, or an integer in the range <code>[-200, 200]</code>.</li>
</ul>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public int evalRPN(String[] tokens) {
        ArrayDeque<Integer> values = new ArrayDeque<>();
        for(String str : tokens){
            if(str.equals("+")){
                values.push(values.pop() + values.pop());
            }else if(str.equals("-")){
                values.push(- values.pop() + values.pop());
            }else if(str.equals("*")){
                values.push(values.pop() * values.pop());
            }else if(str.equals("/")){
                int a = values.pop();
                int b = values.pop();
                values.push(b/a);
            }else{
                values.push(Integer.valueOf(str));
            }
        }
        return values.pop();
    }
}
​
```



202309写的，很多年过去了这也太巧了吧，和之前的几乎一样。


```java
class Solution {
    public int evalRPN(String[] tokens) {
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        for (String token : tokens) {
            switch (token) {
                case "+":
                    stack.push(stack.pop() + stack.pop());
                    break;
                case "-":
                    stack.push( - stack.pop() + stack.pop());
                    break;
                case "*":
                    stack.push(stack.pop() * stack.pop());
                    break;
                case "/":
                    int b = stack.pop();
                    int a = stack.pop();
                    stack.push(a / b);
                    break;
                default:
                    stack.push(Integer.valueOf(token));
            }
        }
        return stack.pop();
    }
}

```