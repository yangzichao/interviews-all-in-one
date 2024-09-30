# Load Balancer

Load Balancer can: 
1. Scalability.
2. High Availability.
3. Increase the Performance (Throughput)
4. Maintainability 


Type of load balancer:
1. DNS is internet infra. It is a load balancer by default. Round Robin.
2. Hardware load balancers:
3. Software load balancers:
4. Global Server Load Balancer (GSLB): 



一些 Load Balancing Solutiongs
1. NGINX 
2. AWS - Elastic Load Balancing (ELB)
    - Application Layer (Layer 7) load balancer
    - NetWork (Layer 4) load balancer
    - Gateway load balancer

# Message Broker (Message Queue)
Synchronous Communication: Requires both client and server should be healthy.

Message Broker, using Queue to store messages between senders and receivers.
It can also:
1. Store/temporariyly buffering the messages
2. Message routing
3. Transformation validation
4. Load balancing. 

Message Broker is the fundamental building block of async software architechture. 


如果涉及到 Transaction 就要想到 Async, 想到把transaction service分成两个 services乃至多个services, 一个是 frontend service， 一个 fulfillment service, 中间用 message broker 连接起来.

所以一个很重要的考量就是，是不是 Async software architechture？


Message Broker's: publish/subscribe pattern 
- publish messages to a channel
- services then subscribe to that channel
- Get notified when a new event is published


Keyword Mapping:

Add fault tolerance -> Message Broker
Async -> Message Broker
Availability and scalabiltiy
Lower the performance (latency) a little, you have to know this trade-off


一些 real-world resolution
- Apache Kafka
- Amazon Simple Queue Service (SQS)


# API Gateway service

用户可能需要调用很多个 service 但是 我们只需要一个API Gateway 搞一切

API Gateway 沿袭一个 pattern 叫 API composition 
就是说把所有的 service 和 API 都放到一个 大 API 里

可以把 security, authorization, authentication 都放到一个地方
- Security
    - Permission related
    - Rate-limitor

API Gateway 还可以进行 Request Routing, 将 Request 发给相应的 api

API Gateway 还可以 monitoring and Alerting

API Gateway 甚至还可以 translation 将不同的request都翻译成 service可以理解的



API Gateway 最主要的目的 还是 api composition 和 request routing. 
但是API Gateway 可能是一个 SPOF. 

API Gateway example:
1. Netflix Zuul
2. Amazon API Gateway


# CDN Content Delivery Network

Static Assets need to be closer to our user.

- Pull Strategy,
- Push Strategy, push and setting a long TTL. 是更好的办法。主动发布到各地。


CDN example

- Akamai
- CloudFlare