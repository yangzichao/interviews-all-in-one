# Coinbase — Drone Delivery

一个人从位置 `0` 出发, 要把包裹送到 `target`. 沿直线上有若干 charging stations. 逐步加需求, 面试时 CoderPad 上**一个 Part 给一个**, 做完再放下一个.

来源: 经典 quant 面试题 "Drone Delivery"(参考 https://ttzztt.gitbooks.io/lc/content/quant-dev/drone-delivery.html), Coinbase VO 也考过, 第 4 part 接的是 0/1 knapsack 变种.

---

## 题面 (Setup)

- 沿一条数轴, 起点 `0`, 终点 `target`, 中间有若干 charging stations (升序, 都在 `[0, target]` 区间).
- 可以走路(每一步任意距离), 也可以**从 station 起飞**坐无人机.
- 无人机满电飞 `R` 单位距离, 飞完落地; **起飞点必须是 station**, 落地后再继续走/再到下一个 station 起飞.
- 求最少**走路**距离(飞行不计).

直觉机制: 你从 0 走到第一个 station, 在那充电起飞, 落地后再走到下一个 station(如果还没到 target 且落地点没正好在某 station 上). 中间 `station_{i+1} - station_i > R` 的间隙就要走路补上.

---

## API 全貌

```java
// Part 1
DronePart1.walkDistance(int target, int[] stations, int range) -> long

// Part 2
DronePart2.totalWalkDistance(int[] targets, int[] stations, int range) -> long

// Part 3
DronePart3.walkDistance(int target, int[] stations, int[] ranges, int globalStartRange) -> long

// Part 4
PackingPart4.maxValue(int[] weights, int[] values, int capacity) -> long
```

---

## 8 个 Part 的核心

> Part 1–4 是面经原题 (经典 quant + Coinbase VO 4-part 渐进设计).
> Part 5–8 是**超越面经**的 follow-up —— Coinbase 团队明确说重点考 "并发 / 数据增长 / 取舍",
> 这些就是面试官真要追问、而面经经常省略的部分.

| Part | 一句话 | 易踩的坑 / 讨论点 |
|------|--------|----------|
| 1 | 单架无人机, fixed range, 一次配送 | 起点 0 不是 station; target 后没有 station; 最后一段也要算 walk |
| 2 | 多包裹, 每次独立, 求总 walk | 每次都从 0 出发, 状态不携带 |
| 3 | 每个 station 自带 drone, range 不同 | `globalStartRange` 是 "你从原点带的那架", 没有就传 0 |
| 4 | 0/1 knapsack 子问题(打包) | 经典 DP, 注意是 0/1 不是 unbounded |
| 5 | **多 drone 并发调度** | 同一 order 不能被两个 drone 抢; 队列结构 / 惊群 / shutdown |
| 6 | **动态重规划 (online)** | drone 在飞中途加新单; greedy vs batch; preemption 成本 |
| 7 | **故障接管 + 持久化** | heartbeat / lease / fencing token; dispatcher 自己崩怎么办 |
| 8 | **地理分片** | sharding key 选 geohash / 邮编 / 一致性哈希; cross-zone handoff |

---

### Part 1 — Single Drone, Fixed Range

```java
static long walkDistance(int target, int[] stations, int range)
```

- `stations` 升序, 都在 `[0, target]`.
- 起点 `0`, 终点 `target`, 都不是 station.
- 走法: 从当前位置走到下一个 station(走路), 在 station 起飞最多 `range`(免费), 落地后位置 = `min(station + range, 下一个 station / target)`. 反复.
- 简化: 把行程切成 "段", 段的 walk = `max(0, gap - range)`, gap 是当前位置到下一个 station 的距离; 最后一段是从最后一个 station(或起点)到 target 的距离, walk = `max(0, gap - range)` 但**只在最后一个 station 起飞那架**才有 range buff, 起点没飞机所以**起点到第一个 station 全部 walk**.

**关键边界:**
- 没有 stations → 全程 walk = `target`.
- target 在最后一个 station + range 之内 → 最后一段 0 walk.
- stations[0] > 0 → 第一段 walk = `stations[0]` (起点没飞机).

**样例:**
```
target=20, stations=[5,15], range=10
  → walk 0→5 (5), fly 5→15 (range 覆盖), walk 15→20 (5). 总=10.

target=20, stations=[], range=10
  → 全部走路, 总=20.

target=10, stations=[5], range=10
  → walk 0→5 (5), fly 5→10 (覆盖). 总=5.
```

---

### Part 2 — Multiple Packages

```java
static long totalWalkDistance(int[] targets, int[] stations, int range)
```

每次独立配送, 每次都从 0 出发, 累加. 没有共享状态, 没有 "顺路" 优化.

**样例:**
```
targets=[10, 20], stations=[5,15], range=10
  → 10: walk 0→5 (5), fly 5→10. = 5
  → 20: walk 0→5 (5), fly 5→15, walk 15→20 (5). = 10
  → 总 15.
```

---

### Part 3 — Per-Drone Range

```java
static long walkDistance(int target, int[] stations, int[] ranges, int globalStartRange)
```

每个 station 自带一架 drone, 各自的 `range`:
- `ranges.length == stations.length`, `ranges[i]` 是 `stations[i]` 起飞时能飞的距离.
- `globalStartRange`: 你从原点带的那架的 range, 没有传 `0`. (起点不是 station, 但能飞)

**机制**: 起点用 `globalStartRange` 直接飞最多 `globalStartRange`, 落地后走路到下一个 station, 该 station 用 `ranges[i]` 起飞, 反复.

**样例:**
```
target=30, stations=[5,15,25], ranges=[20,1,2], globalStartRange=0
  → 起点没飞机. walk 0→5 (5). fly 5→15(range=20 直接到 25 还 over). 不过我们只飞到下一个 station 或 target.
    模型上 — 你飞到 min(5+20, 下一个有用位置). 这里设计上从 stations[1]=15 起飞也是个 station, 你可以贪心地飞过下一个 station 直达更远的 station, 但每次起飞用当前 station 的 range. 也可以选择不在某 station 起飞.
  → 决策问题, 自己想 DP / greedy.
```

**Clarifications**(可以问面试官):
- 是否必须在每个 station 起飞? **不**, 可以跳过 station(走过去不起飞).
- 飞行能不能跨过中间的 station 落到更远处? **可以**, 落地点不必是 station.

---

### Part 4 — 0/1 Knapsack (Packing Sub-Problem)

```java
static long maxValue(int[] weights, int[] values, int capacity)
```

经典 0/1 knapsack: `n` 件 item, 每件最多选一次, 总重量 ≤ `capacity`, 求最大 value.

`weights.length == values.length == n`, `capacity >= 0`.

**样例:**
```
weights=[1,2,3], values=[6,10,12], capacity=5
  → 选 (2,10) + (3,12) = 22.

weights=[1,2,3], values=[6,10,12], capacity=0
  → 0.
```

**典型 trade-off 讨论(面试加分):**
- 2D DP `dp[i][c]` O(n·capacity) 时间 + O(n·capacity) 空间
- 滚动数组 O(n·capacity) 时间 + O(capacity) 空间
- 注意 0/1 vs unbounded 的循环方向

---

## Part 5 — 多 Drone 并发调度 (Coinbase 必问的并发方向)

**问题陈述:**
订单池里有 M 个待送 target. 系统起 N 个 drone, 每个 drone 一个线程跑
"拿单 → 送 → 拿下一个". 同一 order 不能被两个 drone 拿到, 拿不到时不能 busy spin,
shutdown 要优雅 (在飞的送完再退).

**面试官最常追问的 4 个 follow-up:**

1. **"订单池用什么数据结构?"**
   → `LinkedBlockingQueue` 简单但锁竞争; `ConcurrentLinkedQueue` 无锁但要自己 wait/notify;
     或者 `ArrayBlockingQueue` 固定容量带反压. 讨论 MPMC 队列.

2. **"100 个 drone 抢同一个池, 多数时候池是空的, 会不会惊群?"**
   → `Condition` 单个 signal vs `signalAll`; thundering herd 怎么避免.
     扩展: park/unpark 一个特定 worker, work stealing.

3. **"VIP 加急订单怎么办?"**
   → `PriorityBlockingQueue` 代价: 入队 O(log n); 老 order 怎么 aging 提高优先级避免饿死.

4. **"怎么测正确性? (exactly-once)"**
   → 跑 1000 单 + 8 drone, 用 `ConcurrentHashMap<orderId, droneId>` putIfAbsent 校验
     `没有重复处理`. 跑 fuzz 测试 + ThreadSanitizer 思路.

**取舍表:**

| 方案 | put / take 复杂度 | 公平性 | 适用 |
|------|------|--------|------|
| `synchronized` 单锁 + ArrayDeque | O(1) 锁竞争重 | FIFO | drone 数少 (<10) |
| `LinkedBlockingQueue` | O(1) 两把锁 (head/tail 分离) | FIFO | 通用首选 |
| `ConcurrentLinkedQueue` + 自旋/park | O(1) 无锁 | 弱 FIFO | 极高并发, drone 数 >> CPU |
| 分片队列 (per-zone) + work-stealing | O(1) 几乎无竞争 | 弱 | drone 数极大 (>100) |

**自检题:**
- 我的 dispatcher shutdown 后, 还在阻塞 `take()` 的 drone 线程会不会卡死?
- 同一个 order 被两个 drone 拿到, 我用什么机制检测? (compare-and-set / put-if-absent)
- 我的代码在 0 个 order + N 个 drone 等待时, CPU 占用是 0% 还是 100%?

---

## Part 6 — 动态重规划 (Online Re-Planning)

**问题陈述:**
订单是 online 的, 时刻 t 才知道 t 时刻的订单. drone 已经在飞了, 新订单进来怎么决策:
继续手头的 → 接新单? 还是 abort 改飞新单? 还是分给空闲 drone?
评价指标: 平均完成时延 / p99 时延 / 总 walk.

**面试官最常追问的 4 个 follow-up:**

1. **"greedy assign 给最近 drone vs 等下一秒 batch 一起规划, 哪个好?"**
   → 引出 online vs offline algorithm 概念; competitive ratio 思路.
     给的方向: 短时间窗口内 mini-batch 比纯 greedy 通常好, 但延迟容忍要够.

2. **"preemption: 在飞 order A, 来了个超急 order B 就在旁边, 要不要拦截?"**
   → 引出 cancellation cost (已飞距离 + 用户体验) vs B 的 SLA 收益.
     方向: 给两个 order 算 "改派后总延迟变化", 改派只在 ΔSLA < 0 时执行.

3. **"低优先级 order 永远抢不到 drone (饿死) 怎么办?"**
   → Aging: 等待时间越长优先级越高; 或者双队列 + reserved slot 给老单.

4. **"怎么测? drone 移动有时间, 怎么模拟?"**
   → 离散事件仿真 (DES): 事件队列 + mock clock; 不用真睡 1 秒.
     方向: `Clock` 接口注入, 测试里推进虚拟时间.

**取舍表 (online dispatch 策略):**

| 策略 | 平均延迟 | p99 延迟 | 实现复杂度 |
|------|---------|---------|-----------|
| Pure greedy (最近 drone) | 中 | 高 (倒霉单会被一直晾着) | 低 |
| Mini-batch (每 1s 整轮规划) | 低 | 中 | 中 |
| Optimization-based (Hungarian / OR-tools) | 最低 | 低 | 高 |
| Greedy + preemption | 低 | 低 | 中-高 |

**自检题:**
- 我的策略下, p99 延迟会不会比平均延迟高几个数量级?
- 一个 corner case: drone 飞到一半 charging station 失效, 要不要回头?
- 我的 mock clock 在多线程场景下还能保持 "时间单调" 吗?

---

## Part 7 — 故障 Drone 接管 + 持久化 (Failover & Recovery)

**问题陈述:**
drone 会失联 (掉电 / 信号丢). 订单分给它了它没回话, 要转给别人; 但小心 split-brain:
原 drone 没真死, 这单不能送两次. dispatcher 进程也可能崩, 重启后未送 order 不能丢.

**面试官最常追问的 4 个 follow-up:**

1. **"失联多久才算死? 30s? 5min?"**
   → 引出 heartbeat + lease (固定超时) vs Phi accrual failure detector (自适应).
     方向: lease 简单但抖动敏感; Phi 适合网络不稳定环境.

2. **"原 drone 复活了, 它手上 order 已经被转给别人, 怎么防止双送?"**
   → Fencing token (单调递增 epoch): dispatcher 给新主一个更大的 token,
     旧主带着小 token 来 commit "送达" 时被拒. 跟 Kafka leader epoch 同思路.

3. **"dispatcher 自己挂了, 飞在路上的 drone 怎么继续?"**
   → WAL: 每次 assign / ack 落盘. 重启时 replay log 重建分配关系.
     可以跟 `In-Memory-Database` Part 7 的 fsync 策略讨论联动.

4. **"怎么保证 'order 已送达' 是 exactly-once?"**
   → 幂等的 ack (用 orderId 作为去重 key) + at-least-once delivery + dedup.

**取舍表:**

| 故障检测 | 检测时间 | 误判率 | 实现 |
|---------|---------|--------|------|
| 固定 lease (30s) | 30s | 中 (网络抖动误判) | 低 |
| 自适应 (Phi accrual) | 可调 | 低 | 中-高 |
| Quorum heartbeat (多个 dispatcher 投票) | 中 | 极低 | 高 (要共识) |

**自检题:**
- 我的 fencing token 在 dispatcher 重启后是否仍单调? (重启后从 0 开始会重复)
- WAL 的 fsync 策略选哪种? 一笔 "order 已送达" 没 fsync 就崩了, 用户却收到了包裹, 怎么办?
- 旧 drone 复活后我的系统怎么告诉它 "你被替换了"?

---

## Part 8 — 地理分片 / 跨区调度 (Geo Sharding)

**问题陈述:**
城市切成 K 个 dispatch zone, 每 zone 自己的 station 池 + drone 池.
target 落 zone A → A 的 drone 送; 但边界 / overload / zone-down 时怎么办?

**面试官最常追问的 4 个 follow-up:**

1. **"sharding key 用什么?"**
   → Geohash / S2 cell (Uber 用 H3) vs 邮编 vs 一致性哈希.
     方向: 地理空间最好用 hierarchical grid (S2/H3), 因为支持 "相邻 cell" 查询.

2. **"跨 zone handoff 怎么保证 exactly-once?"**
   → 复用 Part 7 的 fencing token; 跨 zone 时 zone A 标记 order=HANDED_OFF + 写 log,
     zone B 拿到 ack 才接管. 类似 2PC 简化版.

3. **"全局 SLA (95% 单 30 分钟达) 在分片下怎么监控?"**
   → 各 zone 上报 metrics 到中心 (Prometheus / OTel), 全局聚合.
     注意 sampling: 单 zone latency 分布 ≠ 全局分布的简单平均.

4. **"drone 跨 zone 飞需要协调空域 / 路径冲突, 怎么搞?"**
   → 数据面 (drone-to-drone) vs 控制面 (zone leader) 分开.
     中心 coordinator (etcd/ZK) 管路径预定, 不在 hot path 上.

**取舍表:**

| 分片 key | 加 zone 代价 | cross-zone scan 友好 | 热点风险 |
|---------|-------------|---------------------|---------|
| `hash(zipcode) % N` | 几乎全迁 ❌ | 差 | 低 |
| 一致性哈希 | ~1/N 迁移 ✓ | 差 | 用 vnode 缓解 |
| Geohash / S2 (范围分片) | 中 | **好** (相邻 cell 直接查) | 高 (闹市区) |
| Hybrid (S2 + load balance) | 中 | 好 | 中 |

**自检题:**
- 我的 router 怎么避免跨 zone routing 的 hot spot (所有 cross-zone 都打一个中心节点)?
- zone 整个挂了, 它的 order 要不要 fan-out 给邻居 zone? 路上飞的 drone 算谁的?
- 全局 backup (整个城市状态) 在分片下如何做出一致快照? (Chandy-Lamport / 全局 timestamp)

---

## 怎么练

```bash
cd src/02c-其他算法面经/Coinbase/Drone-Delivery

javac DroneDelivery.java DroneDeliveryTest.java

java DroneDeliveryTest                # 跑所有 Part
java DroneDeliveryTest part1          # 只跑 Part 1
java DroneDeliveryTest part1 part2    # 跑指定的几个
```

输出格式:

```
Part 1 SKIPPED (not implemented)
Part 2 SKIPPED (not implemented)
Part 3 SKIPPED (not implemented)
Part 4 SKIPPED (not implemented)

Passed=0  Failed=0  Skipped=4
```

骨架在 [DroneDelivery.java](DroneDelivery.java), 测试在 [DroneDeliveryTest.java](DroneDeliveryTest.java). 每个 Part 在文件里是一段独立的 `public static class`, 后缀 `PartN`:

```
PART 1: DroneDelivery.DronePart1              [⚠ 你来写]
PART 2: DroneDelivery.DronePart2              [⚠ 你来写]
PART 3: DroneDelivery.DronePart3              [⚠ 你来写]
PART 4: DroneDelivery.PackingPart4            [⚠ 你来写]
PART 5: DispatcherPart5                       [⚠ 超越面经 follow-up — 并发调度]
PART 6: PlannerPart6                          [⚠ 超越面经 follow-up — 动态重规划]
PART 7: FailoverDispatcherPart7               [⚠ 超越面经 follow-up — 故障接管]
PART 8: GeoRouterPart8 (建议入口)             [⚠ 超越面经 follow-up — 地理分片, 偏设计讨论]
```

**为什么独立 class?** 真实面试是渐进的——做完 Part 1 才会让你看 Part 2. 这种切片结构让你能专注当前 Part 而不破坏已完成的部分. Part 4 跟 Part 1-3 关系最远(连同一个 problem 都不算), 单独 class 更清楚.
