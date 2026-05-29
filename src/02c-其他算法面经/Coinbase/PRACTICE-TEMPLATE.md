# Coinbase 面试练习 Template

这份文档是**面试练习的统一约定**。所有 Coinbase 子题目（`Block-Mining/`, `Crypto-Order-System/`, ...）都按这个模板组织。新加题目、扩展 Part 5+ 都参考本文。

未来的 AI session 看到这份文档，应该能立刻接上工作而不破坏既有风格。

---

## 文件结构（每个题一个目录）

```
<Problem-Folder>/
├── README.md              # 题目说明 + 每 Part 的讨论
├── <Name>.java            # 单文件 scaffold，Part 1..N 都在里面
└── <Name>Test.java        # 单文件测试 runner，每 Part 一个 Runnable
```

**坚决不要**做的事：
- 拆成 `Part1.java`, `Part2.java`... 多文件 → 违反约定
- 给 stub 写"参考答案" → 违反"练习不剧透"原则
- 用框架（JUnit / TestNG）→ 这是裸 Java 练习，要能 `javac && java` 直接跑

---

## Java 文件结构

### 总体

```java
import java.util.*;

/**
 * 4-part Coinbase interview practice.
 * ... 一段说明 ...
 */
public class <Name> {
    // ====== PART 1 ====== [✓ DONE / ⚠ TODO]
    // ====== PART 2 ====== ...
    // ...
}
```

### 每个 Part 的 header

每个 Part 顶上必须有一段标准化 header，让读者一眼看到 schema 演化：

```java
// ====================================================================
// PART N  —  一句话标题                                         [状态]
// ====================================================================
// 与 Part N-1 比:
//   同: ...（什么沿用了，比如 EntryPart{N-1} 不变直接复用）
//   变: ...（schema 变了什么，引入新的 EntryPartN）
//   新: ...（新增的方法签名）
//
// 你要写的: ...（这个 Part 要实现的具体方法清单）
```

状态用 `[✓ DONE]` / `[⚠ TODO]` 标。

### Entry / map 命名约定（**最重要**）

- **只在 schema 真的变了才引入新的 `EntryPartN` 和 `mapPartN`**。
- 如果 Part N 的数据结构跟 Part N-1 完全一样（只是加了新方法、改了行为），**直接复用** `EntryPart{N-1}` 和 `mapPart{N-1}`，让 `putPartN/getPartN` 是 1 行 wrapper 套到上一 part：
  ```java
  public String getPartN(String key, int timestamp) {
      return getPart{N-1}(key, timestamp);
  }
  ```
- 这避免了重复定义同一个 record 5 遍。

### 方法命名

- 后缀化：`putPart1`, `getPart3`, `scanPart2`, `backupPart4`, ...
- 重载也按 Part 走：`putPart3(k,v,t)` 和 `putPart3(k,v,t,ttl)` 是同一 Part 的两个方法。
- 跨 Part 的辅助方法私有化并加后缀：`private boolean isExpiredPart3(...)`。

### Stub 的写法（**练习不剧透原则**）

未实现的 Part **必须** throw `UnsupportedOperationException` —— test runner 会把它识别为 `SKIPPED`。

```java
public int backupPartN(int timestamp) {
    throw new UnsupportedOperationException("TODO: Part N — 一句话提示，不给思路");
}
```

**不要**在 stub 里给算法提示。提示放 README，不放代码。

---

## Test 文件结构

```java
import java.util.*;

public class <Name>Test {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", <Name>Test::testPart1);
        tests.put("part2", <Name>Test::testPart2);
        // ... 加新 Part 时在这里加一行

        List<String> toRun = args.length == 0 ? new ArrayList<>(tests.keySet()) : Arrays.asList(args);
        for (String name : toRun) {
            Runnable t = tests.get(name);
            if (t == null) {
                System.out.println("unknown part: " + name + ", available: " + tests.keySet());
                System.exit(2);
            }
            run(name, t);
        }
        System.out.printf("%nPassed=%d  Failed=%d  Skipped=%d%n", passed, failed, skipped);
        if (failed > 0) System.exit(1);
    }

    static void run(String name, Runnable test) {
        String label = "Part " + name.substring(4);
        try {
            test.run();
            System.out.println(label + " PASSED");
            passed++;
        } catch (UnsupportedOperationException e) {
            System.out.println(label + " SKIPPED (not implemented)");
            skipped++;
        } catch (AssertionError e) {
            System.out.println(label + " FAILED: " + e.getMessage());
            failed++;
        } catch (Throwable e) {
            System.out.println(label + " ERROR: " + e);
            e.printStackTrace(System.out);
            failed++;
        }
    }

    static void assertEq(Object expected, Object actual, String msg) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(msg + " — expected: " + expected + ", actual: " + actual);
        }
    }

    // ====== 每 Part 的测试方法 ======
    static void testPart1() { /* ... */ }
    static void testPart2() { /* ... */ }
    // ...
}
```

跑测试：
```bash
javac <Name>.java <Name>Test.java
java <Name>Test                 # 跑全部
java <Name>Test part1 part2     # 只跑指定
```

---

## Part 划分原则

### Part 1–4：**面经原题**（interview transcript）

