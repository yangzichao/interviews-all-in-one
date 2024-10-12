

```java
class Solution {
    public List<Integer> findAnagrams(String s, String p) {
        int windowSize = p.length();
        if (s.length() < windowSize) return new ArrayList<>();
        // initialize the window
        Map<Character, Integer> freqCountOfWindow = new HashMap<>();
        for (int i = 0; i < windowSize - 1; i++) {
            freqCountOfWindow.put(s.charAt(i), freqCountOfWindow.getOrDefault(s.charAt(i), 0) + 1);
        }
        Map<Character, Integer> pFreqCount = getCharFrequencyCount(p);
        List<Integer> ans = new ArrayList<>();
        for (int i = 0; i < s.length() - windowSize + 1; i++) {
            int right = i + windowSize - 1;
            char rightChar = s.charAt(right);
            freqCountOfWindow.put(rightChar, freqCountOfWindow.getOrDefault(rightChar, 0) + 1);
            if (i > 0) {
                char lastCharToRemove = s.charAt(i - 1);
                freqCountOfWindow.put(lastCharToRemove, freqCountOfWindow.get(lastCharToRemove) - 1);
                if (freqCountOfWindow.get(lastCharToRemove).compareTo(0) == 0) {
                    freqCountOfWindow.remove(lastCharToRemove);
                }
            }
            if (areCharFrequencyCountTheSame(freqCountOfWindow, pFreqCount)) {
                ans.add(i);
            }
        }
        return ans;
    }

    private boolean areCharFrequencyCountTheSame(Map<Character, Integer> map1, Map<Character, Integer> map2) {
        if (map1.size() != map2.size()) return false;
        for (char key : map1.keySet()) {
            if (!map2.containsKey(key)) return false;
            if (map2.get(key).compareTo(map1.get(key)) != 0) return false;
        }
        return true;
    }
    // 注意 上面可以用 map 的 equals 直接代替

    private Map<Character, Integer> getCharFrequencyCount(String s) {
        Map<Character, Integer> charFrequencyCount = new HashMap<>();
        for (char c : s.toCharArray()) {
            charFrequencyCount.put(c, charFrequencyCount.getOrDefault(c, 0) + 1);
        }
        return charFrequencyCount;
    }

}
```