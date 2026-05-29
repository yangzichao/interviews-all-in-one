import java.util.*;

/**
 * ============================================================================
 *  PROBLEM — In-Memory File System (progressive design)
 * ============================================================================
 *
 *  Design an in-memory hierarchical file system. The system grows in PARTS.
 *  In a real interview you only see the next part after finishing the
 *  current one.
 *
 *  ----------------------------------------------------------------------
 *   PATH FORMAT (applies to every method, every Part)
 *   ----------------------------------------------------------------------
 *   - Always absolute, Unix-style, starting with '/'.
 *   - Root is "/" (the only path that ends in '/').
 *   - Non-root paths have NO trailing slash:  "/a", "/a/b/c", "/a/b/file.txt".
 *   - Components are non-empty, opaque strings (LC 588 uses
 *     [a-zA-Z][a-zA-Z0-9]* for names — treat as plain strings).
 *
 *   Inputs are guaranteed well-formed — DO NOT defensively validate:
 *     - no "." or ".."
 *     - no duplicate slashes ("/a//b")
 *     - no empty components, no whitespace
 *     - mkdir(path) never targets an existing file
 *     - mv (Part 2) preconditions are listed in the PART 2 section below
 *
 *   Splitting recipe:
 *     String[] parts = path.equals("/")
 *         ? new String[0]
 *         : path.substring(1).split("/");
 *   "/" → empty array (stay at root); "/a/b" → ["a", "b"].
 *
 *  ----------------------------------------------------------------------
 *   PART 1 — LC 588 baseline
 *   ----------------------------------------------------------------------
 *   ls(path) -> List<String>
 *       If path is a file, return a single-element list containing the file
 *       name. If path is a directory, return its entries (files + sub-dirs)
 *       sorted lexicographically. An empty directory returns an empty list.
 *
 *   mkdir(path)
 *       Create a directory at path, creating all missing intermediate
 *       directories. If the directory already exists, do nothing. The path
 *       is guaranteed not to refer to an existing file.
 *
 *   addContentToFile(path, content)
 *       If the file does not exist, create it (and any missing parents) and
 *       write `content`. If the file exists, append `content` to it.
 *
 *   readContentFromFile(path) -> String
 *       Return the file's content as a String.
 *
 *  ----------------------------------------------------------------------
 *   PART 2 — mv  (kept simple on purpose)
 *   ----------------------------------------------------------------------
 *   mv(src, dest)
 *       Move `src` (a file or directory) to `dest`. `dest` is the new full
 *       path — not a directory you "move into".
 *
 *       To keep this Part simple, you MAY assume:
 *         - `src` exists; `dest`'s parent directory exists; `dest` itself
 *           does not exist yet (no overwrite case)
 *         - `src` is not an ancestor of `dest` (no cycle case)
 *         - `src` != `dest`
 *
 *       The harder cases are README discussion only; don't code them.
 *
 *  ----------------------------------------------------------------------
 *   PART 3 — simple thread safety
 *   ----------------------------------------------------------------------
 *   Same API as Part 2, but safe under concurrent callers using the
 *   SIMPLEST viable strategy: ONE lock for the whole file system.
 *   (See README §"Part 3" for the trade-off discussion of why we don't
 *   reach for per-node locks here.)
 *
 *  ----------------------------------------------------------------------
 *   PART 4 — distributed (whiteboard discussion only, no code)
 *   ----------------------------------------------------------------------
 *   See README §"Part 4". No methods to implement.
 * ============================================================================
 *
 *  Practice-code conventions in this file (NOT part of the problem):
 *    - Methods are suffixed lsPart1 / mvPart2 / lsPart3 / ... so each part
 *      is independent and earlier parts keep passing after you change
 *      direction.
 *    - Each part's banner lists [same / changed / new] vs. the previous
 *      part. A new node/data structure is introduced ONLY when the
 *      schema actually changes — otherwise reuse the previous one.
 */
public class InMemoryFileSystem {

    // ====================================================================
    // PART 1  —  ls / mkdir / addContentToFile / readContentFromFile [⚠ TODO]
    // ====================================================================
    // 同: (first part)
    // 变: (first part)
    // 新: 一个 tree node 表示（自己决定怎么存 children、怎么区分 file/dir），
    //     以及 4 个公开方法。
    //
    // 你要写的:
    //   - lsPart1(path) -> List<String>     文件返 [filename]，目录返字典序子项
    //   - mkdirPart1(path)                  递归建中间目录
    //   - addContentToFilePart1(path, content)  不存在则建，存在则追加
    //   - readContentFromFilePart1(path) -> String
    //
    // 自己定义你需要的字段 / 内部类。下面只给方法 stub —— 不剧透实现。

    class Node {
        final boolean isFile;
        String name;
        String content;
        Map<String, Node> children;

