# API-Driven Data Structure Design

这类题不是传统 LeetCode，也不是完整 Object-Oriented Design。外界更通用的叫法是：

**Data Structure Design**

更精确一点，可以叫：

**API-Driven Data Structure Design**

也可以理解成：

- **Data-structure design interview**
- **System-aware coding question**
- **Requirement-driven coding problem**
- **Mini system design + implementation**

Event Store 就是很典型的一类：题面给了几个 API，例如 `record` / `query` / `count`，但真正难点不是某个固定算法，而是如何从不完整需求里选数据结构、定义语义、控制 trade-off，并把这个设计落成代码。

---

## 核心：控场流程，不是背数据结构

这类题最重要的不是“我知道哪些数据结构 pattern”，而是：

> I can control the conversation from vague requirements to a concrete implementation plan.

也就是说，你要把面试从开放聊天拉回到可执行的代码路径。你的目标不是问一堆 clarification，也不是展示所有可能方案，而是在合适的时间主动收束：

```text
What is the API functional contract?
What are each API's inputs, outputs, and state changes?
What state do I need to store?
How does each API read or mutate that state?
Which access paths need to be fast?
Do I need ordering, range, prefix, top-k, or pagination?
Which indexes must be kept consistent on writes?
What data model and index strategy will I implement now?
What fallback would I choose if assumptions change?
```

真正的面试节奏应该是：

```text
API Functional Contract -> Access Patterns -> Data Model + Indexes -> Update/Query Rules -> Trade-off -> Code
```

这比简单的 `Restate -> Clarify -> Code` 更稳，因为第一步先锁住 **API functional contract**：每个 API 的 input、output、state change，也就是这道题的 functional requirements。然后不要把讨论发散成完整 system design，而是立刻回到 data structure design 的核心：state、access patterns、primary store、secondary indexes、update rules、query path。

这份笔记的使用方式：

```text
屏幕上写少量结构，口头解释取舍。
不要把所有设计讨论都写进代码注释。
```

面试官需要看到你有计划，但不需要看到一篇论文。最好的状态是：

- 屏幕上有 10-20 行清晰的 requirement/design skeleton
- 口头上解释为什么这样选
- 一旦面试官 buy in，马上开始实现

---

## 面试控场流程

### 0. 开场：先声明你会先确认 API functional contract

不要直接问零散问题。先告诉面试官你接下来要做什么：

> Let me first clarify the API functional contract: what each API takes in, what it returns, and what state it is expected to update. Then I’ll use the non-functional requirements, like concurrency and throughput, to choose the data structure.

更贴近 data structure design 的说法是：

> Let me first clarify the API contract. Then I’ll map each operation to the state it reads or updates, so we can choose the right data structure and indexes.

这句话的作用是控场。它告诉面试官：我不是没方向地问问题，我先确认 functional requirements，再把每个 API 映射到 state、access path 和 index maintenance。

### 1. API Functional Contract：确认每个 API 的输入、输出、状态变化

先把每个 API 的 contract 压缩成一两句话：

> We need to store events and support `record`, `query(userId, start, end)`, and `count`. The key operation is a user-scoped range query by timestamp.

这一步不要讲方案。只确认 functional requirements：

```text
- What does each API take as input?
- What does each API return?
- Which API mutates state?
- What is the expected behavior for missing, duplicate, or boundary cases?
```

### 2. Access Patterns：让 API 告诉你它想要什么数据结构

不要问十几个泛泛的 system design 问题。问最能改变 data structure 的问题：

```text
- What state do I need to store?
- Which fields are used for lookup?
- Which operations are read-heavy or write-heavy?
- Do we need range, prefix, top-k, ordered output, or pagination?
- Are updates/deletes common, or is this mostly append-only?
- Which indexes must be updated when the primary object changes?
```

如果面试官给了模糊回答，不要卡住。你可以把它变成 working assumption：

> I’ll assume X for now; if that changes, I’ll adjust the data structure.

### 3. Data Model + Indexes：选择主存储和必要索引

这是最关键的控场动作。你不是为了分类而分类，而是要把 API 和 access patterns 翻译成 data model：

