package net.openhft.chronicle.core.threads;

/**
 * @author Rob Austin.
 */
@FunctionalInterface
public interface VanillaEventHandler {
    /**
     * perform all tasks once and return ASAP.
     *
     * @return true if you expect more work very soon.
     * @throws InvalidEventHandlerException when it is not longer valid.
     */
    boolean action() throws InvalidEventHandlerException, InterruptedException;
}
