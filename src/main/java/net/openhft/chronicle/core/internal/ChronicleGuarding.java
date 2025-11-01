/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

public class ChronicleGuarding {

    @SuppressWarnings("EmptyMethod")
    public static void bootstrap() {
        // Intentionally left empty.
        // Acts as a class-loading guard/initialisation hook invoked from static blocks
        // elsewhere. This method provides a stable call-site without side effects.
    }
}
