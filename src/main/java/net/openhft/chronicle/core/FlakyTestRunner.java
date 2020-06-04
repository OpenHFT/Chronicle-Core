package net.openhft.chronicle.core;

import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;

public enum FlakyTestRunner {
    ;

    public static <T extends Throwable> void run(RunnableThrows<T> rt) throws T {
        try {
            rt.run();
        } catch (Throwable t) {
            System.gc();
            Jvm.pause(500);
            rt.run();
            Slf4jExceptionHandler.WARN.on(FlakyTestRunner.class, "Flaky test threw an error the first run, passed the second time", t);
        }
    }

    public interface RunnableThrows<T extends Throwable> {
        void run() throws T;
    }
}
