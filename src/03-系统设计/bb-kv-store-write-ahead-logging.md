# WAL (Write Ahead Logging) 
WAL 保证了 atomicity and durability.

Write-Ahead Logging (WAL) is a standard method for ensuring data integrity.
used for crash and transaction recovery

Briefly, WAL's central concept is that changes to data files (where tables and indexes reside) must be written only after those changes have been logged, that is, after WAL records describing the changes have been flushed to permanent storage.

- 它的思想就是写入永久的数据库之前，先用cache记录下这个改变。积攒一些改变一起写入。(这个就叫chunck； trade off是 chunck size 大，提升throughput, 但是增加了 latency.)
- WAL is written to disk. (Disk 便宜 而且 WAL 是 sequential I/O 会 轻松达到500Mb/s以上的读写速度)
- PostgreSQL, MYSQL 等等都有类似的机制。
- 经常和 in memory db 配合使用, WAL 作为一个 failure recovery.

Common Features of WAL:

- Data Durability: By writing changes to the WAL before applying them to the database, you ensure that no changes are lost in case of a failure.
- Crash Recovery: After a crash, the WAL can be used to replay recent changes that haven’t been fully written to the database.
- Sequential I/O: WAL writes are sequential, which can be faster than random I/O operations on the main database, making WALs efficient for write-heavy workloads.

# KV Store and WAL 
因为你想快，所以用 in-memory kv store, 但是 in-memory 又很危险，一旦断电就全没了。所以用 WAL 作为一个 failure recovery 的手段。Crash Recovery。

In KV stores, data needs to be written and read quickly, but ensuring durability can be challenging due to the distributed and high-speed nature of these systems. A WAL helps mitigate these issues by providing a reliable mechanism to handle writes and recover from crashes.

每10分钟或者多久 做一个 snapshot, 断电之后就用 snapshot timestamp 之后的 WAL 就可以恢复 db 的状态。
Snapshot 可以 download 成一个 blob, 然后 cache 和 db read from 这个 blob. 这样就把读写的flow给分开了。which could be a cleaner design.


这个 snapshot service 关系到 data freshness 的限制，如果要求 1hour 可以这样。