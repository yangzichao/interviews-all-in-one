这个题很像是一个 dp 对吧，但是首先可以至少想到用 bfs.
dfs 是不行的，容易直接跑到太远然后回不来。 bfs 还行吧，但是由于
指数增长的时间和空间复杂度，稍微大一点就会 TLE 或者 MLE.

```java
class Solution {
    private int speed;
    private int pos;
    public int racecar(int target) {
        if(target == 0) return 0;
        this.speed = 1;
        this.pos = 0;
        ArrayDeque<int[]> infoQueue = new ArrayDeque<>(); // 0: pos, 1: speed
        infoQueue.offer(new int[]{pos, speed});
        boolean isArrive = false;
        int operations = 0;
        while(!infoQueue.isEmpty()){
            int size = infoQueue.size();
            while(size > 0){
                int[] curInfo = infoQueue.poll();
                int curPos = curInfo[0];
                int curSpeed = curInfo[1];
                if(curPos == target){
                    return operations;
                }
                infoQueue.offer(new int[]{curPos + curSpeed, 2 * curSpeed});  // 加速
                infoQueue.offer(new int[]{curPos, curSpeed > 0 ? -1 : 1});   // 反向
                size--;
            }
            operations += 1;
        }
        return operations;
    }
}
```

下面的代码有两处提升，一个是，用 Map 记录，如果同一个地方出现过同样的速度，那么我们就不用再考虑了。  
这是一个很好的提升。  
还有一个地方是剪枝，请看 comments:

```java
class Solution {
    private ArrayDeque<int[]> infoQueue;
    private Map<Integer, Set<Integer>> visited;

    public int racecar(int target) {
        this.infoQueue = new ArrayDeque<>();
        this.visited = new HashMap<>();

        infoQueue.offer(new int[]{0,0,1}); // init operations = 0, position = 0, speed = 1

        while(infoQueue.size() > 0) {
            int[] curInfo = infoQueue.poll();
            int operations = curInfo[0];
            int position = curInfo[1];
            int speed = curInfo[2];

            if (position == target) {
                return operations;
            }
            if (visited.containsKey(position) && visited.get(position).contains(speed)) {
                continue;
            }
            visited.putIfAbsent(position, new HashSet<>());
            visited.get(position).add(speed);
            // 这里有两个选择，我们的选择是，在没有超过target之前一直加速。
            // 只有加速超过了target，或者掉头开的过多了，我们才调转方向。
            infoQueue.offer(new int[]{operations + 1, position + speed, speed * 2 });
            if ((position + speed > target && speed > 0) || (position + speed < target && speed < 0)) {
                infoQueue.offer(new int[]{operations + 1, position, speed > 0 ? -1: 1 });
            }
        }
        return 0;
    }
}
```
