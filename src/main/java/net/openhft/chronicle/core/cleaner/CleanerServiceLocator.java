/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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
import net.openhft.chronicle.core.cleaner.impl.reflect.ReflectionBasedByteBufferCleanerService;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public final class CleanerServiceLocator {
    private static boolean initialised = false;
    private static ByteBufferCleanerService instance;

    // Suppresses default constructor, ensuring non-instantiability.
    private CleanerServiceLocator() {
    }

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
                Jvm.warn().on(CleanerServiceLocator.class,  "Unable to find suitable cleaner service, falling back to using reflection");
            }

            instance = cleanerService;
            initialised = true;
        }

        return instance;
    }

    private static boolean isAllowedInThisMajorVersion(final ByteBufferCleanerService svc) {
        final TargetMajorVersion version = svc.getClass().getDeclaredAnnotation(TargetMajorVersion.class);

        return version == null ||
                version.majorVersion() == TargetMajorVersion.ANY_VERSION ||
                version.majorVersion() == Jvm.majorVersion() ||
                (version.includeNewer() && Jvm.majorVersion() > version.majorVersion()) ||
                (version.includeOlder() && Jvm.majorVersion() < version.majorVersion());
    }
}