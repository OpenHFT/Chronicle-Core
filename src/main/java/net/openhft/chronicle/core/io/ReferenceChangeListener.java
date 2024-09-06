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

import org.jetbrains.annotations.Nullable;

/**
 * An interface to be notified when references to a {@link ReferenceCounted} are added, removed, or transferred.
 * Implement this interface to receive notifications about changes in the reference counts of objects.
 */
public interface ReferenceChangeListener {

    /**
     * Called when a reference is added to a {@link ReferenceCounted} object.
     * <p>
     * WARNING: This may be called from a synchronized block in DualReferenceCounted, so be careful
     * what you do here that might introduce a deadlock!
     *
     * @param referenceCounted The ReferenceCounted to which the reference was added
     * @param referenceOwner   The owner of the reference added
     */
    default void onReferenceAdded(ReferenceCounted referenceCounted, ReferenceOwner referenceOwner) {
    }

    /**
     * Called when a reference is removed from a {@link ReferenceCounted} object.
     * <p>
     * WARNING: This may be called from a synchronized block in DualReferenceCounted, so be careful
     * what you do here that might introduce a deadlock!
     *
     * @param referenceCounted The ReferenceCounted to which the reference was removed
     * @param referenceOwner   The owner whose reference was removed, or null if that is not known
     */
    default void onReferenceRemoved(@Nullable ReferenceCounted referenceCounted, ReferenceOwner referenceOwner) {
    }

    /**
     * Called when a reference is transferred from one owner to another for a {@link ReferenceCounted} object.
     * <p>
     * WARNING: This may be called from a synchronized block in DualReferenceCounted, so be careful
     * what you do here that might introduce a deadlock!
     *
     * @param referenceCounted The ReferenceCounted object on which the reference was transferred.
     * @param fromOwner        The previous owner from whom the reference was transferred.
     * @param toOwner          The new owner to whom the reference was transferred.
     */
    default void onReferenceTransferred(ReferenceCounted referenceCounted, ReferenceOwner fromOwner, ReferenceOwner toOwner) {
    }
}
