package net.openhft.chronicle.core.onoes;

/**
 * Created by Peter on 13/06/2016.
 */
@FunctionalInterface
public interface ExceptionHandler {
    default void on(Class clazz, Throwable thrown) {
        on(clazz, "", thrown);
    }

    default void on(Class clazz, String message) {
        on(clazz, message, null);
    }

    void on(Class clazz, String message, Throwable thrown);
}
