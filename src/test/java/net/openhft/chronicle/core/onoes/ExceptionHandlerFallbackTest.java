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
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.Jvm;
import org.junit.AssumptionViolatedException;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * Test for {@link Slf4jExceptionHandler} to ensure that it falls back to the default
 */
class ExceptionHandlerFallbackTest {
    static final int FAILED_INITIALIZATION = 2;

    /**
     * Test to ensure that the Slf4jExceptionHandler falls back to the default
     */
    @Test
    void classShouldFallBackWhenDelegateThrows() throws IllegalAccessException {
        Field initializationState = Jvm.getField(LoggerFactory.class, "INITIALIZATION_STATE");
        int state;
        try {
            state = initializationState.getInt(null);
            initializationState.setInt(null, FAILED_INITIALIZATION);
        } catch (IllegalAccessException e) {
            throw new AssumptionViolatedException(e.toString());
        }
        try {
            Slf4jExceptionHandler.WARN.on(
                    ExceptionHandlerFallbackTest.class,
                    "message",
                    new Exception("delegate failure"));
        } finally {
            initializationState.setInt(null, state);
        }
    }
}
