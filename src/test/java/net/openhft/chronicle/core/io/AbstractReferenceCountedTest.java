/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractReferenceCountedTest extends ReferenceCountedTracerContractTest {

    @Test
    public void reserve() throws IllegalStateException, IllegalArgumentException {
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

        MyReferenceCounted() {
        }

        @Override
        protected void performRelease() {
            performRelease++;
        }
    }
}
