/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
        try {
            d.toString();
            fail();
            throw new InvalidMarshallableException(null); // keep the compiler happy
        } catch (InvalidMarshallableException expected) {
            // expected
        }
        d.b = 1;
        try {
            d.toString();
            fail();
            throw new InvalidMarshallableException(null); // keep the compiler happy
        } catch (InvalidMarshallableException expected) {
            // expected
        }
        d.a = "hi";
        d.b = 1;
        assertEquals("DTOWithValidateToString{a='hi', b=1}", d.toString()); // is ok
        d.b = 0;
        try {
            d.toString();
            fail();
            throw new InvalidMarshallableException(null); // keep the compiler happy
        } catch (InvalidMarshallableException expected) {
            // expected
        }
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
        try {
            d.toString();
            fail();
            throw new InvalidMarshallableException(null); // keep the compiler happy
        } catch (InvalidMarshallableException expected) {
            // expected
        }
        boolean failed = false;
        try {
            ValidatableUtil.endValidateDisabled();
            failed = true;
        } catch (AssertionError expected) {
            // expected
        }
        assertFalse(failed);
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
