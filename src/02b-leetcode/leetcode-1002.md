这个题其实不是 easy 绝对是 medium了。挺好的一个题。

```java
class Solution {
    public List<String> commonChars(String[] words) {
        int[] curCount = getStrCount(words[0]);
        for (int i = 1; i < words.length; i++) {
            curCount = intersect(curCount, getStrCount(words[i]));
        }
        List<String> ans = new ArrayList<>();
        for (int i = 0; i < 26; i++) {
            int k = curCount[i];
            while (k > 0) {
                k--;
                ans.add(String.valueOf((char) (i + 'a')));
            }
        }
        return ans;
    }
    private int[] intersect(int[] count1, int[] count2) {
        for (int i = 0; i < 26; i++) {
            count1[i] = Math.min(count1[i], count2[i]);
        }
        return count1;
    }
    private int[] getStrCount(String word) {
        int[] count = new int[26];
        for (char c : word.toCharArray()) {
            count[c - 'a']++;
        }
        return count;
    }
}
```