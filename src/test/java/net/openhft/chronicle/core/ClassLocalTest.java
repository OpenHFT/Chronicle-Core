//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.ClassLocal;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClassLocalTest extends CoreTestCommon {

    @Test
    public void computeValue() {
        long[] count = {0};
        ClassLocal<String> toString = ClassLocal.withInitial(aClass -> {
//            System.out.println(aClass);
            count[0]++;
            return aClass.toGenericString();
        });
        for (int i = 0; i < 1000; i++) {
            toString.get(ClassValue.class);
        }
        assertEquals(1, count[0]);

    }
}
