package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface ReferenceCountedTracer extends ReferenceCounted {
    @Deprecated(/* to be removed in x.22 */)
    @NotNull
    static ReferenceCountedTracer onReleased(final Runnable onRelease, String uniqueId) {
        return onReleased(onRelease, uniqueId, ReferenceCountedTracer.class);
    }

    @NotNull
    static ReferenceCountedTracer onReleased(final Runnable onRelease, Supplier<String> uniqueId, Class type) {
        return Jvm.isResourceTracing()
                ? new DualReferenceCounted(
                new TracingReferenceCounted(onRelease, uniqueId.get(), type),
                new VanillaReferenceCounted(() -> {
                }, type))
                : new VanillaReferenceCounted(onRelease, type);
    }

    // throws IllegalStateException
    default void throwExceptionIfReleased() throws ClosedIllegalStateException {
        if (refCount() <= 0)
            throw new ClosedIllegalStateException("Released");
    }

    void warnAndReleaseIfNotReleased() throws ClosedIllegalStateException;

    void throwExceptionIfNotReleased() throws IllegalStateException;

    StackTrace createdHere();
}
