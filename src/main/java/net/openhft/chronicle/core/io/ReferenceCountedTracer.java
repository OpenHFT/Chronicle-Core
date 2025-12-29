/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Extends {@link ReferenceCounted} with tracing utilities.
 * Implementations record which {@link ReferenceOwner} reserved or released a
 * resource and can report where an unexpected release occurred. Tracing is
 * useful when diagnosing {@link ClosedIllegalStateException} or
 * {@link ThreadingIllegalStateException} thrown by misused resources.
 */
public interface ReferenceCountedTracer extends ReferenceCounted {

    /**
     * Factory method that creates a new instance of . The instance returned
     * is based on the resource tracing configuration of the JVM.
     *
     * @param onRelease The {@link Runnable} that will be executed when the object is released.
     * @param uniqueId  A {@link Supplier} of unique identifiers for the object. It should provide a unique
     *                  string identifier for each invocation.
     * @param type      The {@link Class} representing the type of the object being reference counted.
     * @return A new instance of .
     */
    @NotNull
    static ReferenceCountedTracer onReleased(final Runnable onRelease, Supplier<String> uniqueId, Class<?> type) {
        return Jvm.isResourceTracing()
                ? new TracingReferenceCounted(onRelease, uniqueId.get(), type)
                : new VanillaReferenceCounted(onRelease, type);
    }

    /**
     * Throws an exception if the object has been released.
     *
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    // TODO move implementation to sub-classes in x.24
    default void throwExceptionIfReleased() throws ClosedIllegalStateException {
        if (refCount() <= 0)
            throw new ClosedIllegalStateException("Reference-counted resource already released");
    }

    /**
     * Releases any remaining references and logs a warning if there were any references to release.
     * <p>
     * This method is intended to be called by a finalizer or in a test to confirm that references are being released correctly.
     * <p>
     * Note: This method will not trigger any {@link ReferenceChangeListener}s as it is mainly used for sanity checks.
     *
     * @throws ClosedIllegalStateException If the object has not been released.
     */
    void warnAndReleaseIfNotReleased() throws ClosedIllegalStateException;

    /**
     * Throws an exception if the object has not been released.
     *
     * @throws IllegalStateException If the object has not been released (i.e., its reference count is greater than 0).
     */
    void throwExceptionIfNotReleased() throws IllegalStateException;

    /**
     * Retrieves the stack trace of the point where the object was created.
     * This can be useful for debugging and tracking down the origin of the object.
     *
     * @return The stack trace where the object was created.
     */
    StackTrace createdHere();
}
