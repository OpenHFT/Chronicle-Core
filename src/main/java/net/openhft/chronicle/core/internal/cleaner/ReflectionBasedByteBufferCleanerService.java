//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.cleaner;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import net.openhft.chronicle.core.internal.util.DirectBufferUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ReflectionBasedByteBufferCleanerService implements ByteBufferCleanerService {
    private static final String JDK8_CLEANER_CLASS_NAME = "sun.misc.Cleaner";
    private static final String JDK9_CLEANER_CLASS_NAME = "jdk.internal.ref.Cleaner";

    private static final MethodHandle CLEANER_METHOD;
    private static final MethodHandle CLEAN_METHOD;
    private static final Impact IMPACT;

    static {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final String cleanerClassname = Jvm.isJava9Plus()
                ? JDK9_CLEANER_CLASS_NAME
                : JDK8_CLEANER_CLASS_NAME;
        MethodHandle cleaner = null;
        MethodHandle clean = null;
        Impact impact = Impact.SOME_IMPACT;
        try {
            final Class<?> cleanerClass = Class.forName(cleanerClassname);
            cleaner = lookup.findVirtual(DirectBufferUtil.directBufferClass(), "cleaner", MethodType.methodType(cleanerClass));
            clean = lookup.findVirtual(cleanerClass, "clean", MethodType.methodType(void.class));
        } catch (IllegalAccessError | NoSuchMethodException | ClassNotFoundException | IllegalAccessException e) {
            // Don't want to record this in tests so just send to slf4j
            final Logger logger = Logger.getLogger(ReflectionBasedByteBufferCleanerService.class.getName());
            if (logger.isLoggable(Level.WARNING)) {
                Logger.getLogger(ReflectionBasedByteBufferCleanerService.class.getName())
                        .warning("Make sure you have set the command line option " +
                                "\"--illegal-access=permit --add-exports java.base/jdk.internal.ref=ALL-UNNAMED\" " +
                                "to enable " + ReflectionBasedByteBufferCleanerService.class.getSimpleName());
            }
            impact = Impact.UNAVAILABLE;
        }
        CLEAN_METHOD = clean;
        CLEANER_METHOD = cleaner;
        IMPACT = impact;
    }

    @Override
    public void clean(final ByteBuffer buffer) {
        if (IMPACT == Impact.UNAVAILABLE) {
            // There might not be a cleaner after all.
            // See https://github.com/OpenHFT/Chronicle-Core/issues/140
            Logger.getLogger(ReflectionBasedByteBufferCleanerService.class.getName())
                    .warning("Cleaning is not available. The ByteBuffer 0x" + Integer.toHexString(System.identityHashCode(buffer)) +
                            " could not be explicitly cleaned and will thus linger until the next GC.");
        } else {
            try {
                final Object cleaner = CLEANER_METHOD.invoke(DirectBufferUtil.directBufferClass().cast(buffer));
                CLEAN_METHOD.invoke(cleaner);
            } catch (Throwable throwable) {
                throw Jvm.rethrow(throwable);
            }
        }
    }

    @Override
    public Impact impact() {
        return IMPACT;
    }
}
