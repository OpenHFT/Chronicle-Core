/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CleanerServiceMixedTest {

    @AfterEach
    void tearDown() throws Exception {
        CleanerServiceTestSupport.resetLocator();
    }

    @Test
    void mixedValidAndInvalidEntriesStillChooseValidProvider() throws Exception {
        ByteBufferCleanerService svcChosen = CleanerServiceTestSupport.chooseService("tmp-services-mixed",
                "does.not.ExistProvider\nnet.openhft.chronicle.core.cleaner.testimpl.AllowedCleaner\n");
        assertEquals("net.openhft.chronicle.core.cleaner.testimpl.AllowedCleaner", svcChosen.getClass().getName(), "mixedValidAndInvalidEntriesStillChooseValidProvider: L23");
    }
}
