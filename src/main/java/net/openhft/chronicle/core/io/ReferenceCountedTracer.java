package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface ReferenceCountedTracer extends ReferenceCounted {
    @NotNull
    static ReferenceCountedTracer onReleased(final Runnable onRelease, Supplier<String> uniqueId, Class<?> type) {
        return Jvm.isResourceTracing()
                ? new TracingReferenceCounted(onRelease, uniqueId.get(), type)
                : new VanillaReferenceCounted(onRelease, type);
    }

    // throws IllegalStateException
    default void throwExceptionIfReleased() throws ClosedIllegalStateException {
        if (refCount() <= 0)
            throw new ClosedIllegalStateException("Released");
    }

    /**
     * Release any remaining references, and log a warning if there were any references to release.
     * <p>
     * Intended to be called by a finalizer or in a test, to confirm that references are being released correctly
     * <p>
     * Note: This will not cause any {@link ReferenceChangeListener}s to fire as it's really just a sanity check
     */
    void warnAndReleaseIfNotReleased() throws ClosedIllegalStateException;

    void throwExceptionIfNotReleased() throws IllegalStateException;

    StackTrace createdHere();
}
