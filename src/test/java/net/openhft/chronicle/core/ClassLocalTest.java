/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.ClassLocal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassLocalTest extends CoreTestCommon {

    @Test
    @DisplayName("ClassLocal computes value once per class")
    void computeValue() {
        long[] count = {0};
        ClassLocal<String> toString = ClassLocal.withInitial(aClass -> {
            count[0]++;
            return aClass.toGenericString();
        });
        for (int i = 0; i < 1000; i++) {
            toString.get(ClassValue.class);
        }
        assertEquals(1, count[0], "ClassLocal should compute value once for ClassValue");

    }
}
