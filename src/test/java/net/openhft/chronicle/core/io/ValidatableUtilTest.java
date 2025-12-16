/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidatableUtilTest {

    @Test
    void testValidateToggle() {
        assertTrue(ValidatableUtil.validateEnabled(), "testValidateToggle: L14");

        ValidatableUtil.startValidateDisabled();
        assertFalse(ValidatableUtil.validateEnabled(), "testValidateToggle: L17");

        ValidatableUtil.endValidateDisabled();
        assertTrue(ValidatableUtil.validateEnabled(), "testValidateToggle: L20");
    }

    @Test
    void testEndValidateDisabledWithoutStart() {
        AssertionError exception = assertThrows(AssertionError.class, ValidatableUtil::endValidateDisabled);
        assertNotNull(exception, "testEndValidateDisabledWithoutStart: L26");
    }

    @Test
    void testValidate() throws InvalidMarshallableException {
        Validatable validatable = mock(Validatable.class);
        ValidatableUtil.validate(validatable);

        verify(validatable, times(1)).validate();
    }
}