        Node (boolean isFile, String name) {
            this.isFile = isFile;
            this.name = name;
            if (isFile) {
                this.content = "";
            } else {
                this.children = new HashMap<>();
            }
        } 

        Node getChild(String name) {
            if (this.children == null) return null;
            return this.children.get(name);
        }

        void setChild(String name, Node node) {
            this.children.put(name, node);
        }
    }

    Node root;

    public InMemoryFileSystem () {
        this.root = new Node(false, "");
    }

    public List<String> lsPart1(String path) {
        throw new UnsupportedOperationException("TODO: Part 1 — ls");
    }

    public void mkdirPart1(String path) {
        throw new UnsupportedOperationException("TODO: Part 1 — mkdir");
    }

    public void addContentToFilePart1(String path, String content) {
        throw new UnsupportedOperationException("TODO: Part 1 — addContentToFile");
    }

    public String readContentFromFilePart1(String path) {
        throw new UnsupportedOperationException("TODO: Part 1 — readContentFromFile");
    }

    private String[] getSegments(String path) {
        return path.length() == 1 ? new String[0] : path.substring(1).split("/");
    }

    // 你自己抽 helper：resolve(path) 纯读 / getOrCreateDir(path) 写允许 /
    //                  parentPathOf(path) + nameOf(path) 拆路径

    // ====================================================================
    // PART 2  —  mv / rm / cp                                       [⚠ TODO]
    // ====================================================================
    // 同: tree node from Part 1（schema 不变，直接复用）
    // 变: 无
    // 新: mvPart2 / rmPart2 / cpPart2
    //     —— Part 2 的 ls/mkdir/addContent/read 是 thin wrapper，直接转 Part 1
    //
    // 这三个放一起就是文件系统题最经典的 follow-up 三连，考的是同一个核心：
    //   mv  = 移动语义：把子树 reference 从旧 parent 摘下、挂到新 parent。O(1)，不拷贝。
    //   rm  = 删除语义：把子树 reference 从 parent 摘下即可，GC 自动回收。O(1)。
    //   cp  = 拷贝语义：必须 DEEP COPY 整棵子树（递归新建 Node）。O(子树大小)。
    // 一句话区分：mv/rm 只动一个引用；cp 要造一整棵新树。面试官就想听这个对比。
    //
    // 你要写的:
    //   - mvPart2(src, dest)
    //   - rmPart2(path)
    //   - cpPart2(src, dest)   ← 注意必须 deep copy，不能跟 mv 一样挪 reference
    //
    // 简化前提（题面允许你假设）:
    //   - mv/cp: src 存在；dest.parent 存在；dest 不存在；src 不是 dest 祖先；src != dest
    //   - rm: path 存在（不是 root）
    //
    // 复杂边界（overwrite、cycle 检测、跨设备、rm 非空目录是否要 -r）
    // 在 README 讨论，不要在代码里展开。

    public List<String> lsPart2(String path) {
        return lsPart1(path);
    }

    public void mkdirPart2(String path) {
        mkdirPart1(path);
    }

    public void addContentToFilePart2(String path, String content) {
        addContentToFilePart1(path, content);
    }

    public String readContentFromFilePart2(String path) {
        return readContentFromFilePart1(path);
    }

    public void mvPart2(String src, String dest) {
        throw new UnsupportedOperationException("TODO: Part 2 — mv");
    }

    public void rmPart2(String path) {
        throw new UnsupportedOperationException("TODO: Part 2 — rm");
    }

    public void cpPart2(String src, String dest) {
        throw new UnsupportedOperationException("TODO: Part 2 — cp (deep copy!)");
    }

    // ====================================================================
    // PART 3  —  线程安全（最简情形：一把锁）                        [⚠ TODO]
    // ====================================================================
    // 同: tree 结构、所有操作的语义都跟 Part 2 一样
    // 变: 在 5 个公开方法外面包一层串行化
    // 新: 一个 lock 字段（建议 java.util.concurrent.locks.ReentrantReadWriteLock）
    //     和 5 个 Part 3 后缀的方法
    //
    // 你要写的:
    //   - 一个 lock 实例字段
    //   - 5 个方法 wrap Part 2：读 (ls / readContentFromFile) 拿 read lock；
    //     写 (mkdir / addContentToFile / mv) 拿 write lock
    //
    // 这是"简单情形"——day-1 上线版本。复杂情形（per-node 锁、mv 跨子树
    // 死锁、copy-on-write、lock-free read）只在 README 讨论，不在代码里展开。

    public List<String> lsPart3(String path) {
        throw new UnsupportedOperationException("TODO: Part 3 — ls under lock");
    }

    public void mkdirPart3(String path) {
        throw new UnsupportedOperationException("TODO: Part 3 — mkdir under lock");
    }

