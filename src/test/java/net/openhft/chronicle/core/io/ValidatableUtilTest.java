//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        Validatable validatable = mock(Validatable.class);
        ValidatableUtil.validate(validatable);

        verify(validatable, times(1)).validate();
    }
}
