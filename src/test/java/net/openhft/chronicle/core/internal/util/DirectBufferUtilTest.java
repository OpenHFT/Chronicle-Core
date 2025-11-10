//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
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
    void addOpens() {
        assumeTrue(Jvm.maxDirectMemory() > 0);
    }

    @Test
    void directBufferClassShouldReturnCorrectClass() {
        assertEquals(sun.nio.ch.DirectBuffer.class, DirectBufferUtil.directBufferClass(), "DirectBuffer class should be returned");
    }

    @Test
    void cleanIfInstanceOfDirectBufferShouldCleanDirectBuffer() {
        assumeFalse(Jvm.isJava9Plus());
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);

        assertDoesNotThrow(() -> DirectBufferUtil.cleanIfInstanceOfDirectBuffer(directBuffer), "Cleaning a direct buffer should not throw an exception");
    }

    @Test
    void cleanIfInstanceOfDirectBufferShouldNotThrowForNonDirectBuffer() {
        ByteBuffer nonDirectBuffer = ByteBuffer.allocate(1024);

        assertDoesNotThrow(() -> DirectBufferUtil.cleanIfInstanceOfDirectBuffer(nonDirectBuffer), "Non-direct buffer should not throw an exception");
    }

    @Test
    void addressOrThrowShouldReturnAddressForDirectBuffer() {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);

        assertDoesNotThrow(() -> DirectBufferUtil.addressOrThrow(directBuffer), "Getting address of a direct buffer should not throw an exception");
    }

    @Test
    void addressOrThrowShouldThrowForNonDirectBuffer() {
        ByteBuffer nonDirectBuffer = ByteBuffer.allocate(1024);

        assertThrows(ClassCastException.class, () -> DirectBufferUtil.addressOrThrow(nonDirectBuffer), "Non-direct buffer should throw ClassCastException");
    }
}
