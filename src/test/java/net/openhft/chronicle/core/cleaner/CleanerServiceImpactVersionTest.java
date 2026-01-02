/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("lower impact preferred and version gate applied")
    void lowerImpactPreferredAndVersionGateApplied() throws Exception {
        resetLocator();
        ByteBufferCleanerService svc = CleanerServiceLocator.cleanerService();
        assertNotNull(svc, "service implementation should be found");
        String name = svc.getClass().getName();
        // SomeImpactCleaner is available but NO_IMPACT (AllowedCleaner) must be chosen.
        assertEquals("net.openhft.chronicle.core.cleaner.testimpl.AllowedCleaner", name, "allowed cleaner should be selected with lowest impact");
        // And DisallowedCleaner must never be selected due to @TargetMajorVersion(99)
        assertNotEquals("net.openhft.chronicle.core.cleaner.testimpl.DisallowedCleaner", name,
                "disallowed cleaner should never be selected due to major version gate");
    }
}
