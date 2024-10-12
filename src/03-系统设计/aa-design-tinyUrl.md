# 描述
常见的 tinyUrl 

# 分析以及重点提取

这个问题比较简单，就是一个对 url 压缩 和 解压缩的实施。
压缩可以通过某种算法，解压缩就是读db的 tinyUrl table.

## 独特的地方
1. 解码 tinyurl 时 返回不是 200 success 而是直接用 302 redirect 到新的 url.
2. 压缩 url 的方法需要讨论。
    - Base64 没有任何 collision issue，但是有 secuiry concern （我可以暴力尝试下一个能用的 url）
    - 哈希算法。有 collision 但是更安全。
        - 如何处理 collision? 
            - 我们用预先设置好的关键词接到长 url 后面，重新算 tiny url。 重复
            - 重复的过程比较复杂。因此我们用一个叫 bloom filter 的东西帮助用概率解决是否中招。