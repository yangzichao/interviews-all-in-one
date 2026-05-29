# 🚀 Java Concurrency Practice (并发编程实战特训)

> 并发编程不仅是面试的试金石，更是构建高可用系统的核心内功。打破对多线程恐惧的唯一方法，就是亲自掌控它们。

本仓库旨在通过高强度的定向刻意练习，帮你建立对并发原语 (Concurrency Primitives) 的“肌肉记忆”。包含两部分：

1. 🐣 **00 — Concurrency Basics (基础篇)** 
   包含 9 个超短的**教学型 Drill**。每个练习聚焦一个核心原语（如 `volatile` / `AtomicLong` 等）。目标是让你将 JUC 核心 API 与底层的专业术语（如可见性、原子性、无锁并发）建立直观映射。👉 **强烈建议从这里起步。**
2. ⚔️ **01-06 生产真题演练** 
   精选自真实系统架构（如 Metrics 监控、限流、消息攒批）的 6 道精简版实战题。重点考察**“在特定业务边界下，如何进行并发工具的技术选型 (Trade-off)”**。

> 💡 **提示**：模板代码（Stub）默认抛出 `UnsupportedOperationException`，测试显示 `SKIPPED`。补全代码逻辑后，测试将变绿 `PASSED`。

---

## 🎯 真题列表 (建议通关 00 篇后再来)

| # | 题目 | 真实出处 | 主要练 | 难度 |
|---|------|----------|--------|------|
| 01 | [Event Counter](01-Event-Counter/) | 任何 metrics / 页面访问计数 | atomic 计数 | ⭐ |
| 02 | [URL Hit Counter](02-URL-Hit-Counter/) | web 分析, 按路由统计访问 | `ConcurrentHashMap` + atomic | ⭐⭐ |
| 03 | [Async Logger](03-Async-Logger/) | SLF4J async appender, Kafka batching | `BlockingQueue` + 单消费者 | ⭐⭐ |
| 04 | [Parallel Fetcher](04-Parallel-Fetcher/) | 爬虫, 并行 API 聚合 | `ExecutorService` + `Future` | ⭐⭐ |
| 05 | [Token Bucket](05-Token-Bucket/) | API gateway 限流, 登录尝试限速 | `Semaphore` / atomic 配额 | ⭐⭐⭐ |
| 06 | [Atomic Config Store](06-Atomic-Config-Store/) | feature flag service, 多 key 一致快照 | `ReadWriteLock` 或 immutable swap | ⭐⭐⭐ |

---

## 🎮 训练指南

每题均为独立工程，进入文件夹后执行：

```bash
cd <题目文件夹>
javac <Name>.java <Name>Test.java
java <Name>Test                  # 运行所有验证 case
java <Name>Test concurrent       # 仅运行高强度并发压测 case
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
