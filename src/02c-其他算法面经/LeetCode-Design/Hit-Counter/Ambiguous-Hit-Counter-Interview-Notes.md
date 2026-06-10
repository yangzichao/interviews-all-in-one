# Ambiguous Hit Counter Interview Notes

## Original-Style Prompt

```python
def track_hit(timestamp):
    # implement me

def get_hit_count_in_last_5_minutes():
    # implement me
```

Backstory: a new webpage launches tomorrow, and the product team forgot metrics. The CEO only wants one realtime answer:

```text
How many hits have we received in the last 5 minutes?
```

## Clarify The API Contract

Ask this first:

```text
Let me first clarify the API contract.
Should getHitCount take now explicitly, like getHitCount(now)?
I assume trackHit(timestamp) records one hit at that timestamp, and getHitCount(now)
returns hits in (now - 300, now].
```

Then ask:

```text
Are timestamps non-decreasing?
If late events are possible, how late can they be?
For the first version, can we treat late events as out of scope?
```

Minimal assumptions for the first version:

- Window is fixed at 300 seconds.
- Caller passes integer timestamps in seconds.
- Query takes an explicit `now`.
- Window semantics are `(now - 300, now]`.
- Timestamps are non-decreasing.
- Late events are out of scope for the first implementation.

## Data Structure Choice

Deque is the simplest exact solution:

- Store one timestamp per hit.
- On query, remove expired timestamps from the front.
- Return deque size.
- Good for sparse or moderate traffic.
- Space is `O(number of hits in the window)`.

Bucketed circular buffer is the better default if traffic may grow:

- Store one count per time bucket.
- With 1-second granularity, 5 minutes needs 300 buckets.
- Each bucket stores `timestamp + count`.
- Space is `O(number of buckets)`, not `O(number of hits)`.
- Many hits in the same second collapse into one counter.

Interview phrasing:

```text
The timestamp unit matters because it affects the natural bucket granularity.
But the bigger design question is traffic density: how many hits do we expect per bucket?
If events are sparse, storing actual hit timestamps in a deque may use less memory than
pre-allocating buckets. If events are dense, buckets are more efficient because many
hits collapse into one counter. Since this launch may grow traffic, I would implement
fixed buckets first.
```

## Bucket Implementation

For a 300-second window:

```text
bucketIndex = timestamp % 300
```

On `trackHit(timestamp)`:

```text
if bucketTimestamp[bucketIndex] == timestamp:
    bucketCount[bucketIndex] += 1
else:
    bucketTimestamp[bucketIndex] = timestamp
    bucketCount[bucketIndex] = 1
```

On `getHitCount(now)`:

```text
sum counts where now - 300 < bucketTimestamp[i] <= now
```

Complexity:

- `trackHit`: `O(1)`
- `getHitCount`: `O(300)`, effectively `O(1)` because the window is fixed
- Space: `O(300)`

## Retention Window

`Data retention policy` is correct English, but this coding problem usually wants:

```text
retention window
```

Use:

```text
We only retain buckets that can still contribute to the last-5-minutes answer.
```

Circular buffer handles retention by reusing old buckets after they fall outside the window.

## Range Query Follow-Up

If the question changes from:

```text
get last 5 minutes
```

to:

```text
query(start, end)
```

then circular buckets still solve bounded retention, but range query may need more discussion.

Keep it simple:

- If the retention window is small, scan the relevant buckets.
- Example: 100 minutes with 1-second buckets is 6000 buckets.
- Scanning 6000 buckets may be acceptable.
- Only introduce prefix sums / Fenwick Tree if arbitrary range queries are frequent enough that scanning is a bottleneck.

One-line summary:

```text
Circular buffer solves retention; prefix sum or Fenwick Tree solves faster range query if needed.
```

## Concurrency

This problem is a good fit for single-writer ownership.

Interview phrasing:

```text
Many request handlers can receive traffic concurrently, but they should enqueue hit events
to one counter owner thread. The owner thread only updates one circular bucket, so the
operation is tiny. Similar to Redis, commands are serialized, so increments are atomic
and no writes are lost. This avoids lock contention on hot buckets.
```

Reads:

- Strong consistency: route reads through the same owner thread.
- Slightly stale reads acceptable: publish a periodic snapshot for readers.
- If one owner thread is not enough: shard by pageId, server, or hash key and sum shard results.

Short version:

```text
Many request handlers, one counter writer.
```

