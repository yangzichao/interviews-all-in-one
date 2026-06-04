# Coinbase Coding Interview — Phrases by Stage

---

## Stage 1 — Clarifying the Problem

> "Before I jump in, let me make sure I understand the requirements."

> "I'm going to assume X — does that match what you have in mind?"

> "Is the input guaranteed to be valid, or should I handle bad input?"

> "What should happen on duplicate IDs — throw, or silently overwrite?"

> "Should I treat this as a strict requirement or a nice-to-have for now?"

---

## Stage 2 — Designing the API / Data Model

> "I'm thinking the method signature looks like this — does that work for you?"

> "I'll return `null` for missing entries rather than throwing — that feel right?"

> "Let me sketch the data model before I start coding so we're aligned."

> "I'll start with the simplest structure that works, then we can optimize."

---

## Stage 3 — Choosing a Data Structure

> "My first instinct is a `HashMap` here — O(1) lookup, simple to implement."

> "If query performance matters as the data grows, I'd add a secondary index."

> "A `TreeMap` gives me sorted order automatically, at the cost of O(log n) inserts."

> "The trade-off here is memory vs. query speed — I'll go with X because..."

> "In production I'd reach for a `ConcurrentSkipListMap`, but for now `TreeMap` is fine."

---

## Stage 4 — Talking Through Your Implementation

> "I'll think out loud as I go — stop me if I'm going in the wrong direction."

> "I'm going to write the happy path first, then handle edge cases."

> "This is O(n) right now — I can bring it down to O(k) with an index if needed."

> "I'll leave a comment here — this is where I'd add validation in production."

---

## Stage 5 — Concurrency

**Thought process — ask these in order:**

1. **Who owns what state?** If one thread owns one piece of state exclusively, you don't need a lock on it at all.
2. **Is communication one-directional?** Router → coordinator → worker. Workers should never write back to shared state upward.
3. **What is the unit of atomicity?** Only after ownership is clear, ask what operations need to be atomic.
4. **Then pick the right primitive.** Lock / ReadWriteLock / CAS — in that order of complexity.

> "Before I think about which lock to use, I want to ask: who owns what state? If I can arrange it so each piece of mutable state has exactly one writer, I get thread safety for free."

> "The cleanest model here is single-writer per symbol — route all orders for BTC-USD to one thread, which owns that order book exclusively. No locks needed inside the book."

> "Locks are what you reach for when ownership is unclear. I'd rather fix the ownership first."

> "If this needs to be thread-safe, the first question is: what's the unit of atomicity?"

> "A single `ConcurrentHashMap` isn't enough here — we have compound operations across multiple maps."

> "I'd use a `ReadWriteLock` — reads are frequent and don't need to block each other."

> "The tricky race is cancel vs. fill — two threads racing to set a terminal state."

> "I'd use CAS here so whichever thread wins, the loser gets a clean rejection."

---

## Stage 6 — Discussing Trade-offs

> "There are a few ways to approach this — want me to walk through the trade-offs?"

> "Option A is simpler but O(n). Option B needs an extra index but gets us O(k). Given the query volume, I'd go with B."

> "This is a classic consistency vs. throughput trade-off."

> "In a low-traffic system I'd keep it simple. At Coinbase scale I'd think about..."

> "I'd flag this as a tech debt comment and revisit if query latency becomes an issue."

---

## Stage 7 — Testing

> "Let me think about the cases worth testing: happy path, edge cases, and failure modes."

> "I'd test the state machine exhaustively — every illegal transition should throw."

> "For concurrency, I'd write a stress test that hammers the same order ID from multiple threads."

> "A good test for this would be: place 1000 orders, cancel half, verify counts."

---

## Stage 8 — Wrapping Up / Follow-ups

> "That's the core implementation — want me to talk through how I'd extend this for concurrency?"

> "I'd refactor this into a base class once the interface stabilizes — didn't want to over-engineer upfront."

> "The bottleneck at scale would be X — here's how I'd address that."

> "Is there a part of this you'd like me to revisit or go deeper on?"

---

## One-liners to Sound Natural Under Pressure

| Situation | What to say |
|-----------|-------------|
| You need a moment to think | "Give me a second to think through the data flow." |
| You're not sure about a requirement | "I'll make an assumption and flag it — correct me if I'm off." |
| You realize mid-way you picked the wrong DS | "Actually, let me reconsider — I think a TreeMap serves us better here." |
| You want to show you know more | "This works for the interview scope — in prod I'd also think about X." |
| You finish early | "I'm happy with this — want to talk about how this scales or handles concurrency?" |
