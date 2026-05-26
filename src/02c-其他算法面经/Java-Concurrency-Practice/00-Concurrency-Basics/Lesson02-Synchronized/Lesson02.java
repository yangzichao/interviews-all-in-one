/**
 * Lesson 02 — synchronized (互斥锁)
 *
 * 看 ../README.md 里 Lesson 02 一节. 关键词: synchronized, intrinsic lock, monitor, critical section.
 *
 * 实现要点:
 *   - private long count;  // 不要用 volatile, 不要用 Atomic
 *   - synchronized void increment(): count++
 *   - synchronized long get(): return count
 */
public class Lesson02 {

    // TODO: private long count;
    private long count;
    public synchronized void increment() {
        count++;
    }

    public synchronized long get() {
        return count;
    }
}

// =============================================================================
// Exercise A — BuggyCounter (no lock): run this and watch count drop below 2000
// =============================================================================
//
// Two threads each call increment() 1000 times. Expected: 2000.
// Without synchronized, lost updates make the result unpredictably lower.
//
// HOW TO RUN (paste into a main() or test):
//
//   BuggyCounter c = new BuggyCounter();
//   Thread a = new Thread(() -> { for (int i = 0; i < 1000; i++) c.increment(); });
//   Thread b = new Thread(() -> { for (int i = 0; i < 1000; i++) c.increment(); });
//   a.start(); b.start(); a.join(); b.join();
//   System.out.println(c.get()); // prints something like 1843, never reliably 2000
//
// Then swap BuggyCounter for Lesson02 and watch it always print 2000.
class BuggyCounter {
    private long count;
    void increment() { count++; }   // no synchronized — data race here
    long get()       { return count; }
}

// =============================================================================
// Exercise B — BankAccount: a real production scenario
// =============================================================================
//
// ⚠️ THIS IS A LEARNING EXERCISE, NOT PRODUCTION CODE.
// In a real banking system, money transfers are handled by a database transaction:
//
//   BEGIN TRANSACTION;
//   UPDATE accounts SET balance = balance - 100 WHERE id = 'A';
//   UPDATE accounts SET balance = balance + 100 WHERE id = 'B';
//   COMMIT;
//
// The database guarantees atomicity, isolation, and durability (ACID) — you do
// not write synchronized for this in production Java code.
//
// This exercise exists purely to build intuition: what does "atomic across two
// objects" mean, and why is a shared lock necessary to achieve it in memory?
//
// The invariant we protect: total balance across ALL accounts must never change.
class BankAccount {
    private static final Object BANK_LOCK = new Object(); // shared across all accounts

    private long balance;

    BankAccount(long initialBalance) {
        this.balance = initialBalance;
    }

    // TODO: make this atomic — deduct `amount` from this account, add to `target`
    //       both operations must happen together under the same lock
    void transfer(BankAccount target, long amount) {
        synchronized (BANK_LOCK) {
            this.balance -= amount;
            target.balance += amount;
        }
    }

    long balance() {
        return balance;
    }
}

/*
 * KEY TAKEAWAYS
 *
 * synchronized on an instance method acquires the intrinsic lock (monitor) of `this` before
 * entering and releases it on exit — even if an exception is thrown. Only one thread can hold
 * that lock at a time; all other threads trying to enter any synchronized method on the same
 * object are blocked until the lock is released. This turns the method body into a critical
 * section: the read-modify-write of count++ becomes effectively atomic because no other thread
 * can interleave between the read and the write.
 *
 * Both increment() and get() must be synchronized on the same lock. If get() lacked synchronized,
 * a reader thread could race with a writer and observe a partially updated value. Sharing the same
 * intrinsic lock guarantees that every read sees a fully committed write.
 *
 * Cost: synchronized involves OS-level blocking and context switches under contention — it is
 * simple and correct but not the fastest option. For a plain counter, AtomicLong (Lesson03) is
 * preferred in high-throughput code.
 */
