# 01 — Event Counter

最基础的并发问题: 一个事件计数器, 多线程并发调用 `record()`, 任意时刻能读到准确的累计值。

**真实出处**: 任何 web 应用都有 —— 比如 "本进程处理了多少次请求", Prometheus client_total counter, 健康检查里的 in-flight request gauge。

---

## API

```java
public class EventCounter {
    public void record();          // 由很多线程并发调用
    public long total();           // 读累计值, 必须看到所有已完成的 record()
    public void reset();           // 归零 (一般只在测试/快照时调)
}
```

---

## 澄清

- `record()` 是 fire-and-forget, 不返回任何东西, 不能丢。
- `total()` 不要求"严格实时" —— 但**已经 return 的 `record()` 调用必须反映在后续 `total()` 里** (happens-before)。
- 没有删除、没有 TTL、没有按时间窗口 —— 就是单调累加。
- 读多还是写多? **写多读少** (counter 一般每次请求都 ++, 读只在 metrics 拉取时)。这个提示会影响选型。

---

## 测试合约

`single`: 单线程 record 一万次, total 等于一万。

`concurrent`: 16 个线程同时跑, 每个 record 100,000 次, 最后 `total()` 必须 **正好** 等于 1,600,000。一个也不能丢, 一个也不能多。

`reset_under_load`: reset 期间 record 仍在并发进行 —— 这个 case 允许"reset 之后还有少量计数, 那是 reset 之后才 record 的"; 但**不允许 total 变成负数或者爆 long**。

---

## 自检 / 面试官追问

1. **`++` 加 `volatile long` 行不行?** —— 不行, 为什么? (这是写错的第一个常见解法)
2. **`AtomicLong` 和 `LongAdder` 在这道题里谁更合适?** 为什么? 哪个量级开始能看出差距?
3. 如果 record 频率是 1M QPS, 但 total() 一秒读一次 —— 你会怎么实现?
4. 如果还要按"最近 1 分钟"做窗口计数呢? (开放讨论, 不要求实现)
