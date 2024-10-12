---

粘一点儿以后处理
---

https://www.1point3acres.com/bbs/thread-768263-1-1.html?fbclid=IwY2xjawFpqR1leHRuA2FlbQIxMAABHRPJ-tFiftUqCvwt4wgmFMXpgxXbBHgw2UFgcKt-KJ-YStFzWmjyCq_Jiw_aem_UndQOBjX33gIR-guEU31rw



## 心得系列

我觉得很重要的就是不要被分布式给局限了。不要什么都丢到server上 scale up.
例子就是 client 上也可以有一些 flow. 比如丰巢快递柜，这个柜子不用时时和server连通或者通讯。
因为柜子在外面，很难保持通讯不中断。

SD：设计todo list，重点在跟朋友分享 有点像google doc，但不用那么复杂

SD：设计延迟付款的系统，重点在怎么实现延迟，我感觉更偏向一个task schedu‍‌‌‌ler


## 交流的语句

Please feel free to stop me, interupt me any time, if you don't stop me, I will not stop talking, please understand that. 


一定要多问面试官
Do you have any questions for me here?
这时候如果有，就意味着面试官想讨论这方面的问题。



API design 之类的东西 可以不用想太多 抓重要的 

数据流 Data flow, 系统怎么输出数据取决于系统的数据怎么流入的。所以先从上游upstream看看数据的流动。

# API design

Restful API 其实写很细不重要
重要的是把输入输出讲好 
RESTful api, once you have your resources well organized, most of the api are trivial.
Let us focus on input, and output.

# Back of the evelope calculation

什么时候做？
当需要的时候。就是当你开始做那个系统的设计之前做，随手做，不用特别认真。


# 考虑什么 Failure recovery


# Non-func

usage pattern（是read heavy还是write heavy），是否需要考虑scalability的问题，是否需要highly available，consistency（strong consistency，还是eventual consistency，是否需要read-your-own-write consistency），latency是否有要求，data durability是否有要求，idempotency（对于付款相关的系统一定强调这个）。

以上有那么多non-functional req，我们不可能对于每个问题都把每个需求说一遍，那样太浪费时间。我们应该关注这个系统的重点，比如让你设计monitoring system，那availability就是重中之重，其他的点比如scalability，虽然重要，但不会是考察的重点，可以这时候简单提一下，在面试的后期再回头讲解如何解决这个问题。functional requirements 和 non-functional requirements应该力求在15分钟以内说完。有些点快速略过很重要，有时候你说的不是重点，虽然说了不会扣分，但是会耽误几十秒时间，使得后面没有时间说重点。系统设计面试，时间管理非常重要。



# 粘贴的一些

面试官把题目说出来以后，问问题，搞清楚需求。需求就分两种：functional requirements 和 non-functional requirements。对于functional req，要根据题目适当地问问题，了解需求。对于non-functional req，基本就是在以下的需求里面挑选出合适的：usage pattern（是read heavy还是write heavy），是否需要考虑scalability的问题，是否需要highly available，consistency（strong consistency，还是eventual consistency，是否需要read-your-own-write consistency），latency是否有要求，data durability是否有要求，idempotency（对于付款相关的系统一定强调这个）。以上有那么多non-functional req，我们不可能对于每个问题都把每个需求说一遍，那样太浪费时间。我们应该关注这个系统的重点，比如让你设计monitoring system，那availability就是重中之重，其他的点比如scalability，虽然重要，但不会是考察的重点，可以这时候简单提一下，在面试的后期再回头讲解如何解决这个问题。functional requirements 和 non-functional requirements应该力求在15分钟以内说完。有些点快速略过很重要，有时候你说的不是重点，虽然说了不会扣分，但是会耽误几十秒时间，使得后面没有时间说重点。系统设计面试，时间管理非常重要。

在搞清楚需求Func/Non Func之后，对于senior面试者，一般都是希望你从这里开始一直主导对话直到结束的。


API Design:
Restful: sync / async api, 需要很长时间的可以给用户返回一个 id，后面去查询任务进程。当然这个是很细节了。

Capacity Estimation:
QPS and peak QPS. peak is like 10x


High-level architechture:



Database:
- schema
- sql/nosql
- sharding, replication

Other:


