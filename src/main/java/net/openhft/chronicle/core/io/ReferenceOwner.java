package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;

public interface ReferenceOwner {
    ReferenceOwner INIT = new VanillaReferenceOwner("init");

    static ReferenceOwner temporary(String name) {
        return Jvm.isResourceTracing() ? new VanillaReferenceOwner(name) : INIT;
    }

    default int referenceId() {
        return System.identityHashCode(this);
    }

    default String referenceName() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(referenceId());
    }
}
