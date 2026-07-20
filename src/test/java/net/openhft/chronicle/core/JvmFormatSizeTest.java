/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import static net.openhft.chronicle.core.JvmParseSizeTest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Canonical test suite for size formatting via {@link Jvm#formatSize(long)}
 */
class JvmFormatSizeTest extends CoreTestCommon {

    @Test
    void formatSizeRenders() {
        assertEquals("0", Jvm.formatSize(0));
        assertEquals("500", Jvm.formatSize(500));      // not a whole 1024 multiple -> raw bytes
        assertEquals("1K", Jvm.formatSize(KIB));
        assertEquals("512M", Jvm.formatSize(512 * MIB));
        assertEquals("1536M", Jvm.formatSize(1536 * MIB)); // 1.5 GiB is not a whole GiB
        assertEquals("5G", Jvm.formatSize(5 * GIB));
        assertEquals("3T", Jvm.formatSize(3 * TIB));
    }

    @Test
    void formatSizeRoundTripsThroughParseSize() {
        final long[] sizes = {0, 1, 1023, KIB, 1025, 500, MIB, 1536 * MIB, 5 * GIB, 3 * TIB,
                123_456_789L, Long.MAX_VALUE, Long.MAX_VALUE - 1023};
        for (long size : sizes)
            assertEquals(size, Jvm.parseSize(Jvm.formatSize(size)),
                    "round-trip failed for " + size + " (formatted as " + Jvm.formatSize(size) + ")");
    }

    @Test
    void formatSizeRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> Jvm.formatSize(-1));
    }
}
