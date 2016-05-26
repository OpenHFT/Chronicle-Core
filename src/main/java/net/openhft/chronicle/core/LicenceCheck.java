package net.openhft.chronicle.core;

/**
 * @author Rob Austin.
 */
public interface LicenceCheck {

    /**
     * checks if the function you are about to call is part of an enterprise product, if the licence
     * fails a runtime exception will be thrown
     */
    default void licenceCheck() {

    }

    default boolean isAvailable() {
        return true;
    }
}
