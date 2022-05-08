package net.openhft.chronicle.core;

import net.openhft.chronicle.testframework.FlakyTestRunner;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/*
@Ignore("see TC - https://github.com/OpenHFT/Chronicle-Core/issues/63, failing on release build " +
        "http://teamcity.higherfrequencytrading" +
        ".com:8111/repository/download/OpenHFT_ReleaseJob_ReleaseByArtifact/257026:id/ReleaseAutomation/projects/chronicle-core-runTests-1527606961685.log")
*/
public class JvmSafepointTest {

    @Test
    public void testSafepoint() throws InterruptedException {
        @SuppressWarnings("AnonymousHasLambdaAlternative")
        Thread t = new Thread() {
            public void run() {
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() < start + 1000
                        && !Thread.interrupted()) {
                    for (int i = 0; i < 10000; i++)
                        Jvm.safepoint();
                }
            }
        };
        t.start();
        Jvm.pause(5);
        int counter = 0;
        int min = Jvm.isAzulZing() ? 0 : 200;
        while (t.isAlive() && counter <= min) {
            StackTraceElement[] stackTrace = t.getStackTrace();
            if (stackTrace.length > 1) {
                String s = stackTrace[1].toString();
                if (s.contains("safepoint"))
                    counter++;
                else if (t.isAlive() && !s.contains("interrupted"))
                    System.out.println(s);
            }
        }
        t.interrupt();
        t.join();
        System.out.println("counter: " + counter);
        assertTrue("counter: " + counter, counter > min);
    }

    @Test
    public void safePointPerf() {
        // This will enable the C2 compiler to kick in.
        FlakyTestRunner.run(true, this::safePointPerf0, 3);
    }

    public void safePointPerf0() {

        for (int t = 0; t <= 5; t++) {
            long start = System.nanoTime();

            int count = 10_000;
            for (int i = 0; i < count; i++)
                Jvm.safepoint();
            long time = System.nanoTime() - start;
            if (t > 2) {
                long avg = time / count;
                System.out.println("avg: " + avg);
                int maxAvg = Jvm.isArm() ? 400 : 200;
                try {
                    assertTrue("avg: " + avg, 1 <= avg && avg < maxAvg);
                    break;
                } catch (AssertionError e) {
                    if (t == 5)
                        throw e;
                }
            }
            Jvm.pause(5);
        }
    }
}
