package net.openhft.chronicle.core.util;

/**
 * Created by Peter on 13/06/2016.
 */
@FunctionalInterface
public interface ThrowingRunnable<T extends Throwable> {
    void run() throws T;
}
