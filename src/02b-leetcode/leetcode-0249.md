

这个题我还挺骄傲的 竟然是我自己独立想出来的。

idea, use the "diff" between any 2 consecutive chars of a string as a key 

就 类似于 abc 转化为 11
az 转化为 -25 但是由于可以 旋转 总之这个题目不好，学一下这个idea就可以了


```java
class Solution {
    public List<List<String>> groupStrings(String[] strings) {
        // idea, use the "diff" between any 2 consecutive chars of a string as a key 
        Map<String, List<String>> map = new HashMap<>();
        for (String string : strings) {
            String key = parseKey(string);
            map.putIfAbsent(key, new ArrayList<>());
            map.get(key).add(string);
        }
        List<List<String>> ans = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            ans.add(entry.getValue());
        }
        return ans;
    }

    private String parseKey(String s) {
        if (s.length() == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < s.length(); i++) {
            int diff = (int) (s.charAt(i - 1) - s.charAt(i) + 26) % 26;
            sb.append(diff).append("-");
            System.out.println(String.valueOf(diff));
        }
        return sb.toString();
    }
}
```