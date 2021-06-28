package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;

import static net.openhft.chronicle.core.io.AbstractCloseable.DISABLE_DISCARD_WARNING;

/**
 * This interface hides some methods which are more for expert lifecycle use but not strictly internal
 */
public interface ManagedCloseable extends Closeable {
    default void warnAndCloseIfNotClosed() {
        if (!isClosing()) {
            if (Jvm.isResourceTracing() && !DISABLE_DISCARD_WARNING) {
                ExceptionHandler warn = Jvm.getBoolean("warnAndCloseIfNotClosed") ? Jvm.warn() : Slf4jExceptionHandler.WARN;
                warn.on(getClass(), "Discarded without closing " + this);
            }
            Closeable.closeQuietly(this);
        }
    }

    default void throwExceptionIfClosed() throws IllegalStateException {
        if (isClosing())
            throw new ClosedIllegalStateException(isClosed() ? "Closed" : "Closing");
    }

    default StackTrace createdHere() {
        return null;
    }
}
