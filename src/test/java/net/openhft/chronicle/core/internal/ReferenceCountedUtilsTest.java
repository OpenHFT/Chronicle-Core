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
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.io.AbstractReferenceCounted;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReferenceCountedUtilsTest {

    @BeforeEach
    public void setUp() {
        ReferenceCountedUtils.enableReferenceTracing();
    }

    @AfterEach
    public void tearDown() {
        ReferenceCountedUtils.disableReferenceTracing();
    }

    @Test
    public void unmonitorShouldRemoveReference() {
        AbstractReferenceCounted referenceCounted = mock(AbstractReferenceCounted.class);
        when(referenceCounted.refCount()).thenReturn(1);

        ReferenceCountedUtils.add(referenceCounted);
        ReferenceCountedUtils.unmonitor(referenceCounted);

        assertDoesNotThrow(ReferenceCountedUtils::assertReferencesReleased, "Unmonitored references should not be checked");
    }
}
