/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.internal.ReferenceCountedUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReferenceTracingIntegrationTest {

    @BeforeEach
    public void enableTracing() {
        ReferenceCountedUtils.enableReferenceTracing();
    }

    @AfterEach
    public void disableTracing() {
        ReferenceCountedUtils.disableReferenceTracing();
    }

    @DisplayName("leak reports include suppressed stack trace details")
    @Test
    void leaksAreReportedWithSuppressedStackTrace() {
        final SampleReference ref = new SampleReference();

        AssertionError error = assertThrows(AssertionError.class, ReferenceCountedUtils::assertReferencesReleased,
                "assertReferencesReleased should throw for unreleased reference");
        assertEquals("Reference counted not released", error.getMessage(), "assertion error should report unreleased references");
        assertEquals(1, error.getSuppressed().length, "assertion error should have exactly one suppressed exception with leak details");
        String detail = error.getSuppressed()[0].toString();
        assertTrue(detail.contains(SampleReference.class.getSimpleName()),
                "suppressed stack trace should mention SampleReference: " + detail);

        ref.warnAndReleaseIfNotReleased();
        ReferenceCountedUtils.assertReferencesReleased();
    }

    @DisplayName("createdHere captures allocation site stack trace")
    @Test
    void createdHereCapturesAllocationSite() {
        SampleReference ref = new SampleReference();
        StackTrace stackTrace = ref.createdHere();
        assertNotNull(stackTrace, "createdHere should be recorded");
        String trace = stackTrace.toString();
        assertTrue(trace.contains("SampleReference"),
                "createdHere stack trace should contain \"SampleReference\": " + trace);

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
