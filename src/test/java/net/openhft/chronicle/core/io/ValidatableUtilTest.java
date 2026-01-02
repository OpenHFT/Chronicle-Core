/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidatableUtilTest {

    @Test
    @DisplayName("Validation toggle disables and re enables checks")
    void testValidateToggle() {
        assertTrue(ValidatableUtil.validateEnabled(), "validation should be enabled by default");

        ValidatableUtil.startValidateDisabled();
        assertFalse(ValidatableUtil.validateEnabled(), "validation should be disabled after startValidateDisabled");

        ValidatableUtil.endValidateDisabled();
        assertTrue(ValidatableUtil.validateEnabled(), "validation should be re-enabled after endValidateDisabled");
    }

    @Test
    @DisplayName("End validate disabled without start validatable")
    void testEndValidateDisabledWithoutStart() {
        AssertionError exception = assertThrows(AssertionError.class, ValidatableUtil::endValidateDisabled,
                "endValidateDisabled should throw without a matching startValidateDisabled");
        assertNotNull(exception, "exception should be captured");
    }

    @Test
    @DisplayName("ValidatableUtil validate invokes validate on target")
    void testValidate() throws InvalidMarshallableException {
        Validatable validatable = mock(Validatable.class);
        ValidatableUtil.validate(validatable);

        verify(validatable, times(1)).validate();
    }

    // --- Additional tests for branch coverage ---

    @Test
    @DisplayName("validate returns non-Validatable object unchanged")
    void validateNonValidatable() throws InvalidMarshallableException {
        String notValidatable = "just a string";
        String result = ValidatableUtil.validate(notValidatable);
        assertSame(notValidatable, result, "validate should return non-Validatable object unchanged");
    }

    @Test
    @DisplayName("validate skips validation when disabled")
    void validateSkipsWhenDisabled() throws InvalidMarshallableException {
        Validatable validatable = mock(Validatable.class);

        ValidatableUtil.startValidateDisabled();
        try {
            ValidatableUtil.validate(validatable);
            verify(validatable, never()).validate();
        } finally {
            ValidatableUtil.endValidateDisabled();
        }
    }

    @Test
    @DisplayName("nested disable/enable works correctly")
    void nestedDisableEnable() {
        assertTrue(ValidatableUtil.validateEnabled(), "initially enabled");

        ValidatableUtil.startValidateDisabled();
        assertFalse(ValidatableUtil.validateEnabled(), "disabled after first start");

        ValidatableUtil.startValidateDisabled();
        assertFalse(ValidatableUtil.validateEnabled(), "still disabled after nested start");

        ValidatableUtil.endValidateDisabled();
        assertFalse(ValidatableUtil.validateEnabled(), "still disabled after first end");

        ValidatableUtil.endValidateDisabled();
        assertTrue(ValidatableUtil.validateEnabled(), "re-enabled after second end");
    }

    @Test
    @DisplayName("requireNonNull does not throw for non-null value")
    void requireNonNullWithValue() {
        assertDoesNotThrow(() -> ValidatableUtil.requireNonNull("value", "field"),
                "requireNonNull should not throw for non-null value");
    }

    @Test
    @DisplayName("requireNonNull throws for null value")
    void requireNonNullWithNull() {
        InvalidMarshallableException ex = assertThrows(InvalidMarshallableException.class,
                () -> ValidatableUtil.requireNonNull(null, "myField"),
                "requireNonNull should throw for null value");
        assertTrue(ex.getMessage().contains("myField"), "message should contain field name");
        assertTrue(ex.getMessage().contains("must not be null"), "message should contain must not be null");
    }

    @Test
    @DisplayName("requireTrue does not throw when test is true")
    void requireTrueWithTrue() {
        assertDoesNotThrow(() -> ValidatableUtil.requireTrue(true, "should pass"),
                "requireTrue should not throw when test is true");
    }

    @Test
    @DisplayName("requireTrue throws when test is false")
    void requireTrueWithFalse() {
        InvalidMarshallableException ex = assertThrows(InvalidMarshallableException.class,
                () -> ValidatableUtil.requireTrue(false, "validation failed"),
                "requireTrue should throw when test is false");
        assertEquals("validation failed", ex.getMessage(), "message should match");
    }

    @Test
    @DisplayName("validate returns null for null input")
    void validateNull() throws InvalidMarshallableException {
        assertNull(ValidatableUtil.validate(null), "validate should return null for null input");
    }
}
