# NFT Generation — Design Scripts (口语稿)

每个 Part 写代码之前, 先向面试官 walk through 一下设计。
这里收集 1~2 分钟的 spoken design statement, 用来读熟 + 复述, 把术语刻进肌肉记忆。

**练法**:
1. 读 2-3 遍, 注意每个**加粗**的术语
2. 合上屏幕, 用自己的话**复述一遍**
3. 复述卡词的地方回来再看, 直到能流畅说完

active recall > 重复阅读。

---

## Part 5 — Concurrent Generation with Global Deduplication

> "Let me walk through my design before I start coding.
>
> This is a **multi-producer single-collection problem with global deduplication**. The core challenge is that multiple worker threads concurrently sample candidate NFTs, and they all need to write into one shared deduplication structure without producing duplicates.
>
> I'll set up three pieces of **shared mutable state**: first, a `ConcurrentHashMap.newKeySet()` as the **dedup set** — I'm picking this because its `add` method returns a boolean atomically, which lets me avoid the classic **check-then-act race** that you'd get if I did `contains` followed by `add`. Second, an `AtomicInteger` as a **slot counter** — workers will call `incrementAndGet`, and if the returned value exceeds the target size, that worker knows it lost the race and stops. Third, instead of one shared result list, **each worker writes into its own local buffer**, and the main thread merges them after all workers complete — this keeps the hot path completely **lock-free**.
>
> For randomness, each worker gets its own `Random` instance, seeded as `masterSeed XOR workerId`, so the generation stays deterministic per-worker without contending on a shared RNG.
>
> For orchestration, I'll use an `ExecutorService` with a fixed thread pool of `numThreads`, submit one task per worker, collect the `Future`s, and call `.get()` on each from the main thread so that any worker exception propagates back as fail-fast.
>
> Two edge cases I want to flag upfront: when `size` approaches the total combination count, rejection sampling degenerates and threads start contending on the same few remaining slots — a more robust strategy at that point is to **enumerate, shuffle, and partition** across workers, but I'll keep that as a follow-up discussion. And second, I'm consciously trading off **list-order determinism** for parallelism — the output set is reproducible given the seed, but the order within the list is not."

### 这段覆盖的术语 checklist

- [ ] **multi-producer single-collection / global deduplication** — 问题归类
- [ ] **shared mutable state** + **check-then-act race** — 并发概念
- [ ] **`ConcurrentHashMap.newKeySet().add()` 返回 boolean atomically** — 具体 API + 为什么选它
- [ ] **`AtomicInteger` slot counter** — 停止条件 / 占坑机制
- [ ] **per-worker local buffer + merge → lock-free hot path** — 性能优化
- [ ] **per-worker seeded Random (`masterSeed XOR workerId`)** — deterministic 的多线程版
- [ ] **`ExecutorService` + `Future.get()` → fail-fast** — orchestration
- [ ] **rejection sampling degenerates → enumerate + shuffle + partition** — 退化场景预案
- [ ] **trade list-order determinism for parallelism** — 主动承认 trade-off

---

## Part 6 — Streaming Generation (待写)

写完 Part 5 后再加。
