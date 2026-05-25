/**
 * 05 — Token Bucket
 *
 * See README.md. 真坑点:
 *   - tryAcquire(n) 必须 all-or-nothing —— 不够就一个都不扣
 *   - refill 必须 cap 在 capacity
 *   - 高并发下不能丢 token 也不能多发 token
 */
public class TokenBucket {

    // TODO: state — capacity + available count

    public TokenBucket(int capacity) {
        throw new UnsupportedOperationException("TODO: ctor — start at full capacity");
    }

    public boolean tryAcquire() {
        throw new UnsupportedOperationException("TODO: try take 1 token");
    }

    public boolean tryAcquire(int n) {
        throw new UnsupportedOperationException("TODO: try take n tokens, all-or-nothing");
    }

    public void refill(int tokens) {
        throw new UnsupportedOperationException("TODO: add tokens, cap at capacity, drop overflow");
    }

    public int available() {
        throw new UnsupportedOperationException("TODO: current token count (weakly consistent ok)");
    }
}
