package net.openhft.chronicle.core;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class JvmSafepointTest {
    @Test
    public void testSafepoint() {
        @SuppressWarnings("AnonymousHasLambdaAlternative")
        Thread t = new Thread() {
            public void run() {
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() < start + 500) {
                    for (int i = 0; i < 100; i++)
                        if (Jvm.SAVEPOINT_ENABLED)
                            Jvm.optionalSafepoint();
                        else
                            Jvm.safepoint();
                }
            }
        };
        t.start();
        Jvm.pause(100);
        int counter = 0;
        while (t.isAlive()) {
            StackTraceElement[] stackTrace = t.getStackTrace();
            if (stackTrace.length > 1) {
                String s = stackTrace[1].toString();
                if (s.contains("safepoint"))
                    counter++;
                else if (t.isAlive())
                    System.out.println(s);
            }
        }
        assertTrue("counter: " + counter, counter > 1000);
    }

    @Test
    public void safePointPerf() {
        for (int t = 0; t < 8; t++) {
            long start = System.nanoTime();

            int count = 2_000_000;
            for (int i = 0; i < count; i++)
                Jvm.safepoint();
            long time = System.nanoTime() - start;
            if (t > 1) {
                long avg = time / count;
                System.out.println("avg: " + avg);
                assertTrue("avg: " + avg, 2 < avg && avg < 200);
            }
            Jvm.pause(5);
        }
    }
}
