import java.util.*;

/**
 * 02 — URL Hit Counter
 *
 * Concurrent per-key counter. See README.md.
 * 你自己挑数据结构 —— 提示就只有 "ConcurrentHashMap 在你手边"。
 */
public class UrlHitCounter {

    // TODO: state

    public void hit(String url) {
        throw new UnsupportedOperationException("TODO: increment count for url, atomic");
    }

    public long countFor(String url) {
        throw new UnsupportedOperationException("TODO: return current count or 0");
    }

    public Map<String, Long> snapshot() {
        throw new UnsupportedOperationException("TODO: immutable snapshot of all counts");
    }
}
