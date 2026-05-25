# Java Concurrency Practice

两部分:

1. **[00 — Concurrency Basics](00-Concurrency-Basics/)** —— 9 个超短**教学型 drill**, 每个聚焦一个 primitive (volatile / synchronized / AtomicLong / ConcurrentHashMap / ReentrantLock / BlockingQueue / CountDownLatch / Semaphore / ExecutorService+Future). 目的是把 JUC 的接口和术语**亲手敲熟**. **先做这个**.
2. **01-06 真题** —— 6 道生产代码里真见过的小问题, 体会"在这种场景下我该挑哪个原语".

stub 抛 `UnsupportedOperationException`, 测试 runner 会显示 SKIPPED。

---

## 真题列表 (做完 00 再来)

| # | 题目 | 真实出处 | 主要练 | 难度 |
|---|------|----------|--------|------|
| 01 | [Event Counter](01-Event-Counter/) | 任何 metrics / 页面访问计数 | atomic 计数 | ⭐ |
| 02 | [URL Hit Counter](02-URL-Hit-Counter/) | web 分析, 按路由统计访问 | `ConcurrentHashMap` + atomic | ⭐⭐ |
| 03 | [Async Logger](03-Async-Logger/) | SLF4J async appender, Kafka batching | `BlockingQueue` + 单消费者 | ⭐⭐ |
| 04 | [Parallel Fetcher](04-Parallel-Fetcher/) | 爬虫, 并行 API 聚合 | `ExecutorService` + `Future` | ⭐⭐ |
| 05 | [Token Bucket](05-Token-Bucket/) | API gateway 限流, 登录尝试限速 | `Semaphore` / atomic 配额 | ⭐⭐⭐ |
| 06 | [Atomic Config Store](06-Atomic-Config-Store/) | feature flag service, 多 key 一致快照 | `ReadWriteLock` 或 immutable swap | ⭐⭐⭐ |

---

## 怎么练

每题独立, 进文件夹后:

```bash
cd <题目文件夹>
javac <Name>.java <Name>Test.java
java <Name>Test                  # 跑所有 case
java <Name>Test concurrent       # 只跑并发 case
```

**测试约定**: 每题都有 `single` (单线程正确性) 和 `concurrent` (多线程压测) 两类 case。单线程 PASS 不算完成 —— 必须 concurrent 也稳定 PASS, 而且**多跑几次都不抖**。如果偶尔 fail, 说明实现里还有 race。

---

## 写这套题的原则

1. **题目本身简单** —— 不考算法, 考"在多线程下能不能写对"。
2. **测试要狠** —— 测试里用 `CountDownLatch` 当起跑枪, 让所有 worker 同时冲, 最大化 race window。
3. **不剧透原语** —— stub 和 README 都不写"用 AtomicLong"。要你自己挑。挑错了, 测试会让你知道。
4. **追问比代码值钱** —— 每题 README 末尾列出"面试官会接着追问什么", 比如:
   - "如果是 100k QPS, AtomicLong 还够吗?"
   - "如果队列满了, producer 应该 block 还是 drop?"
   - "Future.get() 抛异常时其他 worker 怎么办?"

---

## 与 Coinbase/ 的关系

Coinbase/ 那套 4-part 是**面经题 + 系统设计 follow-up**, 完整的 1 小时 onsite 模拟。
这套是**单点并发原语训练**, 每题 15-30 分钟, 适合在 Coinbase 题之间穿插练手感。
