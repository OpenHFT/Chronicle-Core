/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import static org.junit.Assert.*;

public class ValidatableTest extends CoreTestCommon {

    @Test
    public void validate() {
        DTOWithValidateToString d = new DTOWithValidateToString();
        assertThrows(InvalidMarshallableException.class, d::toString);
        d.b = 1;
        assertThrows(InvalidMarshallableException.class, d::toString);
        d.a = "hi";
        d.b = 1;
        assertEquals("DTOWithValidateToString{a='hi', b=1}", d.toString()); // is ok
        d.b = 0;
        assertThrows(InvalidMarshallableException.class, d::toString);
    }

    @Test
    public void validateDisabled() {

        assertTrue(ValidatableUtil.validateEnabled());
        ValidatableUtil.startValidateDisabled();
        assertFalse(ValidatableUtil.validateEnabled());
        DTOWithValidateToString d = new DTOWithValidateToString();
        try {
            assertEquals("DTOWithValidateToString{a='null', b=0}", d.toString()); // is ok

            d.b = 1;
            assertEquals("DTOWithValidateToString{a='null', b=1}", d.toString()); // is ok

            d.a = "hi";
            d.b = 1;
            assertEquals("DTOWithValidateToString{a='hi', b=1}", d.toString()); // is ok

            ValidatableUtil.startValidateDisabled();
            try {
                d.b = 0;
                assertEquals("DTOWithValidateToString{a='hi', b=0}", d.toString()); // is ok
            } finally {
                ValidatableUtil.endValidateDisabled();
            }
        } finally {
            ValidatableUtil.endValidateDisabled();
            assertTrue(ValidatableUtil.validateEnabled());
        }
        assertThrows(InvalidMarshallableException.class, d::toString);
        assertThrows(AssertionError.class, ValidatableUtil::endValidateDisabled);
        assertTrue(ValidatableUtil.validateEnabled());
    }

    static class DTOWithValidateToString implements Validatable {
        String a;
        long b;

        @Override
        public void validate() throws InvalidMarshallableException {
            if (a == null) throw new InvalidMarshallableException("a must not be null");
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
