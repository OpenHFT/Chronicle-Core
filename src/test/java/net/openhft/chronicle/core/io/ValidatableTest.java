/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import static org.junit.Assert.*;

public class ValidatableTest {

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

        ValidatableUtil.startValidatableDisabled();
        DTOWithValidateToString d = new DTOWithValidateToString();
        try {
            assertEquals("DTOWithValidateToString{a='null', b=0}", d.toString()); // is ok

            d.b = 1;
            assertEquals("DTOWithValidateToString{a='null', b=1}", d.toString()); // is ok

            d.a = "hi";
            d.b = 1;
            assertEquals("DTOWithValidateToString{a='hi', b=1}", d.toString()); // is ok

            ValidatableUtil.startValidatableDisabled();
            try {
                d.b = 0;
                assertEquals("DTOWithValidateToString{a='hi', b=0}", d.toString()); // is ok
            } finally {
                ValidatableUtil.endValidateDisabled();
            }
        } finally {
            ValidatableUtil.endValidateDisabled();
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