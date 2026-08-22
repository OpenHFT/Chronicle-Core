/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class WeakThreadLocalTest extends CoreTestCommon {

    @Test
    void getReturnsSameInstanceWithinAThreadAndCallsSupplierOnce() {
        final AtomicInteger created = new AtomicInteger();
        final WeakThreadLocal<Object> tl = new WeakThreadLocal<>(() -> {
            created.incrementAndGet();
            return new Object();
        });

        final Object first = tl.get();
        final Object second = tl.get();

        assertSame(first, second);
        assertEquals(1, created.get());
    }

    @Test
    void eachThreadGetsItsOwnValue() throws InterruptedException {
        final WeakThreadLocal<Object> tl = new WeakThreadLocal<>(Object::new);

        final Object mine = tl.get();
        final Object[] other = new Object[1];
        final Thread t = new Thread(() -> other[0] = tl.get());
        t.start();
        t.join();

        assertNotNull(other[0]);
        assertNotSame(mine, other[0]);
    }

    @Test
    void removeForcesRecreation() {
        final AtomicInteger created = new AtomicInteger();
        final WeakThreadLocal<Object> tl = new WeakThreadLocal<>(() -> {
            created.incrementAndGet();
            return new Object();
        });

        final Object first = tl.get();
        tl.remove();
        final Object second = tl.get();

        assertNotSame(first, second);
        assertEquals(2, created.get());
    }

    @Test
    void softReferenceTypeAlsoCachesPerThread() {
        final AtomicInteger created = new AtomicInteger();
        final WeakThreadLocal<Object> tl =
                new WeakThreadLocal<>(() -> {
                    created.incrementAndGet();
                    return new Object();
                }, WeakThreadLocal.ReferenceType.SOFT);

        assertSame(tl.get(), tl.get());
        assertEquals(1, created.get());
    }

    @Test
    void nullSupplierIsRejected() {
        assertThrows(NullPointerException.class, () -> new WeakThreadLocal<>(null));
    }

    @Test
    void nullSupplierResultIsRejectedForBothReferenceTypes() {
        for (WeakThreadLocal.ReferenceType referenceType : WeakThreadLocal.ReferenceType.values()) {
            final WeakThreadLocal<Object> tl = new WeakThreadLocal<>(() -> null, referenceType);
            final NullPointerException exception = assertThrows(NullPointerException.class, tl::get);
            assertEquals("supplier returned null", exception.getMessage());
        }
    }
}
