
这个题非常好，非常非常好



```java
class Solution {
    public int[] exclusiveTime(int n, List<String> logs) {
        // 以下是理解错误的版本 以为就是简单的stack模拟
        // int[] exclusiveTimes = new int[n];
        // Deque<int[]> stack = new ArrayDeque<>(); // 0: id, 1: timestamp
        
        // for (String log : logs) {
        //     String[] parsed = log.split(":");
        //     int funcId = Integer.valueOf(parsed[0]);
        //     int timestamp = Integer.valueOf(parsed[2]);
        //     if (parsed[1].equals("end")) {
        //         int[] cur = stack.pop();
        //         exclusiveTimes[funcId] += (timestamp - cur[1]);
        //     } else {
        //         stack.push(new int[]{funcId, timestamp});
        //     }
        // }
        // return exclusiveTimes;

        //  这个问题 首先注意到这个 log 对函数的树状结构
        //  其次注意到 父节点的执行时间要减去所有子节点的执行时间
    
        int[] exclusiveTimes = new int[n];
        Deque<int[]> stack = new ArrayDeque<>(); // 0: id, 1: timestamp, 2: child nodes intervals
        
        for (String log : logs) {
            String[] parsed = log.split(":");
            int funcId = Integer.valueOf(parsed[0]);
            int timestamp = Integer.valueOf(parsed[2]);
            if (parsed[1].equals("end")) { // 结算当前节点
                int[] cur = stack.pop();   
                int timediff =  (timestamp - cur[1] + 1); //计算时间区间
                if (!stack.isEmpty()) {  // 这一步是把当前子节点的区间给到父节点上
                    int[] curTop = stack.pop(); 
                    curTop[2] += timediff;
                    stack.push(curTop);
                }

                exclusiveTimes[funcId] += (timediff - cur[2]); // 当前时间区间减去子节点区间就是净时间
            } else {
                stack.push(new int[]{funcId, timestamp, 0});
            }
        }
        return exclusiveTimes;
     }
}
```