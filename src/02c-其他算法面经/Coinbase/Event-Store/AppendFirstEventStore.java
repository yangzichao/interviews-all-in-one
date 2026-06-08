import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Event Store answer optimized for mostly-in-order timestamps.
 *
 * Assumptions:
 * - Every query is scoped to one userId, so each user can own an independent
 *   time index and lock.
 * - Timestamps are mostly increasing per user. The common write path should
 *   therefore be append-only instead of paying TreeMap's O(log n) cost on
 *   every event.
 * - Out-of-order events exist but are relatively rare. A small late buffer is
 *   acceptable to scan during query; if the buffer grows, we batch sort + merge
 *   it back into the main sorted list.
 * - There is no eventId in the original problem, so this store does not dedupe.
 *   If the same logical event is recorded twice, it is counted twice.
 * - Multiple events can have the same timestamp. Query results are sorted by
 *   timestamp only; same-timestamp relative order is not part of the contract.
 *
 * Core idea:
 * - Keep one per-user sorted main list.
 * - Common writes append to the tail in O(1).
 * - Out-of-order writes go to a small late buffer first.
 * - Queries binary-search the sorted main list and also scan the late buffer.
 * - When the late buffer grows, sort + merge it into the main list in batch.
 */
public class AppendFirstEventStore {

    public record Event(String userId, long timestamp, String payload) {}

    private static final int DEFAULT_LATE_BUFFER_MERGE_THRESHOLD = 64;

    private final ConcurrentHashMap<String, UserEvents> eventsByUserId;
    private final int lateBufferMergeThreshold;

    public AppendFirstEventStore() {
        this(DEFAULT_LATE_BUFFER_MERGE_THRESHOLD);
    }

    public AppendFirstEventStore(int lateBufferMergeThreshold) {
        if (lateBufferMergeThreshold <= 0) {
            throw new IllegalArgumentException("lateBufferMergeThreshold must be positive");
        }
        this.eventsByUserId = new ConcurrentHashMap<>();
        this.lateBufferMergeThreshold = lateBufferMergeThreshold;
    }

    public void record(Event event) {
        Objects.requireNonNull(event, "event");

        UserEvents userEvents = eventsByUserId.computeIfAbsent(
                event.userId(),
                ignoredUserId -> new UserEvents()
        );

        userEvents.lock.writeLock().lock();
        try {
            if (canAppendToSortedEvents(userEvents.sortedEvents, event)) {
                userEvents.sortedEvents.add(event);
            } else {
                userEvents.lateBuffer.add(event);
                if (userEvents.lateBuffer.size() >= lateBufferMergeThreshold) {
                    mergeLateBufferIntoSortedEvents(userEvents);
                }
            }
        } finally {
            userEvents.lock.writeLock().unlock();
        }
    }

    public List<Event> query(String userId, long startInclusive, long endInclusive) {
        if (startInclusive > endInclusive) {
            return List.of();
        }

        UserEvents userEvents = eventsByUserId.get(userId);
        if (userEvents == null) {
            return List.of();
        }

        userEvents.lock.readLock().lock();
        try {
            List<Event> result = new ArrayList<>();

            int startIndex = lowerBoundByTimestamp(userEvents.sortedEvents, startInclusive);
            for (int index = startIndex; index < userEvents.sortedEvents.size(); index++) {
                Event event = userEvents.sortedEvents.get(index);
                if (event.timestamp() > endInclusive) {
                    break;
                }
                result.add(event);
            }

            for (Event lateEvent : userEvents.lateBuffer) {
                if (isInRange(lateEvent, startInclusive, endInclusive)) {
                    result.add(lateEvent);
                }
            }

            result.sort(EVENT_ORDER);
            return result;
        } finally {
            userEvents.lock.readLock().unlock();
        }
    }

