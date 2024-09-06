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

/**
 * A {@link ByteBufferCleanerService} implementation that uses reflection to invoke the cleaner on {@link ByteBuffer} instances.
 * <p>
 * This implementation works across Java 8 and Java 9+ environments by dynamically detecting and using
 * the appropriate cleaner mechanism. It supports the cleaning of direct buffers to free up native memory.
 * </p>
 * <p>
 * For Java 9+, the cleaner class used is {@code jdk.internal.ref.Cleaner}, whereas for Java 8, it uses {@code sun.misc.Cleaner}.
 * </p>
 * <p>
 * Users should ensure the appropriate JVM arguments are set, such as:
 * {@code --illegal-access=permit --add-exports java.base/jdk.internal.ref=ALL-UNNAMED}, to allow access to internal classes.
 * </p>
 */
public final class ReflectionBasedByteBufferCleanerService implements ByteBufferCleanerService {

    // Class names for Java 8 and Java 9+ cleaner classes
    private static final String JDK8_CLEANER_CLASS_NAME = "sun.misc.Cleaner";
    private static final String JDK9_CLEANER_CLASS_NAME = "jdk.internal.ref.Cleaner";

    // Method handles for invoking the cleaner
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
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException e) {
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

    /**
     * Cleans the specified direct {@link ByteBuffer} by invoking the cleaner using reflection.
     * <p>
     * If the cleaner is not available, a warning will be logged, and the buffer will remain uncleaned until garbage collection occurs.
     * </p>
     *
     * @param buffer The direct {@link ByteBuffer} to clean.
     * @throws RuntimeException If any error occurs during the invocation of the cleaner method.
     */
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

    /**
     * Indicates the impact of using this cleaner service.
     * <p>
     * The impact could be either {@link Impact#SOME_IMPACT} or {@link Impact#UNAVAILABLE}, depending on whether the cleaner could be initialized.
     * </p>
     *
     * @return The {@link Impact} of using this cleaner service.
     */
    @Override
    public Impact impact() {
        return IMPACT;
    }
}
