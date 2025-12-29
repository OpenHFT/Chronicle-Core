/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ObjectUtilsConvertToTest extends CoreTestCommon {

    static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(Boolean.TRUE, "Y"),
                Arguments.of(Boolean.TRUE, "yes"),
                Arguments.of(Boolean.FALSE, "N"),
                Arguments.of(Boolean.FALSE, "no"),
                Arguments.of(1.0, "1.0"),
                Arguments.of(1, "1"),
                Arguments.of(1L, "1"),
                Arguments.of(DEnum.ZERO, "Zero"),
                Arguments.of(DEnum.ONE, "One"),
                Arguments.of(DEnum.TWO, "Two")
        );
    }

    @DisplayName("convertTo behaviour under expected input and output conditions")
    @ParameterizedTest
    @MethodSource("data")
    void convertTo(Object converted, String input) throws IllegalStateException, IllegalArgumentException {
        assertEquals(converted, ObjectUtils.convertTo(converted.getClass(), input), "ObjectUtils.convertTo should parse string input into expected target type");
    }

    static class DEnum implements CoreDynamicEnum<DEnum> {
        static final DEnum ZERO = new DEnum("Zero", 0);
        static final DEnum ONE = new DEnum("One", 1);
        static final DEnum TWO = new DEnum("Two", 2);

        private final String name;

        DEnum(String name, int ordinal) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int ordinal() {
            return 0;
        }
    }
}
