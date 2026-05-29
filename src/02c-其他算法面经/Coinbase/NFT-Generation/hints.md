# NFT Generation — Hints (写之前别看)

只有卡住才翻。每一节按从轻到重排,只看到能让你过下一步的那一行就停。

---

## Part 2 — No Duplicates

<details>
<summary>提示 1: 不可行判断</summary>

总组合数 = ∏(每个 trait 的 values 个数)。size > 这个数 → 物理不可能。

</details>

<details>
<summary>提示 2: 朴素方案</summary>

重复采样 Part 1,用 `Set<Map<String,String>>` 去重。命中就丢,继续抽。

</details>

<details>
<summary>提示 3: 朴素方案的退化点</summary>

当 size 接近总组合数,后期每次抽几乎都撞 → 退化到指数 retry。

</details>

<details>
<summary>提示 4: 替代方案</summary>

枚举所有组合 → `Collections.shuffle(list, random)` → 取前 size 个。
组合数大时不可行(组合爆炸),小时简单可靠。

</details>

<details>
<summary>提示 5: 切换阈值</summary>

实际工程:size / total < 50% 用 rejection sampling;> 50% 用 enumerate+shuffle。
面试时**先写一个,口头讨论另一个**,不必两个都实现。

</details>

---

## Part 3 — Per-Value Capacity Caps

<details>
<summary>提示 1: 预检</summary>

每个 trait 的 cap 总和 ≥ size,这是 trait 内必要条件(单个 trait 都凑不够 size 张就无解)。

</details>

<details>
<summary>提示 2: 运行时状态</summary>

每个 value 维护一个**剩余余量**(Map<String, Map<String, Integer>> 或类似)。
选某 value 前 check 余量 > 0,选完 -1。

</details>

<details>
<summary>提示 3: 跑着崩了怎么办</summary>

某轮发现所有 trait 都没合法 value(全 capped 完了又凑不出 distinct NFT)→ `IllegalStateException`。
**别返回部分结果**。

</details>

<details>
<summary>提示 4: 唯一性 + cap 同时满足的难点</summary>

cap 用完不代表无解 —— 可能换组合就行。
所以"卡住"的判断是:**当前 NFT 槽位**的所有 trait 候选都被 cap 排除,且换其他 NFT 也凑不出新 distinct → 才 throw。
简单版:rejection sampling 配合 retry 上限。

</details>

---

## Part 4 — Weighted Probabilities

<details>
<summary>提示 1: 数据结构</summary>

每个 trait 预算 **累积权重** `cum[i] = weights[0] + ... + weights[i]`。
`total = cum[last]`。

</details>

<details>
<summary>提示 2: 采样</summary>

`r = random.nextInt(total)` → 在 `cum` 上 binary search 找第一个 > r 的 index(或 `Collections.binarySearch` + 处理插入点)。

</details>

<details>
<summary>提示 3: 替代方案</summary>

- **直接展开 list**:`["common"]*9 + ["rare"]*1`,等概率取。权重小时最简单,大时爆内存。
- **alias method**(Walker):O(1) 采样,setup O(n)。面试一般不要求,口头提一句加分。

</details>

<details>
<summary>提示 4: 测试随机性</summary>

不要 assert 精确概率。
- 大样本(1000+)+ 松界限(期望 ±10%)测分布方向
- 或只测合法性 / 唯一性,把分布留作 sanity check
- 用固定 seed 让测试 deterministic

</details>

---

## 通用 Coinbase 风格 trade-off(写完再看)

- **不要堆复杂度**: rejection sampling 在 80% 情况下足够,面试官想看你**意识到** trade-off 而不是默写 alias method
- **deterministic 是硬要求**: 测试要 reproduce → seed 必须可控
- **错误优先 throw,不要返回半成品**: 金融场景默认 fail-fast
- **写之前问 1-2 个真问题**(size 量级、能否预枚举),不要为了 perform 而问
