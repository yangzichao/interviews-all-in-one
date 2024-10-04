
3 type of requirements

- Feature of the system (functional requirements)
    - What does the system must do
    - When have an input to the system, what is the output of the system
    - This does not dictate the architechture of our system
- Quality Attributes (non-functional requirements)
    - What the system must have
        - Scalability, Availability, Reliability, Performance, Security
    - This dictate the architechture of our system
- System Constrains (Limitation and boundary)
    - Like strict deadlines, limited budget, and limited human power




# Functional Requirements

最好的获取信息的方法
1. Use Cases
    - Situation / Scenario in which our system is used
2. User Flows
    - Step by step / graphical representation of each use case



# Non-functional Requirements
1. Qualities of functional requirement
2. Overall properties of the system
    - must be measurable and testable 
    - Trade off 就在这里了
    - 可行性

## Quality Attributes

### Performance
- Performance - Response time 响应时间
Response time = processing time (process request and sending response in server) + waiting time (wait in queue)
所以要测试两部分的时间
第一要测试响应时间正确，最好get a distribution of the response time
关注 Tail Latency （shorter tail 最好）
例子: 30 ms at 99th percentile of response time

- Performance - Throughput 吞吐量
Throughput - Amount of work / data done by the system per unit time

Performance 如果不行，可能的原因 High CPU usage, high memory usage, too many connections/IO, message queue is at capacity

### Scalability 

- Scale up, vertical scalability
    - Upgrade the single computer
- Scale out, horizontal scalability
    - Add more units (servers, database)
- Team/Organization scalability 
    - Add more engineer, if we can separate the work better.
    - Isolation of the work, loosely coupled module/service

### Availability

- 多少时间下线 down time, availability = uptime / total time
- MTBF, mean time between failures
- MTTR, mean time to recovery

更精确的表达是 Availability = MTBF / (MTBF + MTTR)


#### High Availability and Fault tolerance
1. Human Error
2. Software Errors
3. Hardware Failures

Fault Tolerance 可以 提高 availability
1. Failure prevention
    - elimniate any SPoF (single point of failure)
    - Spatial Redundancy, more replica
        - Active - Active architecture: multi master
        - Active - Passive architecture: master slave
    - Time Redundancy, repeat request multiple times
2. failure detection and isolation
    - Monitoring Service, listen to the pings/heartbeat of instances
3. Failure recovery
    - Stop, restart the host, rollback
    - rollback is very common in database



#### SLA, SLO, SLI

- SLA: service level agreement. Legal contracts about quality Arrtributes
- SLO: service level objectives. Target of our service. One SLA has many SLO.
- SLI: Service Level indicators. Quantitave measure of our compliance.
通过SLI看看SLO是不是得到合规，通过检查每个SLO看看SLA是不是合规。



# 总结时间 

总结一下 func:
- 要尽快的搞明白最主要的几个 func requirements, 甚至可以分类。
    比如有很多个 func requirements, 一类是读，一类是写。或者一类是商家，一类是用户。
    所以快速分类，然后再写每个分类下的 func requirement.
- 然后可以一个一个func来分析这个最重要的non-func 是什么。

总结一下 Non-func:

- Scalability：
    - 能 adding more resources to increase the system's capability
    - vertical scale: more powerful machine
    - Horizontal scale: add more resource
    - Team scale: micro-service / event-driven service

- Availability: 
    - 定义
        - 使用 uptime / total time 或者
        - MTBF, mean time between failures
        - MTTR, mean time to recovery
        - Availability = MTBF / (MTBF + MTTR) 
    - Availability: Fault Tolerance
- Performance:
    - Performance: Latency
        - 用 response time 来表征 = waiting time + processing time
    - Performance: Throughput
        - work load / unit time, QPS
    - Performance: Data Freshness 带来的 throughput 挑战。这种涉及到其他的用户需要获取别的用户上传的数据，比如论坛，比如Yelp。
- Reliability
- Database / Data related :
    - CAP:
        - Data: C
        - A
        - P
    - CP:
    - AP:

- Security



- Microservice / event driven service 带来的
    - Maintainability
    - Extensibility
    - Inter-operability



思路：
思考什么是最重要的用户体验。

