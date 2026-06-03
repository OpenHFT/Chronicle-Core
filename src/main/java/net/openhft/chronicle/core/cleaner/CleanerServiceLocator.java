/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.annotation.TargetMajorVersion;
import net.openhft.chronicle.core.internal.cleaner.ReflectionBasedByteBufferCleanerService;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;

import java.nio.ByteBuffer;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.LongSupplier;

/**
 * A utility class to locate the appropriate {@link ByteBufferCleanerService} implementation.
 *
 * <p>This class employs the ServiceLoader mechanism to dynamically locate and instantiate
 * an implementation of the ByteBufferCleanerService interface. It selects the most suitable
 * implementation based on the impact level and the Java major version compatibility.
 *
 * <p>If no suitable service provider is found, it falls back to a reflection-based cleaner service.
 *
 * <p>This class is thread-safe and ensures that only a single instance of ByteBufferCleanerService
 * is created and shared among all callers.
 */
public final class CleanerServiceLocator {

    private static boolean initialised = false;
    private static ByteBufferCleanerService instance;

    // Suppresses default constructor, ensuring non-instantiability.
    private CleanerServiceLocator() {
    }

    /**
     * Returns a singleton instance of {@link ByteBufferCleanerService}.
     *
     * <p>This method uses the ServiceLoader mechanism to dynamically locate and instantiate
     * an implementation of the ByteBufferCleanerService interface. If no suitable service
     * provider is found, it falls back to a reflection-based cleaner service.
     *
     * @return The singleton instance of ByteBufferCleanerService.
     */
    public static synchronized ByteBufferCleanerService cleanerService() {
        if (!initialised) {
            final ServiceLoader<ByteBufferCleanerService> available =
                    ServiceLoader.load(ByteBufferCleanerService.class,
                            Thread.currentThread().getContextClassLoader());

            ByteBufferCleanerService cleanerService = null;

            try {
                for (final ByteBufferCleanerService next : available) {
                    if (isAllowedInThisMajorVersion(next) &&
                            (cleanerService == null || next.impact().compareTo(cleanerService.impact()) < 0)) {
                        cleanerService = next;
                    }
                }
            } catch (ServiceConfigurationError e) {
                Jvm.error().on(CleanerServiceLocator.class, "Error while trying to load service providers", e);
            }

            if (cleanerService == null) {
                cleanerService = new ReflectionBasedByteBufferCleanerService();
                Jvm.warn().on(CleanerServiceLocator.class, "Unable to find suitable cleaner service, falling back to using reflection");
            }

            verifySelectedCleaner(cleanerService);

            instance = cleanerService;
            initialised = true;
        }

        return instance;
    }

    /**
     * Determines if the given {@link ByteBufferCleanerService} is compatible with the current Java major version.
     *
     * @param svc The ByteBufferCleanerService to check.
     * @return true if the service is compatible, false otherwise.
     */
    private static boolean isAllowedInThisMajorVersion(final ByteBufferCleanerService svc) {
        final TargetMajorVersion version = svc.getClass().getDeclaredAnnotation(TargetMajorVersion.class);

        return version == null ||
                version.majorVersion() == TargetMajorVersion.ANY_VERSION ||
                version.majorVersion() == Jvm.majorVersion() ||
                (version.includeNewer() && Jvm.majorVersion() > version.majorVersion()) ||
                (version.includeOlder() && Jvm.majorVersion() < version.majorVersion());
    }

    /**
     * Logs the selected cleaner and warns if it cannot actually free direct memory, surfacing a
     * silent off-heap leak. Diagnostic only: it never changes the selected service.
     */
    static void verifySelectedCleaner(final ByteBufferCleanerService svc) {
        verifySelectedCleaner(svc, Jvm::usedDirectMemory);
    }

    // usedDirectMemory is injectable so the accounting-unavailable path is testable.
    static void verifySelectedCleaner(final ByteBufferCleanerService svc, final LongSupplier usedDirectMemory) {
        final String name = svc.getClass().getName();
        try {
            if (svc.impact() == ByteBufferCleanerService.Impact.UNAVAILABLE) {
                warnLeakingCleaner(name);
                return;
            }

            final int probeSize = 1 << 12;
            final long before = usedDirectMemory.getAsLong();
            final ByteBuffer buffer = ByteBuffer.allocateDirect(probeSize);
            final long afterAllocation = usedDirectMemory.getAsLong();
            svc.clean(buffer);
            final long afterClean = usedDirectMemory.getAsLong();

            if (afterAllocation <= before) {
                // accounting unavailable: can't prove a leak, so stay quiet
                Jvm.debug().on(CleanerServiceLocator.class, "Selected ByteBuffer cleaner: " + name +
                        " (impact=" + svc.impact() + "); effectiveness unverified as direct-memory accounting is unavailable");
                return;
            }

            if (afterClean < afterAllocation) {
                Jvm.debug().on(CleanerServiceLocator.class, "Selected ByteBuffer cleaner: " + name +
                        " (impact=" + svc.impact() + ", verified to free direct memory)");
            } else {
                warnLeakingCleaner(name);
            }
        } catch (Throwable t) {
            // diagnostic only — must not affect selection
            Jvm.debug().on(CleanerServiceLocator.class, "Could not verify ByteBuffer cleaner " + name, t);
        }
    }

    private static void warnLeakingCleaner(final String name) {
        Jvm.warn().on(CleanerServiceLocator.class, "Selected ByteBuffer cleaner " + name +
                " does not free direct memory; direct ByteBuffers will leak until GC.");
    }
}
