import java.util.*;
import java.util.function.IntPredicate;

/**
 * 4-part Coinbase interview practice — Iterator / Interleaving Iterator.
 *
 * 每个 Part 是独立的 class,后缀 PartN。先无脑独立写,做完再讨论抽公共逻辑。
 *
 * 这不是产品代码,是练习代码 —— 让你能专注当前 Part 而不破坏已完成的部分。
 */
public class Iterators {

    // ====================================================================
    // PART 1  —  RangeIterator(start, end, step)                    [⚠ TODO]
    // ====================================================================
    // 半开区间 [start, end), step >= 1.
    //
    //   new RangeIteratorPart1(0, 5, 1)   →  0,1,2,3,4
    //   new RangeIteratorPart1(0, 10, 3)  →  0,3,6,9
    //   new RangeIteratorPart1(5, 5, 1)   →  (空)
    //
    // next() 在 hasNext()==false 时 → 抛 NoSuchElementException.

    public static class RangeIteratorPart1 implements Iterator<Integer> {
        private int lo;
        private int hi;
        private int step;
        public RangeIteratorPart1(int start, int end, int step) {
            lo = start;
            hi = end;
            this.step = step;
        }

        @Override
        public boolean hasNext() {
            return lo < hi;
        }

        @Override
        public Integer next() {
            if (!hasNext()) throw new NoSuchElementException();
            lo += step;
            return lo - step;
        }
    }

    // ====================================================================
    // PART 2  —  InterleavingIterator                               [⚠ TODO]
    // ====================================================================
    // 输入 k 个 Iterator<Integer>, 公平 round-robin 输出, 跳过已耗尽的.
    //
    //   [[1,2,3],[4,5],[6],[],[7,8,9]] → 1,4,6,7,2,5,8,3,9
    //
    // 注意:
    //   - 输入是 Iterator<Integer> 不是 List<Integer> (不能预知长度)
    //   - 输入 list 的 iterator 顺序就是 round-robin 顺序

    public static class InterleavingIteratorPart2 implements Iterator<Integer> {
        // 用 ArrayDeque 当 round-robin 队列:
        //   - 只放 "还有值的 iterator"
        //   - next() 从头拿一个 iter, 取一个值, 若 iter 还有 → 推回尾部
        //   - 耗尽的 iter 自然脱队, 不需要单独 remove / index 维护

        private final Deque<Iterator<Integer>> queue = new ArrayDeque<>();

        public InterleavingIteratorPart2(List<Iterator<Integer>> iters) {
            for (Iterator<Integer> it : iters) {
                if (it.hasNext()) queue.addLast(it);
            }
        }

        @Override
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        @Override
        public Integer next() {
            if (!hasNext()) throw new NoSuchElementException();
            Iterator<Integer> it = queue.pollFirst();
            int value = it.next();
            if (it.hasNext()) queue.addLast(it);
            return value;
        }
    }

    // ====================================================================
    // PART 3  —  FilterIterator                                     [⚠ TODO]
    // ====================================================================
    // wrap 已有的 Iterator<Integer>, 只暴露满足 predicate 的元素.
    //
    //   src=[1,-2,3,-4,5,6], keep=(x>0) → 1,3,5,6
    //
    // 用到的 API:
    //   - Iterator<Integer>: it.hasNext(), it.next()
    //   - IntPredicate keep: keep.test(int) -> boolean
    //     (函数式接口, 调用方传 lambda 例如 x -> x > 0)
    //
    // 难点:
    //   - hasNext() 必须真的还有 (不能因为底层非空就乐观返 true)
    //   - hasNext() 可以被多次调用, 不能跳元素、不能重复消费

    public static class FilterIteratorPart3 implements Iterator<Integer> {
        private final Iterator<Integer> it;
        private final IntPredicate keep;
        private Integer cur;
        private boolean hasNext;
        public FilterIteratorPart3(Iterator<Integer> it, IntPredicate keep) {
            this.it = it;
            this.keep = keep;
            this.hasNext = false;
            findNext();
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public Integer next() {
            if (!hasNext()) throw new NoSuchElementException();
            int val = cur;
            findNext();
            return val;
        }

        private void findNext() {
            hasNext = false;
            cur = null;
            while (it.hasNext()) {
                Integer candidate = it.next();
                if (candidate != null && keep.test(candidate)) {
                    cur = candidate;
                    hasNext = true;
                    break;
                }
            }
        }
    }

