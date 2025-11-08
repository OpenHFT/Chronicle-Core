/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.internal.ReferenceCountedUtils;
import net.openhft.chronicle.core.io.ReferenceOwner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReferenceTracingIntegrationTest {

    @Before
    public void enableTracing() {
        ReferenceCountedUtils.enableReferenceTracing();
    }

    @After
    public void disableTracing() {
        ReferenceCountedUtils.disableReferenceTracing();
    }

    @Test
    public void leaksAreReportedWithSuppressedStackTrace() {
        SampleReference ref = new SampleReference();

        AssertionError error = assertThrows(AssertionError.class, ReferenceCountedUtils::assertReferencesReleased);
        assertEquals("Reference counted not released", error.getMessage());
        assertEquals(1, error.getSuppressed().length);
        String detail = error.getSuppressed()[0].toString();
        assertTrue("stack trace should mention SampleReference", detail.contains(SampleReference.class.getSimpleName()));

        ref.warnAndReleaseIfNotReleased();
        ReferenceCountedUtils.assertReferencesReleased();
    }

    @Test
    public void createdHereCapturesAllocationSite() {
        SampleReference ref = new SampleReference();
        StackTrace stackTrace = ref.createdHere();
        assertNotNull("createdHere should be recorded", stackTrace);
        assertTrue(stackTrace.toString().contains("SampleReference"));

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
