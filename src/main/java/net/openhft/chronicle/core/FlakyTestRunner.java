package net.openhft.chronicle.core;

import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;

@Deprecated // For removal in x.23 . This is a class that supports testing and is moved to Chronicle-Test-Framework
public final class FlakyTestRunner {

    // Suppresses default constructor, ensuring non-instantiability.
    private FlakyTestRunner() {
    }

    private static boolean inRun = false;

    public static <T extends Throwable> void run(RunnableThrows<T> rt) throws T {
        run(true, rt);
    }

    public static <T extends Throwable> void run(boolean flakyOnThisArch, RunnableThrows<T> rt) throws T {
        if (!flakyOnThisArch) {
            rt.run();
            return;
        }
        try {
            // stop accidental call back to self.
            if (inRun)
                throw new AssertionError("Can't run nested");
            inRun = true;

            rt.run();

        } catch (Throwable t) {
            System.err.println("Rerunning failing test");
            System.gc();
            Jvm.pause(500);
            rt.run();
            Slf4jExceptionHandler.WARN.on(FlakyTestRunner.class, "Flaky test threw an error the first run, passed the second time", t);
        } finally {
            inRun = false;
        }
    }

    public interface RunnableThrows<T extends Throwable> {
        void run() throws T;
    }
}
