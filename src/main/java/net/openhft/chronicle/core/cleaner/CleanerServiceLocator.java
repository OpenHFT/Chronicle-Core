/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.annotation.TargetMajorVersion;
import net.openhft.chronicle.core.internal.ClassUtil;
import net.openhft.chronicle.core.internal.cleaner.Jdk9ByteBufferCleanerService;
import net.openhft.chronicle.core.internal.cleaner.ReflectionBasedByteBufferCleanerService;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Function;

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
     * Checks whether the selected service invokes the probe buffer's JDK cleaner.
     * Diagnostic only: it never changes the selected service or infers success from global memory usage.
     */
    static void verifySelectedCleaner(final ByteBufferCleanerService svc) {
        verifySelectedCleaner(svc, buffer -> Jvm.getValue(buffer, "cleaner"));
    }

    static void verifySelectedCleaner(final ByteBufferCleanerService svc,
                                      final Function<ByteBuffer, Object> cleanerLookup) {
        final String name = svc.getClass().getName();
        ByteBuffer buffer = null;
        try {
            if (svc.impact() == ByteBufferCleanerService.Impact.UNAVAILABLE) {
                warnLeakingCleaner(name);
                return;
            }

            buffer = ByteBuffer.allocateDirect(1 << 12);
            final Object cleaner = cleanerLookup.apply(buffer);
            if (isCleanerInvoked(cleaner))
                throw new IllegalStateException("Probe buffer was already cleaned");

            svc.clean(buffer);

            // The JDK cleaner unlinks itself before running its deallocator. Check this buffer's
            // invocation state after the synchronous clean call, not process-wide memory totals.
            // This is not an independent measurement of native deallocation completion.
            if (isCleanerInvoked(cleaner)) {
                Jvm.debug().on(CleanerServiceLocator.class, "Selected ByteBuffer cleaner: " + name +
                        " (impact=" + svc.impact() + ", verified to invoke the direct buffer cleaner)");
            } else {
                warnLeakingCleaner(name);
            }
        } catch (Throwable t) { // NOSONAR — diagnostic probe must never destabilise selection, incl. Errors
            Jvm.error().on(CleanerServiceLocator.class, "Could not verify ByteBuffer cleaner " + name, t);
        } finally {
            // Keep the buffer reachable through observation and release it even for no-op or
            // throwing providers. The JDK cleaner is idempotent; never run its deallocator directly.
            if (buffer != null) {
                try {
                    final ByteBufferCleanerService cleanup = Jvm.isJava9Plus()
                            ? new Jdk9ByteBufferCleanerService()
                            : new ReflectionBasedByteBufferCleanerService();
                    cleanup.clean(buffer);
                } catch (Throwable t) {
                    Jvm.error().on(CleanerServiceLocator.class, "Could not release ByteBuffer cleaner probe", t);
                }
            }
        }
    }

    /**
     * Recognises only the JDK cleaner list states used by direct buffers. In particular,
     * Reference.next is queue linkage and must not be mistaken for legacy Cleaner.next.
     * Unknown layouts leave verification inconclusive rather than identifying a leaking service.
     */
    static boolean isCleanerInvoked(final Object cleaner) throws ReflectiveOperationException {
        final Class<?> type = cleaner.getClass();
        switch (type.getName()) {
            case "sun.misc.Cleaner":
            case "jdk.internal.ref.Cleaner":
                return declaredCleanerState(cleaner, "next", type.getName()) == cleaner;
            case "java.nio.BufferCleaner$PhantomCleaner":
                return declaredCleanerState(cleaner, "node", "java.nio.BufferCleaner$CleanerList$Node") == null;
            default:
                throw new UnsupportedOperationException("Unrecognised direct buffer cleaner layout: " + type.getName());
        }
    }

    private static Object declaredCleanerState(final Object cleaner, final String name,
                                              final String expectedType) throws ReflectiveOperationException {
        final Field field = cleaner.getClass().getDeclaredField(name);
        if (!field.getType().getName().equals(expectedType))
            throw new UnsupportedOperationException("Unrecognised direct buffer cleaner state: " + field);
        ClassUtil.setAccessible(field);
        return field.get(cleaner);
    }

    private static void warnLeakingCleaner(final String name) {
        Jvm.warn().on(CleanerServiceLocator.class, "Selected ByteBuffer cleaner " + name +
                " does not free direct memory.");
    }
}
