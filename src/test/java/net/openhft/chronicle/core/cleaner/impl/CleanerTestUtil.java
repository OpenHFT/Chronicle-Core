/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner.impl;

import net.openhft.chronicle.core.Jvm;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

public final class CleanerTestUtil {

    public static final class ReservedMemorySnapshot {
        public final long before;
        public final long after;

        private ReservedMemorySnapshot(long before, long after) {
            this.before = before;
            this.after = after;
        }
    }

    private CleanerTestUtil() {
    }

    public static ReservedMemorySnapshot captureReservedMemory(final Consumer<ByteBuffer> cleaner) {
        requireNonNull(cleaner);
        try {
            final AtomicLong reservedMemory;

            // Unable to reflect on Java17+
            if (Jvm.majorVersion() < 16) {
                Class<?> bitsClass = Class.forName("java.nio.Bits");
                Field field;
                try {
                    field = bitsClass.getDeclaredField("RESERVED_MEMORY");
                } catch (NoSuchFieldException nfe) {
                    // Java8 name
                    field = bitsClass.getDeclaredField("reservedMemory");
                }
                field.setAccessible(true);
                reservedMemory = (AtomicLong) field.get(null);
            } else {
                // Just assume zero...
                reservedMemory = new AtomicLong();
            }
            long allocatedBefore = reservedMemory.get();
            final ByteBuffer bb = ByteBuffer.allocateDirect(64);
            cleaner.accept(bb);
            long allocatedAfter = reservedMemory.get();

            return new ReservedMemorySnapshot(allocatedBefore, allocatedAfter);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("Failed to read reserved memory counters", e);
        }
    }
}
