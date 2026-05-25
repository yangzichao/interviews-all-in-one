import java.util.*;

/**
 * 06 — Atomic Config Store
 *
 * See README.md. 核心要求:
 *   - putAll 对外原子: reader 要么看全旧, 要么看全新
 *   - snapshot 是一致视图, 不能横跨两次 putAll
 *   - 读路径要快
 *
 * 两条主流路径都对, 选一个并能说出 trade-off:
 *   (a) volatile reference + copy-on-write immutable map
 *   (b) ConcurrentHashMap + ReentrantReadWriteLock
 */
public class ConfigStore {

    // TODO: state

    public ConfigStore() {
        throw new UnsupportedOperationException("TODO: ctor");
    }

    public String get(String key) {
        throw new UnsupportedOperationException("TODO: hot-path read");
    }

    public void putAll(Map<String, String> updates) {
        throw new UnsupportedOperationException("TODO: atomic multi-key update; keys not in `updates` are preserved");
    }

    public Map<String, String> snapshot() {
        throw new UnsupportedOperationException("TODO: consistent immutable view");
    }
}
