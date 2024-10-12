
非常简单的题。不要多想。
```java
class Solution {
    public boolean isIsomorphic(String s, String t) {
        return isOneIsomorphic(s, t) && isOneIsomorphic(t, s);
    }

    public boolean isOneIsomorphic(String s, String t) {
        if (s.length() != t.length()) return false;
        Map<Character, Character> mapping = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            char sChar = s.charAt(i);
            if (mapping.containsKey(sChar)) {
                if (!mapping.get(sChar).equals(t.charAt(i))) return false;
            } else {
                mapping.put(sChar, t.charAt(i));
            }
        }
        
        return true;
    }
}
```