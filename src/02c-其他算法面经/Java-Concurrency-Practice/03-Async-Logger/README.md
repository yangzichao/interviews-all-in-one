# 03 — Async Logger

生产者-消费者最小形态。多个业务线程 `log(line)`, 一个后台线程把行 flush 到 sink (磁盘 / 网络 / `StringBuilder`)。close 时必须把缓冲里的全部写出。

**真实出处**: SLF4J / Log4j2 的 `AsyncAppender`, Kafka producer 的 `batching`, Datadog agent 的 metric flushing —— 套路都是这一个。

---

## API

```java
public class AsyncLogger implements AutoCloseable {
    public AsyncLogger(int capacity, Appendable sink);  // 启动后台 worker

    public void log(String line);                       // 业务线程调用; 满时的行为见下
    public void close() throws InterruptedException;    // flush 剩余 + 停 worker, 幂等
}
```

---

## 澄清 (这些是写之前必须想清楚的)

- **满了怎么办**? 三个常见策略, 选一个并说明:
  - **block** —— `log()` 阻塞直到有空位 (backpressure, 但拖慢业务线程)
  - **drop** —— 直接丢, 返回 (高吞吐, 但丢数据)
  - **drop-oldest** —— 把队首扔了, 自己进 (滚动 buffer)
  - 这里我们规定 **block** —— 业务正确性优先。
- 一行 = 一次写出。worker 拿到一行写一行, 不做批合并 (简化练习)。
- `close()` 之后 `log()` 抛 `IllegalStateException` 还是静默丢, 你自己选 —— 但要在 README 里说清并和测试一致。这里规定 **静默丢**。
- worker 异常: sink 抛 `IOException` 时整个 logger 进入 "broken" 状态, 后续 `log()` 静默丢 —— 也是简化, 真实系统会重试 / 落 dead letter。
- 顺序保证: **同一线程内的多次 log 必须按调用顺序出现在 sink 里**。跨线程不要求顺序。

---

## 测试合约

`single`: 主线程 log 100 行, close, sink 里恰好 100 行, 顺序与调用一致。

`concurrent_no_loss`: 8 个 producer, 每个 log 1000 行 (内容含线程 id), close, sink 里恰好 8000 行 (不要求跨线程顺序)。

`per_thread_order`: 同一线程 log "T0-1", "T0-2", "T0-3", 跨多个线程并发 —— close 后, 任何线程的行在 sink 里相对顺序保持递增。

`close_idempotent`: 连调 close 两次不抛, 第二次是 no-op。

---

## 自检 / 面试官追问

1. **为什么不能直接 `synchronized (sink) { sink.append(line) }` 在业务线程里写?** —— 答: I/O 把业务线程拖死, 这就是为什么要 async。但 trade-off 是什么?
2. **`ArrayBlockingQueue` vs `LinkedBlockingQueue`** —— 这道题你会选哪个? 为什么? (capacity 已经给了)
3. **worker 怎么知道什么时候停?** poison pill / `Thread.interrupt()` / volatile flag —— 你的选择和后果?
4. **close() 时如果有 producer 在 `log()` 阻塞等空位** —— 会卡住吗? 怎么处理?
5. 如果改成 "batch 100 行 flush 一次", API 该怎么动?
