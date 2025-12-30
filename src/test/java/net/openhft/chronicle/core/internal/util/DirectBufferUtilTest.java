/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
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

    @DisplayName("Direct buffer class should return correct class util")
    @Test
    void directBufferClassShouldReturnCorrectClass() {
        assertEquals("sun.nio.ch.DirectBuffer", DirectBufferUtil.directBufferClass().getName(), "DirectBuffer class name should be returned");
    }

    @DisplayName("Clean if instance of direct buffer should clean direct buffer util")
    @Test
    void cleanIfInstanceOfDirectBufferShouldCleanDirectBuffer() {
        assertFalse(Jvm.isJava9Plus(), "This method gets no such method error on Java 9+");
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);

        assertDoesNotThrow(() -> DirectBufferUtil.cleanIfInstanceOfDirectBuffer(directBuffer), "Cleaning a direct buffer should not throw an exception");
    }

    @DisplayName("Clean if instance of direct buffer should not throw for non direct buffer")
    @Test
    void cleanIfInstanceOfDirectBufferShouldNotThrowForNonDirectBuffer() {
        ByteBuffer nonDirectBuffer = ByteBuffer.allocate(1024);

        assertDoesNotThrow(() -> DirectBufferUtil.cleanIfInstanceOfDirectBuffer(nonDirectBuffer), "Non-direct buffer should not throw an exception");
    }

    @DisplayName("Address or throw should return address for direct buffer")
    @Test
    void addressOrThrowShouldReturnAddressForDirectBuffer() {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);

        assertDoesNotThrow(() -> DirectBufferUtil.addressOrThrow(directBuffer), "Getting address of a direct buffer should not throw an exception");
    }

    @DisplayName("Address or throw should throw for non direct buffer")
    @Test
    void addressOrThrowShouldThrowForNonDirectBuffer() {
        ByteBuffer nonDirectBuffer = ByteBuffer.allocate(1024);

        assertThrows(ClassCastException.class, () -> DirectBufferUtil.addressOrThrow(nonDirectBuffer), "Non-direct buffer should throw ClassCastException");
    }
}
