/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * An interface for monitoring and tracing the reference counting of objects. It extends the {@link ReferenceCounted}
 * interface by providing additional methods for tracing and throwing exceptions in certain conditions.
 * This can be helpful in debugging and identifying issues related to resource management and reference counting.
 */
public interface ReferenceCountedTracer extends ReferenceCounted {

    /**
     * Factory method that creates a new instance of {@link ReferenceCountedTracer}. The instance returned
     * is based on the resource tracing configuration of the JVM.
     *
     * @param onRelease The {@link Runnable} that will be executed when the object is released.
     * @param uniqueId  A {@link Supplier} of unique identifiers for the object. It should provide a unique
     *                  string identifier for each invocation.
     * @param type      The {@link Class} representing the type of the object being reference counted.
     * @return A new instance of {@link ReferenceCountedTracer}.
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
     * @throws ClosedIllegalStateException If the object has been released (i.e., its reference count is less than or equal to 0).
     */
    // TODO move implementation to sub-classes in x.24
    default void throwExceptionIfReleased() throws ClosedIllegalStateException {
        if (refCount() <= 0)
            throw new ClosedIllegalStateException("Released");
    }

    /**
     * Releases any remaining references and logs a warning if there were any references to release.
     * <p>
     * This method is intended to be called by a finalizer or in a test to confirm that references are being released correctly.
     * </p>
     * <p>
     * Note: This method will not trigger any {@link ReferenceChangeListener}s as it is mainly used for sanity checks.
     * </p>
     *
     * @throws ClosedIllegalStateException If the object has been released.
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
