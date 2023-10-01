

再看到这个题就不要浪费时间了，很没有意思。
leetcode在这里的描述非常不清晰不准确。可以说是第一大烂题。

竟然花了这么久写了这个垃圾题，卧槽。

```java
class Solution {
    public List<String> mostVisitedPattern(String[] username, int[] timestamp, String[] website) {
        Map<String, List<Pair<Integer, String>>> userStats = new HashMap<>();
        for (int i = 0; i < username.length; i++) {
            userStats.putIfAbsent(username[i], new ArrayList<>());
            userStats.get(username[i]).add(new Pair<>(timestamp[i], website[i]));
        }
        Map<String, Integer> patternCounts = new HashMap<>();
        int maxScore = 0;
        String candidate = "";

        for (String user : userStats.keySet()) {
            List<Pair<Integer, String>> list = userStats.get(user);
            if (list.size() < 3) continue;
            Collections.sort(list, (a, b) -> a.getKey() - b.getKey());
            Set<String> userPatternSet = new HashSet<>();
            for (int i = 0; i < list.size() - 2; i++) {
                for (int j = i + 1; j < list.size() - 1; j++) {
                    for (int k = j + 1; k < list.size(); k++) {
                        int[] patternIndics = new int[]{i, j, k};
                        String patternString = getPatternString(list, patternIndics);
                        userPatternSet.add(patternString);
                    }
                }
            }

            for (String pattern : userPatternSet) {
                int patternScore = patternCounts.getOrDefault(pattern, 0) + 1;
                patternCounts.put(pattern, patternScore);
                if (patternScore > maxScore) {
                    maxScore = patternScore;
                    candidate = pattern;
                }
                if (patternScore == maxScore) {
                    candidate = candidate.compareTo(pattern) < 0 ? candidate : pattern;
                }
            }
        }
        
        return Arrays.asList(candidate.split(" "));
    }

    private String getPatternString(List<Pair<Integer, String>> list, int[] patternIndics) {
        StringBuilder sb = new StringBuilder();
        int count = patternIndics.length;
        for (int index : patternIndics) {
            sb.append(list.get(index).getValue());
            count--;
            if (count > 0) sb.append(' ');
        }
        return sb.toString();
    }
}
```