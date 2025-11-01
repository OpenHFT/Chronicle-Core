/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner.impl.reflect;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.cleaner.impl.CleanerTestUtil;
import net.openhft.chronicle.core.internal.cleaner.ReflectionBasedByteBufferCleanerService;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class ReflectionBasedByteBufferCleanerServiceTest extends CoreTestCommon {
    @Test
    public void shouldCleanBuffer() {
        CleanerTestUtil.test(new ReflectionBasedByteBufferCleanerService()::clean);
        assertTrue(true);
    }
}
