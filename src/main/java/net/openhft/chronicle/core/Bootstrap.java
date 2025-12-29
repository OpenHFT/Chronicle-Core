/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.internal.ChronicleGuarding;

/**
 * Holds bootstrap wiring that must load before other Chronicle components initialise.
 */
public class Bootstrap {
    /**
     * Provided for reflective usage; prefer {@link #bootstrap()}.
     */
    @SuppressWarnings("PMD.UnnecessaryConstructor")
    @Deprecated(/* make private in 2026 */)
    public Bootstrap() {
    }
    static {
        ChronicleGuarding.bootstrap();
    }

    /**
     * Triggers static initialisers of dependent bootstrap classes.
     */
    @SuppressWarnings("EmptyMethod")
    public static void bootstrap() {
        // used to trigger static initializers
    }
}
