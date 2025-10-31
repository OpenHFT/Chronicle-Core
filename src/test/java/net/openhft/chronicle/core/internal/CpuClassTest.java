/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

public class CpuClassTest {
    @Test
    public void getCpuModel() {
        final String cpuClass = CpuClass.getCpuModel();
        System.out.println("cpuClass: " + cpuClass + ", os.name: " + System.getProperty("os.name") + ", os.arch: " + System.getProperty("os.arch"));
        if (Jvm.isMacArm()) {
            assertTrue(cpuClass, cpuClass.startsWith("Apple M"));

        } else if (Jvm.isArm()) {
            assertTrue(cpuClass, cpuClass.startsWith("ARMv")
                            || cpuClass.startsWith("aarch64"));

        } else {
            assertTrue(cpuClass,
                    cpuClass.contains("Intel")
                            || (cpuClass.startsWith("AMD ")));
        }

        assertNotNull(cpuClass);
    }

    @Test
    public void removingTag() {
        // TODO FIX on MacOS. sysctl -a returned 141, https://github.com/OpenHFT/Chronicle-Core/issues/557
        assumeFalse(net.openhft.chronicle.core.internal.Bootstrap.IS_MAC);
        final String actual = CpuClass.removingTag().apply("tag: value");
        assertEquals("value", actual);
    }

    @Test
    public void getCpuModelShouldReturnNonNullValue() {
        assertNotNull(CpuClass.getCpuModel(), "CPU model should not be null");
    }

    @Test
    public void getCpuModelShouldReturnNonEmptyValue() {
        assertNotEquals("", CpuClass.getCpuModel(), "CPU model should not be an empty string");
    }
}
