/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class CpuClassTest {
    @Test
    void getCpuModel() {
        final String cpuClass = CpuClass.getCpuModel();
        System.out.println("cpuClass: " + cpuClass + ", os.name: " + System.getProperty("os.name") + ", os.arch: " + System.getProperty("os.arch"));
        if (Jvm.isMacArm()) {
            assertTrue(cpuClass.startsWith("Apple M") || cpuClass.startsWith("aarch64"), cpuClass);

        } else if (Jvm.isArm()) {
            assertTrue(cpuClass.startsWith("ARMv")
                    || cpuClass.startsWith("aarch64"), cpuClass);

        } else {
            assertTrue(cpuClass.contains("Intel")
                    || (cpuClass.startsWith("AMD ")), cpuClass);
        }

        assertNotNull(cpuClass);
    }

    @Test
    void removingTag() {
        // TODO FIX on MacOS. sysctl -a returned 141, https://github.com/OpenHFT/Chronicle-Core/issues/557
        assumeFalse(Bootstrap.IS_MAC);
        final String actual = CpuClass.removingTag().apply("tag: value");
        assertEquals("value", actual);
    }

    @Test
    void getCpuModelShouldReturnNonNullValue() {
        assertNotNull(CpuClass.getCpuModel(), "CPU model should not be null");
    }

    @Test
    void getCpuModelShouldReturnNonEmptyValue() {
        assertNotEquals("", CpuClass.getCpuModel(), "CPU model should not be an empty string");
    }
}
