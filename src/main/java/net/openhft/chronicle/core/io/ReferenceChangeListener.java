package net.openhft.chronicle.core.io;

import org.jetbrains.annotations.Nullable;

/**
 * An interface to be notified when references to a {@link ReferenceCounted} are added, removed or transferred
 */
public interface ReferenceChangeListener {

    /**
     * A reference was added
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
     * A reference was removed
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
     * A reference was transferred
     * <p>
     * WARNING: This may be called from a synchronized block in DualReferenceCounted, so be careful
     * what you do here that might introduce a deadlock!
     *
     * @param referenceCounted The ReferenceCounted on which the reference was transferred
     * @param fromOwner        The owner the reference was transferred from
     * @param toOwner          The owner the reference was transferred to
     */
    default void onReferenceTransferred(ReferenceCounted referenceCounted, ReferenceOwner fromOwner, ReferenceOwner toOwner) {
    }
}
