base62 是很基础的算法，在 tinyURL 和 一次谷歌面经当中都被问到了。

base62 可以轻易改成 baseK 算法。
下面附上 decode 和 encode 的算法。

```java
import java.util.HashMap;
import java.util.Map;

public class Base62 {
    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = BASE62_CHARS.length();
    
    public static String encode(long num) {
        // encode 5282 -> 1xG
        // G -> Gx -> Gx1, reverse 得到 1xG
        if (num == 0) {
            return "" + BASE62_CHARS.charAt(0);
        }
        
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            int remainder = (int) (num % BASE);
            sb.append(BASE62_CHARS.charAt(remainder));
            num /= BASE;
        }
        
        return sb.reverse().toString();
    }
    
    public static long decode(String base62) {
        long result = 0;

        for (char c : base62.toCharArray()) {
            int charValue = BASE62_CHARS.indexOf(c);
            result = result * BASE + charValue;
        }
        
        return result;
    }
    
    public static void main(String[] args) {
        long num = 123456789;
        String encoded = encode(num);
        System.out.println("Encoded: " + encoded);
        
        long decoded = decode(encoded);
        System.out.println("Decoded: " + decoded);
    }
}

```