```text
Primary store:
- What is the source of truth?
- Is it id -> entity, userId -> bucket, key -> value, or something else?

Secondary indexes:
- Which access patterns need to be fast?
- Which index serves each hot API?
- What is the write/update cost of maintaining each index?

Working assumptions:
- Which workload detail am I assuming?
- Under what condition would I switch structures?
```

你可以边写边说：

> Once the API contract is clear, I’ll map each operation to the state it needs to read or update. That should tell us the primary store and any secondary indexes we need.

这句话非常重要。它把讨论从 requirement gathering 拉回 data structure design：state、access paths、indexes、index maintenance。

### 4. Commit：主动选择当前要写的 data model

不要一直停留在 trade-off。到这里要主动收束：

> Given these access patterns, I’ll use a per-user primary bucket and a timestamp-aware index inside each bucket.

然后写：

```text
Map<userId, UserEvents>

UserEvents:
- sortedEvents: main timestamp-sorted list
- lateBuffer: out-of-order events
```

接着一句话解释复杂度：

> Common writes are O(1). Query uses binary search on the sorted list and scans the late buffer, so it is O(log n + k + b).

### 5. Fallback：主动说明什么时候换方案

这一步让面试官知道你不是强行套方案：

> If late events are frequent or arbitrarily old, I would switch to a per-user TreeMap. But under the mostly-ordered, high-throughput write assumption, append-first is a better fit.

这个 fallback 不要讲太久。它的作用是关掉分支，而不是展开另一个设计。

### 6. Buy-in：进入代码前要确认方向

用一句话把讨论切到 implementation：

> Does this direction sound reasonable? If yes, I’ll implement the core single-threaded version first, then discuss concurrency as a follow-up.

这是非常实际的控场句。它把“讨论”变成“编码”。

### 7. Code：先写核心，不要一开始写 follow-up

代码顺序也要控场：

```text
1. Event record
2. UserEvents bucket
3. fields: eventsByUserId
4. record()
5. query()
6. count()
7. helper: lowerBound / merge buffer
```

不要一开始就写 `ReadWriteLock`、WAL、sharding、background compaction。除非面试官明确要求，否则这些是 follow-up。

### 8. Follow-up：实现完再扩展

实现核心后，再主动补：

```text
Concurrency:
- Put one lock per UserEvents bucket.
- Different users do not block each other.
- Use ReadWriteLock only if read-heavy.

If disorder is common:
- Use TreeMap<Long, List<Event>>.

If data is huge:
- Use segments / compaction / persistence.
```

---

## 屏幕上写什么，口头说什么

这是最容易失控的地方。你不应该把所有 reasoning 都写进代码注释；那会浪费时间，也会让你看起来还没准备好写代码。

### 屏幕上写

只写能约束实现的内容：

```java
/*
Hard requirements:
- query returns all events for one user in [start, end]
- events may arrive out of timestamp order

Clarified choices:
- no dedupe
- return sorted by timestamp

Optimization signal:
- mostly ordered timestamps

Design:
- Map<userId, UserEvents>
- UserEvents = sortedEvents + lateBuffer
- query = binary search sortedEvents + scan lateBuffer

Fallback:
- if disorder is common, use TreeMap
*/
```

这段的目标不是完整解释，而是给你和面试官一个共同的 implementation contract。

### 口头说

口头讲这些：

```text
- why mostly ordered is only an optimization signal
- why append-first beats TreeMap under this workload
- why lateBuffer preserves correctness
- when you would switch to TreeMap
- why concurrency is a follow-up unless explicitly required
```

也就是说：

```text
screen = contract
voice = reasoning
code = execution
```

---

## 常见失控点和拉回句

### 1. 问太多 clarification

问题：

> 一直问边界，迟迟不开写。

拉回句：

> I think I have enough to choose a data structure. I’ll proceed with these assumptions and call out where they would change the design.

### 2. Trade-off 展开太久

问题：

> TreeMap、List、SkipList、segments 全讲了，但没有 commit。

拉回句：

> For this implementation, I’ll choose the append-first design because it matches the mostly-ordered write pattern. I’ll keep TreeMap as the fallback if disorder is common.

### 3. 过早写并发

问题：

> 一开始就写 `ReadWriteLock`，核心算法还没出来。

拉回句：

> I’ll first implement the core single-threaded data structure. The concurrency wrapper is straightforward once the per-user bucket is correct.

