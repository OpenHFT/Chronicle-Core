/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.io.CleaningRandomAccessFileTestSupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class CleaningRandomAccessFileTest extends CoreTestCommon {

    @Test
    void resourceLeak() throws IOException {
        CleaningRandomAccessFileTestSupport.assertNoResourceLeak();
    }
}
