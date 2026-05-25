# 05 — Token Bucket

经典限流算法。桶里有 N 个 token, 一个请求消耗 1 个; refill 由外部 (调度器或调用方) 周期性补充, 不超 capacity。`tryAcquire()` 拿不到 token 立刻返回 false (不阻塞)。

**真实出处**: AWS API Gateway, Stripe API, GitHub API 的限流; Nginx `limit_req`; Guava `RateLimiter` (Guava 那个还做了 "提前预支" 的优化, 这里我们做最朴素版)。

---

## API

```java
public class TokenBucket {
    public TokenBucket(int capacity);          // 初始 = capacity (满桶)

    public boolean tryAcquire();                // 拿 1 个 token; 没就返回 false
    public boolean tryAcquire(int n);           // 拿 n 个; 必须 all-or-nothing

    public void refill(int tokens);             // 加 tokens, 上限 capacity (超出丢弃)
    public int available();                     // 当前 token 数
}
```

---

## 澄清

- **并发模型**: 任意多个线程并发调 `tryAcquire`, refill 也可能从单独的 scheduler 线程触发。所有 method 都必须 thread-safe。
- **`tryAcquire(n)` 是 all-or-nothing**: 桶里有 5, 调 `tryAcquire(7)` —— 返回 false, **不消耗任何 token**。这是 token bucket 的核心语义, 写错了等于没写。
- **refill 超过 capacity**: 多出来的 token **丢弃** (不"借给将来"也不报错)。
- **available()**: 弱一致即可, 用于 metrics/debug, 不能用它做 "先看够不够再 acquire" 的决策。
- **公平性**: 不要求 FIFO。竞争中谁先 CAS 成功谁拿到, 都行。

---

## 测试合约

`single_drain`: capacity=5, 连续 `tryAcquire()` 5 次都 true, 第 6 次 false。

`refill_caps_at_capacity`: capacity=5, 当前 = 5, refill(10) 后 available 还是 5。

`all_or_nothing`: capacity=5, 当前 = 3, `tryAcquire(5)` 返回 false **并且** available 还是 3 (没偷偷扣)。

`concurrent_drain`: capacity=1000, 16 个线程各 `tryAcquire()` 1000 次, **总成功次数恰好 = 1000** (不多不少), available 最后 = 0。

`refill_under_load`: capacity=100, 初始 100。8 个 consumer 死循环 `tryAcquire`, 1 个 refiller 每 5ms refill(50)。跑 200ms 后停下来 —— available 必须在 [0, 100] 区间内, **不能爆**。

---

## 自检 / 面试官追问

1. 用 `synchronized` 写 token bucket 当然能对, 但 **`AtomicInteger` + CAS loop** 也能 lock-free 写对 —— 你能写出 CAS 版的 `tryAcquire(n)` 吗? all-or-nothing 怎么保证?
2. `Semaphore` 是不是天然就是 token bucket? 区别在哪? (提示: refill 超过 capacity 时 `Semaphore.release` 不会自动 cap)
3. 这道题如果改成"阻塞式" `acquire(n)` 等到有 n 个 token —— 你的实现要怎么改? `wait/notify` 还是 `Condition`?
4. 分布式 (多个进程共享一个 bucket) 怎么做? Redis Lua script / Redis Cell / DynamoDB conditional update —— 任选一个聊优劣。
5. 真实的 Stripe API 每个用户独立 bucket, 用户 ID 上亿 —— 内存怎么管?
