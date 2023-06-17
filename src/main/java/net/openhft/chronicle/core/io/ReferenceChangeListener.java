package net.openhft.chronicle.core.io;

import org.jetbrains.annotations.Nullable;

/**
 * An interface to be notified when references to a {@link ReferenceCounted} are added, removed, or transferred.
 * Implement this interface to receive notifications about changes in the reference counts of objects.
 */
public interface ReferenceChangeListener {

    /**
     * Called when a reference is added to a {@link ReferenceCounted} object.
     *
     * @param referenceCounted The ReferenceCounted to which the reference was added
     * @param referenceOwner   The owner of the reference added
     */
    default void onReferenceAdded(ReferenceCounted referenceCounted, ReferenceOwner referenceOwner) {
    }

    /**
     * Called when a reference is removed from a {@link ReferenceCounted} object.
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
