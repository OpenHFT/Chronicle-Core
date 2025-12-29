/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.annotation.TargetMajorVersion;
import net.openhft.chronicle.core.internal.cleaner.ReflectionBasedByteBufferCleanerService;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * A utility class that locates the appropriate {@link ByteBufferCleanerService} implementation for the current runtime.
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
     * Returns the singleton {@link ByteBufferCleanerService} selected for the current JVM and impact level.
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
}
