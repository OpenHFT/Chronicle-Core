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
    @Test
    @DisplayName("Parsing cache interns and reuses parsed values")
    void intern() {
        @NotNull ParsingCache<BigDecimal> pc = new ParsingCache<>(128, BigDecimal::new);
        @Nullable BigDecimal bd1 = pc.intern("1.234");
        @Nullable BigDecimal bd2 = pc.intern("12.234");
        @Nullable BigDecimal bd1b = pc.intern("1.234");
        assertNotEquals(bd1, bd2, "different inputs should yield distinct values");
        assertSame(bd1, bd1b, "parsing cache should return same instance (reference equality)");
        assertEquals(2, pc.valueCount(), "Cache should contain exactly two distinct values after interning");
    }

    @Test
    @DisplayName("ParsingCache returns null when null input string argument is supplied")
    void internReturnsNullForNullInput() {
        @NotNull ParsingCache<BigDecimal> pc = new ParsingCache<>(128, BigDecimal::new);
        assertNull(pc.intern(null), "ParsingCache intern returns null when input string is null");
    }

    @Test
    @DisplayName("Parsing cache handles many values filling cache buckets")
    void internHandlesManyValues() {
        @NotNull ParsingCache<String> pc = new ParsingCache<>(128, s -> s);
        // Fill cache with many values to trigger bucket collisions
        for (int i = 0; i < 256; i++) {
            String s = "value" + i;
            String interned = pc.intern(s);
            assertEquals(s, interned, "interned value should equal original at index " + i);
        }
        // Verify some values are still retrievable
        assertEquals("value0", pc.intern("value0"), "cache should return same value on re-intern");
    }

    @Test
    @DisplayName("Parsing cache toggle alternates bucket selection")
    void toggleAlternatesBucketSelection() {
        @NotNull ParsingCache<String> pc = new ParsingCache<>(128, s -> s);
        // Access toggle to verify it alternates
        assertFalse(pc.toggle, "cache toggle should start false");
        pc.toggle();
        assertTrue(pc.toggle, "cache toggle should be true after first toggle");
        pc.toggle();
        assertFalse(pc.toggle, "cache toggle should be false after second toggle");
    }

    @Test
    @DisplayName("Parsing cache uses secondary bucket on hash collision")
    void internUsesSecondaryBucketOnCollision() {
        @NotNull ParsingCache<String> pc = new ParsingCache<>(128, s -> s);
        // Add many values to ensure some end up in secondary buckets
        // and some values need to use the secondary bucket path
        for (int i = 0; i < 512; i++) {
            String s = String.valueOf(i);
            pc.intern(s);
        }
        // Re-intern to hit the secondary bucket lookup path
        for (int i = 0; i < 512; i++) {
            String s = String.valueOf(i);
            String result = pc.intern(s);
            assertEquals(s, result, "re-interning should return equivalent value at index " + i);
        }
    }

    @Test
    @DisplayName("Parsing cache valueCount returns correct count")
    void valueCountReturnsCorrectCount() {
        @NotNull ParsingCache<String> pc = new ParsingCache<>(128, s -> s);
        assertEquals(0, pc.valueCount(), "empty cache should have zero values");
        pc.intern("one");
        assertTrue(pc.valueCount() >= 1, "cache should have at least one value after intern");
        pc.intern("two");
        assertTrue(pc.valueCount() >= 2, "cache should have at least two values after second intern");
    }

    @Test
    @DisplayName("Parsing cache handles StringBuilder input values")
    void internHandlesStringBuilder() {
        @NotNull ParsingCache<BigDecimal> pc = new ParsingCache<>(128, BigDecimal::new);
        StringBuilder sb = new StringBuilder("1.234");
        BigDecimal bd1 = pc.intern(sb);
        assertNotNull(bd1, "intern should return non-null for non-null StringBuilder");
        assertEquals(new BigDecimal("1.234"), bd1, "interned value should match input");

        // Re-intern same content should return cached value
        BigDecimal bd2 = pc.intern("1.234");
        assertSame(bd1, bd2, "cache should return same instance for equivalent content");
    }
}
