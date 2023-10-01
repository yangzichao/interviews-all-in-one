这个题我不觉得是个很好的题目
因为直觉就是使用 hashmap + heap sort. 也可以价格 quick select 变得更快。
可以研究下 TreeMap 的解法。但是我并不觉得有必要，因为是一个 leaderboard.因此 log(n) 和 o(n) 并没有很巨大的差别。

下面是一个人总结的：
sort array when top() is called
addScore: O(1)
top: O(nlogn)
reset: O(1)

keep sorted LinkedList
addScore: O(n)
top: O(k) or O(n)
reset: O(1)

size k min heap when top() is called
addScore: O(1)
top: O(nlogk)
reset: O(1)

Balanced Binary Tree
addScore: O(log n) insrt to binaryTree
top: O(k) pre-order travel
reset: O(log n) del and maintain the tree

Quick select when top() is called
addScore: O(1)
top: avg O(n) worse O(n^2) like quick sort but only partition the top k we want
reset: O(1)



还是写了个 heap 的解法。
```java
class Leaderboard {
    Map<Integer, Integer> scores;
    public Leaderboard() {
        this.scores = new HashMap<>();
    }
    
    public void addScore(int playerId, int score) {
        scores.put(playerId, scores.getOrDefault(playerId, 0) + score);
    }
    
    public int top(int K) {
        PriorityQueue<Integer> topK = new PriorityQueue<>();
        for (int key : scores.keySet()) {
            topK.offer(scores.get(key));
            while (topK.size() > K) {
                topK.poll();
            }
        }
        int sum = 0;
        while (!topK.isEmpty()) sum += topK.poll();
        return sum;
    }
    
    public void reset(int playerId) {
        scores.put(playerId, 0);
    }
}

/**
 * Your Leaderboard object will be instantiated and called as such:
 * Leaderboard obj = new Leaderboard();
 * obj.addScore(playerId,score);
 * int param_2 = obj.top(K);
 * obj.reset(playerId);
 */
```




用 TreeMap 的写法也可以

```java
class Leaderboard {

    Map<Integer, Integer> scores;
    TreeMap<Integer, Integer> sortedScores;
    
    public Leaderboard() {
        this.scores = new HashMap<Integer, Integer>();
        this.sortedScores = new TreeMap<>((a, b) -> b - a);
    }
    
    public void addScore(int playerId, int score) {
        
        // The scores dictionary simply contains the mapping from the
        // playerId to their score. The sortedScores contain a BST with 
        // key as the score and value as the number of players that have
        // that score.        
        if (!scores.containsKey(playerId)) {
            scores.put(playerId, score);
            sortedScores.put(score, sortedScores.getOrDefault(score, 0) + 1);
        } else {
            int preScore = scores.get(playerId);
            int playerCount = sortedScores.get(preScore);

            if (playerCount == 1) {
                sortedScores.remove(preScore);
            } else {
                sortedScores.put(preScore, playerCount - 1);
            }
            
            int newScore = preScore + score;
            scores.put(playerId, newScore);
            sortedScores.put(newScore, sortedScores.getOrDefault(newScore, 0) + 1);
        }
    }
    
    public int top(int K) {
        int count = K;
        int sum = 0;
        for (Map.Entry<Integer, Integer> entry : sortedScores.entrySet()) {
            int key = entry.getKey();
            int times = entry.getValue();
            if (count - times >= 0) {
                sum = sum + key * times;
                count -= times;
            } else {
                sum += key * count;
                break;
            }
        }
        return sum;
    }
    
    public void reset(int playerId) {
        int preScore = scores.get(playerId);
        scores.remove(playerId);
        sortedScores.put(preScore, sortedScores.get(preScore) - 1);
        if (sortedScores.get(preScore) == 0) {
            sortedScores.remove(preScore);
        }
    }
}
```