/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OSNetworkingFallbackTest {

    @Test
    void hostnameAndIpAddressNonEmpty() {
        String hn = OS.getHostName();
        assertNotNull(hn, "hostnameAndIpAddressNonEmpty: L15");
        assertFalse(hn.isEmpty(), "hostnameAndIpAddressNonEmpty: L16");

        String ip = OS.getIPAddress();
        assertNotNull(ip, "hostnameAndIpAddressNonEmpty: L19");
        assertFalse(ip.isEmpty(), "hostnameAndIpAddressNonEmpty: L20");
    }
}
