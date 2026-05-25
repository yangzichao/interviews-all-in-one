# 00 — Concurrency Basics (教学型 drill)

> **这一节不是题, 是把 JUC (java.util.concurrent) 的常用 API 亲手敲一遍**。
> 每个 lesson 都很小 (15-30 行), 聚焦**一个原语**, 目的是让你: ①记住接口名 ②记住术语 ③体会"这个东西到底解决什么问题"。
> 做完这 9 个 lesson 再去碰 01-06 的真题, 会舒服很多。

---

## 怎么跑

```bash
cd Lesson01-Volatile
javac Lesson01.java Lesson01Test.java
java Lesson01Test
```

stub 里抛 `UnsupportedOperationException`, 测试会输出 `SKIPPED`。你把 TODO 填掉、再跑, 应该看到 `PASSED`。

---

## 学习顺序 (按依赖关系排好)

| # | 主题 | 核心 API | 一句话概括 |
|---|------|----------|------------|
| 01 | [volatile (可见性)](Lesson01-Volatile/) | `volatile` 关键字 | 写了别的线程能立刻看到 |
| 02 | [synchronized (互斥)](Lesson02-Synchronized/) | `synchronized` 关键字 | 同一时刻只让一个线程进临界区 |
| 03 | [AtomicLong / CAS](Lesson03-Atomic/) | `AtomicLong`, `compareAndSet` | 不上锁也能正确地 ++ |
| 04 | [ConcurrentHashMap](Lesson04-ConcurrentHashMap/) | `computeIfAbsent`, `merge` | "查不到就插入"这种复合操作得是原子的 |
| 05 | [ReentrantLock + Condition](Lesson05-ReentrantLock/) | `lock.lock()`, `tryLock`, `Condition` | synchronized 的可控版本 |
| 06 | [BlockingQueue](Lesson06-BlockingQueue/) | `ArrayBlockingQueue.put / take` | producer-consumer 的标准 hand-off |
| 07 | [CountDownLatch](Lesson07-CountDownLatch/) | `latch.countDown()`, `latch.await()` | "等 N 件事都做完"的一次性闸门 |
| 08 | [Semaphore](Lesson08-Semaphore/) | `sem.acquire()`, `sem.release()` | 限制同时进入资源的线程数 |
| 09 | [ExecutorService + Future](Lesson09-Executor-Future/) | `submit(Callable)`, `future.get()` | 扔任务给线程池、之后取结果 |

---

## 通用术语表 (interview vocabulary)

记面试里别人/你自己嘴里要冒出来的英文词:

| 中文 | English | 一句话 |
|------|---------|--------|
| 临界区 | critical section | 一次只能一个线程进的代码段 |
| 互斥 | mutual exclusion / mutex | 临界区的保护机制 |
| 内存可见性 | memory visibility | 一个线程的写, 别的线程**多久能看到** |
| 内存模型 | Java Memory Model (JMM) | 定义"什么时候能看到"的规则 |
| happens-before | happens-before | JMM 里"A 的写一定先于 B 的读"的关系 |
| 比较并交换 | compare-and-swap (CAS) | "如果还是旧值就改成新值, 否则失败重试", 硬件指令 |
| 无锁 | lock-free | 用 CAS 而不是 mutex 实现并发 |
| 自旋 | spin / busy-wait | 死循环里反复 check, 不挂起线程 |
| 阻塞 | block | 线程挂起, 让出 CPU, 等条件满足 |
| 死锁 | deadlock | 两个线程互相等对方放锁, 永远卡住 |
| 活锁 | livelock | 都在动, 但谁也推进不了 |
| 饥饿 | starvation | 一个线程永远抢不到锁 |
| 竞争 | contention | 多个线程同时抢一个资源 |
| 数据竞争 | data race | 读和写同一个无保护变量, 行为未定义 |
| 重入 | reentrancy | 同一线程能再次进入它已经持有的锁 |
| 公平性 | fairness | 锁是否按到达顺序给 (FIFO) |
| 背压 | back-pressure | 下游慢, 让上游也慢下来 (满队列 block 生产者) |
| 一次性 | one-shot | latch 用完不能 reset, 信号一次有效 |
| 许可证 | permit | semaphore 里的"票", acquire 拿一张, release 还一张 |
| 取消 | cancel | `future.cancel(true)` 试图中断任务 |
| 超时 | timeout | 等不到就放弃, 不死等 |
| 中断 | interrupt | 通知线程"该停了", 由线程自己决定怎么停 |
| 关闭 | shutdown / shutdownNow | 线程池停止接受新任务 / 试图中断在跑的任务 |

