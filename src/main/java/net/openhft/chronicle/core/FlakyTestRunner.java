package net.openhft.chronicle.core;

import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;

public enum FlakyTestRunner {
    ;

    static boolean IN_RUN = false;

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
            if (IN_RUN)
                throw new AssertionError("Can't run nested");
            IN_RUN = true;

            rt.run();

        } catch (Throwable t) {
            System.err.println("Rerunning failing test");
            System.gc();
            Jvm.pause(500);
            rt.run();
            Slf4jExceptionHandler.WARN.on(FlakyTestRunner.class, "Flaky test threw an error the first run, passed the second time", t);
        } finally {
            IN_RUN = false;
        }
    }

    public interface RunnableThrows<T extends Throwable> {
        void run() throws T;
    }
}
