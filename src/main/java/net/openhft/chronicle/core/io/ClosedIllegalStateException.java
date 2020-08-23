package net.openhft.chronicle.core.io;

public class ClosedIllegalStateException extends IllegalStateException {
    public ClosedIllegalStateException(String s) {
        super(s);
    }

    public ClosedIllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
