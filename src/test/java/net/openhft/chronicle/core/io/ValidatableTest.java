/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidatableTest extends CoreTestCommon {

    @DisplayName("validate behaviour under expected input and output conditions")
    @Test
    void validate() {
        DTOWithValidateToString d = new DTOWithValidateToString();
        assertThrows(InvalidMarshallableException.class, d::toString,
                "toString should throw when a and b are unset");
        d.b = 1;
        assertThrows(InvalidMarshallableException.class, d::toString,
                "toString should throw when a is null");
        d.a = "hi";
        d.b = 1;
        assertEquals("DTOWithValidateToString{a='hi', b=1}", d.toString(), "toString should succeed when all fields are valid"); // is ok
        d.b = 0;
        assertThrows(InvalidMarshallableException.class, d::toString,
                "toString should throw when b is non-positive");
    }

    @DisplayName("validateDisabled behaviour under expected input and output conditions")
    @Test
    void validateDisabled() {

        assertTrue(ValidatableUtil.validateEnabled(), "validation should be enabled by default");
        ValidatableUtil.startValidateDisabled();
        assertFalse(ValidatableUtil.validateEnabled(), "validation should be disabled after calling startValidateDisabled");
        DTOWithValidateToString d = new DTOWithValidateToString();
        try {
            assertEquals("DTOWithValidateToString{a='null', b=0}", d.toString(), "toString should succeed with invalid fields when validation is disabled"); // is ok

            d.b = 1;
            assertEquals("DTOWithValidateToString{a='null', b=1}", d.toString(), "toString should succeed with partially valid fields when validation is disabled"); // is ok

            d.a = "hi";
            d.b = 1;
            assertEquals("DTOWithValidateToString{a='hi', b=1}", d.toString(), "toString should succeed with valid fields when validation is disabled"); // is ok

            ValidatableUtil.startValidateDisabled();
            try {
                d.b = 0;
                assertEquals("DTOWithValidateToString{a='hi', b=0}", d.toString(), "toString should succeed with invalid b value in nested disabled scope"); // is ok
            } finally {
                ValidatableUtil.endValidateDisabled();
            }
        } finally {
            ValidatableUtil.endValidateDisabled();
            assertTrue(ValidatableUtil.validateEnabled(), "validation should be re-enabled after calling endValidateDisabled");
        }
        assertThrows(InvalidMarshallableException.class, d::toString,
                "toString should throw when validation is re-enabled and fields are invalid");
        assertThrows(AssertionError.class, ValidatableUtil::endValidateDisabled,
                "endValidateDisabled should throw when called without a matching start");
        assertTrue(ValidatableUtil.validateEnabled(), "validation should remain enabled after endValidateDisabled throws");
    }

    static class DTOWithValidateToString implements Validatable {
        String a;
        long b;

        @Override
        public void validate() throws InvalidMarshallableException {
            if (a == null) throw new InvalidMarshallableException("DTO field a must not be null");
            if (b <= 0) throw new InvalidMarshallableException("b must be positive");
        }

        @Override
        public String toString() {
            try {
                ValidatableUtil.validate(this);
            } catch (InvalidMarshallableException e) {
                throw Jvm.rethrow(e);
            }
            return "DTOWithValidateToString{" +
                    "a='" + a + '\'' +
                    ", b=" + b +
                    '}';
        }
    }
}
