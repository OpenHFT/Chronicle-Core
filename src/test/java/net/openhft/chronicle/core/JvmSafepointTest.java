package net.openhft.chronicle.core;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/*
@Ignore("see TC - https://github.com/OpenHFT/Chronicle-Core/issues/63, failing on release build " +
        "http://teamcity.higherfrequencytrading" +
        ".com:8111/repository/download/OpenHFT_ReleaseJob_ReleaseByArtifact/257026:id/ReleaseAutomation/projects/chronicle-core-runTests-1527606961685.log")
*/
public class JvmSafepointTest {

    @Test
    public void testSafepoint() {
        @SuppressWarnings("AnonymousHasLambdaAlternative")
        Thread t = new Thread() {
            public void run() {
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() < start + 500) {
                    for (int i = 0; i < 100; i++)
                        Jvm.safepoint();
                }
            }
        };
        t.start();
        Jvm.pause(100);
        int counter = 0;
        while (t.isAlive() && counter <= 200) {
            StackTraceElement[] stackTrace = t.getStackTrace();
            if (stackTrace.length > 1) {
                String s = stackTrace[1].toString();
                if (s.contains("safepoint"))
                    counter++;
                else if (t.isAlive())
                    System.out.println(s);
            }
        }
        System.out.println("counter: " + counter);
        assertTrue("counter: " + counter, counter > 200);
    }

    @Test
    public void safePointPerf() {

        for (int t = 0; t <= 5; t++) {
            long start = System.nanoTime();

            int count = 10_000;
            for (int i = 0; i < count; i++)
                Jvm.safepoint();
            long time = System.nanoTime() - start;
            if (t > 2) {
                long avg = time / count;
                System.out.println("avg: " + avg);
                int maxAvg = Jvm.isArm() ? 400 : OS.isWindows() || Jvm.isCodeCoverage() ? 200 : 100;
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
