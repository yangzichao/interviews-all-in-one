# 273 Integer-to-English-Words

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
<div><p>Convert a non-negative integer to its english words representation. Given input is guaranteed to be less than 2<sup>31</sup> - 1.</p>
<p><b>Example 1:</b></p>
<pre><b>Input:</b> 123
<b>Output:</b> "One Hundred Twenty Three"
</pre>
<p><b>Example 2:</b></p>
<pre><b>Input:</b> 12345
<b>Output:</b> "Twelve Thousand Three Hundred Forty Five"</pre>
<p><b>Example 3:</b></p>
<pre><b>Input:</b> 1234567
<b>Output:</b> "One Million Two Hundred Thirty Four Thousand Five Hundred Sixty Seven"
</pre>
<p><b>Example 4:</b></p>
<pre><b>Input:</b> 1234567891
<b>Output:</b> "One Billion Two Hundred Thirty Four Million Five Hundred Sixty Seven Thousand Eight Hundred Ninety One"
</pre>
</div></section>
 
 ## Method One 
 
``` Java
class Solution {
    public String numberToWords(int num) {
        if(num == 0){
            return "Zero";
        }
        
        int billions = num/1000000000;
        int millions = (num%1000000000)/1000000;
        int thousands = ((num%1000000000)%1000000)/1000;
        int rest = ((num%1000000000)%1000000)%1000;
        
        String result = "";
        if(billions != 0){
            result += three(billions) + " Billion";
        }
        if(millions != 0){
            result += three(millions) + " Million";
        }
        if(thousands != 0){
            result += three(thousands) + " Thousand";
        }
        if( rest != 0) {
            result += three(rest);
        }
        return result.trim();
    }
    public String one(int n){
        switch(n){
            case 1: return " One";
            case 2: return " Two";
            case 3: return " Three";
            case 4: return " Four";
            case 5: return " Five";
            case 6: return " Six";
            case 7: return " Seven";
            case 8: return " Eight";
            case 9: return " Nine";
        }
        return "";
    }
    
    public String twoLessThan20(int n){
        switch(n){
            case 10: return " Ten";
            case 11: return " Eleven";
            case 12: return " Twelve";
            case 13: return " Thirteen";
            case 14: return " Fourteen";
            case 15: return " Fifteen";
            case 16: return " Sixteen";
            case 17: return " Seventeen";
            case 18: return " Eighteen";
            case 19: return " Nineteen";
        }
        return "";
    }
    
    public String two(int n){
        switch(n){
            case 20: return " Twenty";
            case 30: return " Thirty";
            case 40: return " Forty";
            case 50: return " Fifty";
            case 60: return " Sixty";
            case 70: return " Seventy";
            case 80: return " Eighty";
            case 90: return " Ninety";
        }
        return "";
    }
    
    public String three(int n){
        String ans = "";
        int hundreds = n /100;
        if(hundreds != 0){
            ans += one(hundreds) + " Hundred";
        }
        int tens = n%100;
        if(tens < 10){
            ans += one(tens);
        }else if( tens < 20){
            ans += twoLessThan20(tens);
        }else{
            ans += two(tens/10*10) + one(tens%10);
        }
        return ans;        
    }
}

```

这题真是没事儿别写，写不明白。

```java
class Solution {
    Map<Integer, String> map;
    char DEL = ' ';
    public String numberToWords(int num) {
        initiateMap();
        if (num == 0) return "Zero";
        StringBuilder sb = new StringBuilder();
        int B = 1000000000, M = 1000000, K = 1000;
        int Bs = (num / B) % K;
        int Ms = (num / M) % K;
        int Ks = (num / K) % K;
        int rest = num % K;
        if (Bs > 0) {
            sb.append(parse999(Bs)).append(DEL).append("Billion");
            if (num % B > 0) sb.append(DEL);
        }
        if (Ms > 0) {
            sb.append(parse999(Ms)).append(DEL).append("Million");
            if (num % M > 0) sb.append(DEL);
        }
        if (Ks > 0) {
            sb.append(parse999(Ks)).append(DEL).append("Thousand");
            if (num % K > 0) sb.append(DEL);
        }
        if (rest > 0) {
            sb.append(parse999(rest));
        }
        return sb.toString();
    }

    private String parse999(int num) {
        StringBuilder sb = new StringBuilder();
        int H = 100;
        int Hs = num / H;
        if (Hs > 0) {
            sb.append(map.get(Hs)).append(DEL).append("Hundred");
            num = num % 100;
            if (num > 0) sb.append(DEL);
        }
        if (num <= 20) {
            sb.append(map.get(num));
        } else {
            sb.append(map.get(num - num % 10));
            if (num % 10 != 0) {
                sb.append(DEL).append(map.get(num % 10));
            }
        }
        return sb.toString();
    }

    private void initiateMap() {
        this.map = new HashMap<>();
        map.put(0, "");
        map.put(1, "One");
        map.put(2, "Two");
        map.put(3, "Three");
        map.put(4, "Four");
        map.put(5, "Five");
        map.put(6, "Six");
        map.put(7, "Seven");
        map.put(8, "Eight");
        map.put(9, "Nine");
        map.put(10, "Ten");
        map.put(11, "Eleven");
        map.put(12, "Twelve");
        map.put(13, "Thirteen");
        map.put(14, "Fourteen");
        map.put(15, "Fifteen");
        map.put(16, "Sixteen");
        map.put(17, "Seventeen");
        map.put(18, "Eighteen");
        map.put(19, "Nineteen");
        map.put(20, "Twenty");
        map.put(30, "Thirty");
        map.put(40, "Forty");
        map.put(50, "Fifty");
        map.put(60, "Sixty");
        map.put(70, "Seventy");
        map.put(80, "Eighty");
        map.put(90, "Ninety");
    }
}
```