    // ====================================================================
    // PART 4  —  CycleIterator                                      [⚠ TODO]
    // ====================================================================
    // 走到 source 末尾后从头循环.
    //
    //   new CycleIteratorPart4([1,2,3]) → 1,2,3,1,2,3,1,2,3,...
    //   new CycleIteratorPart4([])      → (永远 hasNext()==false)
    //
    // source 是 List<Integer> 不是 Iterator (要能从头重读).
    //
    // 用到的 API:
    //   - source.isEmpty() -> boolean
    //   - source.size()    -> int
    //   - source.get(i)    -> Integer  (i 必须在 [0, size) 内, 否则越界)
    //   提示: % size 让 index 永远落回合法范围.

    public static class CycleIteratorPart4 implements Iterator<Integer> {
        private final List<Integer> list;
        private int index;
        public CycleIteratorPart4(List<Integer> source) {
            list = new ArrayList<>();
            for (Integer element : source) {
                if (element != null) {
                    list.add(element);
                }
            }
            this.index = 0;
        }

        @Override
        public boolean hasNext() {
            return !list.isEmpty();
        }

        @Override
        public Integer next() {
            if (!hasNext()) {
               throw new NoSuchElementException();
            }
            Integer candidate = list.get(index);
            index = (index + 1) % list.size();
            return candidate;
        }
    }

    // ====================================================================
    // PART 5  —  ConcurrentInterleavingIterator (thread-safe)       [⚠ TODO]
    // ====================================================================
    // 与 Part 2 比:
    //   同: 接口形状 hasNext()/next() 不变, 内部仍然是 round-robin 公平合并
    //   变: 多个线程会同时调用 hasNext()/next() —— 必须线程安全
    //   新: 没有新 API —— 但要在内部加锁 / 用 concurrent 集合
    //
    // 问题陈述:
    //   假设 N 个消费者线程并发从同一个 InterleavingIterator 取数, 每个线程
    //   都跑 `while (it.hasNext()) consume(it.next());`. 要求:
    //     (a) 不出现 NoSuchElementException —— 即不会出现两个线程都通过了
    //         hasNext()==true 检查, 但第二个 next() 时 underlying iter 已耗尽
    //     (b) 同一个 underlying iter 的 v_i 不会被多个线程同时拿走 (即不会
    //         有人拿了一半的 batch 别人又 poll 出来再取一次)
    //     (c) 不允许同一个元素被 emit 两次 —— 全集合 emit 序列是输入元素的
    //         一个 permutation 子集 (顺序不再严格 round-robin, 因为线程调度
    //         本身就是非确定的; 这点要主动跟面试官 call out)
    //     (d) 高频 next() 不应该被 hasNext() 严重阻塞
    //
    // 面试要讨论的取舍 (这是 Coinbase 团队明确说重点考的):
    //   1. 整个对象 synchronized —— 简单, 但 hasNext() 也要等 next() 释放锁
    //   2. hasNext+next 合并为一个原子方法 tryNext() (返回 Optional) —— 更
    //      Java-style, 避开 TOCTOU; 但破坏了 Iterator<E> 接口
    //   3. 仅 next() 内部加锁, hasNext() 当作 hint (允许 false-positive)
    //      —— 取错了再 throw 给调用方处理? 违反 (a)
    //   4. ConcurrentLinkedDeque 做 queue + CAS pop/push —— lock-free 但
    //      复合操作 (poll → next → push back) 需要细心设计
    //   5. 用 BlockingQueue 当 buffer, 单独 producer 线程 drain 上游 ——
    //      解耦消费/生产, 但成了 push-pull 混合体, 已经不是单纯 iterator 了
    //   面试官最常追问: "如果其中一个 underlying iter 自己也是 stateful 且
    //   非线程安全的怎么办?" —— 因为我们已经 serialize 了对它的访问, 安全.
    //
    // 你要写的: 把 Part 2 的实现包一层并发安全; 选定一种策略并能说出取舍.

    public static class ConcurrentInterleavingIteratorPart5 implements Iterator<Integer> {
        public ConcurrentInterleavingIteratorPart5(List<Iterator<Integer>> iters) {
            throw new UnsupportedOperationException("TODO: Part 5 — 选一种并发策略");
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException("TODO: Part 5 — thread-safe hasNext");
        }

        @Override
        public Integer next() {
            throw new UnsupportedOperationException("TODO: Part 5 — thread-safe next");
        }
    }

