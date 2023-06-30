/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QueryCloseableGptTest {

    private static class TestCloseable implements QueryCloseable {
        private boolean closing;
        private boolean closed;

        public void startClosing() {
            this.closing = true;
        }

        public void finishClosing() {
            this.closing = true;
            this.closed = true;
        }

        @Override
        public boolean isClosing() {
            return closing;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }
    }

    @Test
    public void testIsClosing() {
        TestCloseable closeable = new TestCloseable();

        assertFalse(closeable.isClosing());

        closeable.startClosing();

        assertTrue(closeable.isClosing());
    }

    @Test
    public void testIsClosed() {
        TestCloseable closeable = new TestCloseable();

        assertFalse(closeable.isClosed());

        closeable.startClosing();

        assertFalse(closeable.isClosed());

        closeable.finishClosing();

        assertTrue(closeable.isClosed());
    }

    @Test
    public void testIsClosingReturnsTrueIfIsClosedReturnsTrue() {
        TestCloseable closeable = new TestCloseable();

        closeable.startClosing();
        assertTrue(closeable.isClosing());
        assertFalse(closeable.isClosed());

        closeable.finishClosing();

        assertTrue(closeable.isClosing());
        assertTrue(closeable.isClosed());
    }
}
