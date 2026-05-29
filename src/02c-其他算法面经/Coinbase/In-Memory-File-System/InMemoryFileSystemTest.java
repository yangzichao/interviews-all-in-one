import java.util.*;

public class InMemoryFileSystemTest {

    static int passed = 0, failed = 0, skipped = 0;

    public static void main(String[] args) {
        Map<String, Runnable> tests = new LinkedHashMap<>();
        tests.put("part1", InMemoryFileSystemTest::testPart1);
        tests.put("part2", InMemoryFileSystemTest::testPart2);
        tests.put("part3", InMemoryFileSystemTest::testPart3);
        // Part 4 is whiteboard discussion only — no runnable test.

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

    // ===== Part 1 =======================================================

    static void testPart1() {
        InMemoryFileSystem fs = new InMemoryFileSystem();

        // Empty root.
        assertEq(Collections.emptyList(), fs.lsPart1("/"), "ls empty root");

        // mkdir creates all intermediate directories.
        fs.mkdirPart1("/a/b/c");
        assertEq(List.of("a"), fs.lsPart1("/"), "root has 'a'");
        assertEq(List.of("b"), fs.lsPart1("/a"), "/a has 'b'");
        assertEq(List.of("c"), fs.lsPart1("/a/b"), "/a/b has 'c'");
        assertEq(Collections.emptyList(), fs.lsPart1("/a/b/c"), "leaf dir empty");

        // addContentToFile creates the file (and any missing parents).
        fs.addContentToFilePart1("/a/b/file.txt", "hello");
        assertEq("hello", fs.readContentFromFilePart1("/a/b/file.txt"), "read after create");

        // addContentToFile on existing file appends.
        fs.addContentToFilePart1("/a/b/file.txt", " world");
        assertEq("hello world", fs.readContentFromFilePart1("/a/b/file.txt"), "append");

        // ls on a file returns a single-element list of its name.
        assertEq(List.of("file.txt"), fs.lsPart1("/a/b/file.txt"), "ls on file");

        // ls on a directory lists files + sub-dirs in lexicographic order.
        assertEq(List.of("c", "file.txt"), fs.lsPart1("/a/b"), "ls dir sorted");

        // addContentToFile also auto-creates missing parents.
        fs.addContentToFilePart1("/fresh/dir/new.txt", "data");
        assertEq("data", fs.readContentFromFilePart1("/fresh/dir/new.txt"), "auto-create parents");
        assertEq(List.of("dir"), fs.lsPart1("/fresh"), "intermediate dir exists");
    }

    // ===== Part 2 =======================================================

    static void testPart2() {
        InMemoryFileSystem fs = new InMemoryFileSystem();
        fs.mkdirPart2("/a/b");
        fs.addContentToFilePart2("/a/b/f.txt", "hi");

        // Move a file across directories — content survives.
        fs.mvPart2("/a/b/f.txt", "/a/g.txt");
        assertEq("hi", fs.readContentFromFilePart2("/a/g.txt"), "file content preserved after mv");
        assertEq(Collections.emptyList(), fs.lsPart2("/a/b"), "src parent empty after mv");

        // Move a directory subtree — children come along.
        fs.mkdirPart2("/x/y");
        fs.addContentToFilePart2("/x/y/data", "v");
        fs.mvPart2("/x/y", "/a/y");
        assertEq("v", fs.readContentFromFilePart2("/a/y/data"), "subtree content preserved");
        assertEq(Collections.emptyList(), fs.lsPart2("/x"), "src dir empty after subtree mv");

        // Rename in place (same parent, different name).
        fs.addContentToFilePart2("/r/old.txt", "k");
        fs.mvPart2("/r/old.txt", "/r/new.txt");
        assertEq("k", fs.readContentFromFilePart2("/r/new.txt"), "rename preserves content");

        // rm a file — gone from its parent.
        fs.addContentToFilePart2("/d/x.txt", "bye");
        fs.rmPart2("/d/x.txt");
        assertEq(Collections.emptyList(), fs.lsPart2("/d"), "file removed from parent");

        // rm a directory subtree — whole subtree disappears.
        fs.mkdirPart2("/d/sub/deep");
        fs.addContentToFilePart2("/d/sub/deep/leaf", "z");
        fs.rmPart2("/d/sub");
        assertEq(Collections.emptyList(), fs.lsPart2("/d"), "subtree removed");

        // cp a file — both copies exist independently.
        fs.addContentToFilePart2("/c/orig.txt", "base");
        fs.cpPart2("/c/orig.txt", "/c/dup.txt");
        assertEq("base", fs.readContentFromFilePart2("/c/orig.txt"), "cp leaves source intact");
        assertEq("base", fs.readContentFromFilePart2("/c/dup.txt"), "cp creates dest with same content");

        // cp must DEEP copy: mutating the copy must NOT touch the original.
        fs.mkdirPart2("/c/srcDir");
        fs.addContentToFilePart2("/c/srcDir/file", "one");
        fs.cpPart2("/c/srcDir", "/c/dstDir");
        fs.addContentToFilePart2("/c/dstDir/file", "-two");  // append only to the copy
        assertEq("one", fs.readContentFromFilePart2("/c/srcDir/file"), "deep copy: source unchanged after mutating copy");
        assertEq("one-two", fs.readContentFromFilePart2("/c/dstDir/file"), "deep copy: copy mutated independently");
        // Adding a brand-new child to the copy must not appear in the source.
        fs.addContentToFilePart2("/c/dstDir/extra", "new");
        assertEq(List.of("file"), fs.lsPart2("/c/srcDir"), "deep copy: new child in copy absent from source");
    }

    // ===== Part 3 =======================================================

    static void testPart3() {
        // 简单情形：单线程下行为应跟 Part 2 一致；真正的并发竞争靠 review，
        // 不指望 unit test 抓出来（unit test 抓 race 也不可靠）。
        InMemoryFileSystem fs = new InMemoryFileSystem();
        fs.mkdirPart3("/a");
        fs.addContentToFilePart3("/a/f", "x");
        assertEq("x", fs.readContentFromFilePart3("/a/f"), "read under lock");
        fs.mvPart3("/a/f", "/a/g");
        assertEq("x", fs.readContentFromFilePart3("/a/g"), "mv under lock");
        assertEq(List.of("g"), fs.lsPart3("/a"), "ls reflects mv");
    }
}