    public void addContentToFilePart3(String path, String content) {
        throw new UnsupportedOperationException("TODO: Part 3 — addContentToFile under lock");
    }

    public String readContentFromFilePart3(String path) {
        throw new UnsupportedOperationException("TODO: Part 3 — readContentFromFile under lock");
    }

    public void mvPart3(String src, String dest) {
        throw new UnsupportedOperationException("TODO: Part 3 — mv under lock");
    }

    // ====================================================================
    // PART 4  —  分布式（白板讨论，无代码）                          [💬 DISCUSS]
    // ====================================================================
    // 不写代码。面经里的 follow-up chain 大致按 Q1 → Q4 推进。
    // 下面是"对着代码自答"用的速查版；展开论证见 README §"Part 4"。
    //
    // ─────────────────────────────────────────────────────────────────────
    // Q1: "How would you make this distributed?"  —— 参考 GFS / HDFS
    // ─────────────────────────────────────────────────────────────────────
    //   - Namenode 存 metadata：inode tree、permission、path → chunk handle
    //     的映射。inode 树 ~GB 量级，可以全放内存（像我们 Part 1 的 Node tree
    //     一样，只是搬到单独的服务）。
    //   - Datanode 存实际 content：文件按 chunk（64 MB / 128 MB）切，每个
    //     chunk 3 副本散到不同 datanode。
    //   - Client 先问 namenode 拿 chunk handle + datanode 列表，再直连
    //     datanode 读写——namenode 不在数据路径上，吞吐才能扩展。
    //
    // ─────────────────────────────────────────────────────────────────────
    // Q2: "How would you shard metadata when one namenode isn't enough?"
    // ─────────────────────────────────────────────────────────────────────
    //   方案 A: 按 path prefix shard（/a/* → shard A, /b/* → shard B）
    //     + 单 shard 内 ls / mkdir 是 local 操作；locality 好
    //     - 热点目录失衡（/users/ 撑爆一个 shard）
    //     - mv /a/x → /b/y 跨 shard，难做原子
    //   方案 B: 按 inode id 一致性哈希
    //     + 负载均匀；mv 不改 inode id → metadata-only 操作
    //     - ls 要 fan-out 到所有 shard 收集 children；rebalance 麻烦
    //   方案 C: federation（每个 shard 一棵完整子树挂载点，HDFS Federation）
    //     + 实现简单，业界常用
    //     - mv 跨挂载点退化为 cp + rm
    //   生产里更常见: prefix shard + client 端 mount table 缓存
    //
    // ─────────────────────────────────────────────────────────────────────
    // Q3: "How would cross-shard mv stay atomic?"
    // ─────────────────────────────────────────────────────────────────────
    //   朴素思路 ❌: 先在 shard B 创建 dest，再在 shard A 删 src
    //     —— 中间崩了就是两份 / 零份。不行。
    //   2PC: coordinator 让 A、B 都 prepare（A: src "pending unlink"，
    //     B: dest "pending link"），全 prepared 才 commit；任一 abort 全回滚。
    //     —— 文件系统语义最常见的选择。
    //   Saga: 拆 link(dest) + unlink(src)，两步 idempotent，失败可重试。
    //     —— 期间外部可能看到两份；fs 语义大多不接受。
    //   单 metadata raft log: 所有 metadata 走一条共识 log，mv 是一条
    //     atomic entry。metadata 写吞吐被 raft 限死，namenode 不能水平扩展。
    //
    // ─────────────────────────────────────────────────────────────────────
    // Q4: "What's the consistency model?"  —— GFS 的核心取舍
    // ─────────────────────────────────────────────────────────────────────
    //   Metadata: strong (linearizable)
    //     用户对 "我刚 mkdir，下一秒 ls 看不到" 非常敏感。
    //   Content: eventually consistent OK
    //     addContentToFile 之后，read 容忍几十 ms 滞后可以接受
    //     （HDFS 就是这样：write 完成只保证"最终"被 replica 看到）。
    //
    // ─────────────────────────────────────────────────────────────────────
    // 自检题（合上 README 也要能回答）
    // ─────────────────────────────────────────────────────────────────────
    //   - 单台 namenode 的吞吐瓶颈在哪？
    //     → metadata 写的 raft / 持久化 / 锁
    //   - 一个 100 GB 的文件怎么存？
    //     → chunked + replicated；client 并行读多个 chunk
    //   - 怎么做 snapshot？
    //     → 写时复制 inode 树根，content chunk 共享不动
    //   - 一个 datanode 挂了多久能恢复？
    //     → namenode 监测心跳超时 → 触发 re-replicate；典型分钟级
    //   - 我们 Part 3 的 RWLock 在分布式版里对应什么？
    //     → namenode 内部的 metadata 锁；客户端看不到，但仍是单点瓶颈
}
