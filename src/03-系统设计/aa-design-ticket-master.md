# 设计 ticket master

## 这个问题的独特之处

这一类的问题，都涉及到一个 inventory management. 
卖票和卖酒店房间一样，就是每个东西都是独特的。两个人定一个房间，同时下单就很麻烦。所以处理 high concurrency 下的问题是这个题的独特之处。
这和卖电子产品，或者工业品不一样，可以卖很多个。这是它有别于设计电商的地方。

### 问题一 double booking

所以 一个确保 Consistency 的办法就是 确保booking的api是 idempotency的
要使用 idempotency key. 这里就是，下单的时候，生成一个 reservationId or bookingId 发给前端，然后一直用这个key 就能保证不重复付钱。

还有就是前端加逻辑防止再次触发。

注意 double booking 是所有 payment 的特色，不是这个问题的最大特色。

### 问题二 two book one ticket at the same time
1. 不要悲观锁定，pessimistic locking, 不可scale.

2. 数据库限制。database constrain. 防止总数为负。这个更容易写一点。
3. 乐观锁定，optimistic locking. 即我们给row加一个 version. 
这个 version 在我们读的时候，一个版本，最后另一个版本，两个版本必须只差1.
4. 更好的 乐观锁定 给这个 row 的 version 变成一个 state, 即reserved. 
然后十分钟时候如果没有完成交易，就变回available.
5. distributed lock with TTL 就是用 Redis 的特性了。回头看看。




### 问题三 microservice 的 Atomicity 
因为 monolithic 很好处理 ACID, 但是 microservice 可能改好几个表都是独立的操作。如果保证这个 ACID 呢？
就是用 SAGA. Saga 是一系列的 local transactions, 如果一个失败了，就往回找补一个compensation transaction。 这个提供的就是 eventual consistency.


# 问题简单描述
其实就是一个订票的系统，具体要求会不一样。

剖析一个系统，我们先从系统的用户着手。这个一类系统有两种用户。

一种是，普通的用户，读多写少。一种是，商家，写多读少。而且对应的是两类 func requirement. 
Ticketmaster 和 online marketplace, hotel 一样都是对应的 inventory management。
这一类问题，既有 high level design, 也有 low-level 的细节。

如果不考虑 user 的区别 核心的 entity 就是 user, event, ticket.

## 类似的题目


# 其他粘贴板

搜索全文可以用 elasticsearch 是一个 full-text search engine.
Elastic Search 是一个数据库 但是不是主要的数据库 有他的话你就 must tolerate eventual consistency

考虑最后再加上 elastic Search 因为普通的 search 也好使。最后再优化吧。
