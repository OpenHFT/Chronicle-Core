package net.openhft.chronicle.core.onoes;

import java.io.PrintStream;
import java.time.LocalDateTime;

/**
 * Created by Peter on 13/06/2016.
 */
public enum PrintExceptionHandler implements ExceptionHandler {
    WARN {
        @Override
        public void on(Class clazz, String message, Throwable thrown) {
            printLog(clazz, message, thrown, System.err);
        }
    },
    DEBUG {
        @Override
        public void on(Class clazz, String message, Throwable thrown) {
            printLog(clazz, message, thrown, System.out);
        }
    };

    private static void printLog(Class clazz, String message, Throwable thrown, PrintStream stream) {
        synchronized (stream) {
            System.err.print(LocalDateTime.now() + " " + Thread.currentThread().getName() + " " + clazz.getSimpleName() + " " + message);
            thrown.printStackTrace(System.err);
        }
    }
}
