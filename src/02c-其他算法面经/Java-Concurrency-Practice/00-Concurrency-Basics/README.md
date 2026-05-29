# 🐣 00 — Concurrency Basics (基础篇：手撕并发原语)

> **这不是冷冰冰的考试题，这是一场带你亲手把 JUC (java.util.concurrent) 核心 API 敲进肌肉记忆的 Crash Course！**
> 每个 Lesson 都极其短小精悍 (约 15-30 行代码)，只聚焦**一个**并发原语。完成它们，你将轻松达成三个成就：① 记住核心 API 名称；② 掌握面试常考英文术语；③ 深刻体会“这玩意儿到底解决了什么痛点”。
> **磨刀不误砍柴工，通关这 9 个小节后再去做 01-06 的真题，你会感觉无比丝滑。**

---

## 🏃 怎么运行？

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

## 🛠️ Lesson 01 — `volatile` (打破线程间的“信息壁垒”)

**❓ 现实痛点**：想象两个人在不同房间办公。线程 A 在疯狂死循环检查 `running` 变量是不是为 `false` 以便下班，而线程 B 其实早已把 `running` 改成了 `false`。结果呢？线程 A 根本看不到！因为它一直在读自己 CPU 缓存里的“旧账本”，导致程序永远无法停止。这就是典型的**内存可见性 (Visibility)** 故障。

**💡 武器库**：给 `running` 加上 `volatile` 关键字。

**🎯 它的作用**：它像是一个大喇叭，强制要求所有线程每次读这个变量时，都必须去公共主存看最新值；每次改写时，必须立刻广播回主存。
> ⚠️ **新手避坑**：`volatile` **绝对不能**保证操作的原子性！如果你写 `volatile int x; x++;` 在多线程下依然会发生惨烈的数据覆盖。

**你的任务**：实现一个可控制启停的 Worker。写对之后，**请试着把 `volatile` 删掉再跑一次测试**，亲眼看看你的程序是如何因为读到“脏数据”而彻底死锁卡住的。

---

## 🛠️ Lesson 02 — `synchronized` (最经典的独木桥)

**❓ 现实痛点**：`count++` 看起来简单，但在 CPU 眼里是三步（把数字拿出来 -> 加 1 -> 写回去）。如果两个线程同时拿到了 `100`，分别加一后写回去，结果变成了 `101`，我们凭空丢失了一次增加。这就是大名鼎鼎的**丢失更新 (Lost Update)** 漏洞。

**💡 武器库**：在方法前加上 `synchronized` 关键字。

**🎯 它的作用**：把包裹的代码变成一座“独木桥”。同一个时刻，绝对只允许一个线程进去执行操作，其他线程必须在外面乖乖排队（阻塞）。这是 Java 的内置锁，简单粗暴。
> ⚠️ **新手避坑**：绝对不要用 `String` 常量或者 `Integer` 装箱对象来作为锁对象，它们在 JVM 里是共享的，会导致本不想干的线程互相堵死。

**你的任务**：实现一个纯靠 `synchronized` 保护的计数器。观察如果不加锁，最后累加的数字为什么总是达不到期望值。

---

## 🛠️ Lesson 03 — `AtomicLong` / CAS (无锁的高级魔法)

**❓ 现实痛点**：`synchronized` 虽然安全，但在极高并发下，几百个线程互相争抢、阻塞、唤醒，**上下文切换的开销**大得离谱，系统会被拖得很慢。

**💡 武器库**：用 JUC 提供的 `AtomicLong` 类，以及底层的 **CAS (Compare-And-Swap)** 技术。

**🎯 它的作用**：无锁 (Lock-free) 编程的核心。它的思想是：“我不上锁了。我改数据的时候，先看一眼内存里的值是不是我刚才看到的旧值，如果是，我就改；如果被人捷足先登了，我不气馁，在死循环里重试 (Retry Loop)”。

**你的任务**：不用任何锁，仅使用 `compareAndSet` 手写一个 CAS 重试循环，来维护全局的最大值。这是并发面试里极高频的一道手撕题，务必把这个 Pattern 刻进骨子里。

---

## 🛠️ Lesson 04 — `ConcurrentHashMap` (绝不止于线程安全)

**❓ 现实痛点**：你用了一个线程安全的 Map，并且写下这样的代码：`if (!map.containsKey(k)) { map.put(k, newObject); }`。**大错特错！** 两个线程可能同时判断 `k` 不存在，然后同时创建了 `newObject` 并写入，你的对象被覆盖了。这是初级程序员最爱踩的 **Check-then-act** 竞态陷阱。

**💡 武器库**：`ConcurrentHashMap` 提供的“复合原子操作” API，比如 `computeIfAbsent`。

**🎯 它的作用**：它能在底层精准地给这个 Key 所在的桶加锁，保证“如果不存在就创建”这两个动作一气呵成，`newObject` 绝对只会被初始化一次。

