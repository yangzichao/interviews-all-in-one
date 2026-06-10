# Guava `RateLimiter` —— 怎么回事

> 配套阅读：本题 **Part 8（滑动窗口限流器）**。Part 8 让你手写一个「数窗口内请求数、
> 超了就拒」的 **sliding-window** 限流器；Guava 的 `RateLimiter` 是另一个流派 ——
> **token bucket（令牌桶）**。把两者放一起看，正好把「限流」这件事的设计空间撑开。
>
> 类全名：`com.google.common.util.concurrent.RateLimiter`（长期标 `@Beta`，单 JVM 用）。

---

## 一句话定位

Guava `RateLimiter` 控制的是**速率（rate，每秒多少个 permit）**，不是「某窗口内的总数」。
它是一个**阻塞式节流器**：你 `acquire()` 一个许可，拿不到就让**当前线程睡**到能拿为止。
它**不是**一个「计数 + 拒绝」的配额器（那是 Part 8 在做的事）。

| 维度 | Part 8（你手写的） | Guava `RateLimiter` |
|------|-------------------|---------------------|
| 模型 | sliding-window counter | token bucket（平滑令牌桶） |
| 控制的量 | 窗口 `(t-W, t]` 内的**总次数** ≤ N | 长期**平均速率** ≤ permits/sec |
| 超限时 | 立刻返回 `false`（拒绝） | **阻塞当前线程**直到有许可（也有非阻塞 `tryAcquire`） |
| 时间来源 | 调用方传 `timestamp` | 读真实时钟（`System.nanoTime`） |
| 范围 | 取决于你实现 | **单 JVM**，不跨节点 |

---

## 使用场景（生产里什么时候掏出它）

核心判断:**你在限制「自己往外发」的请求,而不是「挡别人打进来」。**

✅ **适合**(客户端 / outbound —— 节奏你自己说了算,等一下没关系):

- 调**第三方 API**(交易所、支付、短信、地图、LLM…),对方有 QPS 上限,你不想被 429 / 封号。
- **批量任务**往数据库 / 下游服务写,主动把自己压成匀速,别一波打穿下游。
- **爬虫 / 数据回填**,礼貌限速。

这些场景为什么用阻塞式 `acquire()` 正好:外发节奏你能控制,**慢一点、排队等一下是可接受的**。

❌ **不适合**(交给别的组件):

- **挡外来请求 / 服务端入口**:别让请求线程睡(占着线程、反而拖垮自己),要「立刻拒」——
  用 **API 网关**(Nginx / Envoy / 云网关),`tryAcquire()` 只是勉强能凑合。
- **跨集群全局限流**(「所有实例加起来每秒别超 N」):Guava 单机各算各的,合起来就超了;
  要 **Redis**(`INCR`+`EXPIRE` / Lua 令牌桶)这类共享存储来记总账。

一句话:**Guava 管的是最里面那段「我自己调下游」的匀速阀门;入口限流和跨节点限流是
网关 + Redis 的活儿。**

| 你想做的事 | 用什么 |
|-----------|--------|
| 限制我自己 outbound 调用的速率(单机) | ✅ Guava `RateLimiter` |
| 拦截打到我服务的外部流量 | API 网关(Nginx / Envoy / 云网关) |
| 全集群一致的限流 | Redis / 分布式令牌桶 |
| 把请求分散到多实例(这是负载均衡,不是限流) | L4/L7 LB(NLB / ALB / Envoy) |

---

## API 速览

```java
// 1) 造一个：稳定 5 permits/秒
RateLimiter limiter = RateLimiter.create(5.0);

// 2) 阻塞获取：拿不到就睡，返回实际睡了多少秒（double）
double slept = limiter.acquire();        // 取 1 个
limiter.acquire(3);                      // 取 3 个，等更久

// 3) 非阻塞 / 限时：拿不到立刻或最多等一会儿
boolean ok  = limiter.tryAcquire();                          // 立刻，拿不到返回 false
boolean ok2 = limiter.tryAcquire(100, TimeUnit.MILLISECONDS);// 最多等 100ms

// 4) 运行时改速率
limiter.setRate(10.0);

// 5) 带预热（冷启动慢、逐渐加速到全速）
RateLimiter warm = RateLimiter.create(100.0, 3, TimeUnit.SECONDS);
```

