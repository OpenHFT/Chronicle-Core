/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidatableUtilTest {

    @Test
    void testValidateToggle() {
        assertTrue(ValidatableUtil.validateEnabled());

        ValidatableUtil.startValidateDisabled();
        assertFalse(ValidatableUtil.validateEnabled());

        ValidatableUtil.endValidateDisabled();
        assertTrue(ValidatableUtil.validateEnabled());
    }

    @Test
    void testEndValidateDisabledWithoutStart() {
        AssertionError exception = assertThrows(AssertionError.class, ValidatableUtil::endValidateDisabled);
        assertNotNull(exception);
    }

    @Test
    void testValidate() throws InvalidMarshallableException {
        RecordingValidatable validatable = new RecordingValidatable();
        ValidatableUtil.validate(validatable);

        assertEquals(1, validatable.validateCount);
    }

    private static final class RecordingValidatable implements Validatable {
        private int validateCount;

        @Override
        public void validate() throws InvalidMarshallableException {
            validateCount++;
        }
    }
}
