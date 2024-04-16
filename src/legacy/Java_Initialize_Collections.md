# 初始化Map 和 Set

```java
// 1
    private Map<Character, String> letters = Map.of(
        '2', "abc", '3', "def", '4', "ghi", '5', "jkl", 
        '6', "mno", '7', "pqrs", '8', "tuv", '9', "wxyz");
// 2
    static Map<Character, List<Character>> map = new HashMap<>();
    static{
        map.put('2', new ArrayList<Character>(Arrays.asList('a','b','c')));
        map.put('3', new ArrayList<Character>(Arrays.asList('d','e','f')));
        map.put('4', new ArrayList<Character>(Arrays.asList('g','h','i')));
        map.put('5', new ArrayList<Character>(Arrays.asList('j','k','l')));
        map.put('6', new ArrayList<Character>(Arrays.asList('m','n','o')));
        map.put('7', new ArrayList<Character>(Arrays.asList('p','q','r','s')));
        map.put('8', new ArrayList<Character>(Arrays.asList('t','u','v')));
        map.put('9', new ArrayList<Character>(Arrays.asList('w','x','y','z')));
    }
```