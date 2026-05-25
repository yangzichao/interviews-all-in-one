import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Lesson 06 — BlockingQueue (producer-consumer)
 *
 * 看 ../README.md 里 Lesson 06 一节. 关键词: bounded queue, back-pressure, put/take vs offer/poll vs add/remove.
 *
 * 实现要点:
 *   - BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);
 *   - produce(item): queue.put(item);     // 满了就 block, 不会扔
 *   - consume():     return queue.take(); // 空了就 block, 不会返回 null
 *
 *   ⚠️ 不要用 queue.add() / queue.remove(), 那俩满/空会抛异常.
 *   ⚠️ 不要用 queue.offer() / queue.poll(), 那俩满/空会返回 false/null, 不 block.
 */
public class Lesson06 {

    // TODO: BlockingQueue<Integer> queue;

    public Lesson06() {
        throw new UnsupportedOperationException("TODO: 用 ArrayBlockingQueue, capacity 10");
    }

    public void produce(int item) throws InterruptedException {
        throw new UnsupportedOperationException("TODO: queue.put(item)");
    }

    public int consume() throws InterruptedException {
        throw new UnsupportedOperationException("TODO: return queue.take()");
    }
}
