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
 * An interface for tracing the reference counting of objects.
 * It extends the {@link ReferenceCounted} interface and provides additional tracing and exception-throwing methods.
 */
public interface ReferenceCountedTracer extends ReferenceCounted {

    /**
     * Creates a new instance of {@link ReferenceCountedTracer} based on the resource tracing configuration.
     *
     * @param onRelease The {@link Runnable} to be executed upon release of the object.
     * @param uniqueId  The unique identifier for the object.
     * @param type      The class type of the object.
     * @return A new instance of {@link ReferenceCountedTracer} based on the resource tracing configuration.
     */
    @NotNull
    static ReferenceCountedTracer onReleased(final Runnable onRelease, Supplier<String> uniqueId, Class<?> type) {
        return Jvm.isResourceTracing()
                ? new TracingReferenceCounted(onRelease, uniqueId.get(), type)
                : new VanillaReferenceCounted(onRelease, type);
    }

    /**
     * @throws ClosedIllegalStateException If the object has been released.
     */
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

    /**
     * @throws IllegalStateException If the object has not been released.
     */
    void throwExceptionIfNotReleased() throws IllegalStateException;

    /**
     * @return The stack trace where the object was created.
     */
    StackTrace createdHere();
}
