/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CleanerServiceImpactVersionTest {

    private static void resetLocator() throws Exception {
        java.lang.reflect.Field init = CleanerServiceLocator.class.getDeclaredField("initialised");
        init.setAccessible(true);
        init.setBoolean(null, false);
        java.lang.reflect.Field inst = CleanerServiceLocator.class.getDeclaredField("instance");
        inst.setAccessible(true);
        inst.set(null, null);
    }

    @AfterEach
    void tearDown() throws Exception {
        resetLocator();
    }

    @Test
    void lowerImpactPreferredAndVersionGateApplied() throws Exception {
        resetLocator();
        ByteBufferCleanerService svc = CleanerServiceLocator.cleanerService();
        assertNotNull(svc);
        String name = svc.getClass().getName();
        // SomeImpactCleaner is available but NO_IMPACT (AllowedCleaner) must be chosen.
        assertEquals("net.openhft.chronicle.core.cleaner.testimpl.AllowedCleaner", name);
        // And DisallowedCleaner must never be selected due to @TargetMajorVersion(99)
        assertNotEquals("net.openhft.chronicle.core.cleaner.testimpl.DisallowedCleaner", name);
    }
}

