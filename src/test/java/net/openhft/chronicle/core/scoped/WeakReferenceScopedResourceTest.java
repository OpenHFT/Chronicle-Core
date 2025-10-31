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
package net.openhft.chronicle.core.scoped;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class WeakReferenceScopedResourceTest {

    private ScopedThreadLocal<AtomicLong> scopedThreadLocal;

    @BeforeEach
    void setUp() {
        scopedThreadLocal = new ScopedThreadLocal<>(AtomicLong::new, 3);
    }

    @Test
    void resourceIsCreatedPreAcquire() {
        final WeakReferenceScopedResource<AtomicLong> sr = new WeakReferenceScopedResource<>(scopedThreadLocal, AtomicLong::new);
        assertNull(sr.get()); // There should be nothing in it (this would never happen in the real world)
        sr.preAcquire();
        assertNotNull(sr.get());
    }

    @Test
    void strongReferenceIsCreatedPreAcquire() {
        final WeakReferenceScopedResource<AtomicLong> sr = new WeakReferenceScopedResource<>(scopedThreadLocal, AtomicLong::new);
        sr.preAcquire(); // creates the strong reference
        assertNotNull(sr.get());
        System.gc();
        assertNotNull(sr.get());
        sr.close(); // clears the strong reference
        System.gc();
        assertNull(sr.get());
    }
}
