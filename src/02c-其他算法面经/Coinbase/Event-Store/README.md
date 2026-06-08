# Coinbase — Event Store

来源：用户口述面经（单题，无 Parts 分层）。

---

## 问题描述

系统接收来自多个用户的事件流。事件携带 userId 和 timestamp，**不保证按时间顺序到达**（out-of-order arrival）。流量高，需要考虑并发。

---

## API

```java
record(Event event)                                   // 写入一条事件
query(String userId, long start, long end) → List<Event>   // 返回该 user 在 [start, end] 内的所有事件
count(String userId, long start, long end) → int           // 返回该 user 在 [start, end] 内的事件总数
```

---

## 已确认的 Clarification

| 问题 | 答案 |
|------|------|
| query 是否总带 userId？ | 是，永远带 userId |
| 同一 userId + 同一 timestamp，可以有多条事件吗？ | 可以 |
| 事件是否近似有序到达？ | Mostly in order（近似有序，但不保证） |

---

## 数据结构讨论

**主结构：** `Map<String, <per-user 有序集合>>` — key 是 userId

### 推荐答案：append-first sorted list + late buffer

题目里最重要的两个信号是：

1. 查询永远带 `userId`
2. 事件 **mostly in order**，但不保证严格有序

所以不要一上来默认每条事件都插进 `TreeMap`。`TreeMap` 能处理任意乱序，但它没有利用 "mostly in order" 这个性质；每次写入都要付 O(log n) 的树操作成本。

更贴合 high-throughput 写入的设计是：

```java
Map<String, UserEvents> eventsByUser;

class UserEvents {
    List<Event> sortedEvents;  // 主列表，按 timestamp 升序，绝大多数写入是 append
    List<Event> lateBuffer;    // 少量乱序到达的事件，先缓冲，不立刻插入主列表中间
}
```

#### record(event)

对某个 user：

```text
if sortedEvents is empty
   or event.timestamp >= last timestamp in sortedEvents:
    append to sortedEvents        // common path: O(1)
else:
    append to lateBuffer          // late arrival: O(1)
```

关键点：**不要每次 late event 都立刻插入 `ArrayList` 中间。**

虽然可以 binary search 找到插入位置 O(log n)，但 `ArrayList` 中间插入要整体搬元素，真正成本是 O(n)。高吞吐写入下，这会把 write path 拖慢。

#### query(userId, start, end)

查询分两部分：

1. 对 `sortedEvents` 做 binary search，找到第一个 `timestamp >= start` 的位置
2. 从该位置向后扫描，直到 `timestamp > end` 停止
3. 对 `lateBuffer` 线性扫描，过滤 `[start, end]`
4. 如果要求结果按 timestamp 排序，再把两边结果 merge/sort

主列表查询复杂度：

```text
O(log n + k)
```

其中 `k` 是命中的事件数。

加上 late buffer 后：

```text
O(log n + k + b)
```

其中 `b` 是 `lateBuffer` 的大小。只要 late buffer 被控制得比较小，这个查询仍然很高效。

#### lateBuffer 什么时候合并？

当 `lateBuffer` 变大时，批量处理：

```text
if lateBuffer.size() > threshold:
    sort lateBuffer by timestamp
    merge lateBuffer into sortedEvents
    clear lateBuffer
```

这本质上是 log-structured / LSM-tree 风格的思路：写入路径先便宜落下，排序和 merge 成本放到批处理里摊销。

---

## 方案取舍

**per-user 有序集合的选项：**

| 方案 | 写入 | 范围查询 | 备注 |
|------|------|----------|------|
| `TreeMap<Long, List<Event>>` | O(log n) | O(log n + k) | 乱序写入时最稳定；不利用近似有序 |
| `ArrayList` + append + query 时 sort | O(1) 均摊 | O(n log n) | 写多读少时合适 |
| `ArrayList` + binary search 插入 | O(n) 最坏 | O(log n + k) | 近似有序时插入多数为尾部追加，均摊接近 O(1)；严格乱序时退化 |
| `ArrayList` sorted main + late buffer | common case O(1) | O(log n + k + b) | 推荐；利用近似有序，late event 批量 merge |

**近似有序下的 trade-off：**
- `TreeMap` 写入永远 O(log n)，不从顺序性中获益
- `ArrayList` + append 在近似有序时均摊 O(1) 写入；但如果每个 late event 都立刻插入中间，会有 O(n) 挪位
- `sorted main + late buffer` 把 late event 的排序成本从每条写入转移到批量 merge；代价是查询要额外检查 buffer
- 这是 average-case vs worst-case 的取舍

**什么时候选 `TreeMap`？**

如果面试官说 timestamp 可能任意乱序、late event 很多，或者 query latency 必须非常稳定，那 `TreeMap<Long, List<Event>>` 更稳。它牺牲写入吞吐，换来每次查询和乱序写入都有清晰的 worst-case bound。

**什么时候选 append-first？**

如果事件确实 mostly in order，而且系统重点是 high-throughput write，那么 append-first 更好。它把 common path 做到 O(1)，只让少量 late events 走 buffer/merge 路径。

面试里可以这样说：

> If timestamps can be arbitrarily out of order, I would use a per-user TreeMap. But given the clarification that events are mostly ordered and write throughput matters, I would optimize the common path as append-only: keep a sorted main list plus a small late-arrival buffer. Range queries binary-search the sorted list and also scan the buffer. If the buffer grows too large, I sort and merge it back in batch.

---

## 并发

- 多线程同时 `record` + `query`
- 需要考虑锁粒度（per-user lock vs global lock）

推荐并发模型：

```java
class UserEvents {
    ReadWriteLock lock;
    List<Event> sortedEvents;
    List<Event> lateBuffer;
}
```

- `record` 拿该 user 的 write lock
- `query` / `count` 拿该 user 的 read lock
- 不同 user 之间互不阻塞
- 同一个 hot user 仍然可能成为瓶颈，这是 per-user ordering/query 的自然代价

如果面试官继续追问更高并发，可以讨论 chunked segments：

```text
UserEvents:
    frozen sorted segments
    active append segment
    late buffer
```

每个 segment 内部有序，query 对多个 segment 分别 binary search，再 merge 结果；后台 compaction 合并旧 segment。这更接近真实存储系统，但 coding 面试里通常先讲 `sortedEvents + lateBuffer` 就够。
