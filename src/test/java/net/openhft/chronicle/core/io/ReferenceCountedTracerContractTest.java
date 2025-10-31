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
package net.openhft.chronicle.core.io;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Any implementor of {@link ReferenceCountedTracer} should implement a test class
 * that extends this or one of its more specific children
 */
public abstract class ReferenceCountedTracerContractTest extends ReferenceCountedContractTest {

    @Override
    protected abstract ReferenceCountedTracer createReferenceCounted();

    @Test
    public void throwIfReleasedWillThrowIfResourceIsReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();
        assertThrows(ClosedIllegalStateException.class, referenceCounted::throwExceptionIfReleased);
    }

    @Test
    public void throwIfReleasedWillNotThrowIfResourceIsNotReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();

        referenceCounted.throwExceptionIfReleased();
    }

    @Test
    public void throwIfNotReleasedWillThrowIfResourceIsNotReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();

        assertThrows(IllegalStateException.class, referenceCounted::throwExceptionIfNotReleased);
    }

    @Test
    public void throwIfNotReleasedWillNotThrowIfResourceIsReleased() {
        ReferenceCountedTracer referenceCounted = createReferenceCounted();

        referenceCounted.releaseLast();
        referenceCounted.throwExceptionIfNotReleased();
    }

    @Test
    public void listenersShouldNotBeNotifiedOnWarnAndReleaseIfNotReleased() {
        ReferenceCountedTracer rc = createReferenceCounted();

        ReferenceOwner a = ReferenceOwner.temporary("a");
        ReferenceOwner b = ReferenceOwner.temporary("b");

        CounterReferenceChangeListener listener = new CounterReferenceChangeListener();
        rc.addReferenceChangeListener(listener);
        rc.reserve(a);
        rc.reserve(b);

        expectException("Discarded without being released");
        rc.warnAndReleaseIfNotReleased();
        assertEquals(0, listener.referenceRemovedCount);
    }
}
