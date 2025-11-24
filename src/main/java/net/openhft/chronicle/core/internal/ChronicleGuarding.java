/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

/**
 * Placeholder hook invoked early in Chronicle component initialisation to keep
 * guarding logic centralised. The method is intentionally empty; its presence
 * allows agents or runtime instrumentation to latch onto a stable entry point
 * without triggering static initialisers in other classes.
 */
public class ChronicleGuarding {

    @SuppressWarnings("EmptyMethod")
    public static void bootstrap() {
    }
}
