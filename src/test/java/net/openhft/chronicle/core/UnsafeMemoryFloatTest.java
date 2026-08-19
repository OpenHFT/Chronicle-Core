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
 * Exercises the {@code float} accessors of {@link UnsafeMemory} (including the
 * {@link UnsafeMemory.ARMMemory} unaligned volatile paths) by analogy with
 * {@link UnsafeMemoryIntTest}. See issue #419.
 */
final class UnsafeMemoryFloatTest implements UnsafeMemoryTestMixin<Float> {

    @Override
    public Class<Float> type() {
        return Float.class;
    }

    @Override
    public IntPredicate alignedToType() {
        return x -> x % 4 == 0;
    }

    @Override
    public Float zero() {
        return 0.0f;
    }

    @Override
    public Float nonZero() {
        return Float.MIN_VALUE;
    }

    @Override
    public Stream<Float> sequence() {
        // Distinct, strictly increasing, non-zero values so the reader threads
        // always observe a change between successive writes.
        return IntStream.rangeClosed(1, 40)
                .mapToObj(i -> i * 1.5f);
    }

    @Override
    public List<NamedOperation<MemoryLongObjConsumer<Float>>> addressWriteOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::writeFloat", UnsafeMemory::writeFloat),
                new NamedOperation<>("UnsafeMemory::writeVolatileFloat", UnsafeMemory::writeVolatileFloat));
    }

    @Override
    public List<NamedOperation<MemoryLongFunction<Float>>> addressReadOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::readFloat", UnsafeMemory::readFloat),
                new NamedOperation<>("UnsafeMemory::readVolatileFloat", UnsafeMemory::readVolatileFloat));
    }

    @Override
    public List<NamedOperation<MemoryObjLongObjConsumer<Float>>> objectWriteOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafePutFloat(Object)", (m, obj, offset, v) -> UnsafeMemory.unsafePutFloat(obj, offset, v)),
                new NamedOperation<>("UnsafeMemory::writeFloat", UnsafeMemory::writeFloat),
                new NamedOperation<>("UnsafeMemory::writeVolatileFloat", UnsafeMemory::writeVolatileFloat));
    }

    @Override
    public List<NamedOperation<MemoryObjLongFunction<Float>>> objectReadOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafeGetFloat", (m, o, a) -> UnsafeMemory.unsafeGetFloat(o, a)),
                new NamedOperation<>("UnsafeMemory::readFloat", UnsafeMemory::readFloat),
                new NamedOperation<>("UnsafeMemory::readVolatileFloat", UnsafeMemory::readVolatileFloat));
    }

    @Override
    public MemoryLongObjConsumer<Float> addressWriteVolatileOperation() {
        return UnsafeMemory::writeVolatileFloat;
    }

    @Override
    public MemoryLongFunction<Float> addressReadVolatileOperation() {
        return UnsafeMemory::readVolatileFloat;
    }

    @Override
    public MemoryObjLongObjConsumer<Float> objectWriteVolatileOperation() {
        return UnsafeMemory::writeVolatileFloat;
    }

    @Override
    public MemoryObjLongFunction<Float> objectReadVolatileOperation() {
        return UnsafeMemory::readVolatileFloat;
    }
}
