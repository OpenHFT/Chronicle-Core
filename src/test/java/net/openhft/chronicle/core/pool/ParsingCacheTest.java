/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ParsingCacheTest extends CoreTestCommon {
    @Test
    public void intern() throws Exception {
        @NotNull ParsingCache<BigDecimal> pc = new ParsingCache<>(128, BigDecimal::new);
        @Nullable BigDecimal bd1 = pc.intern("1.234");
        @Nullable BigDecimal bd2 = pc.intern("12.234");
        @Nullable BigDecimal bd1b = pc.intern("1.234");
        assertNotEquals(bd1, bd2);
        assertSame(bd1, bd1b);
        assertEquals(2, pc.valueCount());
    }
}
