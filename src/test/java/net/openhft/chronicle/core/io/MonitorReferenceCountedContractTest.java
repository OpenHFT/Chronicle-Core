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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Any implementation of {@link ReferenceCountedTracer} should implement a test class
 * that extends this class
 */
public abstract class MonitorReferenceCountedContractTest extends ReferenceCountedTracerContractTest {

    @Override
    protected abstract MonitorReferenceCounted createReferenceCounted();

    @Test
    public void warnAndReleaseWillLogAWarningAndReleaseWhenMonitored() {
        final MonitorReferenceCounted referenceCounted = createReferenceCounted();
        referenceCounted.unmonitored(false);
        referenceCounted.warnAndReleaseIfNotReleased();
        assertEquals(0, referenceCounted.refCount());
        expectException("Discarded without being released");
    }

    @Test
    public void warnAndReleaseWillJustReleaseWhenMonitored() {
        final MonitorReferenceCounted referenceCounted = createReferenceCounted();
        referenceCounted.unmonitored(true);
        referenceCounted.warnAndReleaseIfNotReleased();
        assertEquals(0, referenceCounted.refCount());
    }

    @Test
    public void warnAndReleaseWillDoNothingIfTheResourceIsAlreadyReleased() {
        final MonitorReferenceCounted referenceCounted = createReferenceCounted();
        referenceCounted.unmonitored(false);
        referenceCounted.releaseLast();
        referenceCounted.warnAndReleaseIfNotReleased();
    }
}
