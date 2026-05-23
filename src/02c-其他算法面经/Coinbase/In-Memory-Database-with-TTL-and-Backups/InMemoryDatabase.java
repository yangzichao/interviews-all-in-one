import java.util.*;

public class InMemoryDatabase {

    record Entry(String value, int timestamp) {}
    Map<String, Entry> map;

    public InMemoryDatabase() {
        this.map = new HashMap<>();
    }

    // ===== Part 1 ======================================================

    public void put(String key, String value, int timestamp) {
        map.put(key, new Entry(value, timestamp));
    }

    public String get(String key, int timestamp) {
        Entry entry = map.get(key);
        return entry == null ? null : entry.value();
    }

    // ===== Part 2 ======================================================

    // 返回格式: "key1(value1), key2(value2)" — 按 key 字典序，逗号+空格分隔
    // 没匹配 -> 返回 ""
    public String scan(String prefix, int timestamp) {
        String res = "";
        ArrayList<String> keyList = new ArrayList<>();
        for (String key : map.keySet()) {
            if (key.startsWith(prefix)) {
                keyList.add(key);
            }
        }
        Collections.sort(keyList);
        for (String key : keyList) {
            if (res.length() > 0) {
                res += ", ";
            }
            res += key +'(' + map.get(key).value() + ')';
        }
        return res;
    }

    // ===== Part 3 ======================================================
    // ttlSeconds 半开区间: entry 在 [timestamp, timestamp + ttlSeconds) 内有效
    // ttlSeconds == 0  -> 立刻过期
    // 覆盖时新 TTL 替换旧 TTL
    // 注意: get/scan 都要过滤过期 entry

    public void put(String key, String value, int timestamp, int ttlSeconds) {
        throw new UnsupportedOperationException("TODO: Part 3 — put with ttl");
    }

    // ===== Part 4 ======================================================
    // backup: 快照当前所有未过期 entry, 返回 backup_id (从 1 开始递增)
    // restore: 用快照内容替换当前 DB
    //   关键: TTL 保留剩余量, 不是重置
    //   - 永久 entry 保持永久
    //   - 已过期 entry 不进 backup
    //   - 新过期时间 = restore 时刻 + 剩余 TTL

    public int backup(int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 4 — backup");
    }

    public void restore(int backupId, int timestamp) {
        throw new UnsupportedOperationException("TODO: Part 4 — restore");
    }
}
