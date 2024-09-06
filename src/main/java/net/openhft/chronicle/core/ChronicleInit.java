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

/**
 * Handles application code which must be loaded first or run, potentially overriding system properties.
 * <p>
 * A {@link Runnable} fully qualified class name may be provided via the {@code chronicle.init.runnable} property,
 * to be run before the initialization of the {@link Jvm} class.
 * <p>
 * A {@link Runnable} fully qualified class name may be provided via the {@code chronicle.postinit.runnable} property,
 * to be run last in the {@link Jvm} initialization static block.
 * <p>
 * Alternatively, a {@link ChronicleInitRunnable} implementing class name may be listed in the
 * {@code META-INF/services/net.openhft.chronicle.core.ChronicleInitRunnable} file in any JAR on the classpath to be
 * discovered via the {@link ServiceLoader} JVM facility. It may provide both init (via {@link Runnable#run()})
 * and post-init (via {@link ChronicleInitRunnable#postInit()}, see above).
 * <p>
 * This class may also be replaced with a different concrete implementation. Code should reside in a static block to
 * be run once. It should contain an empty static init() method called to trigger class loading.
 */
public final class ChronicleInit {
    // Property keys for init and post-init runnables
    public static final String CHRONICLE_INIT_CLASS = "chronicle.init.runnable";
    public static final String CHRONICLE_POSTINIT_CLASS = "chronicle.postinit.runnable";

    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private ChronicleInit() {
        // Prevent instantiation
    }

    // Static block to initialize runnables specified by system properties or ServiceLoader
    static {
        // Initialization logic for classes specified via system properties
        String initRunnableClass = System.getProperty(CHRONICLE_INIT_CLASS);
        if (initRunnableClass != null && !initRunnableClass.isEmpty()) {
            try {
                // Load and instantiate the specified Runnable class
                Class<? extends Runnable> descendant = (Class<? extends Runnable>) Class.forName(initRunnableClass);
                Runnable chronicleInit = descendant.newInstance();
                // Execute the init method of the Runnable
                chronicleInit.run();
            } catch (Exception ex) {
                // Print stack trace to System.err since logging may not be initialized yet
                ex.printStackTrace();
            }
        }

        // Load and execute all ChronicleInitRunnable implementations found via ServiceLoader
        try {
            ServiceLoader<ChronicleInitRunnable> runnableLoader = ServiceLoader.load(ChronicleInitRunnable.class);

            for (Runnable runnable : runnableLoader) {
                try {
                    runnable.run();
                } catch (Exception ex) {
                    // Print stack trace to System.err
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            // Print stack trace to System.err
            ex.printStackTrace();
        }
    }

    /**
     * A method intended to trigger class loading and static initialization.
     * <p>
     * This method is designed to be idempotent and may be called multiple times without any side effects.
     */
    static void init() {
        // No operation; serves only to trigger class loading
    }

    /**
     * Executes post-initialization logic. This should be run only once in the static block of the {@link Jvm} class.
     * <p>
     * This method loads and executes any post-initialization runnables specified by system properties or
     * discovered via the {@link ServiceLoader}.
     */
    static void postInit() {
        // Post-initialization logic for classes specified via system properties
        String initRunnableClass = System.getProperty(CHRONICLE_POSTINIT_CLASS);
        if (initRunnableClass != null && !initRunnableClass.isEmpty()) {
            try {
                // Load and instantiate the specified Runnable class
                Class<? extends Runnable> descendant = (Class<? extends Runnable>) Class.forName(initRunnableClass);
                Runnable chronicleInit = descendant.newInstance();
                // Execute the post-init method of the Runnable
                chronicleInit.run();
            } catch (Exception ex) {
                // Print stack trace to System.err since logging may not be initialized yet
                ex.printStackTrace();
            }
        }

        // Load and execute all ChronicleInitRunnable implementations found via ServiceLoader
        try {
            ServiceLoader<ChronicleInitRunnable> runnableLoader = ServiceLoader.load(ChronicleInitRunnable.class);

            for (ChronicleInitRunnable runnable : runnableLoader) {
                try {
                    // Execute post-initialization logic
                    runnable.postInit();
                } catch (Exception ex) {
                    // Print stack trace to System.err
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            // Print stack trace to System.err
            ex.printStackTrace();
        }
    }
}
