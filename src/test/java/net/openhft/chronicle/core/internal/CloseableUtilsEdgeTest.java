/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.io.Closeable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CloseableUtilsEdgeTest {

    static final class CountingCloseable implements AutoCloseable {
        final AtomicInteger count;
        CountingCloseable(AtomicInteger c) { this.count = c; }
        @Override public void close() { count.incrementAndGet(); }
    }

    static final class ThrowingCloseable implements AutoCloseable {
        @Override public void close() throws Exception { throw new Exception("boom"); }
    }

    @Test
    void closeQuietlyHandlesNullArrayAndCollections() {
        assertDoesNotThrow(() -> Closeable.closeQuietly((Object[]) null));
        List<Object> list = new ArrayList<>();
        list.add(null);
        list.add(new Object[] {null});
        assertDoesNotThrow(() -> Closeable.closeQuietly(list));
    }

    @Test
    void closeQuietlyClosesElementsAndIgnoresThrowers() {
        AtomicInteger c = new AtomicInteger();
        Object[] arr = new Object[] {
                new CountingCloseable(c),
                new ThrowingCloseable(),
                new CountingCloseable(c)
        };
        assertDoesNotThrow(() -> Closeable.closeQuietly(arr));
        assertEquals(2, c.get());
    }
}

