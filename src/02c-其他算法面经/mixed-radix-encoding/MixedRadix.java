import java.util.*;

/**
 * 4-part practice — Mixed-Radix Encoding.
 *
 * 每个 Part 是独立的 static method, 互不依赖.
 * 先无脑独立写, 做完再讨论复用.
 *
 * 详细 spec 见 README.md. 卡住翻 hints.md.
 */
public class MixedRadix {

    // ====================================================================
    // PART 1  —  encode(digits, bases) → N                          [⚠ TODO]
    // ====================================================================
    //
    // 把每位的 digit 数组 (digits) 在对应进制 (bases) 下编码成一个非负 long.
    //
    // 约定: digits 和 bases 同长度 L >= 1 (caller 保证非空).
    //       bases[i] >= 1.
    //       0 <= digits[i] < bases[i].
    //       第 0 位是 "最高位", 最后一位是 "个位".
    //
    //   bases  = [3, 2, 4]   // 高 → 低
    //   digits = [2, 0, 1]
    //   N      = 2 · (2·4) + 0 · 4 + 1 · 1  =  17
    //
    //   bases  = [10, 10, 10]
    //   digits = [1, 2, 7]
    //   N      = 127
    //
    // 边界:
    //   - L == 1 → N = digits[0]
    //
    // 不需要处理:
    //   - 空数组 (caller 保证 L >= 1)
    //   - 输入长度不一致
    //   - digit 越界
    //   - 总数 overflow (假设乘积装得下 long)

    public static long encode(int[] digits, int[] bases) {
        int N = bases.length;
        long[] multipliers = new long[N];
        multipliers[N - 1] = 1;
        for (int i = N - 2; i >= 0; i--) {
            multipliers[i] = multipliers[i + 1] * bases[i + 1];
        }
        long num = 0;
        for (int i = 0; i < N; i++) {
            num += digits[i] * multipliers[i];
        }
        return num;
    }

    // ====================================================================
    // PART 2  —  decode(N, bases) → digits                          [⚠ TODO]
    // ====================================================================
    //
    // Part 1 的逆运算. 把 N 在 bases 下拆回每位的 digit 数组.
    //
    //   N      = 17, bases = [3, 2, 4]
    //   digits = [2, 0, 1]
    //
    //   N      = 127, bases = [10, 10, 10]
    //   digits = [1, 2, 7]
    //
    // 约定:
    //   bases.length >= 1 (caller 保证非空).
    //   返回 int[] 长度 == bases.length, 高位在前.
    //   0 <= N < ∏ bases[i] (假设 caller 保证).
    //
    // 边界:
    //   - N = 0 → 返回全 0
    //
    // 验收:
    //   - decode(encode(d, b), b) == d  对所有合法 (d, b)

    public static int[] decode(long n, int[] bases) {
        int N = bases.length;
        long[] multipliers = new long[N];
        multipliers[N - 1] = 1;
        for (int i = N - 2; i >= 0; i--) {
            multipliers[i] = multipliers[i + 1] * bases[i + 1];
        }

        int[] decoded = new int[N];
        for (int i = 0; i < N; i++) {
            decoded[i] = (int) (n / multipliers[i]);
            n %= multipliers[i];
        }
        return decoded;
    }

    // ====================================================================
    // PART 3  —  kthCombination(values, k) → 第 k 个组合              [⚠ TODO]
    // ====================================================================
    //
    // 给定 N 个有序列表 values[0..N), 它们的笛卡尔积 (Cartesian product) 一共
    // 有 total = ∏ values[i].size() 个组合.
    // 把这 total 个组合按 "高位变化最慢, 低位变化最快" 编号 [0, total), 返回第 k 个.
    //
    //   values = [["a","b","c"], ["x","y"]]
    //   total  = 3 × 2 = 6
    //   编号 (k = 0..5):
    //     0 → [a, x]
    //     1 → [a, y]
    //     2 → [b, x]
    //     3 → [b, y]
    //     4 → [c, x]
    //     5 → [c, y]
    //
    // 提示: 把每个 values[i] 当 mixed-radix 的一位, 复用 Part 2 的 decode.
    //
    // 约定:
    //   - values.size() >= 1, 每个 values[i] 非空 (caller 保证)
    //
    // 不需要处理: k 越界, values 为 null, 空 list

    public static <T> List<T> kthCombination(List<List<T>> values, long k) {
        int N = values.size();
        // bases = 每个 list 的长度
        int[] bases = new int[N];
        for (int i = 0; i < N; i++) bases[i] = values.get(i).size();
        // 复用 Part 2: decode k 拿到每位 index
        int[] indices = decode(k, bases);
        // 按 index 从每个 list 取一个
        List<T> result = new ArrayList<>(N);
        for (int i = 0; i < N; i++) result.add(values.get(i).get(indices[i]));
        return result;
    }

    // ====================================================================
    // PART 4  —  kthPermutation(n, k) → [1..n] 的第 k 个 permutation  [⚠ TODO]
    // ====================================================================
    //
    // 经典 LC 60. 给 n, 返回 [1, 2, ..., n] 的字典序第 k 个 permutation (0-indexed).
    //
    //   n = 3, k = 0 → [1, 2, 3]
    //   n = 3, k = 1 → [1, 3, 2]
    //   n = 3, k = 2 → [2, 1, 3]
    //   n = 3, k = 3 → [2, 3, 1]
    //   n = 3, k = 4 → [3, 1, 2]
    //   n = 3, k = 5 → [3, 2, 1]
    //
    // 提示: 这是 mixed-radix 的另一个 famous instance —— Lehmer code / factorial number system.
    //       每位的进制是 (n-1)!, (n-2)!, ..., 1!, 0!  → 跟 Part 1/2 完全同构.
    //
    // 约定: n >= 1, 0 <= k < n!  (n 最多 12 左右, 否则 n! 爆 int)
    //
    // 边界:
    //   - n = 1 → 永远返回 [1]
    //   - n = 0 → 不用处理

    public static int[] kthPermutation(int n, int k) {
        // factorial weights: fact[i] = (n-1-i)!
        //   n=4 → fact = [6, 2, 1, 1]  (3!, 2!, 1!, 0!)
        int[] fact = new int[n];
        fact[n - 1] = 1;
        for (int i = n - 2; i >= 0; i--) fact[i] = fact[i + 1] * (n - 1 - i);

        // 可选数字池
        List<Integer> available = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) available.add(i);

        // decode k 并从 available 里逐位 remove
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            int digit = k / fact[i];
            k %= fact[i];
            result[i] = available.remove(digit);
        }
        return result;
    }
}
