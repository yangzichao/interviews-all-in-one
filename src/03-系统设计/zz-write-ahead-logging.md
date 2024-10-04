# WAL (Write Ahead Logging) 
WAL 保证了 atomicity and durability.

Write-Ahead Logging (WAL) is a standard method for ensuring data integrity.
used for crash and transaction recovery

Briefly, WAL's central concept is that changes to data files (where tables and indexes reside) must be written only after those changes have been logged, that is, after WAL records describing the changes have been flushed to permanent storage.

- 它的思想就是写入永久的数据库之前，先用cache记录下这个改变。积攒一些改变一起写入。
- WAL is written to disk.
- PostgreSQL, MYSQL 等等都有类似的机制。
- 经常和 in memory db 配合使用, WAL 作为一个 failure recovery.
