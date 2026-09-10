/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
@SuppressWarnings({"java:S4057", "java:S4507", "CallToPrintStackTrace", "CSClassForNameInput", "CSReflectiveConstructorInvoke", "CSServiceLoaderBoundary", "CSStartupHookClassName"})
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
                // CSCatchBroadException keep this broad catch because startup hook loading from the configured class name is best-effort and initialization should continue after reporting the failure.
            } catch (Exception ex) {
                // CSPrintStackTrace keep printStackTrace here because startup hook failures can happen before Chronicle logging is configured.
                ex.printStackTrace();
            }
        }

        try {
            ServiceLoader<ChronicleInitRunnable> runnableLoader = ServiceLoader.load(ChronicleInitRunnable.class);

            for (Runnable runnable : runnableLoader) {
                runQuietly(runnable);
            }
            // CSCatchBroadException keep this broad catch because service-loader startup discovery is best-effort and initialization should continue after reporting the failure.
        } catch (Exception ex) {
            // CSPrintStackTrace keep printStackTrace here because startup hook failures can happen before Chronicle logging is configured.
            ex.printStackTrace();
        }
    }

    private static void runQuietly(Runnable runnable) {
        try {
            runnable.run();
            // CSCatchBroadException keep this broad catch because this helper is the terminal boundary for one startup hook invocation and only reports the failure before moving to the next hook.
        } catch (Exception ex) {
            // CSPrintStackTrace keep printStackTrace here because startup hook failures can happen before Chronicle logging is configured.
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
                // CSCatchBroadException keep this broad catch because post-init hook loading from the configured class name is best-effort and initialization should continue after reporting the failure.
            } catch (Exception ex) {
                // CSPrintStackTrace keep printStackTrace here because post-init hook failures can happen before Chronicle logging is configured.
                ex.printStackTrace();
            }
        }

        try {
            ServiceLoader<ChronicleInitRunnable> runnableLoader = ServiceLoader.load(ChronicleInitRunnable.class);

            for (ChronicleInitRunnable runnable : runnableLoader) {
                runQuietly(runnable);
            }
            // CSCatchBroadException keep this broad catch because service-loader post-init discovery is best-effort and initialization should continue after reporting the failure.
        } catch (Exception ex) {
            // CSPrintStackTrace keep printStackTrace here because post-init hook failures can happen before Chronicle logging is configured.
            ex.printStackTrace();
        }
    }

    private static void runQuietly(ChronicleInitRunnable runnable) {
        try {
            runnable.postInit();
            // CSCatchBroadException keep this broad catch because this helper is the terminal boundary for one post-init hook invocation and only reports the failure before moving to the next hook.
        } catch (Exception ex) {
            // CSPrintStackTrace keep printStackTrace here because post-init hook failures can happen before Chronicle logging is configured.
            ex.printStackTrace();
        }
    }
}
