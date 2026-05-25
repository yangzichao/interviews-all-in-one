import java.util.*;

public class Lesson09Test {
    public static void main(String[] args) throws Exception {
        try {
            Lesson09 l = new Lesson09();
            List<Long> nums = new ArrayList<>();
            long expected = 0;
            for (long i = 1; i <= 10_000; i++) { nums.add(i); expected += i; }
            // expected = 10000 * 10001 / 2 = 50,005,000

            long actual = l.sumInParallel(nums, 8);
            if (actual != expected) {
                throw new AssertionError("sum mismatch: expected " + expected + " got " + actual
                        + "  (split 漏元素 or future.get 没收齐?)");
            }

            // 再试一个 workers > nums.size 的边界, 防止 split 写挂
            long small = l.sumInParallel(Arrays.asList(1L, 2L, 3L), 8);
            if (small != 6) {
                throw new AssertionError("小输入 sum mismatch: expected 6 got " + small);
            }
            System.out.println("Lesson09 PASSED  (sum=" + actual + ")");
        } catch (UnsupportedOperationException e) {
            System.out.println("Lesson09 SKIPPED (not implemented)");
        } catch (AssertionError e) {
            System.out.println("Lesson09 FAILED: " + e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
}