来自 PracHub / 网络面经 / 用户口述的实际 4-part 渐进设计题。
**保真还原**——不要改题目难度、API 签名、边界条件。

### Part 5+：**超越面经的 follow-up**

Coinbase 团队明确说重点考察四个维度：
1. **数据结构选型**
2. **数据随时间增长的存储 / 查询效率**
3. **并发 (concurrency)**
4. **取舍 (trade-offs) 讨论**

Part 5+ 的目的是把这四个维度真正展开。面经常常省略这部分——但在实际面试里，做完 Part 4 之后 follow-up 才是拉开候选人差距的地方。

**Part 5+ 的标准选题方向**（每题挑 3–5 个最契合的）：

| 方向 | 适用题 |
|------|--------|
| **并发安全** | 几乎全部题 —— Coinbase 必问 |
| **后台维护任务**（清理 / 重平衡 / GC） | 有状态 + TTL/过期的题 |
| **持久化 / WAL** | 写多的题（DB、Order、Kafka、Log） |
| **分片 / 横向扩展** | 数据规模相关的题 |
| **流式 vs 批处理** | 数据处理类题 (Log, Kafka, Currency) |
| **观测性 / metrics** | 适合追问"出问题怎么排查" |
| **限流 / 反作弊 / 速率控制** | 写入路径有滥用风险（Order, NFT） |
| **算法升级**（朴素 → 优化结构） | DS 相关题（Iterator, Currency, Order） |
| **故障恢复 / 一致性** | 分布式相关讨论 |

**Part 5+ 必须**：
- 每 Part 写明"问题陈述"
- 列出 **面试官最常追问的 3–4 个具体 follow-up 问题**（带答题方向，**不给完整答案**）
- 给一个对比表（方案 vs 取舍）
- 留 stub method 让用户写

**Part 5+ 不必**：
- 每个方向都覆盖。一道题挑 3–5 个最契合的就够。
- 都能写出运行测试。设计讨论类（如"分片"）可以纯文字。

---

## README 结构（每个题必须有）

```markdown
# Coinbase — <题目名>

一句话说明 + 来源（PracHub / LC 链接 / 内部口述）。

---

## API 全貌

\`\`\`java
methodA(...) -> ...   // Part 1
methodB(...) -> ...   // Part 2
// ...
\`\`\`

---

## N 个 Part 的核心

> Part 1–4 是面经原题。
> Part 5+ 是超越面经的 follow-up。

| Part | 一句话 | 易踩的坑 / 讨论点 |
|------|--------|----------|
| 1 | ... | ... |
| 2 | ... | ... |
| 3 | ... | ... |
| 4 | ... | ... |
| 5 | **并发安全** | ... |
| 6 | **某 follow-up** | ... |
| 7 | **某 follow-up** | ... |
| 8 | **某 follow-up** | ... |

### Part 2 的 trade-off 讨论（面试加分点）
（每个值得展开的 Part 单独一节）

---

## Part 5 — 并发访问（Coinbase 面试最常追问的方向）

**问题**：...

**面试官最常问的 4 个 follow-up**：
1. **"..."** → 引出 ... 讨论。
2. **"..."** → ...
3. **"..."** → ...
4. **"..."** → ...

**自检题**（写完代码自己问自己）：
- ...
- ...

---

## Part 6 — <题目>
（同样格式）

---

## 怎么练

\`\`\`bash
cd src/02c-其他算法面经/Coinbase/<Folder>
javac <Name>.java <Name>Test.java
java <Name>Test
java <Name>Test part1 part2
\`\`\`

---

## 代码结构（练习用，不是产品代码）

每个 Part 在 `<Name>.java` 里**完全独立** —— Entry/map/方法都带 PartN 后缀，避免改新 Part 时弄坏旧 Part。

\`\`\`
PART 1: ...                                              [状态]
PART 2: ...                                              [状态]
...
\`\`\`
```

---

## 检查清单（每次扩展 Part 5+ 后必须确认）

1. ☐ 改完 `<Name>.java` 后跑 `javac <Name>.java <Name>Test.java` 编译通过
2. ☐ 跑 `java <Name>Test` 确认原有 Part 仍 PASS，新 Part 显示 SKIPPED
3. ☐ README 里每个新 Part 都有"问题陈述 + 4 个 follow-up 问题 + 取舍表 + 自检题"
4. ☐ 没有在 stub 或 README 里给出完整算法答案
5. ☐ Entry/map 没有不必要的重复（schema 没变就复用上一 Part 的）
6. ☐ Part header 写了"同 / 变 / 新"三行

---

## "我已经做过这题了" 的特殊处理

如果用户说"这题我做过了"：
- **可以**：在 README 里加更深的 trade-off 讨论、把 Part 5+ 的 follow-up 写得更详细
- **可以**：在代码里加 hint 注释（但 hint 而不是答案）
- **不可以**：直接把 Part 1–4 的正确实现填上 —— 那不是"练习"了
- 重点放在 **Part 5+** 上：那才是用户没做过的部分

---

## 参考实现样板

`In-Memory-Database-with-TTL-and-Backups/` 是这套约定的**金标准范例**。新扩展前先读一遍那个目录的 `README.md` 和 `InMemoryDatabase.java`。