---

# 课程内容

## Lesson 01 — `volatile` (内存可见性)

**问题**: 一个线程 `running = true` 在自旋, 另一个线程 `running = false`, 第一个线程**可能永远停不下来** —— 因为编译器/CPU cache 让它一直读旧值。

**解决**: 给 `running` 加 `volatile`。每次读必须从主存读, 每次写立刻刷主存。

**术语**:
- `volatile`: 保证**可见性**和**有序性** (禁止某些重排), 但**不保证原子性** (`volatile int x; x++` 仍然不是 atomic)
- visibility, stale read, memory barrier, JMM happens-before

**Drill**: 实现 `Lesson01`:
- `volatile boolean running`
- `long ticks` (普通 long, 只在 worker 线程里写)
- `Thread worker`
- `start()`: 启动 worker, 它 `while (running) { ticks++; }`
- `stop()`: 设 `running = false`, `worker.join()`
- `ticks()`: 返回 ticks

**观察**: 把 `volatile` 去掉再跑, 在某些 JIT 优化下测试会卡住 → 这就是 visibility 问题。

---

## Lesson 02 — `synchronized` (互斥锁)

**问题**: `i++` 不是原子的 (它是 load → add → store 三步). 两个线程一起 ++ 会丢更新。

**解决**: 用 `synchronized` 关键字把这三步包成一个原子操作。

**术语**:
- `synchronized`: Java 的内置 (intrinsic / monitor) 锁
- monitor, mutex, lock object
- 加在方法上 = 锁 `this` (静态方法锁 Class 对象); 加在 block 上要显式指定锁对象
- **不允许**: 用 String literal 或 Integer 装箱对象做锁 (会共享, 招意外冲突)

**Drill**: 实现 `Lesson02`:
- `private long count;` (注意: **不**用 volatile, 不用 Atomic)
- `synchronized void increment()`: count++
- `synchronized long get()`: 返回 count
- 测试: 8 个线程 × 10000 次 increment, 期望 80000

**观察**: 把 `synchronized` 去掉, 测试会得到一个 < 80000 的数 (race lost update)。

---

## Lesson 03 — `AtomicLong` / CAS (无锁原子)

**问题**: synchronized 在高竞争下会让线程互相阻塞, 上下文切换很贵。

**解决**: 用 CPU 的 CAS 指令做无锁更新。`AtomicLong.incrementAndGet()` 内部就是 `compareAndSet` 循环。

**术语**:
- CAS = compare-and-swap, 硬件原子指令: "if memory == expected then memory = new"
- lock-free (无锁), wait-free (无等待, 更强)
- ABA 问题: 值从 A 变 B 又变 A, CAS 看不出来 (用 `AtomicStampedReference` 解决)
- retry loop: CAS 失败就重试

**Drill**: 实现 `Lesson03`:
- `AtomicLong count` 和 `AtomicLong max`
- `increment()`: 用 `count.incrementAndGet()`
- `get()`: 返回 count
- `updateMax(long candidate)`: 用 `max.compareAndSet(old, candidate)` 的 retry loop, 只在 candidate > 当前 max 时更新
- `max()`: 返回 max
- 测试: 16 线程并发 updateMax, 最后 max 应该等于所有 candidate 的最大值

**观察**: 这是手写 CAS retry loop 的经典模式, 记住这个 pattern。

---

## Lesson 04 — `ConcurrentHashMap` (复合原子操作)

**问题**: `if (!map.containsKey(k)) map.put(k, newValue())` 不是原子的。两个线程同时进来都会 put 一次, 后写的覆盖先写的, 而且 `newValue()` 可能被调两次 (浪费/副作用)。

**解决**: `ConcurrentHashMap.computeIfAbsent(k, lambda)` 是**单一原子操作**, 整张表针对这个 key 上锁, lambda 至多被调一次。

