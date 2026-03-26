/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class ObjectUtilsConvertToTest extends CoreTestCommon {

    @ParameterizedTest
    @MethodSource("data")
    void convertTo(Object converted, String input) throws IllegalStateException, IllegalArgumentException {
        assertEquals(converted, ObjectUtils.convertTo(converted.getClass(), input));
    }

    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Boolean.TRUE, "Y"},
                {Boolean.TRUE, "yes"},
                {Boolean.FALSE, "N"},
                {Boolean.FALSE, "no"},
                {1.0, "1.0"},
                {1, "1"},
                {1L, "1"},
                {DEnum.ZERO, "Zero"},
                {DEnum.ONE, "One"},
                {DEnum.TWO, "Two"},
        });
    }

    static class DEnum implements CoreDynamicEnum<DEnum> {
        static final DEnum ZERO = new DEnum("Zero", 0);
        static final DEnum ONE = new DEnum("One", 1);
        static final DEnum TWO = new DEnum("Two", 2);

        private final String name;
        private final int ordinal;

        DEnum(String name, int ordinal) {
            this.name = name;
            this.ordinal = ordinal;
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
