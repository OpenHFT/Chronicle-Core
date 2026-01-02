/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class OSNetworkingFallbackTest {

    @Test
    @DisplayName("hostname and IP address fallbacks are non empty")
    void hostnameAndIpAddressNonEmpty() {
        String hn = OS.getHostName();
        assertNotNull(hn, "hostname fallback should return non-null");
        assertFalse(hn.isEmpty(), "hostname should not be empty string");

        String ip = OS.getIPAddress();
        assertNotNull(ip, "IP address fallback should return non-null");
        assertFalse(ip.isEmpty(), "IP address should not be empty string");
    }
}