要点：
- `acquire(n)` 里的 `n` **可以大于每秒速率**，只是会等更久；它不会报错。
- `acquire()` 返回值是「为这次获取睡了多少秒」，常被忽略，但能用来观测被节流的程度。
- 没有 `release()` —— 令牌是按时间自然「长」出来的，不是借还模型（区别于信号量）。

---

## 核心机制：惰性令牌桶 + 「记账给下一个人」

### 高层算法（先记这版，再抠细节）

把它想成一个**桶**:

1. 桶以固定速率 `rate` 往里**滴令牌**(每 `1/rate` 秒一个);滴满 `maxPermits` 就不再加
   —— 这个上限就是你能攒下的「突发额度」。
2. 来个请求要 `n` 个令牌:
   - 桶里 **≥ n**:拿走 `n`,**立刻放行**(这就是空闲后能瞬间突发的原因)。
   - 桶里**不够**:把现有的先拿走,**剩下的等「按速率滴出来」那么久**,再放行。

就这么简单 —— **匀速滴、够就走、不够就等差额**。Token bucket 的全部精髓在这两步。

Guava 的实际实现只是把第 1 步「滴令牌」**惰性化**了:不开后台线程真去滴,而是记一个时间戳
`nextFreeTicketMicros`,等下次有人来时,用「现在 − 上次」的时间差**反推**这段时间该滴多少。
语义和「真有个线程在滴」完全等价,但**零线程、每次调用 O(1)**。下面是这套惰性实现的细节。

### 实际实现：惰性 + 时间戳

很多人以为令牌桶有个后台线程在「每隔 1/rate 秒往桶里丢一个令牌」。**Guava 没有后台线程。**
它用一组数（在 `SmoothRateLimiter` 里）惰性计算：

- `storedPermits` —— 当前攒下的（过去没用完的）许可数。
- `maxPermits` —— `storedPermits` 的上限（决定能 burst 多少）。
- `stableIntervalMicros` —— `1 / permitsPerSecond`，稳定状态下两个许可之间的间隔。
- `nextFreeTicketMicros` —— **下一个许可免费可得的时刻**（一个「欠账时钟」）。

### resync：用「时间差」换「攒下的许可」
每次请求进来，先 `resync(now)`：如果 `now > nextFreeTicketMicros`，说明这段时间限流器是
闲着的，就把闲置时长换算成许可加进 `storedPermits`（封顶 `maxPermits`），再把
`nextFreeTicketMicros` 推到 `now`。这就是「惰性补充」——不补后台线程，只在被调用时算账。

### reserveEarliestAvailable：先服务、后记账（关键技巧）
`acquire(n)` 的核心是这样的（简化）：

```
resync(now)
returnValue       = nextFreeTicketMicros          // ★ 先记下「旧」的时刻
spendFromStored   = min(n, storedPermits)         // 先花攒下的
freshPermits      = n - spendFromStored           // 不够的部分按稳定速率现挣
waitMicros        = cost(spendFromStored)         // SmoothBursty 里这部分=0
                  + freshPermits * stableIntervalMicros
nextFreeTicketMicros += waitMicros                // ★ 把代价推给「未来」
storedPermits        -= spendFromStored
return returnValue                                // ★ 当前线程只睡到「旧」时刻
```

**最反直觉、也最该记住的一点**：函数返回的是**旧的** `nextFreeTicketMicros`，而把这次请求
的代价 `waitMicros` **加到了未来**。也就是说：

> **当前这次 `acquire` 永远不为「自己」买单，它买的是「上一个人」留下的账；
> 自己的账留给下一个来的人。**

后果就是那个经典现象 —— **空闲一段后的第一次 `acquire` 不用等，哪怕你一次取一大把**：

```java
RateLimiter r = RateLimiter.create(2.0);   // 2 permits/秒
r.acquire(10);   // 立刻返回 0.0！（账记到未来，下一个调用才会等）
r.acquire(1);    // 这下要等 ~5 秒（替上面那 10 个买单）
```

这是「平滑突发」的代价后置设计，不是 bug。

