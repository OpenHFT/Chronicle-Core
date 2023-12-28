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
import net.openhft.chronicle.core.util.IgnoresEverything;
import net.openhft.chronicle.core.util.Mocker;
import org.junit.Test;
import org.slf4j.Logger;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ExceptionHandlerTest extends CoreTestCommon {

    @Test
    public void ignoresEverything() {
        assertTrue(ExceptionHandler.ignoresEverything() instanceof IgnoresEverything);
    }

    @Test
    public void ignoresEverything2() {
        assertTrue(Mocker.ignored(ExceptionHandler.class) instanceof IgnoresEverything);
    }

    @Test
    public void onWithClassAndThrowableShouldDelegateProperly() {
        ExceptionHandler handler = mock(ExceptionHandler.class, CALLS_REAL_METHODS);
        Class<?> clazz = this.getClass();
        Throwable thrown = new RuntimeException();

        handler.on(clazz, thrown);

        verify(handler).on(clazz, "", thrown);
    }

    @Test
    public void onWithClassAndMessageShouldDelegateProperly() {
        ExceptionHandler handler = mock(ExceptionHandler.class, CALLS_REAL_METHODS);
        Class<?> clazz = this.getClass();
        String message = "Test message";

        handler.on(clazz, message);

        verify(handler).on(clazz, message, null);
    }

    @Test
    public void onWithLoggerAndMessageShouldDelegateProperly() {
        ExceptionHandler handler = mock(ExceptionHandler.class, CALLS_REAL_METHODS);
        Logger logger = mock(Logger.class);
        String message = "Test message";

        handler.on(logger, message);

        verify(handler).on(logger, message, null);
    }

    @Test
    public void isEnabledShouldAlwaysReturnTrue() {
        ExceptionHandler handler = mock(ExceptionHandler.class, CALLS_REAL_METHODS);
        assertTrue(handler.isEnabled(this.getClass()));
    }

    @Test
    public void defaultHandlerShouldReturnSelf() {
        ExceptionHandler handler = mock(ExceptionHandler.class, CALLS_REAL_METHODS);
        assertSame(handler, handler.defaultHandler());
    }
}
