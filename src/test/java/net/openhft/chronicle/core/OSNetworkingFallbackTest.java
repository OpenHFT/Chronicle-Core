//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OSNetworkingFallbackTest {

    @Test
    void hostnameAndIpAddressNonEmpty() {
        String hn = OS.getHostName();
        assertNotNull(hn);
        assertFalse(hn.isEmpty());

        String ip = OS.getIPAddress();
        assertNotNull(ip);
        assertFalse(ip.isEmpty());
    }
}