所以我会直接说"okay now let me list a few topics that we wanna cover"，然后直接写下"API design", "capacity estimation", "high-level architecture design"，"database (schema, sql/nosql, sharding, replication)"和"other topics"。这些就是系统设计可能会讨论的大方面，提前写好可以保证你在主导对话的同时记得涵盖所有的点，不然自己一直说，可能一兴奋忘了说重点。列出这些点之后我会说"now let's start with API design, what do you think?"然后面试官就会回答好或者不好，你就可以继续说下去了。这里要注意的是，我们其实只是在假装"drive the conversation"，最后先说哪个再说哪个其实还是面试官决定的，除非在少数情况下面试官没有偏好，告诉你先说哪个都行，那你就可以自己随便挑。注意，如果面试官让你自己挑顺序，你也要按照合理的顺序，或者让自己最舒服的顺序，来最大化自己的收益。下面具体说说买个方面应该怎么应对。
API design。如果这个系统会暴露出一些API，那么一般这是个考察重点。在说这个主题的时候，一般都是对于每个API，定好名字，定好输入是哪些，输出是哪些就行了。有少数情况面试官会问你是用RPC还是Rest API，有的还会问一些Rest API的知识，这些都准备一下就行了。有的情况下设计synchronized API和asynchronized API会有很大不同，那么就要说清楚你选择哪个，以及理由是什么：比如有的时候一个操作需要执行很久，这时候一般选择设计一个async API先立刻返回一个id给caller，让caller拿着id去查询操作的执行情况，而不应该只设计一个sync API让caller一直阻塞在那里。有时候一些API看起来是读东西的，所以不需要任何输入，这时候需要注意，就算不需要任何输入，也需要一个id来识别caller，因为有时候要做rate limiting，这个在面试的时候随便提一下就可以了，真正如何做rate limiting可以放在最后"other topics"里面说。
Capacity estimation。这个简单，grokking里面讲得不错了。一般就是把QPS (read, write)，network bandwidth(upload, download)，disk storage算一下就行了。注意，算QPS和bandwidth的时候，要把average value和peak value都算一下，一般是先算average，然后你可以说假设peak比average高一个数量级。1M/day换算成QPS就是12/s，这个要记住。算disk storage的时候，直接说“假设我们存5年的数据”，因为5年约等于2000天，好算，同时不要忘记存在disk上的数据都有replication，一般直接说"let's assume the replication factor is 3"，然后乘以3就行了。
High-level architecture design。这个就是画图，没啥好说的。建议提前熟悉一下面试要用的画图软件。
Database (schema, sql/nosql, sharding, replication)。一般讨论数据库的时候就是讨论这些点，说schema的时候一般说一下有哪些表，每张表有哪些column，谁是primary key就行了。对于sql/nosql，要根据场景来说，
我遇到的只能用sql的场景很少，只能用nosql的场景也不多，一般都是二者都可以，但是我会跟面试官说虽然二者都可以，但我更偏向于nosql，因为这里写操作比较多。

但是大家还是要根据具体情况来，比如订票系统，牵扯到多张表的distributed transaction，那么只能是sql，还有购物相关的系统，user-order-item这些东西是天生relational的，那么sql也是更好的选择。

我印象中的其他系统就很少遇到那种只能用sql的。对于sharding，一般都有一个"shard by user id"还是"shard by item id"的trade off，如果是user id，那么会有Hotspotting问题（因为有的user特别活跃），如果是item id，虽然可以解决hotspotting问题，但是如果需要读某个user的数据，就需要访问所有的shard。这个trade off对于大多数题目都适用，如果是你在讲database的时候主动提出，会大大加分。


Other topics（cache/how to scale/push vs. pull/monitoring/rate limiting/failure handling/logging）。如果以上所有东西说完还有时间，一般面试官会让你在这里面挑一两个点讲一讲，就挑最拿手的讲就行了。注意，有时候这里面的某个点会是这个系统的重点，那么就应该把这个点放在前面着重讲。


# 心得总结 20241006
- 我们 User Obesssion 从系统的 user 问起？
    Who are the users of the system?
- 然后根绝不同的 user, 我们问 use case/
    What can user do with our system? Like how user can write to the system, and read from the system. How can this user use our system?
    What functions can the user perform?
- 小结一下，就是以用户为中心，引出 use case, which is part of the func requirement. 然后就每个 use case 可以接着问 non-func 并要求给一些数字进行数字的估算。
- 这里，问 non-func 应该着重从该 use-case 的 scale, performance, CP over AP 来问。scale 和 performace 都要就假设问一些数字。
- 问完每个 use case 的 non-func 可以问一下整个系统的 non-func 如果有必要的话。What is the overall quality attribute of the system?

non-func 的考虑，要抓重点问。
- Scalability
- CP over AP? 
  

# 总结 20241007
加了东西，就要说增加了复杂度。more element to maintain, documents, proper training for all devs to acknowledge this. 

# 总结 20241008
增加一个套路：
问 use case 从读写数据库两方面去想。
然后每个 use case, 去想 数据是什么样的，去问数据是什么样的，然后问non-func. 

因为我们看的都是 DDIA，所以数据和客户，始终是我们最关注的。
用户，一定要知道用户规模，和用户操作的频率， 从而知道数据规模啊。
当然用户也不是全部都在用，折合一下DAU。

由于我们想关注最重要的 clarificaiton, 所以我们要尽快结束前面搞清楚要求的部分。但是又要不粘锅，所以补上，I have more questions to clarify, but we can go to those question later in detailed design. I think right now we are ok for the high level design.


Latency 是所有的 non-func 里最重要的。


MVP: minimal viable product

# 20241009

Workload isolation
比如一套数据，很多用户上传，很多用户搜索。
如果 api 就都交给一个 service 的话，这个就 容易打架。
需要的是不一样性能的机型，往往也会交给两个组处理。所以 upload 和 query
的 service 也可以分开。

query 一个数据 （不模糊 不全文 不 prefix suffix search） 在 100ms 以内就能完成。


Blob store 一定有个 Blob server, 不然直接写磁盘吗？
Blob store 就意味着 里面已经包含了一个 blob server了。

Blob server 比 用 CDN 所以 trade off 来了，可以算自己的 blob server
和 CDN 的价格。然后 mix use both 也可以，只对部分的用户使用 cdn.

谈到scale的时候，不要上来就盲目 scale 这个那个，而是 problem-driven:
因为我要解决这个问题，所以我要 scale, 我要搞很多。


# 20241010

万金油的话
For the sake of this interview, I try to keep the scope
of this problem to the MVP version.

# 20241011

MySQL single server 可支持 1000 QPS, REDIS 可以 100k QPS

REDIS:
- Master-slave 的结构 disaster tolerance. 
- 可以通过lua脚本支持 Atomicity.
- 单线程数据库，i/omuptiplexing 实现并发（查一查）

九章的这个秒杀视频确实还不错
https://www.youtube.com/watch?v=4FLQMDJjNXU
