public class Lesson01Test {
    public static void main(String[] args) throws Exception {
        try {
            Lesson01 l = new Lesson01();
            l.start();
            Thread.sleep(50);
            long mid = l.ticks();
            if (mid <= 0) throw new AssertionError("worker 没在跑? ticks=" + mid);

            long t0 = System.nanoTime();
            l.stop();
            long elapsedMs = (System.nanoTime() - t0) / 1_000_000;
            if (elapsedMs > 1000) {
                throw new AssertionError("stop() 等了 " + elapsedMs + "ms 才回来 — worker 看不到 running=false (volatile 漏了?)");
            }
            long after = l.ticks();
            if (after < mid) throw new AssertionError("ticks 倒退? mid=" + mid + " after=" + after);
            System.out.println("Lesson01 PASSED  (ticks≈" + after + ", stop took " + elapsedMs + "ms)");
        } catch (UnsupportedOperationException e) {
            System.out.println("Lesson01 SKIPPED (not implemented)");
        } catch (AssertionError e) {
            System.out.println("Lesson01 FAILED: " + e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
}
