/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.internal.ReferenceCountedUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReferenceTracingIntegrationTest {

    @BeforeEach
    public void enableTracing() {
        ReferenceCountedUtils.enableReferenceTracing();
    }

    @AfterEach
    public void disableTracing() {
        ReferenceCountedUtils.disableReferenceTracing();
    }

    @Test
    public void leaksAreReportedWithSuppressedStackTrace() {
        final SampleReference ref = new SampleReference();

        AssertionError error = assertThrows(AssertionError.class, ReferenceCountedUtils::assertReferencesReleased);
        assertEquals("Reference counted not released", error.getMessage(), "leaksAreReportedWithSuppressedStackTrace: L31");
        assertEquals(1, error.getSuppressed().length, "leaksAreReportedWithSuppressedStackTrace: L32");
        String detail = error.getSuppressed()[0].toString();
        assertTrue(detail.contains(SampleReference.class.getSimpleName()), "stack trace should mention SampleReference");

        ref.warnAndReleaseIfNotReleased();
        ReferenceCountedUtils.assertReferencesReleased();
    }

    @Test
    public void createdHereCapturesAllocationSite() {
        SampleReference ref = new SampleReference();
        StackTrace stackTrace = ref.createdHere();
        assertNotNull(stackTrace, "createdHere should be recorded");
        assertTrue(stackTrace.toString().contains("SampleReference"), "createdHereCapturesAllocationSite: L45");

        ref.releaseLast(ReferenceOwner.INIT);
        ReferenceCountedUtils.assertReferencesReleased();
    }

    private static final class SampleReference extends AbstractReferenceCounted {
        private boolean released;

        @Override
        protected void performRelease() {
            released = true;
        }

        boolean released() {
            return released;
        }
    }
}
