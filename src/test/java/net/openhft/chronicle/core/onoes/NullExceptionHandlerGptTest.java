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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class NullExceptionHandlerGptTest extends CoreTestCommon {

    @Test
    public void testNullExceptionHandler() {
        Logger logger = LoggerFactory.getLogger(NullExceptionHandlerGptTest.class);
        String testMessage = "Test message";
        Exception testException = new RuntimeException("Test exception");

        // Test if NullExceptionHandler is indeed doing nothing (no exceptions should be thrown)
        assertDoesNotThrow(() -> NullExceptionHandler.NOTHING.on(logger, testMessage, testException));
        assertFalse(NullExceptionHandler.NOTHING.isEnabled(NullExceptionHandlerGptTest.class));
    }
}
