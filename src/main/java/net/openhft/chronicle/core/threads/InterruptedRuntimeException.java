package net.openhft.chronicle.core.threads;

/**
 * An unchecked alternative to {@link InterruptedException}.
 * <p>
 * This should generally not be used, prefer {@link InterruptedException} wherever possible, but
 * there are some scenarios where we need to throw an unchecked exception after being interrupted.
 * <p>
 * If {@link InterruptedException} was caught, remember to set {@link Thread#interrupt()} prior to throwing these.
 */
public class InterruptedRuntimeException extends IllegalStateException {

    public InterruptedRuntimeException() {
    }

    public InterruptedRuntimeException(String s) {
        super(s);
    }

    public InterruptedRuntimeException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public InterruptedRuntimeException(Throwable throwable) {
        super(throwable);
    }
}
