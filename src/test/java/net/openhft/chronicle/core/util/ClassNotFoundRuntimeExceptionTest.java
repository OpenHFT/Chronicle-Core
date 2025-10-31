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
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.util.ClassNotFoundRuntimeException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClassNotFoundRuntimeExceptionTest {

    @Test
    public void testConstructor() {
        ClassNotFoundException cause = new ClassNotFoundException("Test class not found");
        ClassNotFoundRuntimeException exception = new ClassNotFoundRuntimeException(cause);

        assertNotNull(exception);
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testGetCause() {
        ClassNotFoundException cause = new ClassNotFoundException("Test class not found");
        ClassNotFoundRuntimeException exception = new ClassNotFoundRuntimeException(cause);

        Throwable throwableCause = exception.getCause();

        assertTrue(throwableCause instanceof ClassNotFoundException);
        assertEquals(cause, throwableCause);
    }
}
