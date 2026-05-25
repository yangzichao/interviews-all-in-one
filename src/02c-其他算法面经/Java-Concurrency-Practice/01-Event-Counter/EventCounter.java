/**
 * 01 — Event Counter
 *
 * Thread-safe counter. Many threads call record(); any thread can call total().
 * See README.md for the contract and the questions to ask yourself.
 *
 * 练习目标: 不剧透 —— 你自己挑用哪个原语。
 */
public class EventCounter {

    // TODO: state goes here

    public void record() {
        throw new UnsupportedOperationException("TODO: thread-safe increment");
    }

    public long total() {
        throw new UnsupportedOperationException("TODO: read current total with proper visibility");
    }

    public void reset() {
        throw new UnsupportedOperationException("TODO: atomic reset");
    }
}
