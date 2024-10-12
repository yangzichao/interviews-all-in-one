# Elasticsearch 是一个  full-text search engine

解决的问题：Queries to search for events based on keywords in the name, description, or other fields will require a full table scan because of the wildcard in the LIKE clause. This can be very slow, especially as the number of events grows.
即 传统的模糊搜索 很慢。因此达不到 low latency.

这个细节很难，我们可以略过。就提一嘴，fuzzy search 需要用到 full-text search engine.


Elasticsearch is a distributed, open-source search and analytics engine built on top of Apache Lucene. 

