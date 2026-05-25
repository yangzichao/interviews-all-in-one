# 04 — Parallel Fetcher

给你一组 "URL" (实际是 string), 让你并发地拿到每个 url 的结果, **按输入顺序** 返回一个 List。慢的 url 不能拖死整体 —— 至多等到 `timeoutMillis`。

**真实出处**: web 爬虫的 page fetcher; 微服务里 "聚合 5 个下游 API 的响应" 的 BFF; LangChain `RunnableParallel`; aws-cli `s3 cp --recursive` 的并发下载。

---

## API

```java
public class ParallelFetcher implements AutoCloseable {
    public ParallelFetcher(int parallelism);

    public List<String> fetchAll(
        List<String> urls,
        Function<String, String> fetcher,   // 调用方提供 "网络" — 测试里就是 sleep + 返回
        long timeoutMillis                  // 整批的 deadline; 超时的 slot 用 null 填
    ) throws InterruptedException;

    @Override public void close();          // 关掉线程池, 幂等
}
```

---

## 澄清

- **顺序**: 返回的 list 长度 == 输入长度, `result.get(i)` 对应 `urls.get(i)`。
- **失败**: `fetcher` 抛异常 → 那个槽位填 `null` (不要让一个失败的下游搞挂整批)。
- **超时**: `timeoutMillis` 是 **整批** 的 deadline (不是每个 url), 到时还没完的槽位也填 `null`, 函数立即返回。
- **重复 url**: fetcher 会被调用多次, 每次独立 (调用方负责去重 / cache, 不是这层的事)。
- **空输入**: 返回空 list。
- **close 之后再 fetchAll** 抛 `IllegalStateException`。

---

## 测试合约

`single_thread_fast`: 5 个 url, fetcher 立即返回 url 的 upper case, 结果顺序与输入一致。

`parallel_speedup`: 8 个 url, 每个 fetcher 睡 200ms, parallelism=8 —— **总耗时 < 500ms** (顺序跑要 1600ms+, 这一题就是测 "你真的并发了")。

`failure_isolation`: 5 个 url 里第 3 个抛异常, 其他 4 个正常返回, 异常那一槽位填 null, 不影响其他。

`batch_timeout`: 10 个 url, 一半 fetcher 睡 100ms, 一半睡 5 秒; timeoutMillis=300。返回 list 长度 = 10, 前一半非 null, 后一半 null; 整体耗时 < 500ms。

---

## 自检 / 面试官追问

1. **`ExecutorService.invokeAll(tasks, timeout)`** 一个 method 几乎就能办这事 —— 你知道它的 cancel 语义吗? 已经在跑的 task 怎么收到取消?
2. **`Future.get(timeout)`** 在循环里逐个调 (累计 deadline) vs **`invokeAll`** —— 哪个真正实现了 "整批 deadline"?
3. parallelism 该选多少? CPU 密集 vs IO 密集差多少?
4. 一个 fetcher 卡住 (`Thread.sleep(Long.MAX_VALUE)`) —— 你的 close() 能不能干净退出? 怎么处理?
5. 如果 url 列表是 1M 条, 不能全塞进线程池, 该怎么改?
