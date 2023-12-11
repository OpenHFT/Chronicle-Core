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

package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.AbstractReferenceCounted;
import net.openhft.chronicle.core.io.ReferenceCounted;
import net.openhft.chronicle.core.util.WeakIdentityHashMap;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static net.openhft.chronicle.core.io.AbstractCloseable.*;

/**
 * Utility class for managing reference counted resources and related operations.
 */
public final class ReferenceCountedUtils {
    private static final AtomicReference<Set<AbstractReferenceCounted>> REFERENCE_COUNTED_SET
            = new AtomicReference<>();

    private ReferenceCountedUtils() {
    }

    /**
     * Adds a reference counted resource to the set of tracked resources.
     *
     * @param referenceCounted The reference counted resource to add.
     */
    public static void add(AbstractReferenceCounted referenceCounted) {
        final Set<AbstractReferenceCounted> set = REFERENCE_COUNTED_SET.get();
        if (set != null)
            set.add(referenceCounted);
    }

    /**
     * Enables tracing of reference counted resources.
     * Tracked resources will be stored in a set.
     * This method should be called to enable tracing before using reference counted resources.
     */
    public static void enableReferenceTracing() {
        enableCloseableTracing();
        REFERENCE_COUNTED_SET.set(
                Collections.synchronizedSet(
                        Collections.newSetFromMap(
                                new WeakIdentityHashMap<>())));
    }

    /**
     * Disables tracing of reference counted resources.
     * Tracked resources will no longer be stored in the set.
     * This method should be called to disable tracing when no longer needed.
     */
    public static void disableReferenceTracing() {
        disableCloseableTracing();
        REFERENCE_COUNTED_SET.set(null);
    }

    /**
     * Asserts that all reference counted resources have been released.
     * This method checks if there are any remaining open references to the tracked resources.
     * If any references are found, an AssertionError is thrown with details of the open resources.
     * If reference tracing is disabled, a warning is logged and the method returns.
     *
     * @throws AssertionError If any reference counted resource has not been released.
     */
    public static void assertReferencesReleased() {
        final Set<AbstractReferenceCounted> traceSet = REFERENCE_COUNTED_SET.get();
        if (traceSet == null) {
            Jvm.warn().on(ReferenceCountedUtils.class, "Reference tracing disabled");
            return;
        }

        assertCloseablesClosed();

        AssertionError openFiles = new AssertionError("Reference counted not released");
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (traceSet) {
            for (AbstractReferenceCounted key : traceSet) {
                if (key == null || key.refCount() == 0)
                    continue;

                try {
                    key.throwExceptionIfNotReleased();
                } catch (Exception e) {
                    openFiles.addSuppressed(e);
                }
            }
        }
        if (openFiles.getSuppressed().length > 0)
            throw openFiles;
    }

    /**
     * Stops monitoring a reference counted resource.
     * This method removes the resource from the set of tracked resources and marks it as unmonitored.
     *
     * @param counted The reference counted resource to unmonitor.
     */
    public static void unmonitor(ReferenceCounted counted) {
        final Set<AbstractReferenceCounted> set = REFERENCE_COUNTED_SET.get();
        if (counted instanceof AbstractReferenceCounted) {
            if (set != null) {
                // The set contains <AbstractReferenceCounted> so, "counted" must be an instance of that
                // for remove to have any effect.
                set.remove(counted);
            }
            ((AbstractReferenceCounted) counted).referenceCountedUnmonitored(true);
        }
    }
}
