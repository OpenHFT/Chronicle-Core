package net.openhft.chronicle.core;

/**
 * Throwable created purely for the purposes of reporting a stack trace.
 * <p>
 * This is not an Error or an Exception and is not expected to be thrown or caught.
 * </p>
 * https://github.com/OpenHFT/Chronicle-Core/issues/75
 */
public class StackTrace extends Throwable {
    public StackTrace() {
    }

    public StackTrace(String message) {
        super(message);
    }

    public StackTrace(String message, Throwable cause) {
        super(message, cause);
    }
}
