# Mixed-Radix Encoding(混合进制编码)

把一组"每位取值范围不同"的离散变量 **双向映射** 到一个整数。
说人话: **每位的"进制"不一样的进制数**。

---

## 类比: 普通 10 进制

```
127  =  1 · 100  +  2 · 10  +  7 · 1
```

每位"权重"是 10 的幂(因为每位都是 10 进制)。

抽出每位:

```
digit_0 = 127 % 10 = 7,   剩 127 / 10 = 12
digit_1 =  12 % 10 = 2,   剩  12 / 10 =  1
digit_2 =   1 % 10 = 1,   剩   1 / 10 =  0
```

---

## 推广: 每位进制不同(mixed-radix)

假设有 3 位, 进制分别是 `[3, 2, 4]`(从高位到低位)。

```
N  =  d_2 · (2 · 4)   +   d_1 · 4   +   d_0 · 1
   =  d_2 · 8         +   d_1 · 4   +   d_0
```

- 第 0 位(最右): 进制 4, 权重 1
- 第 1 位: 进制 2, 权重 4
- 第 2 位(最左): 进制 3, 权重 8

总取值数 = `3 × 2 × 4 = 24`,所以 `N ∈ [0, 24)`。

---

## Encode: (d_2, d_1, d_0) → N

```java
long N = 0;
long weight = 1;
for (int i = digits.length - 1; i >= 0; i--) {
    N += digits[i] * weight;
    weight *= bases[i];
}
```

例: `(d_2=2, d_1=0, d_0=1)` → `N = 2·8 + 0·4 + 1·1 = 17`

---

## Decode: N → (d_2, d_1, d_0)

```java
long[] digits = new long[bases.length];
for (int i = bases.length - 1; i >= 0; i--) {
    digits[i] = N % bases[i];
    N /= bases[i];
}
```

例: `N = 17`
```
d_0 = 17 % 4 = 1,   剩 17 / 4 = 4
d_1 =  4 % 2 = 0,   剩  4 / 2 = 2
d_2 =  2 % 3 = 2,   剩  2 / 3 = 0
```
→ `(2, 0, 1)` ✓

**Encode 跟 decode 互为逆运算**, 是个双射(bijection)。

---

## 这是不是标准 CS 概念?

**是的, 而且非常底层。** 只是没有像 "Dijkstra" 那样有一个霸气的名字, 所以很多人不知道它叫 mixed-radix。

- **Knuth TAOCP Vol 2 §4.1** 专门讲 "Positional Number Systems",mixed-radix 是其中一节。
- **Lehmer code / factorial number system** ([wiki](https://en.wikipedia.org/wiki/Factorial_number_system)) —— 用 `[n!, (n-1)!, ..., 1!]` 当进制, 把 permutation 编号成整数。LC "find kth permutation" 用的就是这个。
- **Combinatorial number system / combinadic** —— 用 `[C(n,k), C(n,k-1), ...]` 当进制, 把 combination 编号。
- **真实世界 mixed-radix 例子**:
  - 时间:`秒(60) + 分(60) + 时(24) + 日(7)` —— 这就是个 mixed-radix 数
  - 角度:`60 秒 + 60 分 + 360 度`
  - 货币:旧英镑 `12 便士 + 20 先令 + 1 镑`(decimalisation 之前)
  - ISBN 第 13 位 check digit
  - 麻将牌编号(条万饼字花)
- **CS 应用**:
  - **DP 状态压缩** —— 多维状态打成一个 int 当 HashMap key
  - **bijective enumeration** —— 把"枚举所有组合"转成"枚举 int", 配合 Floyd's algorithm 做 distinct 抽样
  - **canonical form** —— 把树/图等离散结构编码成 int 做 hash / 去重
  - **CSP (constraint satisfaction)** —— variable assignment 编码

---

## 典型用法: 不重复随机选 k 个组合

问题: N 个 trait, 每个 trait 有若干 value, 不重复随机选 `k` 个 NFT。

```
1. total = ∏ values[i]                   ← mixed-radix 的总数
2. 从 [0, total) 选 k 个 distinct int     ← Floyd's algorithm, O(k)
3. 每个 int decode 成一组 trait value     ← mixed-radix decode
```

这样 **不撞车**、**不爆内存**、**O(k)**, 不管 k / total 比例多大都好用。

跟 retry-based rejection sampling 比, 优势在 `k ≈ total` 的退化场景。

参考 [Coinbase/NFT-Generation/](Coinbase/NFT-Generation/) `Part 2-Followup`。

---

## 易踩的坑

1. **overflow**: 多 trait × 多 value 会让 `total` 爆 long。用 `Math.multiplyExact` 检测, 大到装不下就直接走 rejection sampling(因为此时 `k << total`, 不会撞)。
2. **encode / decode 顺序必须一致**: 上面例子用 "右到左" 取余, encode 也得 "右到左" 累加。打个 unit test 跑 `decode(encode(x)) == x` 验证。
3. **bases 为 0 或 1 时的退化**: 某个 trait 只有 1 个 value, 进制 = 1, 这一位永远是 0 —— 不影响正确性, 只是"位数"变少。bases 里出现 0 就完蛋(总数 = 0),应当在输入校验时拒绝。
