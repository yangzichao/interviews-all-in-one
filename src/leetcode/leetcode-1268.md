In real world people prefer trie + cache (cache top results in each TrieNode) to achieve O(1) query. You build/update trie once in a few hours but will be queried a few million times. It is more important to have low query latency than have most up-to-date suggestion. 



