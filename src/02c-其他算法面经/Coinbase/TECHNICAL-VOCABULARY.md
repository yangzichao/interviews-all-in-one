# Technical Vocabulary — Coinbase Coding Interview

Terms you should be able to say out loud and explain in one sentence.

---

## API Design

| Term | One-line definition | Example sentence |
|------|---------------------|-----------------|
| **API signature** | The method name + parameter types + return type — the "contract" callers depend on | "Before I start coding, let me confirm the API signature — `placeOrder` takes a String orderId and returns void, right?" |
| **Idempotency** | Calling the same operation twice produces the same result as calling it once | "Should `cancelOrder` be idempotent? If I cancel an already-cancelled order, should it silently succeed or throw?" |
| **Immutability** | An object whose state cannot change after creation (e.g. Java `record`) | "I'm using a Java record here so the Order object is immutable — to change state I create a new instance." |
| **Invariant** | A condition that must always be true (e.g. "an order in CANCELLED state can never transition") | "The key invariant is that once an order reaches a terminal state, no method can change it." |
| **Contract** | The agreed behavior between caller and implementation (what it accepts, what it guarantees) | "The contract for `getOrder` is: return null if not found, never throw for a missing ID." |
| **Edge case** | An input at the boundary of what's valid (empty list, duplicate ID, null, max value) | "The edge cases I want to cover are: duplicate order ID, cancelling a FILLED order, and calling cancelAll on a user with no orders." |
| **Happy path** | The normal, expected flow with valid input and no errors | "I'll implement the happy path first — place an order, pause it, then cancel it — and then handle invalid transitions." |

---

## Data Structures

| Term | One-line definition | Example sentence |
|------|---------------------|-----------------|
| **Primary index** | The main lookup structure, usually by ID — e.g. `Map<orderId, Order>` | "The primary index is a HashMap keyed by orderId — that gives us O(1) lookup by ID." |
| **Cardinality** (基数) | The number of distinct values a field can take — high = many distinct values, low = few. 基数越高，索引越有价值：userId 每人唯一（高基数），type 只有 4 个值（低基数）。 | "userId is high-cardinality — it narrows the result set to a tiny fraction. `type` is low-cardinality — indexing it barely helps because each bucket still holds 25% of the data." |
| **Secondary index** | An extra structure for a different access pattern — e.g. `Map<userId, List<Transaction>>` alongside the primary `Map<id, Transaction>` | "I'll add a secondary index from userId to their transactions — that way the userId filter is O(1) lookup instead of a full scan. For type and date I'll still scan, because those are range/low-cardinality filters that don't benefit from a hash index." |
| **Compound key** | A key made of multiple fields — e.g. `(state, userId)` as a map key | "If per-user queries are hot, I can use a compound key of (state, userId) for a third-level index." |
| **Amortized O(1)** | Not O(1) every call, but O(1) on average over many calls (e.g. ArrayList resize) | "ArrayList append is amortized O(1) — most calls are instant, and the occasional resize averages out." |
| **TreeMap** | Sorted map backed by a red-black tree — O(log n) insert/lookup, keys always in order | "I'll use a TreeMap for the order book so the best bid is always at the last key — O(log n) insert, O(1) peek." |
| **ConcurrentSkipListMap** | Thread-safe sorted map — like TreeMap but safe for concurrent reads and writes | "If we need concurrent access to the sorted price levels, I'd swap TreeMap for ConcurrentSkipListMap." |
| **Deque / ArrayDeque** | Double-ended queue — O(1) add/remove at both ends | "Each price level holds an ArrayDeque of orders — new orders enqueue at the tail, fills dequeue from the head." |
| **Priority Queue** | Heap-backed structure — always gives you the min (or max) element in O(log n) | "A priority queue works for Block Mining — always pull the highest-fee transaction in O(log n)." |

---

## Concurrency

