/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractReferenceCountedTest extends ReferenceCountedTracerContractTest {

    @DisplayName("Reserve increments reference count on resource")
    @Test
    void reserve() throws IllegalStateException, IllegalArgumentException {
        Jvm.setResourceTracing(true);

        MyReferenceCounted rc = createReferenceCounted();
        assertEquals(1, rc.refCount(), "Reference count should be 1 after initial creation");

        exerciseReserveLifecycle(rc, () -> rc.performRelease);
    }

    @Override
    protected MyReferenceCounted createReferenceCounted() {
        return new MyReferenceCounted();
    }

    static class MyReferenceCounted extends AbstractReferenceCounted {
        int performRelease;

        @Override
        protected void performRelease() {
            performRelease++;
        }
    }
}
