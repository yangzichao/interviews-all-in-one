OO Design for a pub-sub system, involving Publisher, Subscriber, Queue and QueueManager


这个题说的很模糊，有至少以下几种具体细分的要求：
1. 经典 point to point
    一个 producer, 一个 queue, 然后 多个 consumer 从一个queue读。然后一个message只会传给一个 consumer. 因此一个consumer也只对应一个queue. 一个queue对应多个 consumer.
2. pub-sub
    多个queue（代表不同的topic）, 然后一个 consumer 可以从多个queue获取消息。因此 queue 和 consumer 是 多对多的关系。 
3. 以上，consumer 从queue 获取消息也分 queue push 和 consumer pull 的区分。因此会有 2*2 的要求。

- 注意到，producer 这里并不复杂， 他们只负责把一个general的message发送给一个queue就行。
- 这里我们把每个topic就用一个queue实现就好。
- 现实中，point to point 的部分，被包装成一个 consumer group. 而 comsumer group 是对应 pub sub 的多对多。
- Push 的模式对应的是 observer design pattern.
- 因为是ood 对于java要注意到，这里queue要注意 thread-safe的问题。如果是，多对多的情形，consu
