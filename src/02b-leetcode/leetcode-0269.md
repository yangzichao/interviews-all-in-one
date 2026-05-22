终于写了这个传说中的 Alien Dictionary, 其实就是一个简单的 拓扑排序的问题 




```java

import java.lang.reflect.Array;class Solution {
    public String alienOrder(String[] words) {
        // Topological sort of the characters

        // get edges of the graph, and indegree
        Map<Character, Set<Character>> map = new HashMap<>();
        int[] indegrees = new int[26];
        for (int i = 1; i < words.length; i++) {
            String prevWord = words[i - 1];
            String curWord = words[i];

            // edge case like [abc, ab] is invalid, should return "" immediately
            if (prevWord.length() > curWord.length() && prevWord.startsWith(curWord)) {
                return ""; 
            }
            for (int j = 0; j < curWord.length() && j < prevWord.length(); j++) {
                if (prevWord.charAt(j) == curWord.charAt(j)) continue;
                map.putIfAbsent(prevWord.charAt(j), new HashSet<>());
                if (!map.get(prevWord.charAt(j)).contains(curWord.charAt(j))) {
                    map.get(prevWord.charAt(j)).add(curWord.charAt(j));
                    indegrees[(int) (curWord.charAt(j) - 'a')]++;
                }
                break;
            }
        }

        // get full char set
        Set<Character> charSet = new HashSet<>();
        for (String word : words) {
            for (char c : word.toCharArray()) charSet.add(c);
        }

        // enqueue
        List<Character> queue = new ArrayList<>();
        for (int i = 0; i < 26; i++) {
            char c = (char) ('a' + i);
            if (indegrees[i] == 0 && charSet.contains(c)) {
                queue.add(c);
            }
        }



        for (int i = 0; i < queue.size(); i++) {
            char cur = queue.get(i);
            if (!map.containsKey(cur)) continue;
            for (char next : map.get(cur)) {
                indegrees[(int) (next - 'a')] -= 1;
                if (indegrees[(int) (next - 'a')] == 0) {
                    queue.add(next);
                }
            }
        }

        // has Cycle
        if (queue.size() != charSet.size()) return "";

        // parseAnswer
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < queue.size(); i++) sb.append(queue.get(i));
        return sb.toString();
    }
}
```