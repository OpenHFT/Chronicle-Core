/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Covers holder classes inside OS to ensure their static init paths are exercised.
 */
public class OSHolderInitTest extends CoreTestCommon {

    @Test
    public void fdFieldHolderExposesFileDescriptorField() {
        Field fdField = OS.FDFieldHolder.FD_FIELD;
        assertNotNull("FDFieldHolder should resolve fd field", fdField);
    }

    @Test
    public void read0AndWrite0MethodHandlesPresent() {
        MethodHandle read0 = OS.Read0Holder.READ0_MH;
        assertNotNull("Read0 method handle should be resolved", read0);

        // One of the write handles should be available depending on JDK signature
        assertTrue("At least one write0 method handle should be resolved",
                OS.Write0Holder.WRITE0_MH != null || OS.Write0Holder.WRITE0_MH2 != null);
    }

    @Test
    public void unmap0MethodHandlePresent() {
        MethodHandle unmap0 = OS.Unmapp0Holder.UNMAPP0_MH;
        assertNotNull("Unmapp0 method handle should be resolved", unmap0);
    }

    @Test
    public void hostnameHolderInitialises() {
        assertNotNull(OS.HostnameHolder.HOST_NAME);
        assertFalse(OS.HostnameHolder.HOST_NAME.isEmpty());
    }

    @Test
    public void ipAddressHolderInitialises() {
        assertNotNull(OS.IPAddressHolder.IP_ADDRESS);
        assertFalse(OS.IPAddressHolder.IP_ADDRESS.isEmpty());
        assertEquals("0.0.0.0", OS.IPAddressHolder.NO_ADDRESS);
    }
}
