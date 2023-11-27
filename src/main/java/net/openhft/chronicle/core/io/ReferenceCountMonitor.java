package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.internal.ReferenceCountedUtils;

import java.util.Set;

public class ReferenceCountMonitor {
    static volatile Set<AbstractReferenceCounted> referenceCountedSet;

    /**
     * Enables reference tracing.
     */
    public static void enableReferenceTracing() {
        ReferenceCountedUtils.enableReferenceTracing();
    }

    /**
     * Disables reference tracing.
     * <p>
     * <b>NOTE:</b> The resources will still be released appropriately, however if detailed tracing won't be recorded
     */
    public static void disableReferenceTracing() {
        ReferenceCountedUtils.disableReferenceTracing();
    }

    /**
     * Asserts that all references have been released.
     */
    public static void assertReferencesReleased() {
        ReferenceCountedUtils.assertReferencesReleased();
    }

    /**
     * Marks a reference-counted resource as unmonitored.
     * <p>
     * <b>NOTE:</b> The resource will still be released appropriately, however it won't give a warning if it is not.
     *
     * @param counted the resource to unmonitor.
     */
    public static void unmonitor(ReferenceCounted counted) {
        ReferenceCountedUtils.unmonitor(counted);
    }

}
