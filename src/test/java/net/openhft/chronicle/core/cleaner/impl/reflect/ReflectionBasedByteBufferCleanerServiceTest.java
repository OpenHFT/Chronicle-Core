/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner.impl.reflect;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.cleaner.impl.CleanerTestUtil;
import net.openhft.chronicle.core.internal.cleaner.ReflectionBasedByteBufferCleanerService;
import org.junit.jupiter.api.Test;

class ReflectionBasedByteBufferCleanerServiceTest extends CoreTestCommon {
    @Test
    void shouldCleanBuffer() {
        CleanerTestUtil.test(new ReflectionBasedByteBufferCleanerService()::clean);
    }
}
