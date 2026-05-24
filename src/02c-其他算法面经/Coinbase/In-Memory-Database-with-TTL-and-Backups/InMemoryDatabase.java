import java.util.*;

/**
 * 4-part Coinbase interview practice.
 *
 * 方法用 putPartN/getPartN/... 后缀区分。每个 Part 的 header 列了与前一 Part
 * 比较的 [同 / 变 / 新]，schema 真的变了才引入新的 EntryPartN / mapPartN, 否则复用。
 *
 * 这不是产品代码，是练习代码 —— 让你能专注当前 Part 而不破坏已完成的部分。
 */
public class InMemoryDatabase {

    // ====================================================================
    // PART 1  —  基础 put / get                                    [✓ DONE]
    // ====================================================================
    // 起点 —— EntryPart1 只存 (value, timestamp), HashMap 装着.

    private record EntryPart1(String value, int timestamp) {}
    private final Map<String, EntryPart1> mapPart1 = new HashMap<>();

    public void putPart1(String key, String value, int timestamp) {
        mapPart1.put(key, new EntryPart1(value, timestamp));
    }

    public String getPart1(String key, int timestamp) {
        EntryPart1 e = mapPart1.get(key);
        return e == null ? null : e.value();
    }

    // ====================================================================
    // PART 2  —  加 scan (prefix, 按字典序, 字符串格式)              [✓ DONE]
    // ====================================================================
    // 与 Part 1 比:
    //   同: EntryPart1 / mapPart1  (schema 没变, 直接复用)
    //   变: 无
    //   新: scanPart2(prefix, timestamp) -> String
    //
    // putPart2 / getPart2 是套壳到 Part 1, 没有逻辑改变.

    public void putPart2(String key, String value, int timestamp) {
        putPart1(key, value, timestamp);
    }

    public String getPart2(String key, int timestamp) {
        return getPart1(key, timestamp);
    }

    public String scanPart2(String prefix, int timestamp) {
        List<String> keyList = new ArrayList<>();
        for (String key : mapPart1.keySet()) {
            if (key.startsWith(prefix)) {
                keyList.add(key);
            }
        }
        Collections.sort(keyList);
        String res = "";
        for (String key : keyList) {
            if (res.length() > 0) res += ", ";
            res += key + '(' + mapPart1.get(key).value() + ')';
        }
        return res;
    }

    // ====================================================================
    // PART 3  —  加 TTL                                            [⚠ TODO]
    // ====================================================================
    // 与 Part 2 比:
    //   同: 数据结构思路一样 (HashMap), scan 字符串格式一样
    //   变: Entry schema 加了 expireAt 字段 (null 表示永久)
    //       -> 新 EntryPart3, 新 mapPart3
    //       get / scan 都要按 expireAt 过滤
    //   新: putPart3(k,v,t,ttl) 重载 (带 TTL 的 put)
    //
    // 你要写的: putPart3 with ttl, getPart3 (判过期), scanPart3 (过滤过期).
    // (Entry 设计是建议; 你也可以改成 (value, putTime, ttl) 或别的, 改了同步改测试)

    private record EntryPart3(String value, Integer expireAt) {}
    private final Map<String, EntryPart3> mapPart3 = new HashMap<>();

    public void putPart3(String key, String value, int timestamp) {
        mapPart3.put(key, new EntryPart3(value, null));  // 没 TTL = 永久
    }

    public void putPart3(String key, String value, int timestamp, int ttlSeconds) {
        mapPart3.put(key, new EntryPart3(value, timestamp + ttlSeconds));
    }

    // expireAt == null  -> 永远不过期
    // expireAt != null  -> 半开区间 [putTime, expireAt), 所以 expireAt <= now 即过期
    private boolean isExpiredPart3(EntryPart3 entry, int timestamp) {
        return entry.expireAt() != null && entry.expireAt() <= timestamp;
    }

    public String getPart3(String key, int timestamp) {
        EntryPart3 entry = mapPart3.get(key);
        if (entry == null || isExpiredPart3(entry, timestamp)) return null;
        return entry.value();
    }

    public String scanPart3(String prefix, int timestamp) {
        List<String> keyList = new ArrayList<>();
        for (String key : mapPart3.keySet()) {
            if (key.startsWith(prefix) && !isExpiredPart3(mapPart3.get(key), timestamp)) {
                keyList.add(key);
            }
        }
        Collections.sort(keyList);
        String res = "";
        for (String key : keyList) {
            if (res.length() > 0) res += ", ";
            res += key + '(' + mapPart3.get(key).value() + ')';
        }
        return res;
    }

    // ====================================================================
    // PART 4  —  backup / restore                                  [⚠ TODO]
    // ====================================================================
    // 与 Part 3 比:
    //   同: EntryPart3 / mapPart3  (schema 没变, 不引入新 Entry)
    //   变: 无
    //   新: backupPart4(timestamp) -> int
    //       restorePart4(backupId, timestamp)
    //       内部要存历史快照 + 单调递增 backupId
    //       restore 时 TTL 保留剩余量, 不重置 (见 README 的 Part 4 陷阱)
    //
    // 现在 5 个方法全是 stub —— 等你完成 Part 3 后:
    //   putPart4 / getPart4 / scanPart4 会套壳到 Part 3
    //   你只需要写 backupPart4 / restorePart4

    private record EntryPart4(String value, Integer expireAt) {}
    private final Map<String, EntryPart4> mapPart4 = new HashMap<>();

    public void putPart4(String key, String value, int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 4 — 等 Part 3 done 后展开");
    }

    public void putPart4(String key, String value, int timestamp, int ttlSeconds) {
        throw new UnsupportedOperationException("TODO: Part 4 — 等 Part 3 done 后展开");
    }

    public String getPart4(String key, int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 4 — 等 Part 3 done 后展开");
    }

    public String scanPart4(String prefix, int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 4 — 等 Part 3 done 后展开");
    }

    public int backupPart4(int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 4 — 等 Part 3 done 后展开");
    }

    public void restorePart4(int backupId, int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 4 — 等 Part 3 done 后展开");
    }
}
