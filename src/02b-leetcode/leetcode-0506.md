



```java
class Solution {
    public String[] findRelativeRanks(int[] score) {
        PriorityQueue<int[]> info = new PriorityQueue<>((a, b) -> b[0] - a[0]);
        for (int i = 0; i < score.length; i++) {
            info.offer(new int[]{score[i], i});
        }
        String[] ans = new String[score.length];
        int index = 0;
        while (!info.isEmpty()) {
            int[] cur = info.poll();
            switch (index) {
                case 0:
                    ans[cur[1]] = "Gold Medal";
                    break;
                case 1:
                    ans[cur[1]] = "Silver Medal";
                    break;
                case 2:
                    ans[cur[1]] = "Bronze Medal";
                    break;
                default:
                    ans[cur[1]] = String.valueOf(index + 1);
            }
            index++;
        }
        return ans;
    }
}
```