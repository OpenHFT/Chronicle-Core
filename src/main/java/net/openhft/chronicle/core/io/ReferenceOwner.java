/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;

/**
 * Represents an entity that owns a reference, typically for resource management.
 * Implementations supply a stable identity used when reserving or releasing a
 * {@link ReferenceCounted} resource.
 * <p>
 * A {@code ReferenceOwner} is often passed to
 * {@link ReferenceCounted#reserve(ReferenceOwner)} and later to
 * {@link ReferenceCounted#release(ReferenceOwner)}. It enables tools such as
 * {@link BackgroundResourceReleaser} to identify which owner failed to release
 * its reservation.
 */
public interface ReferenceOwner {

    /**
     * A predefined {@link ReferenceOwner} instance representing an initial reference.
     * This instance is intended to be used as the first owner when none is provided.
     */
    ReferenceOwner INIT = new VanillaReferenceOwner("init");

    /**
     * A predefined {@link ReferenceOwner} instance representing a temporary reference.
     * This can be used as a placeholder owner for temporary references, typically those which
     * do not have a clearly defined long-term owner.
     */
    ReferenceOwner TMP = new VanillaReferenceOwner("tmp");

    /**
     * Creates and returns a temporary  with the given name.
     * <p>
     * When resource tracing is enabled, a new {@link VanillaReferenceOwner} is created with the specified name.
     * Otherwise, the predefined {@link ReferenceOwner#TMP} instance is returned, regardless of the provided name.
     *
     * @param name The name to be assigned to the temporary reference owner, used for identification and debugging purposes.
     * @return A temporary  instance.
     */
    static ReferenceOwner temporary(String name) {
        return Jvm.isResourceTracing() ? new VanillaReferenceOwner(name) : TMP;
    }

    /**
     * Returns a unique identifier (ID) for this reference owner. The ID can be used for tracking
     * and managing resources owned by this instance.
     * <p>
     * The default implementation uses the identity hash code of this reference owner instance as the ID.
     *
     * @return An integer representing the unique reference ID of this owner.
     */
    default int referenceId() {
        return System.identityHashCode(this);
    }

    /**
     * Returns a human-readable name for this reference owner. This name can be used for identification,
     * debugging, and logging purposes.
     * <p>
     * The default implementation generates a name using the simple class name of the reference owner,
     * followed by '@', and then the reference ID encoded in base 36.
     *
     * @return A string representing the human-readable name of this reference owner.
     */
    default String referenceName() {
        return getClass().getSimpleName() + "@" + Integer.toString(referenceId(), 36);
    }
}
