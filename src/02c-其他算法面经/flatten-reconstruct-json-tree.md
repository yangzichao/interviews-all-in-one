# Flatten & Reconstruct 任意嵌套树(JSON 编解码)

## 题意

输入是一棵 `list` / `dict` 任意互相嵌套的树,单值(scalar)就是叶子:

```python
[1, 2, [1, 2, [3]], {"a": 3, "b": [1, 3, 4]}]
```

要求两步:

1. **flatten** — 序列化成某种扁平表示
2. **reconstruct** — 从扁平表示**无损还原**出原结构(list/dict 类型、key、顺序、深度、空容器都要还原)

本质就是手写一个 JSON 的 encode/decode(面试一般不让你用 `json.dumps`)。

## 关键洞察

flatten 时必须把**结构信息**一起编码进去,否则还原不回来。容器有两种:

- `list`:有序,靠位置
- `dict`:无序(逻辑上),靠 key

如果只 flatten 出值,`[1, 2]` 和 `{"0": 1, "1": 2}` 会撞车。所以每个节点都要带一个 **type tag**:容器还要带"子节点数量"或边界。

---

## 方案 A:结构与值分离(推荐)

flatten 产出**两样东西**:

1. **skeleton** — 纯结构信息,前序遍历的 token 流;叶子只放占位符 `"V"`,**不带值**
2. **values** — 一个一维列表,前序顺序存放所有叶子的值

图解(以 `[1, 2, [3], {"a": 4}]` 为例):

```
原树
[1, 2, [3], {"a": 4}]
 │  │   │      │
 1  2  [3]   {"a": 4}
        │       │
        3       4

         flatten 一拆为二
        ┌──────────────┴──────────────┐
   skeleton(纯结构)              values(纯值,前序)
   L,4                            [1, 2, 3, 4]
   ├─ V          ← 占位
   ├─ V          ← 占位
   ├─ L,1
   │  └─ V       ← 占位
   └─ D,1
      └─ K"a"
         └─ V    ← 占位

         reconstruct 合二为一
   遍历 skeleton,每遇到一个 V 就从 values 顺序弹一个填回
   → [1, 2, [3], {"a": 4}]
```

token 设计(沿用 LeetCode 428 的 child-count 思路,无歧义、单遍重建、支持空容器):

- 容器 list → `("L", n)`,后面紧跟 n 个子节点的 token
- 容器 dict → `("D", n)`,后面紧跟 n 组 `("K", key)` + 子节点 token
- 叶子 → `"V"`(占位符,值另存进 `values`)

注意:**key 属于结构**(它定义 shape),留在 skeleton 里;只有叶子 scalar 才进 `values`。

```python
def flatten(node):
    """前序遍历,把结构和值拆成两份。"""
    skeleton = []   # 纯结构 + 叶子占位符
    values = []     # 纯值,前序顺序

    def walk(n):
        if isinstance(n, list):
            skeleton.append(("L", len(n)))
            for child in n:
                walk(child)
        elif isinstance(n, dict):
            skeleton.append(("D", len(n)))
            for key, value in n.items():
                skeleton.append(("K", key))
                walk(value)
        else:                          # scalar 叶子
            skeleton.append("V")       # 占位,不带值
            values.append(n)

    walk(node)
    return skeleton, values


def reconstruct(skeleton, values):
    """遍历 skeleton,遇到占位符就从 values 顺序取值填回。"""
    cursor = 0   # skeleton 游标
    vi = 0       # values 游标

    def build():
        nonlocal cursor, vi
        token = skeleton[cursor]
        cursor += 1
        if token == "V":               # 叶子占位 → 取一个值
            value = values[vi]
            vi += 1
            return value
        tag, payload = token
        if tag == "L":
            return [build() for _ in range(payload)]
        # tag == "D"
        result = {}
        for _ in range(payload):
            _, key = skeleton[cursor]   # ("K", key)
            cursor += 1
            result[key] = build()
        return result

    return build()
```

为什么 child count 比"分隔符 / 括号"好:不用处理转义、不用回溯找匹配括号,读到 `("L", 3)` 就知道接下来恰好 3 个子树,一遍扫完。

值列表分离出来后,可以单独整批处理再塞回:

```python
skeleton, values = flatten(tree)
values = [transform(v) for v in values]   # 脱敏 / 翻译 / 加密,结构不动
new_tree = reconstruct(skeleton, values)
```

### 边界 case 都被覆盖

- 空容器:`[]` → skeleton `[("L", 0)]`、values `[]`,build 时循环 0 次,正确还原
- 整数 key 的 dict:`{0: "x"}` 的 key 原样存进 `("K", 0)`,不会和 list index 混淆
- 根节点就是 scalar:`5` → skeleton `["V"]`、values `[5]`,也能还原

---

## 方案 B:path-based flatten(字面意义的"拍平成 path→value")

如果面试官要的是 `(path, value)` 形式(常见于"flatten a dictionary"那类题):

```
[1, 2, [1, 2, [3]], {"a": 3, "b": [1, 3, 4]}]
↓
(0,)         -> 1
(1,)         -> 2
(2, 0)       -> 1
(2, 1)       -> 2
(2, 2, 0)    -> 3
(3, "a")     -> 3
(3, "b", 0)  -> 1
(3, "b", 1)  -> 3
(3, "b", 2)  -> 4
```

path 的每一段:`int` 段 → 父节点是 list,`str` 段 → 父节点是 dict。还原时按 path 逐层建容器。

**但 path-based 有两个坑,面试要主动说出来:**

1. **空容器丢失**。`[]` / `{}` 没有任何叶子,就不会产生任何 path,还原时凭空消失。要修就得额外为空容器记一条"占位 path"。
2. **类型歧义**。如果 dict 的 key 恰好是整数(`{0: ...}`),光看 path 段没法区分它和 list index。要修就得在每段里带上类型标记(等于退化成方案 A 的思路)。

所以 path-based 适合"key 都是字符串、不关心空容器"的简化版;通用 JSON 还原优先方案 A。

---

## 复杂度

| | flatten | reconstruct |
|---|---|---|
| 时间 | O(N) | O(N) |
| 空间 | O(N) token + O(H) 递归栈 | O(H) 递归栈 |

N = 节点总数,H = 树高(深嵌套时注意递归深度,可改显式栈)。

---

## 自测

```python
def _roundtrip(x):
    skeleton, values = flatten(x)
    assert reconstruct(skeleton, values) == x, x

if __name__ == "__main__":
    cases = [
        [1, 2, [1, 2, [3]], {"a": 3, "b": [1, 3, 4]}],
        [],
        {},
        5,
        {"x": [], "y": {}, "z": {0: "int-key"}},
        [[[[1]]]],
    ]
    for c in cases:
        _roundtrip(c)
    print("all passed")
```
