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

package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Mocker;
import net.openhft.chronicle.core.util.IgnoresEverything;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
public class ExceptionHandlerTest extends CoreTestCommon {
    private ExceptionHandler exceptionHandler;
    private Logger logger;

    @Test
    public void ignoresEverything() {
        assertTrue(ExceptionHandler.ignoresEverything() instanceof IgnoresEverything);
    }

    @Test
    public void ignoresEverything2() {
        assertTrue(Mocker.ignored(ExceptionHandler.class) instanceof IgnoresEverything);
    }

    @Before
    public void setUp() {
        logger = mock(Logger.class);
        exceptionHandler = (logger0, message, thrown) -> logger.error(message, thrown);
    }

    @Test
    public void testOnWithClassAndThrowable() {
        Throwable throwable = new RuntimeException("test");
        exceptionHandler.on(getClass(), throwable);
        verify(logger, times(1)).error("", throwable);
    }

    @Test
    public void testOnWithClassAndMessage() {
        String message = "test message";
        exceptionHandler.on(getClass(), message);
        verify(logger, times(1)).error(message, (Throwable) null);
    }

    @Test
    public void testOnWithClassMessageAndThrowable() {
        Throwable throwable = new RuntimeException("test");
        String message = "test message";
        exceptionHandler.on(getClass(), message, throwable);
        verify(logger, times(1)).error(message, throwable);
    }

    @Test
    public void testOnWithLoggerAndMessage() {
        String message = "test message";
        exceptionHandler.on(logger, message);
        verify(logger, times(1)).error(message, (Throwable) null);
    }

    @Test
    public void testIsEnabled() {
        assertTrue(exceptionHandler.isEnabled(getClass()));
    }

    @Test
    public void testDefaultHandler() {
        assertSame(exceptionHandler, exceptionHandler.defaultHandler());
    }

    @Test
    public void testOnWithNullMessage() {
        exceptionHandler.on(getClass(), (String) null);
        verify(logger, times(1)).error(null, (Throwable) null);
    }

    @Test
    public void testOnWithNullThrowable() {
        String message = "test message";
        exceptionHandler.on(getClass(), message, null);
        verify(logger, times(1)).error(message, (Throwable) null);
    }

    @Test
    public void testOnWithNullClass() {
        assertNotNullCheck(() -> exceptionHandler.on((Class) null, "test"));
    }

    @Test
    public void testIsEnabledWithDifferentClass() {
        assertTrue(exceptionHandler.isEnabled(String.class));
    }

    @Test
    public void testDefaultHandlerIsNotNull() {
        assertNotNull(exceptionHandler.defaultHandler());
    }

    @Test
    public void testIsEnabledWithNullClass() {
        assertNotNullCheck(() -> exceptionHandler.isEnabled(null));
    }

    @Test
    public void testOnWithLoggerAndNullMessage() {
        exceptionHandler.on(logger, null);
        verify(logger, times(1)).error(null, (Throwable) null);
    }
}