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

    /**
     * A method to call when an exception occurs. It assumes there is a different handler for different levels.
     *
     * @param clazz   the error is associated with, e.g. the one in which it was caught
     * @param message any message associated with the error, or empty String.
     * @param thrown  any Thorwable caught, or null if there was no exception.
     */
    void on(Class clazz, String message, Throwable thrown);

    default boolean isEnabled(Class aClass) {
        return true;
    }
}
