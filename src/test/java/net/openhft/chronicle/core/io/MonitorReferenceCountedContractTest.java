/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Any implementation of {@link ReferenceCountedTracer} should implement a test class
 * that extends this class
 */
@SuppressWarnings("PMD.JUnit5TestShouldBePackagePrivate") // JUnit4 annotations require public class
public abstract class MonitorReferenceCountedContractTest extends ReferenceCountedTracerContractTest {

    @Override
    protected abstract MonitorReferenceCounted createReferenceCounted();

    @Test
    public void warnAndReleaseWillLogAWarningAndReleaseWhenMonitored() {
        final MonitorReferenceCounted referenceCounted = createReferenceCounted();
        referenceCounted.unmonitored(false);
        referenceCounted.warnAndReleaseIfNotReleased();
        assertEquals(0, referenceCounted.refCount(), "warnAndReleaseWillLogAWarningAndReleaseWhenMonitored: L25");
        expectException("Discarded without being released");
    }

    @Test
    public void warnAndReleaseWillJustReleaseWhenMonitored() {
        final MonitorReferenceCounted referenceCounted = createReferenceCounted();
        referenceCounted.unmonitored(true);
        referenceCounted.warnAndReleaseIfNotReleased();
        assertEquals(0, referenceCounted.refCount(), "warnAndReleaseWillJustReleaseWhenMonitored: L34");
    }

    @Test
    public void warnAndReleaseWillDoNothingIfTheResourceIsAlreadyReleased() {
        final MonitorReferenceCounted referenceCounted = createReferenceCounted();
        referenceCounted.unmonitored(false);
        referenceCounted.releaseLast();
        referenceCounted.warnAndReleaseIfNotReleased();
        assertEquals(0, referenceCounted.refCount(), "warnAndReleaseWillDoNothingIfTheResourceIsAlreadyReleased: L43");
    }
}
