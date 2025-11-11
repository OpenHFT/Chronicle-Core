/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.internal.ChronicleGuarding;

/**
 * Contains the pieces which must be loaded first
 */
public class Bootstrap {
    public Bootstrap() {
    }
    static {
        ChronicleGuarding.bootstrap();
    }

    @SuppressWarnings("EmptyMethod")
    public static void bootstrap() {
        // used to trigger static initializers
    }
}
