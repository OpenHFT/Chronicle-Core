package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import org.jetbrains.annotations.NotNull;

public interface ReferenceCountedTracer extends ReferenceCounted {
    @NotNull
    static ReferenceCountedTracer onReleased(final Runnable onRelease, String uniqueId) {
        return Jvm.isResourceTracing()
                ? new DualReferenceCounted(
                new TracingReferenceCounted(onRelease, uniqueId),
                new VanillaReferenceCounted(() -> {
                }))
                : new VanillaReferenceCounted(onRelease);
    }

    default void throwExceptionIfReleased() throws IllegalStateException {
        if (refCount() <= 0)
            throw new IllegalStateException("Released");
    }

    void warnAndReleaseIfNotReleased();

    void throwExceptionIfNotReleased();

    StackTrace createdHere();
}
