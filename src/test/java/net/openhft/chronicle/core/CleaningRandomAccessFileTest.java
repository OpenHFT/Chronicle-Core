/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CleaningRandomAccessFileTest extends CoreTestCommon {

    @Test
    public void resourceLeak() throws IOException {
        net.openhft.chronicle.core.io.CleaningRandomAccessFileTest.assertNoResourceLeak();
    }
}