### 4. 面试官追一个 follow-up，主线被带走

问题：

> 还没写完 query，就开始讨论 sharding / WAL / snapshot。

拉回句：

> That is a good follow-up. I’ll note it as an extension, but first I want to finish the in-memory core API.

### 5. 发现 assumption 可能不成立

问题：

> 写到一半意识到 late events 可能很多。

拉回句：

> If late events are common, this design should switch to TreeMap. Under the current mostly-ordered assumption, I’ll continue with the buffer approach; otherwise I can pivot now.

---

## 可背的控场口播

> Let me first separate correctness from optimization. Correctness is that `query` must return all events for the given user in the requested time range, even if events arrive out of order. I’ll clarify the range semantics, sorting requirement, dedupe, and whether events are mostly ordered per user.
>
> Based on that, I’ll treat mostly ordered timestamps as an optimization signal, not a correctness guarantee. So I’ll use an append-first design: for each user, keep a sorted main list and a small late buffer. Normal writes append in O(1); late events go to the buffer; queries binary-search the main list and scan the buffer.
>
> If late events are frequent or arbitrary, I would switch to a per-user TreeMap. But under the mostly-ordered, high-throughput assumption, this design is a better fit. If that sounds reasonable, I’ll implement the core single-threaded version first and discuss concurrency after.

---

## Pre-Code Comment Template

写代码前，可以先写这样的注释。不要太长，但要足够让面试官知道你的 plan。

```java
/*
Hard requirements:
- record(event): store the event.
- query(userId, start, end): return all events for that user in range.
- count(userId, start, end): return the number of matching events.
- Events may arrive out of timestamp order.

Clarified choices:
- Treat range as inclusive [start, end].
- No eventId, so no dedupe.
- Return query results sorted by timestamp.

Optimization signals:
- Events are mostly ordered by timestamp.
- Write/query throughput matters.

Design:
- Partition by userId.
- For each user:
    sortedEvents: main list sorted by timestamp.
    lateBuffer: small list for out-of-order events.
- record:
    If timestamp >= last timestamp in sortedEvents, append.
    Otherwise, append to lateBuffer.
- query:
    Binary search sortedEvents for first timestamp >= start.
    Scan until timestamp > end.
    Also scan lateBuffer.
- If lateBuffer grows too large:
    Sort lateBuffer and merge it into sortedEvents.

Fallback:
- If events are frequently or arbitrarily out of order, use TreeMap.
*/
```

---

## Appendix A：题型参考 pattern

这类题经常长得不一样，但内部结构很像。你可以把它们看成：

> Given a small API, design the backing data structure under ambiguous correctness semantics and workload constraints.

常见例子：

| 题型 | API 表面 | 真正考点 |
|------|----------|----------|
| Event Store | `record` / `query(userId, timeRange)` / `count` | time index、乱序写入、range query、write/query trade-off |
| Hit Counter / Rate Limiter | `hit(timestamp)` / `getHits(now)` / `allow()` | sliding window、过期数据清理、精确 vs 近似、memory bound |
| Transaction Pagination | `add(transaction)` / `page(filter, cursor, limit)` | stable ordering、cursor vs offset、secondary index、keyset pagination |
| In-Memory DB | `put` / `get` / `scan(prefix)` / `backup` / `restore` | prefix scan、TTL、snapshot、backup consistency、index selection |
| In-Memory File System | `ls` / `mkdir` / `read` / `mv` / `cp` | tree structure、ordered listing、copy vs move semantics、lock granularity |
| Order Management / Order Book | `place` / `cancel` / `query` / `match` | lifecycle state machine、secondary indexes、priority queues、price-time ordering |

这些题的共同点不是“都用某个数据结构”，而是都要求你做同一件事：

```text
API contract -> data organization -> index choice -> update rules -> query algorithm -> trade-off fallback
```

---

## Appendix B：常见设计 pattern

### 1. Primary Store + Secondary Index

最常见的结构：

```text
primary store:
  id -> entity

secondary indexes:
  userId -> ids
  state -> ids
  timestamp -> ids/events
  prefix/sortKey -> ids
```

适用题：

- Transaction Pagination: `transactionsById` + `transactionsByUserId`
- Order System: `ordersById` + `orderIdsByUserId` + `orderIdsByState`
- Event Store: `eventsByUserId` + per-user time index

