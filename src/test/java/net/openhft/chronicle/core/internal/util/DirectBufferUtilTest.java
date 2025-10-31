/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class DirectBufferUtilTest {

    @BeforeEach
    public void addOpens() {
        assumeTrue(Jvm.maxDirectMemory() > 0);
    }

    @Test
    public void directBufferClassShouldReturnCorrectClass() {
        assertEquals(sun.nio.ch.DirectBuffer.class, DirectBufferUtil.directBufferClass(), "DirectBuffer class should be returned");
    }

    @Test
    public void cleanIfInstanceOfDirectBufferShouldCleanDirectBuffer() {
        assumeFalse(Jvm.isJava9Plus());
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);

        assertDoesNotThrow(() -> DirectBufferUtil.cleanIfInstanceOfDirectBuffer(directBuffer), "Cleaning a direct buffer should not throw an exception");
    }

    @Test
    public void cleanIfInstanceOfDirectBufferShouldNotThrowForNonDirectBuffer() {
        ByteBuffer nonDirectBuffer = ByteBuffer.allocate(1024);

        assertDoesNotThrow(() -> DirectBufferUtil.cleanIfInstanceOfDirectBuffer(nonDirectBuffer), "Non-direct buffer should not throw an exception");
    }

    @Test
    public void addressOrThrowShouldReturnAddressForDirectBuffer() {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);

        assertDoesNotThrow(() -> DirectBufferUtil.addressOrThrow(directBuffer), "Getting address of a direct buffer should not throw an exception");
    }

    @Test
    public void addressOrThrowShouldThrowForNonDirectBuffer() {
        ByteBuffer nonDirectBuffer = ByteBuffer.allocate(1024);

        assertThrows(ClassCastException.class, () -> DirectBufferUtil.addressOrThrow(nonDirectBuffer), "Non-direct buffer should throw ClassCastException");
    }
}
