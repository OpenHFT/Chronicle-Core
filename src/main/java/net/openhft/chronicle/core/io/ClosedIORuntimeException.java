package net.openhft.chronicle.core.io;

/**
 * An IORuntimeException triggered when a closed underlying IO resource is used.
 */
public class ClosedIORuntimeException extends IORuntimeException {
    public ClosedIORuntimeException(String message) {
        super(message);
    }

    public ClosedIORuntimeException(String message, Throwable thrown) {
        super(message, thrown);
    }

}
