/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidatableTest extends CoreTestCommon {

        @Test
        void validate() {
            DTOWithValidateToString d = new DTOWithValidateToString();
            assertThrows(InvalidMarshallableException.class, d::toString,
                    "toString should reject missing fields");
            d.b = 1;
            assertThrows(InvalidMarshallableException.class, d::toString,
                    "toString should reject missing a value");
            d.a = "hi";
            d.b = 1;
            assertEquals("DTOWithValidateToString{a='hi', b=1}", d.toString(), "toString should succeed when all fields are valid"); // is ok
            d.b = 0;
            assertThrows(InvalidMarshallableException.class, d::toString,
                    "toString should reject invalid b value");
        }

    @Test
    void validateDisabled() {

        assertTrue(ValidatableUtil.validateEnabled(), "validation should be enabled by default");
        ValidatableUtil.startValidateDisabled();
        assertFalse(ValidatableUtil.validateEnabled(), "validation disabled after start");
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
            assertTrue(ValidatableUtil.validateEnabled(), "validation enabled after end");
        }
        assertThrows(InvalidMarshallableException.class, d::toString,
                "toString should reject invalid data when enabled");
        assertThrows(AssertionError.class, ValidatableUtil::endValidateDisabled,
                "endValidateDisabled should fail when not started");
        assertTrue(ValidatableUtil.validateEnabled(), "validation stays enabled after error");
    }

    static class DTOWithValidateToString implements Validatable {
        String a;
        long b;

        @Override
        public void validate() throws InvalidMarshallableException {
            if (a == null) throw new InvalidMarshallableException("required property value must be set");
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
