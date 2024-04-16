# 位运算 Bit Manipulation

<p>
1. << : 左移运算符，num << 1,相当于num乘以2  低位补0   <br/>
2. >> : 右移运算符，num >> 1,相当于num除以2  高位补0   <br/>
3. >>> : 无符号右移，忽略符号位，空位都以0补齐          <br/>
4. % : 模运算 取余																	<br/>
5. ^ : 位异或 第一个操作数的的第n位于第二个操作数的第n位相反，那么结果的第n为也为1，否则为0 <br/>
6. & : 与运算 第一个操作数的的第n位于第二个操作数的第n位如果都是1，那么结果的第n为也为1，否则为0 <br/>
7. | : 或运算 第一个操作数的的第n位于第二个操作数的第n位 只要有一个是1，那么结果的第n为也为1，否则为0 <br/>
8. ~ : 非运算 操作数的第n位为1，那么结果的第n位为0，反之，也就是取反运算（一元操作符：只操作一个数） <br/>
<p>



[Java Bit-shifting circular？](https://stackoverflow.com/questions/21685632/is-java-bit-shifting-circular)

```java
a << 32 == a; // true, why? 因为实际上相当于 你输入的移动位数要 %32 即 a << (b%32)
a << 32 & 0x1f;
```

LC 136 LC 137

出奇迹：
268

## Swap

<pre>
注意要避免 a == b 的swap，否则就坏了。
交换 a,b
a = a^b;
b = a^b; (a^b)^b = a
a = a^b; (a^b)^((a^b)^b) = (a^b)^a = b 
</pre>
