# 394J. Decode String

https://leetcode.com/problems/decode-string/

## Method: 最佳
用双栈是很正常的。
需要好好想想这个题。

```Java
class Solution {
    public String decodeString(String s) {
        Stack<Integer> stackI = new Stack<>(); // stack for number
        Stack<String> stackS = new Stack<>();  // stack for str
        int i = 0; // index

        String temp = ""; // temp 即是 temp 也是 最后的ans
        while(i < s.length() ){
            char c = s.charAt(i);
            if(Character.isDigit(c)){ //如果是数字的话 得到这个整数
                int num = 0;
                while(Character.isDigit(s.charAt(i))){
                    num = num*10 + (int) (s.charAt(i) - '0');
                    i++;
                }
                stackI.push(num);
            }else if( c == '['){
                // 这里我们遇到  '['，就我们知道要进入到下一层了
                // 由于下一层里需要处理重复的String，所以先在String栈里暂存一下我们现有的String
                //等过一会儿遇到 ']'了再取出来拼出正确的String。
                stackS.push(temp);
                temp = ""; //暂存完毕之后，暂时清空我们的temp
                i++;
            }else if(c == ']'){
                int times = stackI.pop(); //取出我们重复的次数
                String last = stackS.pop();//取出我们之前暂存的String
                //重建我们的新String。
                while(times > 0){
                    last += temp;
                    times--;
                }
                //拿到了正确的String之后出了这级括号
                temp = last;
                i++;
            }else{
                temp += String.valueOf(c);
                i++;
            }
        }
        return temp;
    }
}
```


## 分享一个错误的答案
分析的很好，但是对题目理解错了。   
没注意还有这种情况 "3[a2[c]]"
```java
class Solution {
    private class Pair{
        private String str;
        private int index;
        public Pair(){};
        public Pair(String str, int index){
            this.str = str;
            this.index = index;
        }
    }
    public String decodeString(String s) {
        int start = 0;
        StringBuilder sb = new StringBuilder();
        while(start < s.length()){
            int[] numberInfo = findNextNumber(s, start);
            int number = numberInfo[0];
            start = numberInfo[1];
            
            Pair strInfo = findNextStr(s, start);
            String str = strInfo.str;
            start = strInfo.index + 1;
            
            sb.append(buildString(str, number));
        }
        return sb.toString();    
    }
    // design
    // findNextNumber(String s, int start) return int[]{number, end};
    
    // findNextStr(String s, int start) return Map<String, Integer> map;
    
    // buildString(String s, int times) return string;
    
    private int[] findNextNumber(String s, int start){
        int index = start;
        int number = 0;
        while(index < s.length() && Character.isDigit(s.charAt(index))){
            number = number*10 + ( (int) s.charAt(index) - '0');
            index++;
        }
        return new int[]{number, index};
    }
    
    private Pair findNextStr(String s, int start){
        StringBuilder sb = new StringBuilder();
        int index = start + 1;
        while(index < s.length() && Character.isLetter(s.charAt(index))){
            sb.append(s.charAt(index));
            index++;
        }
        return new Pair(sb.toString(), index);
    }
    
    private String buildString(String str, int times){
        StringBuilder sb = new StringBuilder();
        int count = times;
        while(count > 0){
            sb.append(str);
            count--;
        }
        return sb.toString();
    }
}
```