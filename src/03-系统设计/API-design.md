API Design


# Types of API
1. Public APIs
    - exposed to general public
    - Any dev can use from theri application
    - Good practice: user should register before they can call
2. Private APIs
    - exposed only internally.
3. Partner APIs
    - Only to businesses

Good practices of define API:

Keeping the operations Idempotent (幂等)

API Pagination (如果request的payload非常大的话)

Asynchronous Operation 

Versioning API


## Remote Procedure Call (RPC)
## REST API

RPC 是 client 直连 server 然后 call method. 
**REST API 是暴露一个 named resource**


1. REST API, server is stateless.
2. REST API, cacheability 可以预先 cache一些result 这样就不必给 server了

Name Resource:
URI (Uniform Resource Identifier)

Best Practices for API naming
1. use noun only
2. make distinction between collections and single resources
3. use meaningful and clear names
4. name should be url friendly 

REST API gives only CRUD operations

GET are considered cacheable by default.
GET, PUT, DELETE are idempotent.

小结：  
非常重要：REST API 就是一个组织资源的方式 这是非常关键的理解。
为什么 REST API scalebility 好，因为1. stateless, 2. cacheable