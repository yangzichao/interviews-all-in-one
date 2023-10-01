又一个 Dijkstra 的好问题，虽然也可以暴力的dfs解一个。




```java
class Solution {
    public double maxProbability(int n, int[][] edges, double[] succProb, int start, int end) {
        boolean[] marked = new boolean[n];
        Map<Integer, Map<Integer, Double>> graph = getGraph(n, edges, succProb);
        PriorityQueue<Pair<Integer, Double>> heap = new PriorityQueue<Pair<Integer, Double>>((a, b) -> {
            if(a.getValue() == b.getValue()) return 0;
            return a.getValue() < b.getValue() ? 1 : -1;
        }); // 注意这里的 comparator 写法，由于comparator要求返回一个整型结果，所以不能直接 (a, b) -> b - a;

        heap.offer(new Pair(start, 1.)); // 注意，这里用 1.0或者1. 不然的话使用整型1会出错。
        while(!heap.isEmpty()){
            Pair<Integer, Double> curInfo = heap.poll();
            int curNode = curInfo.getKey();
            double curProb = curInfo.getValue();
            if(curNode == end){
                return curProb;
            }
            if(marked[curNode]) continue;
            marked[curNode] = true;
            for(int nextNode : graph.get(curNode).keySet()){
                double nextProb = curProb * graph.get(curNode).get(nextNode);
                heap.offer(new Pair(nextNode, nextProb));
            }
        }
        return 0;
    }

    private Map<Integer, Map<Integer, Double>> getGraph(int n, int[][] edges, double[] succProb){
        Map<Integer, Map<Integer, Double>> graph = new HashMap<>();
        for(int i = 0; i < n; i++){
            graph.putIfAbsent(i, new HashMap<>());
        }
        for(int i = 0; i < edges.length; i++){
            int v1      = edges[i][0];
            int v2      = edges[i][1];
            double prob = succProb[i];
            graph.get(v1).put(v2, prob);
            graph.get(v2).put(v1, prob);
        }
        return graph;
    }
}
```