核心 trade-off：

```text
More indexes:
  + faster query
  - slower write
  - more memory
  - every update must keep indexes consistent
```

面试里可以这样说：

> I’ll keep one source of truth by id, then add secondary indexes only for hot query patterns. Each extra index improves reads but makes writes and updates more expensive.

### 2. Sorted Access Path

只要 API 里有 range、top-k、pagination、prefix、ordered output，就要问：

```text
Do I need sorted access at write time or query time?
```

常见选择：

| 方案 | 写入 | 查询 | 何时选 |
|------|------|------|--------|
| HashMap + query-time sort/filter | 快 | 慢 | 数据小、query 不热、先写 baseline |
| TreeMap / TreeSet / SkipList | 中等 | 快 | range/prefix/pagination 是 hot path |
| ArrayList sorted by append pattern | 很快 | 快 | 输入 mostly ordered |
| Heap / priority queue | 快速取最值 | 不适合任意删除/范围 | top-k、best price、expiry |

对应例子：

- Event Store: mostly ordered timestamp -> sorted list + late buffer
- Transaction Pagination: cursor 编排序键 -> keyset pagination
- File System: children 用 HashMap，`ls` 时 sort；如果 listing 是 hot path 再考虑 TreeMap
- Order Book: price level 用 TreeMap，level 内 FIFO queue

### 3. Cursor / Boundary Instead Of Position

分页题里的核心 pattern：

```text
cursor should encode the last seen sort key, not the list index.
```

为什么？

- offset/index 会因为新插入、删除、并发更新而漂移
- sort key cursor 即使原记录被删，也可以继续表达“从这个边界之后继续”

适用题：

- Transaction Pagination
- sharded pagination
- event/log catch-up
- market data replay

面试里可以这样说：

> The cursor should represent a boundary in the sort order, not a mutable position in an array.

### 4. Time Window / Expiry / Cleanup

只要题里有最近 N 秒、TTL、rate limit、hit counter、expire，就会出现这个 pattern：

```text
store enough timestamped state to answer the window;
delete expired data lazily or proactively.
```

常见选择：

| 方案 | 何时选 |
|------|--------|
| Queue / deque | exact sliding window, append by time |
| Buckets / ring buffer | bounded time range, lower memory |
| Lazy cleanup | implementation simple, memory may grow |
| Background cleanup | controls memory, adds concurrency complexity |
| TreeMap by expireAt | need find all expired entries efficiently |

适用题：

- Hit Counter
- Rate Limiter
- In-Memory DB TTL
- Order expiry / stop order trigger

面试里要主动说：

> Expired data can be removed lazily during reads, or proactively by a background cleaner. Lazy cleanup is simpler; proactive cleanup gives better memory control.

### 5. State Machine + Index Maintenance

如果 entity 有 lifecycle，比如 order 状态、job 状态、file node 操作，先写状态机，再写 indexes。

```text
ACTIVE -> PAUSED -> CANCELLED
ACTIVE -> FILLED
terminal states cannot transition
```

然后每次状态变化都要问：

```text
Which indexes must be updated atomically with the primary object?
```

适用题：

- Crypto Order System
- job/application queues
- in-memory file operations
- task scheduler

核心 trade-off：

```text
Single primary map:
  + simple updates
  - query by state/user is slow

Primary map + secondary indexes:
  + fast query
  - every transition must update indexes correctly
```

### 6. Snapshot / Consistency Boundary

一旦题目出现并发、backup、scan、pagination、restore，就要问：

```text
Does this operation need a consistent snapshot?
```

例子：

- `scan(prefix)` 输出里所有 key 是否来自同一时刻？
- `backup()` 能不能和 `put()` 交错？
- `query()` 能不能看到一半 old data、一半 new data？
- pagination 跨多次 request 是否要求强 snapshot？

常见策略：

| 策略 | 何时选 |
|------|--------|
| global lock | 最简单，吞吐差 |
| per-bucket/per-user lock | 分区自然、不同 key 互不阻塞 |
| ReadWriteLock | read-heavy，且需要读期间不被写打断 |
| copy-on-write snapshot | scan/backup 慢操作，避免长时间持锁 |
| versioned/MVCC | 强 snapshot，多版本复杂度高 |

