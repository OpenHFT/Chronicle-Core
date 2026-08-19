/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Exercises the {@code double} accessors of {@link UnsafeMemory} (including the
 * {@link UnsafeMemory.ARMMemory} unaligned volatile paths) by analogy with
 * {@link UnsafeMemoryLongTest}. See issue #419.
 */
final class UnsafeMemoryDoubleTest implements UnsafeMemoryTestMixin<Double> {

    @Override
    public Class<Double> type() {
        return Double.class;
    }

    @Override
    public IntPredicate alignedToType() {
        return x -> x % 8 == 0;
    }

    @Override
    public Double zero() {
        return 0.0;
    }

    @Override
    public Double nonZero() {
        return Double.MIN_VALUE;
    }

    @Override
    public Stream<Double> sequence() {
        // Distinct, strictly increasing, non-zero values so the reader threads
        // always observe a change between successive writes.
        return IntStream.rangeClosed(1, 40)
                .mapToObj(i -> i * 1.5);
    }

    @Override
    public List<NamedOperation<MemoryLongObjConsumer<Double>>> addressWriteOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::writeDouble", UnsafeMemory::writeDouble),
                new NamedOperation<>("UnsafeMemory::writeVolatileDouble", UnsafeMemory::writeVolatileDouble));
    }

    @Override
    public List<NamedOperation<MemoryLongFunction<Double>>> addressReadOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::readDouble", UnsafeMemory::readDouble),
                new NamedOperation<>("UnsafeMemory::readVolatileDouble", UnsafeMemory::readVolatileDouble));
    }

    @Override
    public List<NamedOperation<MemoryObjLongObjConsumer<Double>>> objectWriteOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafePutDouble(Object)", (m, obj, offset, v) -> UnsafeMemory.unsafePutDouble(obj, offset, v)),
                new NamedOperation<>("UnsafeMemory::writeDouble", UnsafeMemory::writeDouble),
                new NamedOperation<>("UnsafeMemory::writeVolatileDouble", UnsafeMemory::writeVolatileDouble));
    }

    @Override
    public List<NamedOperation<MemoryObjLongFunction<Double>>> objectReadOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafeGetDouble", (m, o, a) -> UnsafeMemory.unsafeGetDouble(o, a)),
                new NamedOperation<>("UnsafeMemory::readDouble", UnsafeMemory::readDouble),
                new NamedOperation<>("UnsafeMemory::readVolatileDouble", UnsafeMemory::readVolatileDouble));
    }

    @Override
    public MemoryLongObjConsumer<Double> addressWriteVolatileOperation() {
        return UnsafeMemory::writeVolatileDouble;
    }

    @Override
    public MemoryLongFunction<Double> addressReadVolatileOperation() {
        return UnsafeMemory::readVolatileDouble;
    }

    @Override
    public MemoryObjLongObjConsumer<Double> objectWriteVolatileOperation() {
        return UnsafeMemory::writeVolatileDouble;
    }

    @Override
    public MemoryObjLongFunction<Double> objectReadVolatileOperation() {
        return UnsafeMemory::readVolatileDouble;
    }
}
