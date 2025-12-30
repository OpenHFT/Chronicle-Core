/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidatableUtilTest {

    @DisplayName("Validation toggle disables and re enables checks")
    @Test
    void testValidateToggle() {
        assertTrue(ValidatableUtil.validateEnabled(), "validation should be enabled by default");

        ValidatableUtil.startValidateDisabled();
        assertFalse(ValidatableUtil.validateEnabled(), "validation should be disabled after startValidateDisabled");

        ValidatableUtil.endValidateDisabled();
        assertTrue(ValidatableUtil.validateEnabled(), "validation should be re-enabled after endValidateDisabled");
    }

    @DisplayName("End validate disabled without start validatable")
    @Test
    void testEndValidateDisabledWithoutStart() {
        AssertionError exception = assertThrows(AssertionError.class, ValidatableUtil::endValidateDisabled,
                "endValidateDisabled should throw without a matching startValidateDisabled");
        assertNotNull(exception, "exception should be captured");
    }

    @DisplayName("ValidatableUtil validate invokes validate on target")
    @Test
    void testValidate() throws InvalidMarshallableException {
        Validatable validatable = mock(Validatable.class);
        ValidatableUtil.validate(validatable);

        verify(validatable, times(1)).validate();
    }
}