面试里可以先实现单线程核心，然后说：

> For concurrency, I’d first define the consistency contract. If each user/key can be isolated, I’ll use per-bucket locking; if scan/backup needs a consistent snapshot, I need either a read lock around the snapshot or copy-on-write/versioning.

---

## 这类题解决的是什么问题？

这类题考的不是“你是否知道一个标准答案”，而是：

1. 你能不能分清 **correctness requirement** 和 **optimization signal**
2. 你能不能从 clarification 中提炼出 implementation constraints
3. 你能不能解释为什么这个数据结构适合当前条件
4. 你能不能说清楚如果条件变了，方案怎么换

以 Event Store 为例：

- Correctness requirement: `query(userId, start, end)` 必须返回该 user 在时间范围内的所有事件。
- Ambiguous requirement: 范围是否 inclusive？结果是否需要排序？有没有 eventId/dedupe？
- Optimization signal: events are mostly ordered；throughput matters。
- Working assumption: late events 相对少，可以先放 buffer。

所以答案不是简单地说“用 TreeMap”或者“用 List”。更好的思路是：

> Correctness requires handling out-of-order events. But mostly ordered input is an optimization signal, so I optimize the common write path as append-only while keeping a late-buffer fallback for correctness.

---

## 它和 LeetCode 的区别

LeetCode 通常是：

```text
Problem is fully specified -> choose known algorithm -> implement.
```

这类题通常是：

```text
Problem is partially specified -> clarify semantics -> choose trade-off -> implement.
```

LeetCode 里，很多条件已经被题面固定好了；你主要是在找最优复杂度。

API-driven data structure design 里，很多条件本身就是面试的一部分。你需要主动问、主动分类、主动收束。比如：

- “mostly ordered” 是 guarantee，还是只是 observed pattern？
- “high throughput” 更偏 write throughput，还是 query latency？
- “out of order” 是任意乱序，还是迟到窗口有限？
- “query” 是否要 snapshot consistency？

如果你直接写代码，很容易写出一个“能跑但没解释 trade-off”的答案。

---

## 它和 Object-Oriented Design 的区别

它也不是典型 OOD。

OOD 通常更关注：

- class hierarchy
- extensibility
- interface design
- entity relationship
- behavior modeling

比如 parking lot、elevator、chess game，这些更像 object model。

Event Store 这种题更关注：

- indexing strategy
- range query
- ingestion pattern
- concurrency boundary
- consistency vs throughput
- memory vs query cost

所以它不是“我要设计多少类”，而是“我要如何组织数据，让 API 在给定 workload 下高效且正确”。

更准确地说，它是：

> API-driven data structure design under realistic system constraints.

---

## Event Store Design Summary

Default structure:

```java
Map<String, UserEvents> eventsByUserId;

class UserEvents {
    List<Event> sortedEvents;
    List<Event> lateBuffer;
}
```

Write path:

```text
if timestamp >= last timestamp:
    append to sortedEvents      // common case O(1)
else:
    append to lateBuffer        // late event O(1)
```

Query path:

```text
1. binary search sortedEvents for first timestamp >= start
2. scan forward until timestamp > end
3. scan lateBuffer
4. sort/merge final result if ordered output is required
```

Complexity:

```text
record:
  common case O(1)

query:
  O(log n + k + b)

where:
  n = number of sorted events for that user
  k = number of matched events
  b = lateBuffer size
```

Trade-off:

```text
TreeMap:
  + stable for arbitrary out-of-order events
  + clean O(log n + k) range query
  - every write pays O(log n)
  - does not exploit mostly ordered input

Sorted list + late buffer:
  + common writes are O(1)
  + exploits mostly ordered input
  + query remains efficient if buffer is small
  - lateBuffer must be controlled
  - if disorder is common, this design degrades
```

---

## One Sentence To Remember

> Separate correctness from optimization, turn clarifications into constraints, choose the simplest data structure that satisfies correctness while exploiting the strongest optimization signal, and state the fallback explicitly.

For Event Store:

> Correctness means returning all events in the user/time range. Mostly ordered timestamps are an optimization signal, so I use append-first sorted lists with a late buffer; if late events are common, I switch to TreeMap.
