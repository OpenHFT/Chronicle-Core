/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SuppressWarnings("deprecation")
class DirectBufferUtilTest {

    @BeforeEach
    void addOpens() {
        assumeTrue(Jvm.maxDirectMemory() > 0);
    }

    @DisplayName("directBufferClassShouldReturnCorrectClass behaviour under expected input and output conditions")
    @Test
    void directBufferClassShouldReturnCorrectClass() {
        assertEquals("sun.nio.ch.DirectBuffer", DirectBufferUtil.directBufferClass().getName(), "DirectBuffer class name should be returned");
    }

    @DisplayName("cleanIfInstanceOfDirectBufferShouldCleanDirectBuffer behaviour under expected input and output conditions")
    @Test
    void cleanIfInstanceOfDirectBufferShouldCleanDirectBuffer() {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);

        assertDoesNotThrow(() -> DirectBufferUtil.cleanIfInstanceOfDirectBuffer(directBuffer), "Cleaning a direct buffer should not throw an exception");
    }

    @DisplayName("cleanIfInstanceOfDirectBufferShouldNotThrowForNonDirectBuffer behaviour under expected input and output conditions")
    @Test
    void cleanIfInstanceOfDirectBufferShouldNotThrowForNonDirectBuffer() {
        ByteBuffer nonDirectBuffer = ByteBuffer.allocate(1024);

        assertDoesNotThrow(() -> DirectBufferUtil.cleanIfInstanceOfDirectBuffer(nonDirectBuffer), "Non-direct buffer should not throw an exception");
    }

    @DisplayName("addressOrThrowShouldReturnAddressForDirectBuffer behaviour under expected input and output conditions")
    @Test
    void addressOrThrowShouldReturnAddressForDirectBuffer() {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);

        assertDoesNotThrow(() -> DirectBufferUtil.addressOrThrow(directBuffer), "Getting address of a direct buffer should not throw an exception");
    }

    @DisplayName("addressOrThrowShouldThrowForNonDirectBuffer behaviour under expected input and output conditions")
    @Test
    void addressOrThrowShouldThrowForNonDirectBuffer() {
        ByteBuffer nonDirectBuffer = ByteBuffer.allocate(1024);

        assertThrows(ClassCastException.class, () -> DirectBufferUtil.addressOrThrow(nonDirectBuffer), "Non-direct buffer should throw ClassCastException");
    }
}
