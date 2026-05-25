# 06 — Atomic Config Store

一个 key-value 配置存储。大量 reader 读 (业务热路径), 偶尔的 writer 做**多 key 原子更新**, 还有任意 reader 可以拿一个**整体一致的快照**。

**真实出处**: feature flag service (LaunchDarkly / Statsig 的本地 cache, "一次 reload 多个 flag, 必须原子地切换"), Envoy 的 RDS 配置热更新, Kubernetes ConfigMap 的客户端缓存。

**为什么这道题需要"原子多 key 更新 + 一致快照"**: 想象两个 flag `payment_provider=stripe` 和 `payment_api_key=stripe_xxx`, 读者在两者之间各读了一次, 中间发生一次切换到 `paypal/paypal_yyy`, 读者会看到 `stripe + paypal_yyy` 这种**永远不应该出现的组合**。

---

## API

```java
public class ConfigStore {
    public String get(String key);                        // 热路径, 高频
    public void putAll(Map<String, String> updates);      // 原子多 key 写; 已有的 key 覆盖, 没列出的 key 保留
    public Map<String, String> snapshot();                // 一致快照, 不可变
}
```

---

## 澄清

- **putAll 的语义**: "对所有 reader 来说, **要么看见整批更新都生效, 要么一个都没生效**"。不能出现中间状态。
- **snapshot 的一致性**: 同一次 `snapshot()` 返回的 map 必须对应某个**单一的 putAll 边界** —— 不能跨边界混合。
- `putAll` 不会删除已存在的 key, 只是覆盖。如果要删 key, 不在这一题范围内。
- `get(key)` 不存在返回 `null`。
- 读多写少, 读路径要尽量快 —— 写路径慢一点没事。

---

## 测试合约

`single`: put `{a:1, b:2}`, 再 put `{b:3, c:4}`, snapshot 应该是 `{a:1, b:3, c:4}` (key 没列就保留)。

`reader_consistency`: 持续的 writer 在 `{currency:USD, symbol:$}` 和 `{currency:EUR, symbol:€}` 之间切换 putAll。20 个 reader 不停拿 snapshot, **任何快照里 currency 和 symbol 都必须配对**, 不能见到 `(USD, €)` 或 `(EUR, $)`。跑 200ms 后停。

`get_under_load`: writer 在 putAll, reader 在 get, 不能抛 `ConcurrentModificationException`, 也不能死锁。

`snapshot_immutable`: 拿到 snapshot 后 mutate 它, 不能影响 store。

---

## 自检 / 面试官追问

1. 一个常见错误解法: `ConcurrentHashMap<String,String>`, putAll 里 for loop 一个个 put —— 为什么过不了 `reader_consistency`?
2. 两种典型解法:
   - **`volatile Map<String,String>` + 每次 putAll 都构造一个新 immutable map 整体替换** (copy-on-write)
   - **`ConcurrentHashMap` + `ReentrantReadWriteLock`** —— writer 拿写锁做整批更新, snapshot 拿写锁做拷贝
   - 各自的优劣? 哪个更适合**读极多写极少**?
3. 第一种方案 (copy-on-write) 在 put 频繁时有什么问题? GC 压力?
4. 如果 put 频率上来了, 想避免每次复制整个 map —— **`AtomicReference<PersistentMap>`** + 不可变持久化结构 (Vavr / Capsule / Clojure-style) 是答题方向, 怎么权衡?
5. 如果还要支持 **subscribe(key, listener)** —— writer 触发回调通知 listener —— 设计上要小心什么? (回调持有锁是经典坑)
