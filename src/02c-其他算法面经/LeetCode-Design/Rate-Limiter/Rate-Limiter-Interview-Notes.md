# Rate Limiter Interview Notes

## Original-Style Prompt

```text
Design an in-memory rate limiter.

Given a user ID and a timestamp, decide whether this request should be allowed.
For example, each user can make at most 100 requests per minute.
```

Possible API:

```java
boolean allow(String userId, int timestamp);
```

## Clarify The API Contract

Ask first:

```text
Let me first clarify the contract. Are we limiting per user, per IP, per API key,
or globally? What is the rule: N requests per fixed window, N requests in any
sliding window, or an average rate with burst capacity?
```

Then ask:

```text
If a request is over the limit, should we reject immediately or wait until it
can proceed? For a server-side inbound API, I assume reject immediately and
return false / HTTP 429.
```

Minimal assumptions for the first coding version:

- Per-user limiter.
- Single process.
- Caller passes integer timestamps in seconds.
- Exact answer.
- Over-limit requests return `false`.
- First version can use `limit` requests per `windowSeconds`.

## Algorithm Choices

### Fixed Window Counter

State per user:

```text
windowStart
count
```

Behavior:

- If `timestamp >= windowStart + windowSeconds`, start a new window.
- If count is below limit, increment and allow.
- Else reject.

Pros:

- Very easy.
- `O(1)` per request.
- Small memory.

Cons:

- Boundary burst problem.
- Example: 100 requests at `00:59` and 100 at `01:00` means 200 requests in about 1 second.

Use this as a warm-up, not the final answer if the interviewer cares about smoothness.

### Sliding Window Log

State per user:

```text
deque of allowed request timestamps
```

Behavior:

- Remove timestamps `<= timestamp - windowSeconds`.
- If deque size is below limit, append timestamp and allow.
- Else reject.

Pros:

- Exact sliding-window semantics.
- Easy to explain.

Cons:

- Stores one timestamp per allowed request.
- Memory is `O(limit * active users)` if each user can fill the window.

Good first implementation if `limit` is small and exactness matters.

### Sliding Window Buckets

State per user:

```text
bucketTimestamp[]
bucketCount[]
```

Behavior:

- Map each timestamp to a circular bucket.
- Each bucket stores the absolute timestamp plus count.
- Query by summing live buckets in `(timestamp - windowSeconds, timestamp]`.

Pros:

- Memory is bounded by bucket count per active user.
- Good when traffic is dense.

Cons:

- Query scans buckets.
- Granularity controls accuracy and memory.

This is close to the Hit Counter bucket solution.

### Token Bucket

State per user:

```text
tokens
lastRefillTimestamp
```

Rule:

- Bucket capacity controls burst size.
- Refill rate controls sustained rate.
- Each request consumes one token.
- If no token is available, reject.

Lazy refill:

```text
elapsed = now - lastRefillTimestamp
tokens = min(capacity, tokens + elapsed * refillRate)
lastRefillTimestamp = now
```

Then:

```text
if tokens >= 1:
    tokens -= 1
    allow
else:
    reject
```

Pros:

- `O(1)` per request.
- Small state per user.
- Allows controlled bursts.
- Common production choice.

Cons:

- Does not enforce a hard "at most N in any window" rule.
- It enforces average rate plus burst capacity.

## Which One Should I Start With?

For a coding interview:

```text
I would start with sliding-window log if the interviewer wants exact "N requests
in any W seconds" semantics and the limit is small.
```

If the prompt sounds like production API rate limiting:

```text
I would choose token bucket because it has O(1) state per user, supports bursts,
and is easy to implement with lazy refill.
```

If this is connected to the Hit Counter problem:

```text
Sliding-window rate limiter is just Hit Counter plus an allow/reject decision.
Before recording this request, count how many allowed requests are already in
the window. If the count is below the limit, record and allow; otherwise reject.
```

## Follow-Ups

### 1. Per-User State

Question:

```text
What if we have millions of users?
```

Answer:

- Keep state only for active users.
- Evict user state after it has been inactive for longer than the retention window.
- In Java, use a `HashMap<String, State>` for the coding version.
- In production, use Redis or another shared store with TTL.

### 2. Concurrency

Question:

```text
What if multiple threads call allow(userId, timestamp)?
```

Answer:

- For the coding version, use `synchronized` or per-user locking.
- The check-and-update must be atomic.
- Do not do "check count" and "insert request" as separate unsynchronized steps.

Interview phrasing:

```text
The important invariant is that allow() is an atomic check-and-update operation.
Two concurrent requests should not both observe available capacity and both pass
if only one slot remains.
```

### 3. Distributed Rate Limiter

Question:

```text
What if requests hit multiple service instances?
```

Answer:

- Local in-memory state is no longer enough.
- Use a centralized store such as Redis.
- Use atomic operations or Lua script so read/decide/write happens atomically.
- Return HTTP 429 when over the limit.

Keep this short unless it is a system design round.

### 4. Fixed Window Boundary Burst

Question:

```text
Why not just use a fixed window counter?
```

Answer:

```text
Fixed window is easy, but it allows bursts at window boundaries. If the rule is
100 requests per minute, the user can send 100 at the end of one minute and 100
at the start of the next. Sliding window or token bucket gives smoother control.
```

### 5. Reject vs Wait

Question:

```text
Should over-limit requests wait or be rejected?
```

Answer:

- Inbound server API: reject immediately, usually HTTP 429.
- Outbound client throttling: waiting can be okay.
- Guava `RateLimiter.acquire()` is more like outbound throttling because it blocks.
- `tryAcquire()` is closer to server-side reject semantics.

## Memorize This

```text
First clarify the rule: fixed window, sliding window, or token bucket.
For exact "N requests in any W seconds", use sliding-window log first.
For production-style API rate limiting, token bucket is usually a better default:
small state, lazy refill, O(1), and controlled bursts.
The key concurrency invariant is atomic check-and-update.
```

## Sources

- Stripe documents real API rate and concurrency limiters, `429` responses, and rate-limit reason headers.
- Redis documents shared rate limiters for distributed services, including fixed window, sliding window, token bucket, TTL cleanup, and Lua scripting for atomic read/decide/write.
- Guava `RateLimiter` is useful contrast: token-bucket-like, single JVM, usually blocking via `acquire()`.

