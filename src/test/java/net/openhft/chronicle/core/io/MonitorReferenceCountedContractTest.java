/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Any implementation of {@link ReferenceCountedTracer} should provide a contract test
 * that extends this class to verify monitoring behaviour.
 */
abstract class MonitorReferenceCountedContractTest extends ReferenceCountedTracerContractTest {

    @Override
    protected abstract MonitorReferenceCounted createReferenceCounted();

    @Test
    @DisplayName("warnAndRelease logs warning and releases when monitored")
    void warnAndReleaseWillLogAWarningAndReleaseWhenMonitored() {
        final MonitorReferenceCounted referenceCounted = createReferenceCounted();
        referenceCounted.unmonitored(false);
        referenceCounted.warnAndReleaseIfNotReleased();
        assertEquals(0, referenceCounted.refCount(), "reference count should be zero after warnAndRelease when monitored");
        expectException("Discarded without being released");
    }

    @Test
    @DisplayName("warnAndRelease releases silently when unmonitored flag")
    void warnAndReleaseWillJustReleaseWhenMonitored() {
        final MonitorReferenceCounted referenceCounted = createReferenceCounted();
        referenceCounted.unmonitored(true);
        referenceCounted.warnAndReleaseIfNotReleased();
        assertEquals(0, referenceCounted.refCount(), "reference count should be zero after warnAndRelease when unmonitored");
    }

    @Test
    @DisplayName("warnAndRelease does nothing when resource released")
    void warnAndReleaseWillDoNothingIfTheResourceIsAlreadyReleased() {
        final MonitorReferenceCounted referenceCounted = createReferenceCounted();
        referenceCounted.unmonitored(false);
        referenceCounted.releaseLast();
        referenceCounted.warnAndReleaseIfNotReleased();
        assertEquals(0, referenceCounted.refCount(), "reference count should remain zero when warnAndRelease called on already released resource");
    }
}
