/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CleanerServicePriorityTest {

    @AfterEach
    void tearDown() throws Exception {
        CleanerServiceTestSupport.resetLocator();
    }

    @Test
    void lowestImpactChosenRegardlessOfDiscoveryOrder() throws Exception {
        ByteBufferCleanerService svcChosen = CleanerServiceTestSupport.chooseService("tmp-services-priority",
                "net.openhft.chronicle.core.cleaner.testimpl.AllowedCleaner\n" +
                        "net.openhft.chronicle.core.cleaner.testimpl.SomeImpactCleaner\n");
        assertEquals("net.openhft.chronicle.core.cleaner.testimpl.AllowedCleaner", svcChosen.getClass().getName(), "lowestImpactChosenRegardlessOfDiscoveryOrder: L24");
    }
}
