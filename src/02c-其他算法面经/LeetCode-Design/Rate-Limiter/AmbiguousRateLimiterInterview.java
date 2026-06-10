/**
 * Real-interview version of "Design Rate Limiter".
 *
 * See Rate-Limiter-Interview-Notes.md for the clarification script and
 * follow-up discussion. This file is intentionally a scaffold so you can
 * practice implementing the variants yourself.
 */
public class AmbiguousRateLimiterInterview {

    public static class FixedWindowRateLimiter {
        public FixedWindowRateLimiter(int limit, int windowSeconds) {
            if (limit <= 0 || windowSeconds <= 0) {
                throw new IllegalArgumentException("limit and windowSeconds must be positive");
            }
            throw new UnsupportedOperationException("TODO: initialize fixed-window state");
        }

        public boolean allow(String userId, int timestamp) {
            throw new UnsupportedOperationException("TODO: fixed-window allow");
        }
    }

    public static class SlidingWindowLogRateLimiter {
        public SlidingWindowLogRateLimiter(int limit, int windowSeconds) {
            if (limit <= 0 || windowSeconds <= 0) {
                throw new IllegalArgumentException("limit and windowSeconds must be positive");
            }
            throw new UnsupportedOperationException("TODO: initialize per-user timestamp logs");
        }

        public boolean allow(String userId, int timestamp) {
            throw new UnsupportedOperationException("TODO: exact sliding-window allow");
        }
    }

    public static class TokenBucketRateLimiter {
        public TokenBucketRateLimiter(int capacity, double refillTokensPerSecond) {
            if (capacity <= 0 || refillTokensPerSecond <= 0.0) {
                throw new IllegalArgumentException("capacity and refill rate must be positive");
            }
            throw new UnsupportedOperationException("TODO: initialize per-user token buckets");
        }

        public boolean allow(String userId, int timestamp) {
            throw new UnsupportedOperationException("TODO: lazy-refill token bucket allow");
        }
    }

    public static void main(String[] args) {
        System.out.println("Implement the TODOs, then add checks here.");
    }
}
