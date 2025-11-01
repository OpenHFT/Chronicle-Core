/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner.impl.jdk9;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.cleaner.impl.CleanerTestUtil;
import net.openhft.chronicle.core.internal.cleaner.Jdk9ByteBufferCleanerService;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import static org.junit.Assume.assumeTrue;

public class Jdk9ByteBufferCleanerServiceTest extends CoreTestCommon {
    @Test
    public void shouldCleanBuffer() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        assumeTrue(Jvm.isJava9Plus());

        CleanerTestUtil.test(new Jdk9ByteBufferCleanerService()::clean);
        assertTrue(true);
    }
}
