# 02 — URL Hit Counter

按 URL (或者任意 string key) 统计访问次数。多线程并发上报, 后台聚合, 不能丢更新。

**真实出处**: web 分析的最朴素形态 (Google Analytics 那一层之前, 你自己服务里的 per-route counter)。Nginx access log 解析、CDN edge 节点的 hit miss 统计、reddit 的 upvote per-post counter 都长这样。

---

## API

```java
public class UrlHitCounter {
    public void hit(String url);                  // 并发调用
    public long countFor(String url);             // 不存在的 url 返回 0
    public Map<String, Long> snapshot();          // 整体快照, 用于上报
}
```

---

## 澄清

- URL 是任意 string, 没有大小写规范化, 调用方负责。
- `countFor` 对不存在的 url 返回 0, 不抛异常。
- `snapshot()` 返回 **不可变** 的拷贝 (caller 拿到之后修改不能影响 store)。
- snapshot 不要求"全局一致快照" —— 它是一个**弱一致**的视图: 不同 url 的计数可能来自不同瞬间。这个边界很重要, 要在面试里说清楚。
- 性能: hit 远多于 countFor / snapshot。

---

## 测试合约

`single`: 单线程 hit 三个 url, 各自次数对得上。

`concurrent`: 16 线程, 每线程 hit 1000 次, hit 的 url 从 50 个候选里随机选, 最后所有 url 的计数加起来 == 16,000。

`new_url_under_load`: 多线程同时 `hit` 同一个**首次出现**的 url —— 必须不丢更新 (检验你"首次 put 时" race 处理对不对)。

---

## 自检 / 面试官追问

1. **为什么不能 `map.put(url, map.getOrDefault(url, 0L) + 1)`?** —— 经典 check-then-act race, 解释清楚。
2. **`ConcurrentHashMap<String, Long>` + `compute` vs `ConcurrentHashMap<String, AtomicLong>`** —— 两种都对, 各自的优缺点?
3. **`putIfAbsent` 创建 `AtomicLong` 时, 有没有 race?** 怎么避免每次 hit 都 new 一个 AtomicLong?
4. 如果 url 数会爆 (上亿 unique), 这个结构还能用吗? 怎么改? (Top-K 估计 / Count-Min Sketch 是答题方向, 不要求实现)
