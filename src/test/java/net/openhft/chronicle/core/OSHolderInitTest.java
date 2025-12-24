/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers holder classes inside OS to ensure their static init paths are exercised.
 */
class OSHolderInitTest extends CoreTestCommon {

    @Test
    void fdFieldHolderExposesFileDescriptorField() {
        Field fdField = OS.FDFieldHolder.FD_FIELD;
        assertNotNull(fdField, "FDFieldHolder should resolve fd field");
    }

    @Test
    void read0AndWrite0MethodHandlesPresent() {
        MethodHandle read0 = OS.Read0Holder.READ0_MH;
        assertNotNull(read0, "Read0 method handle should be resolved");

        // One of the write handles should be available depending on JDK signature
        assertTrue(OS.Write0Holder.WRITE0_MH != null || OS.Write0Holder.WRITE0_MH2 != null,
                "At least one write0 method handle should be resolved");
    }

    @Test
    void unmap0MethodHandlePresent() {
        MethodHandle unmap0 = OS.Unmapp0Holder.UNMAPP0_MH;
        assertNotNull(unmap0, "Unmapp0 method handle should be resolved");
    }

    @Test
    void hostnameHolderInitialises() {
        assertNotNull(OS.HostnameHolder.HOST_NAME, "host name should be initialised");
        assertFalse(OS.HostnameHolder.HOST_NAME.isEmpty(), "host name should not be empty");
    }

    @Test
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    void ipAddressHolderInitialises() {
        assertNotNull(OS.IPAddressHolder.IP_ADDRESS, "IP address should be initialised");
        assertFalse(OS.IPAddressHolder.IP_ADDRESS.isEmpty(), "IP address should not be empty");
        assertEquals("0.0.0.0", OS.IPAddressHolder.NO_ADDRESS, "no-address constant should be 0.0.0.0");
    }
}