**术语**:
- `ConcurrentHashMap` (CHM): 分段锁 / per-bin 锁, 比 `Collections.synchronizedMap` 快得多
- check-then-act race: "先查再写"的经典 bug 模式
- compound atomic operation: 复合原子操作, JDK 给你打包好的 (`computeIfAbsent`, `merge`, `putIfAbsent`)
- `merge(k, v, mergeFn)`: 不存在就放 v, 存在就 `mergeFn.apply(oldV, v)`

**Drill**: 实现 `Lesson04`:
- `ConcurrentHashMap<String, AtomicLong> counts`
- `record(String key)`: 用 `computeIfAbsent` 拿到 (或新建) `AtomicLong`, 然后 `.incrementAndGet()`. (或者: 用 `merge`, 把 Long 当 value)
- `count(String key)`: 返回当前计数 (key 不存在返回 0)
- `getOrCreate(String key)`: 用 `computeIfAbsent` 返回一个 per-key 的 `Object`, **同一 key 必须返回同一个实例**
- 测试: 32 线程并发 record 同一 key, 总数必须精确

**观察**: 如果你用 `if (!containsKey) put`, 在 `getOrCreate` 的测试里**会**看到两次不同的实例。

---

## Lesson 05 — `ReentrantLock` + `Condition`

**问题**: `synchronized` 不能 tryLock (要么拿到要么死等), 不能中断, 不能选公平性。

**解决**: `ReentrantLock` 是 `synchronized` 的可控版本。配合 `Condition` 实现 "等某个条件" 的等待/唤醒。

**术语**:
- explicit lock (显式锁) vs intrinsic lock (内置锁)
- reentrant: 同一线程可以重复 `lock()`, 配对的 `unlock()` 次数要相等
- fairness: 构造 `new ReentrantLock(true)` 是公平锁, FIFO, 但慢一点
- `tryLock()` / `tryLock(timeout)`: 拿不到就放弃 / 等一段时间放弃
- **must use try-finally**: `lock.lock(); try { ... } finally { lock.unlock(); }` —— 这是面试必背 idiom
- `Condition.await() / signal() / signalAll()`: 对应 `Object.wait() / notify() / notifyAll()`, 但可以一个锁挂多个 Condition

**Drill**: 实现 `Lesson05`:
- `ReentrantLock lock`
- `long count`
- `increment()`: 用 try-finally idiom 包住 count++
- `get()`: 同上
- `boolean tryIncrement(long timeoutMillis)`: 用 `lock.tryLock(timeoutMillis, MILLISECONDS)`, 拿不到返回 false, 拿到就 count++ 并返回 true
- 测试: 16 线程并发 increment + 一个线程一直占着锁, tryIncrement(10ms) 应该绝大多数 false

---

## Lesson 06 — `BlockingQueue` (producer-consumer)

**问题**: 生产者快, 消费者慢, 需要一个**有上限**的中间缓冲, 且生产者满了要**自动减速** (不能 OOM)。

**解决**: `ArrayBlockingQueue<T>(capacity)`. `put(t)` 满了 block; `take()` 空了 block。

**术语**:
- producer-consumer pattern
- bounded queue (有界队列) vs unbounded (`LinkedBlockingQueue` 默认 Integer.MAX_VALUE — 危险)
- back-pressure: 满队列 block 上游, 上游就慢下来 → 系统自适应
- 三组接口语义对照:
  - **throw**: `add` / `remove` / `element` — 满/空时抛异常
  - **return special value**: `offer` / `poll` / `peek` — 满/空时返回 false / null
  - **block**: `put` / `take` — 满/空时**阻塞**
  - **timed**: `offer(t, time, unit)` / `poll(time, unit)` — 阻塞但有超时
- poison pill: 给消费者发一个特殊值表示"该退出了"

**Drill**: 实现 `Lesson06`:
- `BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10)`
- `void produce(int item)`: `queue.put(item)`
- `int consume()`: 返回 `queue.take()`
- 测试: 1 producer 推 100 个数字, 1 consumer 拿 100 个, 求和验证 (4950)

**观察**: 选 capacity = 10 而生产 100, 消费者要是不跑生产者就 block 在第 11 个。

