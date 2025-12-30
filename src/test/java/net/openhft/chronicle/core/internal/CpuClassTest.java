/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class CpuClassTest {
    @DisplayName("CPU model string matches expected vendor patterns")
    @Test
    void getCpuModel() {
        final String cpuClass = CpuClass.getCpuModel();
        System.out.println("cpuClass: " + cpuClass + ", os.name: " + System.getProperty("os.name") + ", os.arch: " + System.getProperty("os.arch"));
        if (Jvm.isMacArm()) {
            assertTrue(cpuClass.startsWith("Apple M") || cpuClass.startsWith("aarch64"), cpuClass);

        } else if (Jvm.isArm()) {
            assertTrue(cpuClass.startsWith("ARMv")
                            || cpuClass.startsWith("aarch64"),
                    cpuClass);

        } else {
            assertTrue(cpuClass.contains("Intel")
                            || (cpuClass.startsWith("AMD ")),
                    cpuClass);
        }

        assertNotNull(cpuClass, "required object should not be null");
    }

    @DisplayName("Removing tag strips prefix and returns value")
    @Test
    void removingTag() {
        // TODO FIX on MacOS. sysctl -a returned 141, https://github.com/OpenHFT/Chronicle-Core/issues/557
        assumeFalse(net.openhft.chronicle.core.internal.Bootstrap.IS_MAC);
        final String actual = CpuClass.removingTag().apply("tag: value");
        assertEquals("value", actual, "removingTag should strip prefix and return value");
    }

    @DisplayName("CPU model returns non null identifier string")
    @Test
    void getCpuModelShouldReturnNonNullValue() {
        assertNotNull(CpuClass.getCpuModel(), "CPU model should not be null");
    }

    @DisplayName("CPU model returns non empty identifier string")
    @Test
    void getCpuModelShouldReturnNonEmptyValue() {
        assertNotEquals("", CpuClass.getCpuModel(), "CPU model should not be an empty string");
    }
}
