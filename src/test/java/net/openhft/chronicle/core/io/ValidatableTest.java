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

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ValidatableTest extends CoreTestCommon {

    private DTOWithValidateToString dto;

    @Before
    public void setUp() {
        dto = new DTOWithValidateToString();
    }

    @Test
    public void shouldThrowExceptionWhenFieldANull() {
        dto.b = 1;
        assertThrows(InvalidMarshallableException.class, dto::toString);
    }

    @Test
    public void shouldThrowExceptionWhenFieldBLessThanOrEqualToZero() {
        dto.a = "hi";
        assertThrows(InvalidMarshallableException.class, dto::toString);
    }

    @Test
    public void shouldPassValidation() {
        dto.a = "hi";
        dto.b = 1;
        assertEquals("DTOWithValidateToString{a='hi', b=1}", dto.toString());
    }

    @Test
    public void shouldNotThrowExceptionWhenValidationDisabled() {
        ValidatableUtil.startValidateDisabled();

        dto.a = null;
        dto.b = 0;
        assertEquals("DTOWithValidateToString{a='null', b=0}", dto.toString());

        dto.b = 1;
        assertEquals("DTOWithValidateToString{a='null', b=1}", dto.toString());
        ValidatableUtil.endValidateDisabled();
    }

    @Test
    public void shouldReEnableValidation() {
        ValidatableUtil.startValidateDisabled();
        assertFalse(ValidatableUtil.validateEnabled());

        ValidatableUtil.endValidateDisabled();
        assertTrue(ValidatableUtil.validateEnabled());
    }

    @Test(expected = AssertionError.class)
    public void shouldThrowAssertionErrorWhenEndValidationCalledWithoutStart() {
        ValidatableUtil.endValidateDisabled();
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