    // ====================================================================
    // PART 6  —  BatchedIterator + 观测性 (batch + metrics)          [⚠ TODO]
    // ====================================================================
    // 与 Part 5 比:
    //   同: 底层还是 round-robin interleaving, 元素总集合 / 顺序约束不变
    //   变: 暴露批量 API nextBatch(n), 减少 per-element 调用开销; 同时维护
    //       观测计数器 (emittedCount, exhaustedSourceCount)
    //   新: nextBatch(int n) -> List<Integer>; stats() -> Stats record
    //
    // 问题陈述:
    //   单元素 next() 调用在高 QPS 时, 锁竞争 + 装箱开销很重. 业务侧通常
    //   一次拉 32 / 128 个. 设计一个 batch 版本:
    //     (a) nextBatch(n) 返回 <= n 个元素, 不足就返回剩下的全部 (耗尽时空 list)
    //     (b) 批内仍然遵循 round-robin 顺序 (跟 Part 2 一致)
    //     (c) 同时提供 stats(): 已 emit 的元素总数, 已耗尽的子 iterator 数,
    //         自创建以来的调用次数. 这些在面试时是 "出了问题怎么排查" 的入口.
    //
    // 面试要讨论的取舍:
    //   1. nextBatch 的并发: 整个 batch 在一把锁内取完 (一致快照) vs 每个
    //      元素独立拿锁 (吞吐高但延迟抖)
    //   2. 返回 ArrayList 还是 int[]?
    //      - List<Integer>: 跟 Iterator<Integer> 接口对齐, 但装箱代价高
    //      - int[]: 零装箱, 但要单独处理 "不足 n 个" 的截断 (Arrays.copyOf)
    //   3. stats 的更新: 加锁的整数计数 vs LongAdder vs AtomicLong
    //      - 高竞争下 LongAdder >> AtomicLong; 但读 stats 不那么频繁
    //   4. 暴露 stats 的口径: 实时快照 vs cumulative since-creation
    //      —— 跟 Prometheus 习惯哪个对齐?
    //   面试官最常追问: "n 设多大? 怎么动态调整?" → 引出自适应批 size 讨论
    //   (类似 Linux NAPI, RxJava request(n) backpressure).
    //
    // 你要写的: nextBatch + stats; 在 Part 5 的并发模型基础上加.

    public static final class StatsPart6 {
        public final long emittedCount;
        public final int exhaustedSourceCount;
        public final long callCount;  // hasNext + next + nextBatch 总调用次数
        public StatsPart6(long emittedCount, int exhaustedSourceCount, long callCount) {
            this.emittedCount = emittedCount;
            this.exhaustedSourceCount = exhaustedSourceCount;
            this.callCount = callCount;
        }
        @Override
        public String toString() {
            return "Stats(emitted=" + emittedCount + ", exhausted=" + exhaustedSourceCount
                    + ", calls=" + callCount + ")";
        }
    }

    public static class BatchedInterleavingIteratorPart6 implements Iterator<Integer> {
        public BatchedInterleavingIteratorPart6(List<Iterator<Integer>> iters) {
            throw new UnsupportedOperationException("TODO: Part 6 — 选并发模型并初始化 stats");
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException("TODO: Part 6");
        }

        @Override
        public Integer next() {
            throw new UnsupportedOperationException("TODO: Part 6");
        }

        public List<Integer> nextBatch(int n) {
            throw new UnsupportedOperationException("TODO: Part 6 — 批量, 不足就少返, 耗尽就空 list");
        }

        public StatsPart6 stats() {
            throw new UnsupportedOperationException("TODO: Part 6 — 返回当前观测计数快照");
        }
    }

    // ====================================================================
    // PART 7  —  SnapshotIterator over a mutating Collection         [⚠ TODO]
    // ====================================================================
    // 与 Part 6 比:
    //   同: 仍然是 Iterator<Integer>, 接口一致
    //   变: 输入不再是 "已经构造好的 Iterator 列表", 而是一个 *活的*
    //       Collection<Integer> —— 在迭代过程中可能被别的线程 add/remove.
    //   新: 构造函数: SnapshotIteratorPart7(Collection<Integer> source).
    //       两种语义模式 (二选一, 写哪个都要说清楚):
    //         (i)  fail-fast: 检测到 source 被改, 下一次 hasNext/next 抛
    //              ConcurrentModificationException
    //         (ii) weakly-consistent / snapshot: 构造时拍一份快照, 之后
    //              source 的变更对当前 iterator 不可见
    //
    // 问题陈述:
    //   `for (Integer x : list) { ... list.remove(...); }` 这种代码在
    //   Java 标准 ArrayList 上是 fail-fast 的; 在 ConcurrentHashMap 上是
    //   weakly-consistent 的. 实现一个明确选定一种语义的迭代器, 并跟
    //   面试官讲清楚为什么选这种.
    //
    // 面试要讨论的取舍:
    //   1. fail-fast:
    //      - 实现: 记录 source 的 modCount (或对外暴露 size + 哈希)
    //      - 优点: 调用方早暴露 bug
    //      - 缺点: 多线程下"看似稳定也可能因 GC / rehash 假阳性"; 不能跨进程
    //   2. weakly-consistent (snapshot copy):
    //      - 实现: 构造时 new ArrayList<>(source)
    //      - 优点: 调用方不会被打断
    //      - 缺点: O(n) 内存 + 看不到新写入; source 很大时构造慢
    //   3. lazy snapshot / COW:
    //      - 不立刻 copy, 但 source 第一次被改时才 copy-on-write
    //      - 优点: 大多数情况下零开销
    //      - 缺点: 需要 source 配合 (得能 hook 它的 mutate 方法)
    //   4. 迭代过程中 source 被 *并发* 修改时的可见性 (happens-before):
    //      - synchronized / volatile / Lock —— 选哪种?
    //   面试官最常追问: "如果 source 不是你设计的, 是第三方传入的 List,
    //   你能保证 fail-fast 吗?" —— 答: 不能完全, 只能记录 size 做 best-effort
    //   ConcurrentModificationException 不在 Java spec 强保证范围内.
    //
    // 你要写的: 选一种语义, 实现并在 README 里说明.