**你的任务**：实现一个严谨的对象工厂。如果你不信邪，依然用了 `if (!containsKey)` 来写，测试用例会立刻用红色的报错无情地拆穿你。

---

## 🛠️ Lesson 05 — `ReentrantLock` (高级锁玩家的标配)

**❓ 现实痛点**：`synchronized` 太死板了。如果拿不到锁，线程就只能死等下去；你没法设置“等 5 秒钟拿不到我就放弃”，也没法控制让排队最久的线程先拿到锁。

**💡 武器库**：JUC 的 `ReentrantLock`（可重入显式锁）。

**🎯 它的作用**：它是 `synchronized` 的威力加强版。它支持 `tryLock()` 超时放弃，支持公平锁，还能配合 `Condition` 实现更精准的线程唤醒。
> ⚠️ **面试必背规矩**：显式锁必须手动释放，所以你**永远、永远**要把 `lock.unlock()` 写在 `finally` 块里！

**你的任务**：实现带“超时放弃”功能的 `tryIncrement`，体验一下并发抢锁抢不到时，优雅退出的快感。

---

## 🛠️ Lesson 06 — `BlockingQueue` (生产者与消费者的完美桥梁)

**❓ 现实痛点**：上游系统生产数据的速度极快，下游消费得慢。如果不加节制，内存很快就会被撑爆 (OOM)。我们需要一个机制，当堆积太多时，能强迫上游“慢下来”。

**💡 武器库**：`ArrayBlockingQueue` (有界阻塞队列)。

**🎯 它的作用**：这是一个自带锁和通知机制的容器。当队列满了，调用 `put()` 的生产者会被强制挂起睡觉；当队列空了，调用 `take()` 的消费者会挂起等待。这就是高可用架构中经常提的**“背压 (Back-pressure)”**机制。

**你的任务**：实现一个容量只有 10 的缓冲池。观察当你强行塞入 100 个元素时，如果消费者不跑，生产者是如何被安全挂在第 11 个元素上的。

---

## 🛠️ Lesson 07 — `CountDownLatch` (一次性发令枪与终点线)

**❓ 现实痛点**：主线程把一份巨大的工作拆给了 50 个子线程并行去做，主线程必须等到这 50 个小弟**全部干完活**，才能继续往下进行最终的汇总。

**💡 武器库**：`CountDownLatch` (倒数计时器)。

**🎯 它的作用**：初始化设为 50，主线程调用 `await()` 苦等；子线程干完活就调用 `countDown()` 减 1。减到 0 时，主线程立刻被放行。注意，它是一次性的，用完就废了。

**你的任务**：写一个通用的批量执行方法，确保这个方法返回时，所有的并发任务必须结结实实地全部跑完。

---

## 🛠️ Lesson 08 — `Semaphore` (系统的限流保护神)

**❓ 现实痛点**：你要调用一个极其脆弱的第三方支付 API，对方明确要求：同一时刻最多只能承受 5 个并发请求。而你的系统有几十个工作线程，怎么控制这股洪流不把对方冲垮？

**💡 武器库**：`Semaphore` (信号量)。

**🎯 它的作用**：它就像是发放“许可证”的门卫。初始化 5 张许可证，进门前必须 `acquire()` 拿票，出门时必须 `release()` 还票。拿不到票的线程只能在门外排队。
> ⚠️ **致命天坑**：万一工作代码抛出异常，线程死掉了，票就永远还不回来了。所以 `release()` **也必须、必须**写在 `finally` 块里！

**你的任务**：实现一个严格限并发的保护壳，并顺便记录下系统在并发巅峰期，到底有多少个线程在同时运行。

---

## 🛠️ Lesson 09 — `ExecutorService` + `Future` (外包团队与取件凭证)

**❓ 现实痛点**：手动去 `new Thread()` 实在太低效了，而且最头疼的是，普通线程跑完就结束了，你根本没法直接让它给你返回计算结果。

**💡 武器库**：大名鼎鼎的 `ExecutorService` (线程池) 和 `Future`。

**🎯 它的作用**：线程池就像一个长期雇佣的外包团队。你把计算任务（`Callable`）扔给它，它马上丢给你一张“取件凭证”（`Future`）。你去忙别的事，等你需要结果了，就拿着凭证调 `future.get()` 去取。没算完？那你就稍等一会儿。

**你的任务**：将一个巨长无比的数组切分，交给线程池里的多个工人去并行求和，最后再通过 Future 把结果收回来合并。这是典型的大数据分治思想。

---

## 🎉 结语：接下来去哪？

如果你一行行手敲打通了这 9 个 Drill，那么恭喜你，JUC 对你不再是未知的黑盒。

现在，请带着你的新武器回到上一层目录，挑战实战题： 
01-Event-Counter → 02-URL-Hit-Counter ...
你会发现，所谓大厂的高频并发题，本质上不过就是把你刚刚学过的这些原语，套上了一个业务的外壳而已。加油！
