# Coinbase — Crypto Order System (OMS)

设计一个订单管理系统(Order Management System),逐步加功能。面试时 CoderPad 上**一个 Part 给一个**,做完再放下一个。

来源:Coinbase VO 高频题(#4)。不在 LeetCode 上,本质是**状态机 + 多维 index** 的 OOP 题。

---

## API 全貌

```java
// 共享数据类型
CryptoOrderSystem.OrderState  // enum: ACTIVE, PAUSED, CANCELLED, FILLED
CryptoOrderSystem.Order(String orderId, String userId, String symbol, long quantity, OrderState state)

// Part 1 — Basic
new OmsPart1()
    .placeOrder(orderId, userId, symbol, quantity)   // 进 ACTIVE; 重复 id → IllegalArgumentException
    .getOrder(orderId) -> Order                      // 没有 → null

// Part 2 — Lifecycle
new OmsPart2()
    .placeOrder(...)
    .getOrder(...)
    .pauseOrder(orderId)    // ACTIVE → PAUSED
    .resumeOrder(orderId)   // PAUSED → ACTIVE
    .cancelOrder(orderId)   // ACTIVE / PAUSED → CANCELLED

// Part 3 — Bulk cancel by user
new OmsPart3()
    .cancelAllOrdersForUser(userId) -> int   // 取消该 user 的 ACTIVE+PAUSED, 返回数量

// Part 4 — Query by state
new OmsPart4()
    .ordersInState(state) -> List<Order>
    .ordersInStateForUser(state, userId) -> List<Order>
```

---

## 8 个 Part 的核心

> Part 1–4 是面经原题(Coinbase VO 高频 4-part 渐进设计题)。
> Part 5–8 是**超越面经**的 follow-up —— Coinbase 团队明确说重点考"并发 / 数据结构 / 取舍",
> 这些就是面试官真要追问、而面经经常省略的部分。

| Part | 一句话 | 易踩的坑 / 讨论点 |
|------|--------|----------|
| 1 | `Map<orderId, Order>` | duplicate id 抛 `IllegalArgumentException`; missing 返 `null` (不抛) |
| 2 | 状态机: ACTIVE ↔ PAUSED, → CANCELLED | 非法转移抛 `IllegalStateException`; CANCELLED 和 FILLED 都是 terminal |
| 3 | 按 user 批量取消 | 只取消 ACTIVE+PAUSED, 返回**实际取消数量** (不算已 CANCELLED/FILLED) |
| 4 | 按 state 查询 (per-user 也支持) | naive 是 O(n), 面试加分点是 two-layer index → O(k) |
| 5 | **并发安全** | 多 index 的原子更新 / cancel-vs-fill race / snapshot vs live |
| 6 | **撮合引擎 + 订单簿** | 价格档位 DS (TreeMap / SkipList / array) / price-time priority / partial fill |
| 7 | **订单类型扩展** (MARKET/IOC/FOK/STOP) | FOK 预检 / STOP 触发条件 DS / per-symbol 单线程串行化 |
| 8 | **WAL 持久化 + 市场数据广播** | fsync 时机 / replay 不重发 tick / snapshot+delta+seq 协议 |

### Part 1 — Basic Place / Get

```java
oms.placeOrder("o1", "alice", "BTC-USD", 100);
Order o = oms.getOrder("o1");   // state = ACTIVE
oms.getOrder("missing");        // null (不抛!)
oms.placeOrder("o1", ...);      // throw IllegalArgumentException — duplicate id
```

### Part 2 — State Machine

```
合法转移:
  ACTIVE → PAUSED      (pauseOrder)
  PAUSED → ACTIVE      (resumeOrder)
  ACTIVE → CANCELLED   (cancelOrder)
  PAUSED → CANCELLED   (cancelOrder)

非法 → IllegalStateException:
  PAUSED → PAUSED      (重复 pause)
  ACTIVE → ACTIVE      (重复 resume)
  CANCELLED → *        (terminal)
  FILLED → *           (terminal — 虽然这一 Part 没暴露 FILLED 怎么进, 但要假设它是 terminal)
```

`FILLED` 这一 Part 不会被任何方法触发,但 state 是 terminal 这一点要写对——`cancelOrder` 在 FILLED 上要抛。

### Part 3 — Cancel All for User

```
userA 有: o1(ACTIVE), o2(PAUSED), o3(CANCELLED), o4(ACTIVE)
cancelAllOrdersForUser("userA") → 返回 3  (o1, o2, o4 被取消; o3 跳过)
```

调用后 o1/o2/o4 都是 CANCELLED,o3 还是 CANCELLED(没变)。

### Part 4 — Query by State

```java
oms.ordersInState(ACTIVE);                        // 所有 ACTIVE 订单
oms.ordersInStateForUser(ACTIVE, "alice");        // alice 的 ACTIVE 订单
```

返回顺序**不指定**——测试用 set 比较。

**面试加分点 — 数据结构设计:**

| 方案 | placeOrder | state 变更 | query by state | query by state+user |
|------|-----------|------------|----------------|---------------------|
| **单 Map<id, Order>**(简单起步) | O(1) | O(1) | O(n) | O(n) |
| **+ Map<State, Set<id>>**(双索引) | O(1) | O(1) | O(k) | O(k) |
| **+ Map<State, Map<userId, Set<id>>>**(三层) | O(1) | O(1) | O(k) | O(k) ↓ user 切片更快 |

> "I'd start with the single map, then propose a secondary index from State → Set of orderIds so that ordersInState becomes O(k). If per-user query is hot, a third layer keyed by userId makes it O(k_user). Trade-off is every state transition has to update the index."

每次 state 变更要同步更新所有 index——这是真正的代码量来源,不是查询本身。

---

## Part 5 — 并发安全 (Coinbase OMS 面试**核心**考点)

**问题**:N 个线程同时调 `placeOrder / pauseOrder / cancelOrder / cancelAllOrdersForUser`,
另一些线程调 `ordersInState*` 查询。要求**所有方法线程安全**,
状态转移必须原子化(read-modify-write 不能撕裂),query 必须看到一致快照。

**为什么这一 Part 比 Part 5 of InMemoryDB 难?** OMS 有**多层副 index**:
主 map `Map<id, Order>`、 `Map<userId, Set<id>>`、 `Map<State, Map<userId, Set<id>>>`。
一次 cancel 要原子地更新所有这些 —— 任何一处不一致都会出现 ghost order。

**面试官最常问的 4 个 follow-up**:

1. **"用 ConcurrentHashMap 就够了吗?"**
   → 不够。ConcurrentHashMap 让单 map 操作原子,但**跨多个 map 的复合操作**(主 map +
   state index + user index)不原子。需要要么加锁包住整个 transition,要么用版本号 /
   MVCC 让 query 容忍中间态。
   → 引出"compound operation atomicity"讨论。

2. **"cancel 跟 fill (撮合成交) 同时发生,谁赢?"** (Part 6 才有 fill,但提前问)
   → 这就是经典 **race to terminal**。两种主流方案:
     - **per-order CAS**:`AtomicReference<Order>`,谁先 CAS 成功谁赢,失败方收到拒绝。
     - **per-order lock + 状态机检查**:拿锁 → 检查当前 state 是否还能转移 → 转移 → 释放。
   → 关键:**transition 函数必须以"前置 state"作为 CAS 的 expected**,不能只看 id。

3. **"`cancelAllOrdersForUser` 要不要全原子?"**
   → 经典两难。全原子(拿大锁挨个 cancel)→ 期间所有其他用户都被阻塞,延迟高。
   逐个 cancel(每单一把小锁)→ 期间用户可以 placeOrder,刚 place 的可能漏掉。
   → 实际工程取舍:**最终一致**,接受 "下一笔 cancel-all 会扫到"。

4. **"`ordersInState(ACTIVE)` 返回的 List 我拿到后,后台改了 state 怎么办?"**
   → 这就是 **snapshot vs live** 取舍。
     - 返回 immutable snapshot(在锁内拷一份)→ 安全但内存翻倍。
     - 返回 weakly-consistent view(像 CHM 的 iterator)→ 看到的可能是混合时刻。
   → 面试加分:**"返回值带 sequence number,调用方需要严格快照可以传 seqNo 回来 verify"**。

**取舍表**:

| 方案 | placeOrder 吞吐 | query 吞吐 | 一致性 | 实现复杂度 |
|------|----------------|-----------|--------|----------|
| 一把大锁 (synchronized this) | 低 | 低 | 强 | 极低 |
| ReadWriteLock | 中 | 高(读并发) | 强 | 低 |
| CHM + per-order CAS | 高 | 高 | 弱(compound 不原子) | 中 |
| 分段锁 (per-userId hash bucket) | 中-高 | 中 | 中 | 中-高 |
| MVCC (版本号 + immutable snapshot) | 高 | 极高 | 强(读历史版本) | 高 |

**自检题**:
- 两个线程同时 `pauseOrder("o1")` 和 `cancelOrder("o1")`,最后状态是什么?面试官接受"未定义"吗?
- `cancelAllOrdersForUser("alice")` 跑到一半,另一线程 `placeOrder("aN", "alice", ...)`,新单会被取消吗?你的语义是什么?
- `ordersInStateForUser(ACTIVE, "alice")` 返回 List 给调用方,调用方迭代时第 3 个 order 被改成 PAUSED,List 里那个 element 是什么状态?

---

## Part 6 — 撮合引擎 + 订单簿 (从 OMS 跳到 Exchange)

**问题**:Part 1-5 都是订单**管理**(track state),没有**撮合**。现在加:
新限价单进来,如果对手盘有可成交价就立刻撮合,残量挂簿。
按 **price-time priority**:同 symbol 内 BUY 高价优先 / SELL 低价优先,同价 FIFO。

**面试官最常问的 4 个 follow-up**:

1. **"价格档位用什么数据结构?"** (这是真考点)
   → 候选:`TreeMap<Price, Queue<Order>>` / `ConcurrentSkipListMap` / 离散化 array of levels / heap of heaps。
   → 真实交易所(LMAX、Nasdaq):**离散化 array + 双向链表 per level**,bestBid 是 O(1),
   插入 O(1),cancel 中间订单需要 `Map<orderId, Node>` 拿到节点引用,然后 O(1) unlink。
   → 面试里写 TreeMap 完全够用,但要**主动**讲"如果延迟极致,会换离散化 array"。

2. **"price-time priority 怎么实现?cancel 中间订单怎么 O(1)?"**
   → 每个 price level 是一个 FIFO。`ArrayDeque` 不能中间删,得用**双向链表 + `Map<orderId, Node>`**:
   place 时 append to tail + 记 Node; cancel 时 O(1) unlink。
   → 这就是 LinkedHashMap 也能 (按 insertion order iterate),但拿到 node ref 不方便。

3. **"撮合是单线程还是多线程?"**
   → **per-symbol 单线程** (LMAX Disruptor) 是业界标配。BTC-USD 一个线程,ETH-USD 另一个,
   完全无锁(线程内串行)。跨 symbol 没撮合关系,不需要同步。
   → 多线程撮合需要 lock-free order book(很复杂),收益不大。

4. **"partial fill 后,被吃掉的对手盘订单 state 是什么?"**
   → 引出 `PARTIALLY_FILLED` state(本 Part 没暴露过渡接口,但讨论要提到)。
   实际:`new Order(...)` 替换 quantity 字段为剩余量,state 仍是 ACTIVE
   (业界叫法不一,Coinbase 内部多用 OPEN/PARTIALLY_FILLED 区分)。

**取舍表 — 价格档位数据结构**:

| DS | bestBid / bestAsk | 同档 enqueue | 中间 cancel | 内存 | 何时选 |
|----|-------------------|--------------|------------|------|--------|
| `TreeMap<Long, ArrayDeque>` | O(log n) | O(1) | O(level) | 紧凑 | 面试默认 |
| `ConcurrentSkipListMap` | O(log n) | O(1) | O(level) | 紧凑 | 多线程读多写少 |
| 双向链表 per level + `Map<id, Node>` | O(log n) | O(1) | **O(1)** | 紧凑 | cancel hot |
| 离散化 array of levels | **O(1)** | O(1) | O(1)(配 Map) | 浪费(稀疏) | 高频延迟极致 |
| Heap of heaps | O(1) bestBid | O(log n) | O(n) ❌ | 紧凑 | 不推荐 |

**自检题**:
- 同价两单 `b1, b2` 先后到达,`b1` quantity=5,卖单进来吃 3 → b1 剩 2 还是 b1 被全部吃完?(price-time priority 答案)
- bestBid 在我刚读出后,另一线程 cancel 了那一档唯一一单 → 我返回的 100L 还有效吗?
- 卖单 `s1@99` 进来,簿上有 buy `b1@100`,b1 quantity=10,s1 quantity=3 → 成交价多少?(price improvement: 99 还是 100?业界答案是**maker 价**,b1 的 100。)

---

## Part 7 — 订单类型扩展 (MARKET / IOC / FOK / STOP)

**问题**:Part 6 只有 GTC limit。扩展:MARKET、LIMIT+IOC、LIMIT+FOK、STOP_LIMIT、STOP_MARKET。

**面试官最常问的 4 个 follow-up**:

1. **"FOK 怎么预检"全成交"?"**
   → 不实际吃单的前提下扫对手盘价格档位,累加 quantity 直到够。够了再走真正撮合;
   不够直接拒(state=REJECTED 或丢弃,看产品定义)。
   → 这要求订单簿支持"只读 walk":返回一个 iterator 但不修改 levels。
   → 面试官追问:"如果两个 FOK 同时预检都通过,真撮合时撞车?"
     → 答案是 **per-symbol 单线程**,FOK 预检 + 撮合在同一线程串行,没有竞争。

2. **"STOP 单的触发条件存哪里?"**
   → 维护两个 TreeMap:
   ```
   buyStops:  TreeMap<TriggerPrice, List<StopOrder>>   // last_trade >= trigger 触发
   sellStops: TreeMap<TriggerPrice, List<StopOrder>>   // last_trade <= trigger 触发
   ```
   每次成交后:`buyStops.headMap(lastTrade, true)` 拿到所有要触发的(O(log n + k))。
   → STOP_MARKET 触发后转 MARKET, STOP_LIMIT 触发后转 LIMIT 挂簿。

3. **"MARKET 单要不要 slippage 保护?"**
   → 实际上 Coinbase 不允许纯 MARKET —— 都是 market with limit price (像 IOC limit)。
   否则:用户 market buy 1000 BTC,对手盘只有 0.1 BTC @100,剩下被 @1,000,000 的烂单吃 → 用户哭。
   → 答题方向:**永远在 quoting 系统里把 MARKET 包装成"对手盘最优价 ± slippage%"的 IOC limit**。

4. **"GTD (Good-Till-Date) 怎么实现?"**
   → 跟 In-Memory-DB Part 6 的 TTL 清理一样:
     - 后台线程定期扫过期单 cancel 之
     - 或者 `TreeMap<ExpireAt, Set<OrderId>>` 每次撮合后看 firstKey 是否 ≤ now,挨个清。

**取舍表 — TimeInForce 实现**:

| TIF | 撮合后残量 | 预检需要 | 实现难度 | 失败语义 |
|-----|-----------|---------|---------|---------|
| GTC | 挂簿 | 不需要 | 低 | 永不失败,挂着等 |
| IOC | 丢弃 | 不需要 | 低 | 总是部分成交即满意 |
| FOK | **全拒** | 需要 walk | 中 | 不够全成交 → 拒单 |
| GTD | 挂簿到 expireAt | 不需要 | 中 | 过期 = 自动 cancel |

**取舍表 — OrderType 实现**:

| Type | 路径 | 关键风险 |
|------|------|---------|
| LIMIT | match → 残量挂簿 | partial fill 簿状态一致性 |
| MARKET | match @ best,残量丢 | **吃穿订单簿** → 必加 slippage cap |
| STOP_LIMIT | 等触发 → 转 LIMIT | trigger 表 O(log n) 查 |
| STOP_MARKET | 等触发 → 转 MARKET | trigger + slippage 双重风险 |

**自检题**:
- FOK BUY 10 @100,簿上 ask side 有 `s1@98 x5, s2@100 x3, s3@101 x10`,预检通过吗?(98+100 累计 8,不够 10 → 拒)
- STOP_LIMIT BUY trigger=105, limit=106:刚触发时如果 best ask 已经 = 110,我的单怎么办?(挂簿 @106 等价格回来,不会乱吃)
- 两个 FOK 同时进来:f1 BUY 5 @100, f2 BUY 5 @100,簿上 ask 一共只有 5 @100。如果 per-symbol 单线程,谁拿到?(先到的全成交,后到的全拒)

---

## Part 8 — WAL 持久化 + 市场数据广播

**问题**:进程崩了,挂在簿上的订单不能丢。同时撮合产生的 trade tick / book delta
要 fanout 给所有 WebSocket 客户(交易所必备)。

**面试官最常问的 4 个 follow-up**:

1. **"WAL 写在撮合之前还是之后?"**
   → **必须之前**(write-ahead)。 顺序:
   ```
   收单 → validate → append WAL → fsync → 撮合 → ACK 给用户 → 广播 tick
   ```
   → 如果 "撮合完才写 WAL",崩溃后 replay 会比 ACK 给用户的状态少 → 灾难。
   → 面试官追问:"那撮合 1ms,fsync 10ms,吞吐爆炸?" → 答案:批量 fsync,或者用 NVMe + group commit。

2. **"恢复时要不要重新广播 trade tick?"**
   → **不要**。WAL replay 只重建内存状态,不重发 market data。否则订阅者收到重复 tick。
   → 实现:区分 "live mode" 和 "replay mode",replay 时 listener fanout 关闭。
   → 客户端的 reconnect 用 sequence number 找断点,自己 catch up,不依赖 server replay 推送。

3. **"广播怎么不被慢的 listener 拖垮撮合线程?"**
   → 三种主流方案:
     - 同步 fanout:撮合线程 in-line `listener.onTrade()` —— 慢 listener 阻塞撮合 ❌
     - **每 listener 一个异步 ring buffer**:撮合只 enqueue,worker 线程 fanout;
       listener 处理慢 → ring 满 → **直接断开连接 + 让客户端 reconnect**。
     - 单一 broadcast 队列,所有 listener 共享 cursor:最快 listener 拉到哪算哪,慢的自己负责追上。
   → Coinbase Pro 实际采用第 2 种。

4. **"新订阅者怎么拿到完整订单簿?同时不丢/不重 delta?"** (snapshot + delta 协议)
   → 撮合线程在某个 seq=S 拿快照:`(snapshot, S)`,发给新订阅者。
   → 同时撮合后续每个 delta 都带递增 seq。
   → 新订阅者:
   ```
   收到 snapshot(S) → 缓冲 seq <= S 的 delta 全丢 → 从 seq = S+1 开始 apply 后续 delta
   ```
   → 这是 Coinbase Pro WebSocket 的真实协议(`l2update` channel)。

**取舍表 — WAL fsync 策略**:

| 策略 | 吞吐 | 崩溃丢失 | Coinbase 真实 |
|------|------|---------|--------------|
| 每笔 fsync | 极低 (~1k ops/s) | 0 | **订单关键路径** 通常用 |
| 批量 fsync (group commit) | 高 | 最近一批 | 高频场景折中 |
| 每 N ms fsync | 极高 | 最近 N ms | 缓存类数据 |

**取舍表 — 广播模型**:

| 模型 | 撮合延迟影响 | 慢 listener 处理 | 实现 |
|------|------------|----------------|------|
| 同步 fanout | 大 (受 listener 拖累) | 阻塞所有人 | 简单 |
| **per-listener async ring** | 极小 | 自动断开 | 中 |
| 单广播队列共享 cursor | 极小 | 慢的自己追 | 中-高 |

**自检题**:
- WAL 写完但 fsync 前进程被 kill -9,重启后状态是什么?(取决于 OS page cache,**不保证**写到了 disk,**ACK 不能在 fsync 之前给**)
- 客户 A 订阅 `BTC-USD` 5 分钟后 reconnect,我怎么让他 catch up 而不丢漏中间 trade?(seq number + 客户端拉)
- 我的 trade tick 在 listener 同步 fanout 中调了一个慢的网络调用 → 下一笔订单撮合延迟变 +500ms,怎么 debug?(metrics + per-listener latency histogram,异步化)

---

## 怎么练

```bash
cd src/02c-其他算法面经/Coinbase/Crypto-Order-System

javac CryptoOrderSystem.java CryptoOrderSystemTest.java

java CryptoOrderSystemTest                # 跑所有 Part
java CryptoOrderSystemTest part1          # 只跑 Part 1
java CryptoOrderSystemTest part1 part2    # 跑指定的几个
```

输出格式:

```
Part 1 SKIPPED (not implemented)
Part 2 SKIPPED (not implemented)
Part 3 SKIPPED (not implemented)
Part 4 SKIPPED (not implemented)

Passed=0  Failed=0  Skipped=4
```

骨架在 [CryptoOrderSystem.java](CryptoOrderSystem.java),测试在 [CryptoOrderSystemTest.java](CryptoOrderSystemTest.java)。每个 Part 在文件里是一段独立的 `public static class`,后缀 `PartN`:

```
PART 1: CryptoOrderSystem.OmsPart1   [⚠ 你来写] — Basic place/get
PART 2: CryptoOrderSystem.OmsPart2   [⚠ 你来写] — State machine
PART 3: CryptoOrderSystem.OmsPart3   [⚠ 你来写] — Bulk cancel by user
PART 4: CryptoOrderSystem.OmsPart4   [⚠ 你来写] — Query by state (+ index)
PART 5: CryptoOrderSystem.OmsPart5   [⚠ 超越面经] — 并发安全
PART 6: CryptoOrderSystem.OmsPart6   [⚠ 超越面经] — 撮合引擎 + 订单簿
PART 7: CryptoOrderSystem.OmsPart7   [⚠ 超越面经] — MARKET/IOC/FOK/STOP
PART 8: CryptoOrderSystem.OmsPart8   [⚠ 超越面经] — WAL + market data
```

**为什么每个 Part 独立 class 而不是继承?** 真实面试是渐进的——做完 Part 1 才会让你看 Part 2,而 Part 4 的数据结构选择(双索引 vs 单 map)如果继承上来就被"锁死"。独立 class 让你能在 Part 4 重新选数据结构而不破坏前面;做完之后还能讨论"我会重构成 `AbstractOms` 然后子类加 index"——这种 trade-off 讨论是面试加分点。

**Order 是 record + state 字段** —— 这意味着改 state 要重新 `new Order(...)` 替换,不是 mutate。这是故意的:面试官常爱问 immutability vs mutability,record 让你天然站对边。
