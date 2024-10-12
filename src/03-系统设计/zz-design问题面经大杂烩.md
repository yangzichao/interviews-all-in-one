一个host有三个api，返回这个host的cpu, disk, network信息，然后自己写一个client程序判断这个host是不是healthy。
题目非常vague，但是也没什么难的，基本就是call api然后if else就完事了。后面有一些system design的问题，比如load balancing，如果加速（cache）等等。有两个问题答得不是很好：
1. 面试官问怎么与server沟通。我说server有一个client，然后client端inject这个client就行了。
2. 面试官问如果处理call server时候的exception。我说就用t‍‌‌‌‌‍‌‌‍‍‍‍‌‌‍‍‍‌‍‌‌ry catch block，如果有exception就看看是什么，如果是client failure就log，如果是throttle就retry。
总体上来讲就像是工作中一个问题和别人探讨。很想知道自己挂在了哪里（可能system design不够细致？可能讲故事时候人家觉得scope不够senior？）Move on, 希望最近面麻的其他人好运吧！


我觉得你的回答不够具体。
1. 面试官问怎么与server沟通。我说server有一个client，然后client端inject这个client就行了。
这里的关键是client是什么？ client可以是一个browser，可以是内部程序。对应的protocol是不同的，例如http或tcp/rpc。可以从最简单的开始，例如http,沟通起来无非就是get, post, put, delete. 这个例子显然get就够用了，如果需要附带其他payload，就用post。具体的client可以用fetch, axios,甚至curl，这些需要具体而明确。只要有明确的host地址和route，沟通就像ping Google一样简单明了。可以搞得很fancy，但是本质不就是ping么。
2. 面试官问如果处理call server时候的exception。我说就用try catch block，如果有exception就看看是什么，如果是client failure就log，如果是thr‍‍‌‌‍‍‌‌‍‌‌‍‍‌‍‍‌‌‌ottle就retry。
有异常是要trycatch，我觉得你的回答大致没问题，对于400+和500+的异常应有善后。当然可以做得更好。方向应该是追问exception的来源。为什么会有exception。这个题目想通过cpu/mem/network 来预测系统是否正常。那么会有什么异常呢？ 400+ errors显然应该避免的，你要设计的东西事关系统性能，不能自己还出错吧。会有什么500+ errors呢？ 几个比较著名的：
Server overloads: Temporary server overloads can cause a 500 error.   => CPU
Memory: Out-of-memory (OOM) issues can cause a 500 error.              => Memory
Connectivity: Temporary connectivity issues can cause a 500 error.       => Network
所以这些exception对你要设计的东西而言不是bug而是feature, 不是failure而是data point，它们正是系统是否健康的关键指标。


