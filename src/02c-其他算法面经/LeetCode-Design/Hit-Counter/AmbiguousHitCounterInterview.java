/**
 * Real-interview version of "Design Hit Counter".
 *
 * Public references for LeetCode 362 usually specify a clean API:
 *
 *   hit(timestamp)
 *   getHits(timestamp) -> hits in the past 300 seconds
 *
 * In a real interview, the prompt is often less precise. Keep the longer
 * clarification script in Ambiguous-Hit-Counter-Interview-Notes.md; this file
 * keeps the implementation examples compact.
 */
public class AmbiguousHitCounterInterview {
    private static final int DEFAULT_WINDOW_SECONDS = 300;

    public static class SingleProcessHitCounter {
        public SingleProcessHitCounter() {
            this(DEFAULT_WINDOW_SECONDS);
        }

        public SingleProcessHitCounter(int windowSeconds) {
            if (windowSeconds <= 0) {
                throw new IllegalArgumentException("windowSeconds must be positive");
            }
            throw new UnsupportedOperationException("TODO: initialize single-process counter");
        }

        public void hit(int timestamp) {
            throw new UnsupportedOperationException("TODO: record one hit");
        }

        public int getHits(int timestamp) {
            throw new UnsupportedOperationException("TODO: return hits in (timestamp - window, timestamp]");
        }
    }

    public static class BucketedHitCounter {
        public BucketedHitCounter() {
            this(DEFAULT_WINDOW_SECONDS);
        }

        public BucketedHitCounter(int windowSeconds) {
            if (windowSeconds <= 0) {
                throw new IllegalArgumentException("windowSeconds must be positive");
            }
            throw new UnsupportedOperationException("TODO: initialize circular buckets");
        }

        public void hit(int timestamp) {
            throw new UnsupportedOperationException("TODO: update one bucket");
        }

        public int getHits(int timestamp) {
            throw new UnsupportedOperationException("TODO: sum live buckets");
        }
    }

    /*
     * Minimal thread-safe wrapper if the interviewer insists on direct
     * multi-threaded calls. Prefer the single-writer model above for the real
     * design discussion because it avoids hot-bucket lock contention.
     */
    public static class SynchronizedHitCounter {
        public SynchronizedHitCounter() {
            this(DEFAULT_WINDOW_SECONDS);
        }

        public SynchronizedHitCounter(int windowSeconds) {
            if (windowSeconds <= 0) {
                throw new IllegalArgumentException("windowSeconds must be positive");
            }
            throw new UnsupportedOperationException("TODO: initialize synchronized wrapper");
        }

        public synchronized void hit(int timestamp) {
            throw new UnsupportedOperationException("TODO: record one hit with synchronization");
        }

        public synchronized int getHits(int timestamp) {
            throw new UnsupportedOperationException("TODO: return synchronized hit count");
        }
    }

    public static void main(String[] args) {
        System.out.println("Implement the TODOs, then add checks here.");
    }
}
