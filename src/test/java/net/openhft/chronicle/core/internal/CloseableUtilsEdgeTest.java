/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.io.Closeable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CloseableUtilsEdgeTest {

    @DisplayName("Close quietly handles null array and collections")
    @Test
    void closeQuietlyHandlesNullArrayAndCollections() {
        assertDoesNotThrow(() -> Closeable.closeQuietly((Object[]) null),
                "closeQuietly should ignore null arrays");
        List<Object> list = new ArrayList<>();
        list.add(null);
        list.add(new Object[]{null});
        assertDoesNotThrow(() -> Closeable.closeQuietly(list),
                "closeQuietly should ignore null elements in collections");
    }

    @DisplayName("Close quietly closes elements and ignores throwers")
    @Test
    void closeQuietlyClosesElementsAndIgnoresThrowers() {
        AtomicInteger c = new AtomicInteger();
        Object[] arr = {
                new CountingCloseable(c),
                new ThrowingCloseable(),
                new CountingCloseable(c)
        };
        assertDoesNotThrow(() -> Closeable.closeQuietly(arr),
                "closeQuietly should suppress close exceptions for array elements");
        assertEquals(2, c.get(), "both non-throwing closeables should be closed despite thrower in between");
    }

    static final class CountingCloseable implements AutoCloseable {
        final AtomicInteger count;

        CountingCloseable(AtomicInteger c) {
            this.count = c;
        }

        @Override
        public void close() {
            count.incrementAndGet();
        }
    }

    static final class ThrowingCloseable implements AutoCloseable {
        @Override
        public void close() {
            throw new IllegalStateException("close failed during test");
        }
    }
}