    public int count(String userId, long startInclusive, long endInclusive) {
        if (startInclusive > endInclusive) {
            return 0;
        }

        UserEvents userEvents = eventsByUserId.get(userId);
        if (userEvents == null) {
            return 0;
        }

        userEvents.lock.readLock().lock();
        try {
            int count = 0;

            int startIndex = lowerBoundByTimestamp(userEvents.sortedEvents, startInclusive);
            for (int index = startIndex; index < userEvents.sortedEvents.size(); index++) {
                Event event = userEvents.sortedEvents.get(index);
                if (event.timestamp() > endInclusive) {
                    break;
                }
                count++;
            }

            for (Event lateEvent : userEvents.lateBuffer) {
                if (isInRange(lateEvent, startInclusive, endInclusive)) {
                    count++;
                }
            }

            return count;
        } finally {
            userEvents.lock.readLock().unlock();
        }
    }

    private static boolean canAppendToSortedEvents(List<Event> sortedEvents, Event event) {
        return sortedEvents.isEmpty()
                || event.timestamp() >= sortedEvents.get(sortedEvents.size() - 1).timestamp();
    }

    private static int lowerBoundByTimestamp(List<Event> sortedEvents, long targetTimestamp) {
        int left = 0;
        int right = sortedEvents.size();

        while (left < right) {
            int middle = left + (right - left) / 2;
            if (sortedEvents.get(middle).timestamp() < targetTimestamp) {
                left = middle + 1;
            } else {
                right = middle;
            }
        }

        return left;
    }

    private static boolean isInRange(Event event, long startInclusive, long endInclusive) {
        return startInclusive <= event.timestamp() && event.timestamp() <= endInclusive;
    }

    private static void mergeLateBufferIntoSortedEvents(UserEvents userEvents) {
        if (userEvents.lateBuffer.isEmpty()) {
            return;
        }

        userEvents.lateBuffer.sort(EVENT_ORDER);

        List<Event> mergedEvents = new ArrayList<>(
                userEvents.sortedEvents.size() + userEvents.lateBuffer.size()
        );

        int sortedIndex = 0;
        int lateIndex = 0;

        while (sortedIndex < userEvents.sortedEvents.size()
                && lateIndex < userEvents.lateBuffer.size()) {
            Event sortedEvent = userEvents.sortedEvents.get(sortedIndex);
            Event lateEvent = userEvents.lateBuffer.get(lateIndex);

            if (EVENT_ORDER.compare(sortedEvent, lateEvent) <= 0) {
                mergedEvents.add(sortedEvent);
                sortedIndex++;
            } else {
                mergedEvents.add(lateEvent);
                lateIndex++;
            }
        }

        while (sortedIndex < userEvents.sortedEvents.size()) {
            mergedEvents.add(userEvents.sortedEvents.get(sortedIndex));
            sortedIndex++;
        }

        while (lateIndex < userEvents.lateBuffer.size()) {
            mergedEvents.add(userEvents.lateBuffer.get(lateIndex));
            lateIndex++;
        }

        userEvents.sortedEvents.clear();
        userEvents.sortedEvents.addAll(mergedEvents);
        userEvents.lateBuffer.clear();
    }

    private static final Comparator<Event> EVENT_ORDER =
            Comparator.comparingLong(Event::timestamp);

    private static class UserEvents {
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private final List<Event> sortedEvents = new ArrayList<>();
        private final List<Event> lateBuffer = new ArrayList<>();
    }

    public static void main(String[] args) {
        AppendFirstEventStore store = new AppendFirstEventStore(2);

        store.record(new Event("alice", 100, "a"));
        store.record(new Event("alice", 300, "c"));
        store.record(new Event("alice", 200, "b")); // late, then merged at threshold
        store.record(new Event("alice", 250, "late"));
        store.record(new Event("bob", 150, "other user"));

        assertEquals(4, store.count("alice", 100, 300), "alice count");
        assertEquals(1, store.count("bob", 100, 300), "bob count");
        assertEquals(0, store.count("missing", 100, 300), "missing user count");

        List<Event> aliceEvents = store.query("alice", 150, 300);
        assertEquals(
                List.of("b", "late", "c"),
                aliceEvents.stream().map(Event::payload).toList(),
                "alice range order"
        );

        System.out.println("AppendFirstEventStore self-check passed");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(
                    message + " expected=" + expected + " actual=" + actual
            );
        }
    }
}