---

## 两种 Smooth：Bursty vs WarmingUp

`RateLimiter.create(...)` 工厂方法根据参数给你两种子类：

### `SmoothBursty`（默认，`create(rate)`）
允许把**最近 1 秒**没用完的许可攒起来（`maxBurstSeconds = 1.0`，
所以 `maxPermits = rate * 1`）。空闲后能瞬间放出最多约 `rate` 个许可的突发，之后回落到稳定
速率。`storedPermitsToWaitTime` 恒为 0 —— 攒下的许可花起来不要钱（不额外等待）。

### `SmoothWarmingUp`（`create(rate, warmupPeriod, unit)`）
模拟「资源冷启动需要预热」：刚开始（攒了很多 `storedPermits` = 系统很闲很冷）发许可**慢**，
随着许可被消耗逐渐**加速**到全速。`storedPermitsToWaitTime` 不再是 0，而是预热曲线（一条
梯形/积分）下的面积 —— 攒得越多、越往冷区，单个许可耗时越长。适合：冷缓存、JIT 没热、
数据库连接池刚起来这类「别一上来就打满」的场景。

| | 闲置后突发 | 冷启动行为 | 典型用途 |
|--|-----------|-----------|---------|
| SmoothBursty | 允许（最多 ~1 秒额度） | 无预热，立刻全速 | 一般限流 |
| SmoothWarmingUp | 允许 | 慢起步，逐渐加速 | 保护需要预热的下游 |

---

## 和本题 / 面试的对照（重点）

把 Guava 放进 Part 8 README 的「取舍表」里看：

- **它是 token bucket，不是 sliding window。** Token bucket 约束的是**平均速率 + 有限突发**；
  sliding-window counter 约束的是**任意 W 秒窗口内的硬上限**。两者在「允许多大突发」上语义
  不同：token bucket 默认放过一波 burst，sliding window 不放。
- **它阻塞，不拒绝。** Guava 默认让你**等**；Part 8 是**拒**（返回 `false`）。要「拒」语义就用
  `tryAcquire()`。面试里这是一个明确的设计选择点：**throttle（削峰/排队）vs reject（直接拒）**。
- **它读真实时钟、且只在单 JVM 内。** Part 7 的分布式 / 跨节点一致性，它**不**解决。多节点限流
  要 Redis（`INCR`+`EXPIRE` / Lua 脚本）或专门的分布式令牌桶。
- **它不回答「过去 5 分钟来了多少次」。** 它没有窗口计数语义 —— 那是 Hit Counter 的活儿。
  别在面试里把「限速率」和「数窗口内次数」混为一谈。

一句话给面试官：
> Guava `RateLimiter` = 单机平滑令牌桶，控平均速率、容许有限突发、默认阻塞排队；
> 我 Part 8 写的是滑动窗口计数器，控窗口内硬上限、超限即拒。选哪个取决于你要
> **「削峰排队」还是「硬性拒绝」**、要不要**容忍突发**、以及是否**跨节点**。

---

## 局限 / 坑

- **单 JVM**：进程内有效，多实例各限各的，合起来就超了。要全局限流另找方案。
- **阻塞语义**：`acquire()` 占着线程睡；高并发下大量线程阻塞要小心线程池耗尽，优先考虑
  `tryAcquire(timeout)`。
- **不是窗口配额**：它管不了「每天最多 1000 次」这类固定配额，那要计数器 + 过期。
- **代价后置**会让「第一发免费」，做容量规划 / 测试断言时容易被这个 burst 行为坑到。
- 线程安全靠内部一把 `synchronized` 互斥锁；极高频争用下这把锁本身可能成为热点。

---

## 最小可跑示例

```java
import com.google.common.util.concurrent.RateLimiter;

RateLimiter limiter = RateLimiter.create(5.0);   // 每秒 5 个
for (int i = 0; i < 10; i++) {
    limiter.acquire();                           // 超速就阻塞，自动摊到 ~2 秒
    handle(request);
}

// 想要「拒绝」而不是「排队」：
if (limiter.tryAcquire()) {
    handle(request);
} else {
    reject(request);   // 这才接近 Part 8 的语义
}
```
