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

import net.openhft.chronicle.core.io.IORuntimeException;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class IORuntimeExceptionTest {

    @Test
    public void testConstructorWithMessage() {
        String message = "Error message";
        IORuntimeException exception = new IORuntimeException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    public void testConstructorWithThrowable() {
        Throwable cause = new IOException("Cause");
        IORuntimeException exception = new IORuntimeException(cause);

        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testConstructorWithMessageAndThrowable() {
        String message = "Error message";
        Throwable cause = new IOException("Cause");
        IORuntimeException exception = new IORuntimeException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testNewIORuntimeException() {
        Exception closedException = new IOException("Connection reset by peer");
        Exception otherException = new IOException("Some other IO error");

        IORuntimeException runtimeClosedException = IORuntimeException.newIORuntimeException(closedException);
        IORuntimeException runtimeOtherException = IORuntimeException.newIORuntimeException(otherException);

        assertTrue(runtimeClosedException instanceof ClosedIORuntimeException);
        assertEquals(closedException, runtimeClosedException.getCause());

        assertFalse(runtimeOtherException instanceof ClosedIORuntimeException);
        assertEquals(otherException, runtimeOtherException.getCause());
    }
}