    public static class SnapshotIteratorPart7 implements Iterator<Integer> {
        public SnapshotIteratorPart7(Collection<Integer> source) {
            throw new UnsupportedOperationException("TODO: Part 7 — 选 fail-fast 还是 weakly-consistent");
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException("TODO: Part 7");
        }

        @Override
        public Integer next() {
            throw new UnsupportedOperationException("TODO: Part 7");
        }
    }

    // ====================================================================
    // PART 8  —  ResumableIterator (可序列化游标 / cursor 模式)       [⚠ TODO]
    // ====================================================================
    // 与 Part 7 比:
    //   同: 仍然走 round-robin interleaving 逻辑
    //   变: 迭代状态要可序列化, 进程崩了换台机器从断点继续
    //   新: saveCursor() -> String;  static restore(String cursor, ...).
    //
    // 问题陈述:
    //   Coinbase 后台跑一个大 batch job, 遍历 N 路 source 拼到下游. 单次
    //   job 跑 1 小时, 中间机器重启了, 不想从头来. 设计一个 iterator,
    //   能把 "已经消费到哪儿" 序列化成一个 cursor 字符串, 重启后用 cursor
    //   构造同样状态的 iterator, 继续 emit 剩下的元素.
    //
    // 面试要讨论的取舍:
    //   1. 上游 source 类型限制:
    //      - 如果是任意 Iterator<Integer>: 没法序列化 (Iterator 本身没 cursor)
    //      - 必须假定每路 source 是 *可重建* 的 —— 比如 List<Integer>,
    //        或者 (file path + offset), 或者 (DB query + last_id).
    //      → 这道题本质是让候选人意识到: "iterator 是状态, 状态要能 dump,
    //        就必须知道怎么把 dump 回放".
    //   2. cursor 编码: JSON / Protobuf / 自定义紧凑格式
    //      - 可读性 vs 大小 vs 演化兼容性
    //   3. exactly-once vs at-least-once:
    //      - 如果 saveCursor() 之后还 emit 了几个再崩, 重启会重复? 还是丢?
    //      - 取决于 cursor 是 emit 前保存还是 emit 后保存 (off-by-one)
    //   4. 跟 Part 5 并发结合:
    //      - 多线程消费时, "我已经发出去的 cursor" 不能往回退. 怎么并发安全
    //        地推进 cursor? 类似 Kafka consumer offset commit.
    //   面试官最常追问: "如果上游也是个 ResumableIterator, 怎么级联?"
    //   → 引出复合 cursor (per-source position + global pointer) 的设计.
    //
    // 这道题答完了基本就是设计简化版 Kafka consumer.
    // 不强求完整可运行 —— 给出接口 + 关键数据结构 + 讨论清楚就够.
    //
    // 建议入口 (写不写都可以):

    public static class ResumableInterleavingIteratorPart8 implements Iterator<Integer> {
        public ResumableInterleavingIteratorPart8(List<List<Integer>> sources) {
            throw new UnsupportedOperationException("TODO: Part 8 — 初始化游标");
        }

        public static ResumableInterleavingIteratorPart8 restore(String cursor, List<List<Integer>> sources) {
            throw new UnsupportedOperationException("TODO: Part 8 — 从 cursor 字符串重建状态");
        }

        public String saveCursor() {
            throw new UnsupportedOperationException("TODO: Part 8 — 把当前 (per-source-index, rr-queue) 状态编码");
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException("TODO: Part 8");
        }

        @Override
        public Integer next() {
            throw new UnsupportedOperationException("TODO: Part 8");
        }
    }
}
