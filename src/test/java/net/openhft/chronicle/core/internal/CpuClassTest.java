/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class CpuClassTest {
    @Test
    void getCpuModel() {
        final String cpuClass = CpuClass.getCpuModel();
        System.out.println("cpuClass: " + cpuClass + ", os.name: " + System.getProperty("os.name") + ", os.arch: " + System.getProperty("os.arch"));
        // A bounded or unavailable OS probe legitimately returns the architecture.
        boolean architectureFallback = cpuClass.equals(System.getProperty("os.arch"));
        if (Jvm.isMacArm()) {
            assertTrue(architectureFallback || cpuClass.startsWith("Apple M") || cpuClass.startsWith("aarch64"), cpuClass);

        } else if (Jvm.isArm()) {
            assertTrue(architectureFallback || cpuClass.startsWith("ARMv")
                    || cpuClass.startsWith("aarch64"), cpuClass);

        } else {
            assertTrue(architectureFallback || cpuClass.contains("Intel")
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

    @Test
    void commandMustCompleteSuccessfullyAndUseStdout() throws Exception {
        assertEquals("fixture CPU", query("ok", 5));
        assertEquals("fallback", query("stderr", 5));
        assertEquals("fallback", query("failure", 5));
        assertEquals("fixture CPU", query("flood", 5));
        assertEquals("fallback", query("oversized", 5));
        long start = System.nanoTime();
        assertEquals("fallback", query("hang", 1));
        assertTrue(System.nanoTime() - start < TimeUnit.SECONDS.toNanos(5));
    }

    private static String query(String mode, long timeout) throws Exception {
        String java = new File(System.getProperty("java.home"), "bin/java").getPath();
        return CpuClass.readModelFromCommand("fallback", new String[]{java, "-cp",
                System.getProperty("java.class.path"), CpuCommand.class.getName(), mode},
                line -> line.startsWith("fixture") ? line : null, timeout, TimeUnit.SECONDS);
    }

    public static class CpuCommand {
        public static void main(String[] args) throws Exception {
            switch (args[0]) {
                case "stderr": System.err.println("fixture error"); return;
                case "hang": new CountDownLatch(1).await(); return;
                case "oversized":
                    for (int i = 0; i < 100000; i++) System.out.println("ignored output line");
                    break;
                case "flood":
                    for (int i = 0; i < 10000; i++) System.out.println("ignored output line");
                    break;
                default: break;
            }
            System.out.println("fixture CPU");
            if (args[0].equals("failure")) System.exit(7);
        }
    }
}
