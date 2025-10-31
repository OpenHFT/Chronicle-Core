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

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertNull;

public class VanillaReferenceCountedTest extends MonitorReferenceCountedContractTest {

    private AtomicInteger onReleasedCallCount;

    @Before
    public void setUp() {
        onReleasedCallCount = new AtomicInteger(0);
    }

    @Override
    protected VanillaReferenceCounted createReferenceCounted() {
        return new VanillaReferenceCounted(onReleasedCallCount::incrementAndGet, VanillaReferenceCounted.class);
    }

    @Test
    public void createdHereWillReturnNull() {
        final VanillaReferenceCounted referenceCounted = createReferenceCounted();
        assertNull(referenceCounted.createdHere());
    }
}
