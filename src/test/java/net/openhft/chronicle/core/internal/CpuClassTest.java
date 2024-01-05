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
            assertEquals(cpuClass, "Apple M1", cpuClass);

        } else if (Jvm.isArm()) {
            assertTrue(cpuClass, cpuClass.startsWith("ARMv"));

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
