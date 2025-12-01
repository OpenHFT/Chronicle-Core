/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

/**
 * Hook invoked when {@link Jvm} finishes static initialisation.
 * <p>
 * Implementations may perform one off initialisation work that depends on {@link Jvm} being
 * fully configured before any application code runs.
 */
public interface ChronicleInitRunnable extends Runnable {
    /**
     * This method will be run once at the end of Jvm.class static initialization.
     */
    default void postInit() {
        // No-op.
    }
}
