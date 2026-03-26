/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner.impl.jdk9;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.cleaner.impl.CleanerTestUtil;
import net.openhft.chronicle.core.internal.cleaner.Jdk9ByteBufferCleanerService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assumptions.*;

class Jdk9ByteBufferCleanerServiceTest extends CoreTestCommon {
    @Test
    void shouldCleanBuffer() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        assumeTrue(Jvm.isJava9Plus());

        CleanerTestUtil.test(new Jdk9ByteBufferCleanerService()::clean);
    }
}
