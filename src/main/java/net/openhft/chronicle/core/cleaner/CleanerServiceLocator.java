/*
 * Copyright 2016-2020 chronicle.software
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

package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.annotation.TargetMajorVersion;
import net.openhft.chronicle.core.internal.cleaner.ReflectionBasedByteBufferCleanerService;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * A utility class to locate and return the appropriate {@link ByteBufferCleanerService} implementation.
 *
 * <p>This class uses the {@link ServiceLoader} mechanism to find an implementation of
 * {@link ByteBufferCleanerService}. It selects the implementation with the lowest impact level
 * that is compatible with the current Java major version. If no suitable implementation is found,
 * it falls back to a reflection-based cleaner service.
 *
 * <p>This class is designed to be thread-safe, ensuring that only one instance of {@link ByteBufferCleanerService}
 * is created and reused.
 */
public final class CleanerServiceLocator {

    /**
     * A flag to indicate whether the cleaner service has been initialized.
     */
    private static boolean initialised = false;

    /**
     * The singleton instance of {@link ByteBufferCleanerService}.
     */
    private static ByteBufferCleanerService instance;

    // Suppresses default constructor, ensuring non-instantiability.
    private CleanerServiceLocator() {
    }

    /**
     * Returns a singleton instance of {@link ByteBufferCleanerService}.
     *
     * <p>This method uses the {@link ServiceLoader} mechanism to dynamically locate and instantiate
     * an implementation of the {@link ByteBufferCleanerService}. If no suitable service
     * provider is found, it falls back to a reflection-based cleaner service.
     *
     * <p>This method ensures that the service is only initialized once and the same instance is
     * returned on subsequent calls.
     *
     * @return The singleton instance of {@link ByteBufferCleanerService}.
     */
    public static synchronized ByteBufferCleanerService cleanerService() {
        // Initialize only once
        if (!initialised) {
            // Load available ByteBufferCleanerService implementations
            final ServiceLoader<ByteBufferCleanerService> available =
                    ServiceLoader.load(ByteBufferCleanerService.class,
                            Thread.currentThread().getContextClassLoader());

            ByteBufferCleanerService cleanerService = null;

            try {
                // Iterate through available implementations to find the most appropriate one
                for (final ByteBufferCleanerService next : available) {
                    // Check if the service is allowed for the current Java version
                    if (isAllowedInThisMajorVersion(next) &&
                            (cleanerService == null || next.impact().compareTo(cleanerService.impact()) < 0)) {
                        cleanerService = next;  // Choose the cleaner with lower impact
                    }
                }
            } catch (ServiceConfigurationError e) {
                // Log an error if something goes wrong with service loading
                Jvm.error().on(CleanerServiceLocator.class, "Error while trying to load service providers", e);
            }

            // If no suitable service was found, fall back to reflection-based cleaner
            if (cleanerService == null) {
                cleanerService = new ReflectionBasedByteBufferCleanerService();
                Jvm.warn().on(CleanerServiceLocator.class, "Unable to find suitable cleaner service, falling back to using reflection");
            }

            instance = cleanerService;
            initialised = true;  // Mark initialization as complete
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

        // Check if the service is allowed based on its @TargetMajorVersion annotation
        return version == null ||
                version.majorVersion() == TargetMajorVersion.ANY_VERSION ||
                version.majorVersion() == Jvm.majorVersion() ||
                (version.includeNewer() && Jvm.majorVersion() > version.majorVersion()) ||
                (version.includeOlder() && Jvm.majorVersion() < version.majorVersion());
    }
}
