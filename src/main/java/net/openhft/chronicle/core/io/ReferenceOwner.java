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

/**
 * Represents an entity that owns a reference, typically for resource management. This interface
 * provides default methods for generating unique reference IDs and human-readable names for reference owners.
 * <p>
 * This can be useful, for example, in scenarios where it's necessary to track the owners of resources
 * such as file handles, network sockets, or any other entities that need to be managed throughout their lifecycle.
 * </p>
 * <p>
 * Implementations of this interface can be used to associate owners with references,
 * making it easier to monitor, debug, and manage resource ownership and ensure that resources are released properly.
 * </p>
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
     * Creates and returns a temporary {@link ReferenceOwner} with the given name.
     * <p>
     * When resource tracing is enabled, a new {@link VanillaReferenceOwner} is created with the specified name.
     * Otherwise, the predefined {@link ReferenceOwner#TMP} instance is returned, regardless of the provided name.
     * </p>
     *
     * @param name The name to be assigned to the temporary reference owner, used for identification and debugging purposes.
     * @return A temporary {@link ReferenceOwner} instance.
     */
    static ReferenceOwner temporary(String name) {
        return Jvm.isResourceTracing() ? new VanillaReferenceOwner(name) : TMP;
    }

    /**
     * Returns a unique identifier (ID) for this reference owner. The ID can be used for tracking
     * and managing resources owned by this instance.
     * <p>
     * The default implementation uses the identity hash code of this reference owner instance as the ID.
     * </p>
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
     * </p>
     *
     * @return A string representing the human-readable name of this reference owner.
     */
    default String referenceName() {
        return getClass().getSimpleName() + "@" + Integer.toString(referenceId(), 36);
    }
}
