/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Any implementation of {@link ReferenceCountedTracer} should implement a test class
 * that extends this class
 */
abstract class MonitorReferenceCountedContractTest extends ReferenceCountedTracerContractTest {

    @Override
    protected abstract MonitorReferenceCounted createReferenceCounted();

    @Test
    void warnAndReleaseWillLogAWarningAndReleaseWhenMonitored() {
        final MonitorReferenceCounted referenceCounted = createReferenceCounted();
        referenceCounted.unmonitored(false);
        referenceCounted.warnAndReleaseIfNotReleased();
        assertEquals(0, referenceCounted.refCount());
        expectException("Discarded without being released");
    }

    @Test
    void warnAndReleaseWillJustReleaseWhenMonitored() {
        final MonitorReferenceCounted referenceCounted = createReferenceCounted();
        referenceCounted.unmonitored(true);
        referenceCounted.warnAndReleaseIfNotReleased();
        assertEquals(0, referenceCounted.refCount());
    }

    @Test
    void warnAndReleaseWillDoNothingIfTheResourceIsAlreadyReleased() {
        final MonitorReferenceCounted referenceCounted = createReferenceCounted();
        referenceCounted.unmonitored(false);
        referenceCounted.releaseLast();
        referenceCounted.warnAndReleaseIfNotReleased();
        assertEquals(0, referenceCounted.refCount());
    }
}
