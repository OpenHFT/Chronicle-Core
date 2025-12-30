/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class ParsingCacheTest extends CoreTestCommon {
    @DisplayName("Parsing cache interns and reuses parsed values")
    @Test
    void intern() {
        @NotNull ParsingCache<BigDecimal> pc = new ParsingCache<>(128, BigDecimal::new);
        @Nullable BigDecimal bd1 = pc.intern("1.234");
        @Nullable BigDecimal bd2 = pc.intern("12.234");
        @Nullable BigDecimal bd1b = pc.intern("1.234");
        assertNotEquals(bd1, bd2, "different inputs should yield distinct values");
        assertSame(bd1, bd1b, "parsing cache should return same instance (reference equality)");
        assertEquals(2, pc.valueCount(), "Cache should contain exactly two distinct values after interning");
    }
}
