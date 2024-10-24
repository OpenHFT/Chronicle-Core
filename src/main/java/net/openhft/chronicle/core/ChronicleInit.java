/*
 * Copyright 2016-2022 chronicle.software
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

package net.openhft.chronicle.core;

import java.util.ServiceLoader;

import static net.openhft.chronicle.core.internal.Bootstrap.uncheckedCast;

/**
 * Handles application code which must be loaded first/run and may override system properties.
 * <p>
 * A {@link Runnable} fully qualified class name may be provided via {@code chronicle.init.runnable} property,
 * to be run before initialization of {@link Jvm} class.
 * <p>
 * A {@link Runnable} fully qualified class name may be provided via {@code chronicle.postinit.runnable} property,
 * to be run last in the  {@link Jvm} initialization static block.
 * <p>
 * Alternatively, a {@link ChronicleInitRunnable} implementing class name may be listed in
 * {@code META-INF/services/net.openhft.chronicle.core.ChronicleInitRunnable} file in any JAR in classpath to be
 * discovered via {@link ServiceLoader} JVM facility. It may provide both init (via {@link Runnable#run()})
 * and post-init (via {@link ChronicleInitRunnable#postInit()}, see above).
 * <p>
 * This class may also be replaced with different concrete implementation. Code should reside in a static block to
 * be run once. It should contain empty static init() method called to trigger class load.
 */
public final class ChronicleInit {
    public static final String CHRONICLE_INIT_CLASS = "chronicle.init.runnable";
    public static final String CHRONICLE_POSTINIT_CLASS = "chronicle.postinit.runnable";

    private ChronicleInit() {
        // Suppresses default constructor, ensuring non-instantiability.
    }

    static {
        // Jvm#getProperty() does not make sense here - not initialized yet
        String initRunnableClass = System.getProperty(CHRONICLE_INIT_CLASS);
        if (initRunnableClass != null && !initRunnableClass.isEmpty()) {
            try {
                Class<? extends Runnable> descendant = uncheckedCast(Class.forName(initRunnableClass));
                Runnable chronicleInit = descendant.getConstructor().newInstance();
                chronicleInit.run();
            } catch (Exception ex) {
                // System.err since the logging subsystem may not be up at this point
                ex.printStackTrace();
            }
        }

        try {
            ServiceLoader<ChronicleInitRunnable> runnableLoader = ServiceLoader.load(ChronicleInitRunnable.class);

            for (Runnable runnable : runnableLoader) {
                try {
                    runnable.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * May be run multiple times, supposed to be idempotent
     */
    @SuppressWarnings("EmptyMethod")
    static void init() {
        // No-op unless class is replaced
    }

    /**
     * Should be only run once by Jvm.class static block
     */
    static void postInit() {
        // Jvm#getProperty() does not make sense here - not initialized yet
        String initRunnableClass = System.getProperty(CHRONICLE_POSTINIT_CLASS);
        if (initRunnableClass != null && !initRunnableClass.isEmpty()) {
            try {
                Class<? extends Runnable> descendant = uncheckedCast(Class.forName(initRunnableClass));
                Runnable chronicleInit = descendant.getConstructor().newInstance();
                chronicleInit.run();
            } catch (Exception ex) {
                // System.err since the logging subsystem may not be up at this point
                ex.printStackTrace();
            }
        }

        try {
            ServiceLoader<ChronicleInitRunnable> runnableLoader = ServiceLoader.load(ChronicleInitRunnable.class);

            for (ChronicleInitRunnable runnable : runnableLoader) {
                try {
                    runnable.postInit();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
