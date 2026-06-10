import java.util.*;

/**
 * Coinbase interview practice — Event Store (single problem).
 *
 * 事件流写入 + 按 userId & 时间范围查询。
 * 事件不保证按时间顺序到达（mostly in order，但不保证）。
 * 高并发写入 + 查询。
 *
 * API:
 *   record(event)                             — 写入一条事件
 *   query(userId, start, end) → List<Event>   — [start, end] 内该 user 的所有事件
 *   count(userId, start, end) → int           — [start, end] 内该 user 的事件总数
 */


# Core Assumptions and Requirements:
* Events are mostly time ordered. 
    * Note this indicating an ArrayList rather than TreeMap. 
* Each timestamp could have more than one events
    * No need to sort further
* 

# Other:
* Concurrency
    * What to discuss about the concurrency 
    * 


Data Structure:
* Partition Data by UserID. Use a HashMap, key is UserId, value is Per User level EventStore
* EventStore

public class EventStore {

    static record Event(String userId, long timestamp, String payload) {}
    static class UserEvents {

    }


    // ====================================================================
    // TODO: 选择 per-user 数据结构并实现以下方法
    // ====================================================================

    
    public EventStore() {
        throw new UnsupportedOperationException("TODO: constructor");
    }

    public void record(Event event) {
        throw new UnsupportedOperationException("TODO: record");
    }

    public List<Event> query(String userId, long start, long end) {
        throw new UnsupportedOperationException("TODO: query");
    }

    public int count(String userId, long start, long end) {
        throw new UnsupportedOperationException("TODO: count");
    }
}
