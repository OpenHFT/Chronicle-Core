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
 * An interface representing the owner of a reference.
 * It provides default methods for generating reference IDs and names.
 */
public interface ReferenceOwner {
    ReferenceOwner INIT = new VanillaReferenceOwner("init");

    /**
     * A predefined {@link ReferenceOwner} representing a temporary reference.
     * This can be used as a reference owner for temporary references.
     */
    ReferenceOwner TMP = new VanillaReferenceOwner("tmp");

    /**
     * Creates a temporary {@link ReferenceOwner} with the given name.
     * If resource tracing is enabled, a new {@link VanillaReferenceOwner} is created;
     * otherwise, the predefined {@link ReferenceOwner#TMP} is returned.
     *
     * @param name The name of the temporary reference owner.
     * @return A temporary {@link ReferenceOwner}.
     */
    static ReferenceOwner temporary(String name) {
        return Jvm.isResourceTracing() ? new VanillaReferenceOwner(name) : TMP;
    }

    /**
     * Returns the reference ID of this owner.
     * The default implementation uses the identity hash code of the owner object.
     *
     * @return The reference ID of this owner.
     */
    default int referenceId() {
        return System.identityHashCode(this);
    }

    /**
     * Returns the reference name of this owner.
     * The default implementation generates a name using the simple class name
     * and the reference ID encoded in base 36.
     *
     * @return The reference name of this owner.
     */
    default String referenceName() {
        return getClass().getSimpleName() + "@" + Integer.toString(referenceId(), 36);
    }
}
