package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.cleaner.impl.reflect.ReflectionBasedByteBufferCleanerService;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public final class CleanerServiceLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanerServiceLocator.class);
    private static boolean initialised = false;
    private static ByteBufferCleanerService instance;

    public static synchronized ByteBufferCleanerService cleanerService() {
        if (!initialised) {
            final ServiceLoader<ByteBufferCleanerService> available =
                    ServiceLoader.load(ByteBufferCleanerService.class,
                            Thread.currentThread().getContextClassLoader());

            ByteBufferCleanerService cleanerService = null;
            try {
                for (final ByteBufferCleanerService next : available) {
                    if (cleanerService == null || next.impact() < cleanerService.impact()) {
                        cleanerService = next;
                    }
                }
            } catch (ServiceConfigurationError e) {
                LOGGER.warn("Error while trying to load service providers", e);
            }

            if (cleanerService == null) {
                cleanerService = new ReflectionBasedByteBufferCleanerService();
                LOGGER.warn("Unable to find suitable cleaner service, falling back to using reflection");
            }

            instance = cleanerService;
            initialised = true;
        }

        return instance;
    }
}