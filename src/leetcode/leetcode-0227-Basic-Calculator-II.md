# 227J. Basic Calculator II
https://leetcode.com/problems/basic-calculator-ii/

## 一个代码搞定 I II III 
分析所有的计算器问题：    

```java
class Solution {
    public int calculate(String s) {
        Queue<Character> tokens = new ArrayDeque<>();
        for( char c : s.toCharArray() ) {
            if( c == ' ') {
                continue;
            }
            tokens.offer(c);
        }
        tokens.offer('t');
        return evaluate(tokens);
    }
        /** 
        有三类字符，1是数字，2是括号，3是运算符。
        对字符串处理，我们的 notations 是这样的: sum +  preNum preOp num  c，
        sum 就是之前所有运算的结果, preNum preOp num 是我们最近邻的一个完整的表达式， c 是我们目前处理的字符。
        如果我们碰到的 c 是数字，说明我们需要更新 num.
        如果 c 是左括号，说明 c 前面是 preOp, 我们开始 dfs 直到 右括号结束，我们把整个括号内的结果作为num.
        如果 c 是右括号，意味着运算终止，此时我们要先处理 sum +  preNum preOp num 再打断循环并向上返回结果。
        如果 c 是一个运算符，那么我们也要立刻处理 sum +  preNum preOp num
            如果 preOp 是 +,-, => sum = sum +  preNum; preNum = +,- num; preOp = c.
            如果 preOp 是 *,/, => sum = sum; preNum = preNum *,/ num; preOp = c.
        因此运算结束的时候，我们会始终保留一个 sum + preNum, 因此需要返回 sum + preNum. 

        // sum + preNum preOp curNum c;
        //steps  input    char sum + preNum preOp curSum
        // 0     "3+2*2", '3', 0   + 0      '+'   3
        // 1     "3+2*2", '+', 0   + 3      '+'   0
        // 2     "3+2*2", '2', 0   + 3      '+'   2
        // 3     "3+2*2", '*', 3   + 2      '*'   0
        // 4     "3+2*2", '2', 3   + 2      '*'   2
        // 5     "3+2*2", 't', 3   + 4  this is an manually set extra step
        */
    public int evaluate( Queue<Character> tokens ) {
        int sum = 0;
        int preNum = 0;
        char preOp = '+';
        int num = 0;
        
        while(  !tokens.isEmpty() ) {
            char c = tokens.poll();
            if( '0' <= c && c <= '9' ) {
                num = num*10 + (c - '0');
            }else{
                switch(preOp) { //注意我们switch的是上一个operator
                    case '+':
                        sum += preNum;
                        preNum = num;
                        break;
                    case '-':
                        sum += preNum;
                        preNum = -num;
                        break;
                    case '*':
                        preNum *= num;
                        break;
                    case '/':
                        preNum /= num;
                        break;
                }
                
                preOp = c;
                num = 0;
            }
        }
        return sum + preNum;
    }
}
```


## Method 几乎最佳了
解释基本都在代码中
原理是用一个整数Stack来保存运算结果
```Java
class Solution {
    public int calculate(String s) {

        Stack<Integer> stack = new Stack<>();
        // 以下两个初始化相当于在运算的头部加上了一个 dummyhead "+0";
        char sign = '+';//用来保存上一个sign
        int sum = 0;    // 用来保存数字

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);

            if(Character.isDigit(c)){
                // 如果该字符是数字的话
                //这里是为了考虑连续几个字符都是数字的情况
                //可以得到正确的十进制数
                sum = sum*10 + (int) (c -'0');
            }
            // 这个判断条件是不能变的。
            // 前两个一起是选择了 + - * / 排除了 空格
            // 后面是因为循环到结尾的时候，还有一个运算没有做
            // 举例： " 3+ 2 " 会停止在 sum = 2, sign = '+'
            // 此时无论最后一位是 ' '还是 '2', 都可以保证强制算最后一次。
            if( (!Character.isDigit(c) && c != ' ' )|| i == s.length() - 1){
                // 注意以下判断条件都是 sign.equals(), 使用的是上个符号
                // 判断完上一个符号之后，由于现在这个符号也不是数字，所以
                // 首先应当对上一个符号进行的运算做清算。
                // 然后再把现在这个符号赋值给sign。
                if(sign == '+' ){
                    //如果上一个符号是 +， 上一个数字就是 +sum
                    stack.push(sum);
                }
                if(sign == '-' ){
                    //如果上一个符号是 -， 上一个数字就是 -sum
                    stack.push(-sum);
                }
                if(sign == '*' ){
                   //如果上一个符号是 * ， 上一个数字就是 sum
                    // 上上个就在 stack.pop() 里
                    stack.push(sum*stack.pop());
                }
                if(sign == '/' ){
                   //如果上一个符号是 / ， 上一个数字就是 sum
                    // 上上个(被除数)就在 stack.pop() 里  
                    stack.push(stack.pop()/sum);
                }
                //不要忘记更新sign;
                sign = c;
                //做了运算不要忘记令sum重回0
                sum = 0;
            }
        }


        //现在stack里面已经存满了一堆正负数了，求和即可。
        int ans = 0;
        for(int n : stack){
            ans += n;
        }
        return ans;
    }
}
```

2023 写的 应该还是套用模板了

```java
class Solution {
    public int calculate(String s) {
        int sum = 0;
        int prev = 0;
        char operation = '+';
        int cur = 0;
        s += 'n';
        for (char c : s.toCharArray()) {
            if (c == ' ') continue;
            if (Character.isDigit(c)) {
                cur = cur * 10 + (int) (c - '0');
            } else {
                switch (operation) {
                    case '+':
                        sum += prev;
                        prev = cur;
                        break;
                    case '-':
                        sum += prev;
                        prev = - cur;
                        break;
                    case '*':
                        prev *= cur;
                        break;
                    case '/':
                        prev /= cur;
                        break;
                }
                cur = 0;
                operation = c;
            }
        }
        sum += prev;
        return sum;
    }
}

```