package net.openhft.chronicle.core.util;

/**
 * Created by Peter on 13/06/2016.
 */
@FunctionalInterface
public interface ThrowingCallable<R, T extends Throwable> {
    R call() throws T;
}