---

## Lesson 07 — `CountDownLatch` (一次性等待)

**问题**: 主线程要等 N 个 worker 都跑完才能继续。

**解决**: `CountDownLatch(n)`, worker 跑完 `countDown()`, 主线程 `await()`。计到 0 就放行, **不能重用**。

**术语**:
- one-shot synchronizer (一次性同步器)
- start gate (起跑枪) — `new CountDownLatch(1)`, 所有 worker 先 `await()`, 主线程 `countDown()` → 大家同时冲, 用来制造 race window
- finish line (终点线) — `new CountDownLatch(N)`, worker 各自 `countDown()`, 主线程 `await()` 等全部完成
- 如果要可重用, 用 `CyclicBarrier` (循环屏障)
- 如果要可加可减, 用 `Phaser`

**Drill**: 实现 `Lesson07`:
- `void runAll(int n, Runnable task)`: 启动 n 个线程各跑一次 `task`, 方法**只在所有 n 个都完成之后**才返回
- 测试: task 是给 AtomicInteger ++, 跑 n = 50, 方法返回后 AtomicInteger 必须正好 50

---

## Lesson 08 — `Semaphore` (限并发数)

**问题**: 第三方 API 限 5 并发, 我们要保证同时最多 5 个线程在调它。

**解决**: `Semaphore(5)`. 调之前 `acquire()`, 调完 `release()`。多余的线程在 acquire 处 block。

**术语**:
- permit (许可证): semaphore 里的票
- counting semaphore (计数信号量) vs binary semaphore (信号量为 1, 等价于 mutex)
- 公平 / 非公平: `new Semaphore(n, true)` 是公平
- **常见坑**: `release()` 必须放 finally, 否则异常路径泄漏 permit, 越漏越少, 最后大家都 acquire 不到
- `tryAcquire()`, `tryAcquire(timeout)`

**Drill**: 实现 `Lesson08`:
- 构造 `Lesson08(int permits)`, 内部 `Semaphore`
- `void access()`: acquire → sleep 30ms (模拟工作) → release, 用 try-finally
- 同时维护一个 `AtomicInteger inFlight` 和 `AtomicInteger peakInFlight`, 记录任何时刻"正在 access 里的线程数"的峰值
- 测试: 创建 permits=3, 启动 12 个线程并发 access, 全部跑完后 peakInFlight 必须 ≤ 3

---

## Lesson 09 — `ExecutorService` + `Future` (线程池 + 异步结果)

**问题**: 不想自己 `new Thread()`, 也不想自己 join 收结果。

**解决**: 把 task 扔给 `ExecutorService`, 它返回 `Future<T>`, `future.get()` 阻塞直到结果出来 (或抛 `ExecutionException`)。

**术语**:
- thread pool, work queue, core / max pool size
- `Runnable` (无返回) vs `Callable<T>` (有返回, 可以抛 checked exception)
- `submit(Callable)` 返回 `Future`; `execute(Runnable)` 不返回任何东西
- `future.get()` 阻塞; `future.get(timeout)` 超时; `future.isDone()` 非阻塞探测
- `invokeAll(tasks)`: 提交一批, **等全部完成**, 返回 `List<Future>`
- `shutdown()` (软停, 等已提交的跑完) vs `shutdownNow()` (硬停, 中断在跑的)
- `ExecutionException`: 任务抛了异常, 异常被包在这里

**Drill**: 实现 `Lesson09`:
- `long sumInParallel(List<Long> nums, int workers)`:
  - 把 nums 切成 workers 份
  - 每份用 `Callable<Long>` 求 partial sum
  - 用 `ExecutorService.invokeAll` 提交, 然后 `future.get()` 收回每份 partial sum
  - 返回总和; 方法返回前必须 shutdown 自己创建的 executor
- 测试: 已知 list, 已知 sum, 比对

---

## 做完之后

回头看 [01-Event-Counter](../01-Event-Counter/) → [02-URL-Hit-Counter](../02-URL-Hit-Counter/) → … 你会发现每道题就是把 1-2 个 lesson 里的 primitive 用到一个真问题上。**先把 9 个 lesson 都过一遍, 这一套真题就稳了**。
