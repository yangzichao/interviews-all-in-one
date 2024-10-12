# Software Architectural Patterns

In order to solve Anti-patterns (mistakes).

Pros:
- Save time and resources. (Everybody use it!)
- Avoid making architecture "Big Ball of Mud" 屎山
- Easy to Maintain.

Migration is common.

# Multi-Tier Architecture / Three Tier Architecture

- 物理上分层，每一层可以分别scale.
- Client-Server Model: 非常适合Rest API, 每一层既是上一层的 Server 也是下一层的 Client.
- 禁止跨层直接沟通，这样层级会更为清晰。否则假以时日会变得越来越难以维护。

## Three Tier Architecture

Most Common and Popular Architectural Pattern for Client-Server, Web-based services.

Three Tier Architecture is also called Monolithic Architecture because 
the business logic is bundled as one deployable unit.

UI : Presentation Tier, Tier 1
    - 显示信息 
    - 获取用户的输入 Take user's input
    - Usually no business logic. (注意这是针对Web的 不要被限制思路了 比如OTA)

Application Tier (Business Tier / Logic Tier):
    - Satisfy the functional requirement
    - Business Logic

Data Tier:
    - Usually has a database. Store our data.

You can scale each tier indenpendently. 

Cons:
- Monolithic Structure of our logic Tier. Codebase has all business logic.
- Makes our application slower and less responsive.
- May have to upgrade each server to run application.
- Low Development Velocity (You can hardly scale on developer)
    - We can Seperate app's code base into separate modules

Organizational scalability of the Three-Tier Architechture is limited.

Three tier is good for:
- Small codebase
- Small number of developers


Two-Tier Architecture:
Less common but still popular. 

Use case: 
- Desktop editors (Office Word)

Four-Tier Architecture:
Add an extra API-Gateway tier

We rarely have 5 or more than 5 tier, which is anti-pattern


# MicroService Architecture.

Monolithic Architecture cannot handle huge code base. 
We may have to consider migrating our code base from monolithic architecture into microservice architecture.

Microservices Architecture has our business logic as a collection
of loosely coupled and independently deployed services.

Pros:
- Easy to onboard new dev
- Easy to scale each service independently
- Better security
- Fault toleration, easier to identify the bug

Cons:
- We don't get all benefits for free, we have to follow best practice.
- Overhead and chanllenges.

Best Practice:

- Single Responsibility Principle:
    - each service takes just one aspect in:
    - Business capability
    - Domain
    - Resource 
    - Action

API gateway can be break down into micro services

API gateway ->
    - Web API Gateway
    - Mobile API Gateway
    - Third Party API Gateway

- Separate Database for each service
    - It is clear the benefit for this.
    - Though have more data replication. But that is the overhead we have to accept.

# Event Driven Architecture
Microservice A to B has dependency relations.
A needs to know api of B, needs to get B response back.

Event-Driven Architecture

Event is an immutable statement of a fact or a change.

Structure of Event-Drive Architecture

- Event Emmitters
- Message Broker
- Event Consumers

Now Microservices A just needs to generate events.
Now not only Microservice B can subscribe to A's events,
other services can subscribe the A's events without changing A.
以前的模式，A需要知道所有要发送端口的API，以及其他信息。每加一个新的service都需要更改A。现在A就不用动了。

Real Time Stream Analysis 也是它的一个特色 所以有事件流的话 可以想到它
- Analyze streams of data
- detect patterns
- Act upon data in real-time

Fraud detection. 



Event Sourcing Pattern ？


CQRS Command Query Responsibility Segregation Pattern
1. High load of update and read operation
    - One type operation for each. 
    - 让被update的DB 发送 events 去 被读的多 DB 
2. Join multi table separated belong to different microservices
    - Join SQL and noSQL. 
    - A B DB都给 C发events. C搞一个综合的view. 我们从C读就好了。

比如 商品的库存和评价。分成两个service, 但是当你搜索商品时 就一起读出来。