| Term | One-line definition | Example sentence |
|------|---------------------|-----------------|
| **Thread safety** | A method or class that behaves correctly when called from multiple threads simultaneously | "This implementation isn't thread-safe yet — if two threads call `placeOrder` simultaneously they could both pass the duplicate-ID check." |
| **Race condition** | A bug where the result depends on the unpredictable order of thread execution | "There's a race condition between cancel and fill — both are racing to set a terminal state on the same order." |
| **Atomic operation** | An operation that completes entirely or not at all — no half-done state visible to other threads | "The state transition has to be atomic — I don't want another thread to see the order halfway through the update." |
| **Compound operation** | Multiple steps that need to happen together atomically (e.g. read + modify + write) | "Updating the primary map and the secondary index is a compound operation — ConcurrentHashMap alone won't make that atomic." |
| **CAS (Compare-And-Swap)** | "Update this value only if it's still X" — the foundation of lock-free algorithms | "I'll use CAS on the order state — whoever successfully swaps from ACTIVE to CANCELLED wins, and the other thread gets a clean failure." |
| **Mutex / Lock** | A mechanism that lets only one thread into a critical section at a time | "The simplest fix is a mutex around the whole transition — correctness first, then we can optimize." |
| **ReadWriteLock** | A lock that allows many concurrent readers OR one exclusive writer — never both | "Reads dominate in this system, so a ReadWriteLock lets all the query threads run in parallel and only blocks when a write comes in." |
| **Synchronized** | Java keyword that puts a lock around a method or block | "I'll mark this method synchronized for now — it's the safest starting point and we can loosen it later." |
| **Deadlock** | Two threads each waiting for the other to release a lock — both stuck forever | "If we lock by userId in one place and by orderId in another, we risk deadlock — I'll establish a consistent lock ordering." |
| **Starvation** | A thread that can never get the lock because others keep cutting in front | "With a plain lock and high write volume, readers could starve — ReadWriteLock has a fairness option to prevent that." |
| **Volatile** | Java keyword that ensures a variable is always read from main memory, not a thread-local cache | "I'd mark this flag volatile so all threads immediately see when it's set — without it, a thread might cache a stale value." |
| **Happens-before** | A guarantee that one operation's result is visible to another thread | "The Java memory model guarantees happens-before across a synchronized block, so the write in thread A is visible to thread B after it acquires the same lock." |
| **MVCC** | Multi-Version Concurrency Control — readers see a consistent snapshot, writers create new versions | "If we want zero-lock reads, we can go MVCC — writers produce a new version of the map, readers hold a reference to the old one until they're done." |
| **Ownership** | Which thread is solely responsible for a piece of state — the first question to ask before reaching for any lock | "Before picking a lock, I ask: who owns this state? If the answer is exactly one thread, I don't need a lock at all." |
| **Single-writer principle** | If exactly one thread ever writes a piece of state, thread safety falls out of the design — no lock needed | "Each symbol's order book has exactly one writer thread, so there are no locks inside the order book itself." |
| **Actor model** | Each actor owns its state exclusively and communicates only via message passing — no shared mutable state | "I'd model this as one actor per symbol — the actor owns its order book, all mutations arrive through its message queue." |
| **Message passing** | Threads communicate by sending immutable messages rather than sharing mutable state | "Instead of a shared map that both threads write to, I pass an Order message to the symbol's queue — the worker thread is the only one that touches the book." |

---

## State Machine

| Term | One-line definition | Example sentence |
|------|---------------------|-----------------|
| **State machine** | A system that is always in one of a finite set of states, with defined legal transitions | "I'll model the order lifecycle as a state machine — each method just validates the current state before transitioning." |
| **Terminal state** | A state you can never leave (e.g. CANCELLED, FILLED) | "CANCELLED and FILLED are both terminal states — any operation on them should throw IllegalStateException." |
| **Transition** | Moving from one state to another (e.g. ACTIVE → PAUSED) | "The only legal transitions from ACTIVE are to PAUSED or CANCELLED — everything else is rejected." |
| **Illegal transition** | An attempt to move to a state that isn't allowed from the current state | "Calling `resumeOrder` on an ACTIVE order is an illegal transition — I'll throw IllegalStateException with a clear message." |

---

## Performance & Scalability

| Term | One-line definition | Example sentence |
|------|---------------------|-----------------|
| **Time complexity** | How runtime grows as input size grows (e.g. O(n), O(log n), O(1)) | "The naive approach is O(n) because we scan all orders — with a secondary index it drops to O(k) where k is just the result set." |
| **Space complexity** | How memory usage grows as input size grows | "The trade-off with multiple indexes is space complexity — we're storing each order ID in up to three maps." |
| **Throughput** | How many operations per second the system can handle | "A single global lock kills throughput — all threads queue up even if they're touching different orders." |
| **Latency** | How long a single operation takes end-to-end | "ReadWriteLock keeps read latency low since concurrent reads don't block each other." |
| **Bottleneck** | The slowest part of the system that limits overall performance | "The bottleneck here is the fsync on every write — we can batch them to improve throughput without much extra risk." |
| **Hot path** | The code that runs most frequently — where performance matters most | "Order placement is the hot path — I want that to be O(1) even if it means more bookkeeping on cancel." |
| **Sharding** | Splitting data across multiple nodes so no single node holds everything | "At Coinbase scale you'd shard by symbol — BTC-USD on one node, ETH-USD on another — so each order book fits in memory." |
| **Snapshot** | A point-in-time copy of data — safe to read without holding a lock | "I'll return a snapshot of the list inside the lock and release immediately — the caller gets a stable copy to iterate over." |

---

## Testing

| Term | One-line definition | Example sentence |
|------|---------------------|-----------------|
| **Unit test** | Tests one method or class in isolation | "I'll write a unit test for the state machine transitions — each legal and illegal path gets its own case." |
| **Edge case test** | Tests boundary conditions (empty input, duplicates, max size) | "The edge cases I want to cover are: cancelling an already-cancelled order, placing with a duplicate ID, and cancelAll on a user with zero orders." |
| **Stress test** | Runs a large volume of operations (or concurrent threads) to find race conditions or slowdowns | "For the concurrent version I'd write a stress test — 100 threads each placing and cancelling the same order, then assert the final state is consistent." |
| **Regression test** | Verifies a bug you fixed doesn't come back | "Once we fix this, I'd add a regression test so this exact scenario never silently breaks again." |
| **Test coverage** | The percentage of code paths exercised by your tests | "The happy path alone isn't enough coverage — I want every branch of the state machine exercised." |
| **Assertion** | A statement in a test that says "this must be true" — fails the test if it isn't | "After cancelling, I assert the order state is CANCELLED and the active-orders index no longer contains it." |

---

## How to Use These in a Sentence

- "I'm going to add a **secondary index** so the query doesn't have to scan the full map."
- "The problem here is a **compound operation** — I need to update two maps atomically."
- "This is a **terminal state**, so any further transition should throw `IllegalStateException`."
- "A `ReadWriteLock` makes sense here because reads vastly outnumber writes."
- "Let me write a quick **stress test** to verify there's no race condition."
