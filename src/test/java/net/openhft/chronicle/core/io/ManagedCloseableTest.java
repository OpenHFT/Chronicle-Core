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

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.ManagedCloseable;
import org.junit.Before;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ManagedCloseableTest {
    @BeforeEach
    public void mockitoNotSupportedOnJava21() {
        Assumptions.assumeTrue(Jvm.majorVersion() <= 17);
    }
    @Test
    public void testWarnAndCloseIfNotClosed() {
        ManagedCloseable closeable = spy(ManagedCloseable.class);

        when(closeable.isClosing()).thenReturn(false);

        closeable.warnAndCloseIfNotClosed();

        verify(closeable, times(1)).close();
    }

    @Test
    public void testThrowExceptionIfClosed() {
        ManagedCloseable closeable = Mockito.spy(ManagedCloseable.class);

        when(closeable.isClosing()).thenReturn(true);
        when(closeable.isClosed()).thenReturn(true);

        assertThrows(ClosedIllegalStateException.class, closeable::throwExceptionIfClosed);
    }

    @Test
    public void testCreatedHere() {
        ManagedCloseable closeable = Mockito.spy(ManagedCloseable.class);

        assertNull(closeable.createdHere());
    }
}
