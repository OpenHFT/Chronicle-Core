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

import net.openhft.chronicle.core.io.AbstractCloseable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CloseableUtilsAssertTest {

    static final class DummyCloseable extends AbstractCloseable {
        boolean performed;
        @Override
        protected void performClose() throws IllegalStateException {
            performed = true;
        }
    }

    @Test
    void assertCloseablesClosedFindsUnclosedAndCloses() {
        // Ensure tracing is enabled for this test
        AbstractCloseable.enableCloseableTracing();
        DummyCloseable dc = new DummyCloseable();
        // do not close it to simulate leak
        AssertionError ae = assertThrows(AssertionError.class, AbstractCloseable::assertCloseablesClosed);
        assertTrue(ae.getSuppressed().length >= 1);
        // The helper should have closed leaked resources
        assertTrue(dc.performed);
    }
}
