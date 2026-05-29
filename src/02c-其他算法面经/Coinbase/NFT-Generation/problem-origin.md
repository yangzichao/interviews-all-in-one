# NFT Generation — 题目原型与参考

> 摘自 [`../coinbase-vo-problem-origins.md`](../coinbase-vo-problem-origins.md) 第 2 题。
> Coinbase 89 篇面经里出现 12 次 ⭐⭐(组合题),9 道 VO 题里频率第二高。

## Coinbase 题面

输入 `{size, traits: [{type, values}, ...]}`。要求:
1. 生成 size 个对象,每个对每种 trait 随机选一个 value
2. 不能重复
3. 每个 value 有数量上限,到达后不能再选
4. 带 weighted probability 的变体(e.g. 尖耳朵 60% / 宽耳朵 40%)

## 经典对照题 (LeetCode + 类似题)

- **LC 528: Random Pick with Weight** ([link](https://leetcode.com/problems/random-pick-with-weight/)) — 加权随机的标准做法(prefix sum + binary search),直接对应 weighted variant。
- **LC 710: Random Pick with Blacklist** — uniqueness / rejection sampling 思路,对应 (2) 不能重复。
- **Glassdoor 直接命中** ([link](https://www.glassdoor.com/Interview/Design-iterator-build-NFTs-with-random-values-for-traits-QTN_7326665.htm)) — "Design iterator, build NFTs with random values for traits" —— 印证这道题是 Coinbase 高频 VO。

## 真实工程实现 / 深度资源

- **HashLips/hashlips_art_engine** ([link](https://github.com/HashLips/hashlips_art_engine)) — 业界最知名的 NFT 生成器(>7k star)。文件名后缀 `#weight` 实现权重;核心算法 = "按权重抽样 + DNA hash 去重 + 达到 editionCount 停止",与面试三要素一一对应。**强烈建议通读 `src/main.js` 的 `createDna()` 和 `isDnaUnique()`**。
- **nftchef/art-engine** ([link](https://github.com/nftchef/art-engine)) — HashLips 进阶 fork,引入 "incompatible traits / required traits" 约束,正好对应"某些组合不能一起出现"的 follow-up。
- **Rounak Banik — Tutorial: Create Generative NFT Art with Rarities** ([link](https://medium.com/scrappy-squirrels/tutorial-create-generative-nft-art-with-rarities-8ee6ce843133)) — 面经里提到的那篇 tutorial 原文。"Cheetah weight=5 vs no-band weight=100 → 20 倍稀有度"这种直观例子,配套 Python 库。
- **rounakbanik/generative-art-nft `nft.py`** ([link](https://github.com/rounakbanik/generative-art-nft/blob/master/nft.py)) — 用 `numpy.random.choice(traits, p=normalized_weights)` 一行实现加权抽样,set 去重,循环到 `TOTAL_IMAGES`——几乎是面试题的标准答案模板。
- **surgewomen — Generative Art Algorithms** ([link](https://www.surgewomen.io/learn-about-web3/generative-art-algorithms-how-to-build-an-nft-collection)) — 工程视角讨论 rejection sampling vs enumeration 的折中:小集合用 rejection sampling 简单够用,大集合或低稀有度组合多时要切换到"枚举所有组合再洗牌"或 decrement-pool。
- **Coinbase NFT Dapp Starter Kit** ([link](https://github.com/coinbase/nft-dapp-starter-kit)) — Coinbase 官方仓库,含 metadata 生成脚本和 merkle allowlist,说明 Coinbase 内部确实关心 NFT pipeline,面试出这题有业务动机。

## 评估

**Coinbase 半自创**——没有单一 LC 编号覆盖全部要求,但每个子需求都是经典:weighted random = LC 528,dedupe = hash set / rejection sampling,capacity 上限 = LC 710 风格的"动态可选集合维护"。Coinbase 把它们包装成贴合自家业务(NFT mint)的复合题。
