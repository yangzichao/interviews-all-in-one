import java.util.concurrent.atomic.AtomicInteger;

public class Lesson07Test {
    public static void main(String[] args) throws Exception {
        try {
            Lesson07 l = new Lesson07();
            final int n = 50;
            final AtomicInteger ran = new AtomicInteger();
            long t0 = System.nanoTime();
            l.runAll(n, () -> {
                try { Thread.sleep(20); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
                ran.incrementAndGet();
            });
            long elapsedMs = (System.nanoTime() - t0) / 1_000_000;

            if (ran.get() != n) {
                throw new AssertionError("runAll 返回时只有 " + ran.get() + "/" + n + " 个 task 完成 — 没等齐?");
            }
            // 50 个并行睡 20ms, 应该总耗时远小于 50*20=1000ms; 给一个宽松 800ms 上限
            if (elapsedMs > 800) {
                throw new AssertionError("耗时 " + elapsedMs + "ms 太长了 — 是不是串行跑了, 没并行?");
            }
            System.out.println("Lesson07 PASSED  (ran=" + ran.get() + ", elapsed=" + elapsedMs + "ms)");
        } catch (UnsupportedOperationException e) {
            System.out.println("Lesson07 SKIPPED (not implemented)");
        } catch (AssertionError e) {
            System.out.println("Lesson07 FAILED: " + e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
}
