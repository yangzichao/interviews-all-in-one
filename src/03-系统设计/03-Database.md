
# Relational Database & ACID

Pros: 
- 可搞 复杂和灵活的 query
- 有效率地储存
- 结构自然
- ACID
    - Atomicity. 要么所有的Transaction一起成功，要么一起失败。
    - Consistency. 如果 commit 了一个transaction 那么未来的所有 query 都会看到这个改变。
    - Isolation. 强调的是并发状态下的Atomicity.即，一个操作A的同时，发生了另一个操作B。B只会看到A发生之前或者之后的状态，而不是任意的中间态。
    Isolation的意思是强调并发的两个transaction互相是 isolated 不会互相看到中间过程。
    - Durability. 数据库在COMMIT之后就会一直保持这个状态，不会改变。

Cons:
- Rigid structure 固定结构 固定 Schema
- Hard to Maintain/Scale
- Slower Read Operation


SQL: ACID, 结构化，读速度偏慢，有效储存 (efficient storage)


# NoSQL Database



Pros:
- No Fix Structure, easy change
- Support more data structure, like list, arrays, map. No need to use ORM (Object Relational Mapping)
- Faster Query

注：SQL 需要用 ORM 是因为需要帮助 mapping Table 和 Java 的这两类数据结构。

Cons: 
- Harder to analyze record
- Rarely support ACID (有些还可以)

3 types of NoSQL:

- Key/Value Store: 像个大HashMap
    - 可储存Blob 等二进制file
    - Redis. DynamoDB
- Document Store: 
    - 储存的是 document 的集合，每个document有一定的结构。
    - 每个document 都有一些不同的 attributes, attribute类型可以不同
    - 比如 一个Document Database 存YAML，XML，JSON
    - MongoDB, Cassandra
- Graph Database: 是Document Store的延伸
    - 可以 Link, Traverse, Analyze
    - 为了分析record之间的关系做出了特别优化
    - 用例：Fraud Detection，Recommendation engine
    - Amazon Neptune, NEO4J

NoSQL: 读快，做cache (in memory key/value), data is not structured(document stores).
注意 in memory kv db, 由于不在 disk 上，因此很昂贵。而且，撑死 1TB RAM 就已经很变态了，如果数据是几十TB 那么就不可能选择 In momory DB.





# Techniques to Improve DB performance

## Database Indexing (以 SQL 为例)
Full table scan is slow. 
Database Index 是一个 helper table, 从表单中的一列或者几列创造出来的。

Index Hash Table or B+ Tree -> Table

比如 Index Hash Table 就储存了 A => B 的 mapping 结果。


Index Trade off: 用空间换时间。写操作会变慢。
NoSQL 也用 Indexing.

## Database Replication 
To avoid SPOF. 

Cons:
- CRUD中除了读，都变更难了
- 并发也更难处理

NoSQL 的 replication 处理的很好

Replication 提升了 Fault Tolerance

## Database Partitioning (Sharding) 分区
- 把DB变成了分布式DB。即每个数据库服务器并不有全部的数据。因此我们可以储存更大的数据。
- NoSQL 很适合，因为他们天生就没有这么多关系。SQL sharding 很具挑战

Partition 还可以用来分割我们的架构。比如区分Webapp和mobile app的用户去不同的服务器。


# CAP Theorem
CAP CA只能二选一, 当有 Network Partition 的时候。如果没有 Network Partition, CA可以都保证。
- Consistency, Read request 的 response 都一样或者都错。
- Availability，所有 request 都成功，但是不保证全都正确
- Partition Tolerance. Network partition 对于分布式系统一定会发生的。
因此 CAP 只能三选二。

Consistency over Availability CP

Availability over Consistency AP


# Scalable Unstructure Data

什么是 Unstructure Data? 就是无 structure, schema, model. 
例子就是 Blob (Binary Large Object) Audio, Video, Image, PDF

User case:
1. User upload Blob. (Compressed, transcoding, sharing, backup)
2. DB backup/archiving. 
3. Web Hosting
4. Large datasets, very big

用 Distributed File System (DFS) 不好

用 Object Store 很好 
Pros:
- scalable storage 
- no limit on number of objects
- High limit for large single file
- provides HTTP + REST API
- Verisoning

Object Store 用一堆桶，每个桶装一类文件。

例子 Amazon S3, Azure Blob... 

这些 Object Store 都会给文件分级，高级的就贵 但是快。

使用 Object Store cons: 
- 使用 api 稍微麻烦一点
- IO performance 差一点


