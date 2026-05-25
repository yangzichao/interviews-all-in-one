# Mixed-Radix — Hints (写之前别看)

每节按从轻到重排, 只看到能让你过下一步的那行就停。

---

## Part 1 — encode

<details>
<summary>提示 1: 公式</summary>

`N = digits[0]·(bases[1]·bases[2]·...·bases[L-1]) + digits[1]·(...) + ... + digits[L-1]·1`

</details>

<details>
<summary>提示 2: 怎么算各位的权重</summary>

从最右(个位)往左扫: weight 从 1 开始, 每过一位 `weight *= bases[i+1]`。
或者从左往右扫: 先算总积, 然后每位除掉自己进制。

</details>

<details>
<summary>提示 3: 朴素实现</summary>

```java
long N = 0;
long weight = 1;
for (int i = bases.length - 1; i >= 0; i--) {
    N += digits[i] * weight;
    weight *= bases[i];
}
return N;
```

</details>

---

## Part 2 — decode

<details>
<summary>提示 1: 逆运算</summary>

encode 是"乘加", decode 是"除模"。每次取 `N % bases[i]` 拿当前位, 然后 `N /= bases[i]` 推进。

</details>

<details>
<summary>提示 2: 哪个方向</summary>

如果 encode 是 "右到左累加" (个位权重 1), decode 也得 "右到左取模"。一致就行。

</details>

<details>
<summary>提示 3: 朴素实现</summary>

```java
int[] digits = new int[bases.length];
for (int i = bases.length - 1; i >= 0; i--) {
    digits[i] = (int)(n % bases[i]);
    n /= bases[i];
}
return digits;
```

</details>

---

## Part 3 — kthCombination

<details>
<summary>提示 1: 直接复用 Part 2</summary>

把每个 `values[i].size()` 当成 bases[i], 直接 `decode(k, bases)` 拿到每位的 index, 再去 values 里查。

</details>

<details>
<summary>提示 2: 实现</summary>

```java
int[] bases = new int[values.size()];
for (int i = 0; i < bases.length; i++) bases[i] = values.get(i).size();
int[] idx = decode(k, bases);
List<T> result = new ArrayList<>();
for (int i = 0; i < bases.length; i++) result.add(values.get(i).get(idx[i]));
return result;
```

</details>

---

## Part 4 — kthPermutation (LC 60, Lehmer code)

<details>
<summary>提示 1: factorial number system</summary>

permutation 编号用的"进制"不是常数, 而是 `(n-1)!, (n-2)!, ..., 1!, 0!`。
比如 n=4 的进制是 `[6, 2, 1, 1]` (因为 3!, 2!, 1!, 0!=1)。

每位 `digit[i]` 的合法范围是 `[0, n-i)`(因为剩 `n-i` 个数字可选)。

</details>

<details>
<summary>提示 2: digit → 实际数字</summary>

维护一个 "可用数字" 的 list `available = [1, 2, ..., n]`。
decode 出 digit[i] 之后, 从 available 里**取下标 digit[i] 的数字**(取走), 放入结果。

</details>

<details>
<summary>提示 3: 例子走一遍 (n=4, k=8)</summary>

```
bases = [6, 2, 1, 1]   ← 3!, 2!, 1!, 0!
k = 8
  d_0 = 8 / 6 = 1, 剩 8 % 6 = 2    (注意这是 "高位除", 不是 Part 2 的低位取模)
  d_1 = 2 / 2 = 1, 剩 0
  d_2 = 0 / 1 = 0, 剩 0
  d_3 = 0 / 1 = 0

available = [1, 2, 3, 4]
  d_0 = 1 → take index 1 → 2,  available = [1, 3, 4]
  d_1 = 1 → take index 1 → 3,  available = [1, 4]
  d_2 = 0 → take index 0 → 1,  available = [4]
  d_3 = 0 → take index 0 → 4,  available = []

result = [2, 3, 1, 4] ✓
```

注意: factorial 系统的 decode 是 **从高位往低位**, 跟 Part 2 方向**相反**。
也可以反着定义 bases, 让方向跟 Part 2 一致, 看你喜欢哪种。

</details>

<details>
<summary>提示 4: 完整实现</summary>

```java
public static int[] kthPermutation(int n, int k) {
    int[] fact = new int[n];
    fact[n - 1] = 1;
    for (int i = n - 2; i >= 0; i--) fact[i] = fact[i + 1] * (n - 1 - i);
    // fact[i] = (n-1-i)!,  e.g. n=4 → fact = [6, 2, 1, 1]

    List<Integer> available = new ArrayList<>();
    for (int i = 1; i <= n; i++) available.add(i);

    int[] result = new int[n];
    for (int i = 0; i < n; i++) {
        int d = k / fact[i];
        k %= fact[i];
        result[i] = available.remove(d);
    }
    return result;
}
```

</details>
