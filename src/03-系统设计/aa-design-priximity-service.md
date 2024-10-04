# Proximity Service

这是 Google Map, Yelp 等的问题。


# 问题解析：

### 特色之处
这个问题最独特的地方就是，2d 的数据快速 index 真的很独特。
这是这个问题的最独特之处，否则它就和论坛啊，什么的题目没有区别。

所以这个题专门要解决的细节问题就是，如何2d的数据快速的查找.
传统的 Index 比如 B+ Tree 对 2d data 非常差。

候选有 R-tree, geohashing, quadtree. 其中 r-tree 我不懂。

解决了这个选择之后，就能很好的解决其他的问题。

### func & non-func

- 用户根据自己的地理位置，快速获取附近的几百个地点的列表。
    - 读取速度要快, < 1 second
- 用户可以读取商户的详细信息。
- 商家更新自己的地点的详细信息。
    - data freshness, < 1h

这里 overall 的 non-func 要求就是 读取地点要快，所以我们着手于这个读取快，因此核心就到了数据结构的选择。
具体的数据库并不重要，比如我们用 Elastic search 还是 别的，其实无所谓的，很多都支持 geocode based search.



# 特色知识

### quadtree 四叉树 
不用特别精通

### Geohashing


cache 和 disk 之间怎么保证 consistency, 一言以蔽之，加version


