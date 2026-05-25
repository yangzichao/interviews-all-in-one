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
}
