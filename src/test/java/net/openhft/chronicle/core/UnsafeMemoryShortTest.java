package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.Ints;
import net.openhft.chronicle.testframework.Series;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.LongStream;
import java.util.stream.Stream;

final class UnsafeMemoryShortTest implements UnsafeMemoryTestMixin<Short> {

    @Override
    public Class<Short> type() {
        return Short.class;
    }

    @Override
    public IntPredicate alignedToType() {
        return Ints.shortAligned();
    }

    @Override
    public Short zero() {
        return 0;
    }

    @Override
    public Short nonZero() {
        return Short.MIN_VALUE;
    }

    @Override
    public Stream<Short> sequence() {
        return Series.powersOfTwo()
                .limit(Short.SIZE - 1)
                .flatMap(i -> LongStream.of(-i - 1, -i, -i + 1, i - 1, i, i + 1))
                .filter(i -> i > Short.MIN_VALUE && i < Short.MAX_VALUE)
                .distinct()
                .sorted()
                .mapToObj(i -> (short) i);
    }

    @Override
    public List<NamedOperation<MemoryLongObjConsumer<Short>>> addressWriteOperations() {
        return Arrays.asList(
                // https://github.com/OpenHFT/Chronicle-Core/issues/341
                //new NamedOperation<>("UnsafeMemory::unsafePutShort", (m, a, v) -> UnsafeMemory.unsafePutShort(a, v)),
                new NamedOperation<>("UnsafeMemory::writeShort", UnsafeMemory::writeShort),
                new NamedOperation<>("UnsafeMemory::writeVolatileShort", UnsafeMemory::writeVolatileShort));
    }

    @Override
    public List<NamedOperation<MemoryLongFunction<Short>>> addressReadOperations() {
        return Arrays.asList(
                // https://github.com/OpenHFT/Chronicle-Core/issues/341
                // new NamedOperation<>("UnsafeMemory::unsafeGetShort", (m, a) -> UnsafeMemory.unsafeGetShort(a)),
                new NamedOperation<>("UnsafeMemory::readShort", UnsafeMemory::readShort),
                new NamedOperation<>("UnsafeMemory::readVolatileShort", UnsafeMemory::readVolatileShort));
    }

    @Override
    public List<NamedOperation<MemoryObjLongObjConsumer<Short>>> objectWriteOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafePutShort(Object)", (m, obj, offset, v) -> UnsafeMemory.unsafePutShort(obj, offset, v)),
                // This operation does not support obj == null
                // new NamedOperation<>("UnsafeMemory::unsafePutShort(byte[])", (m, obj, offset, v) -> UnsafeMemory.unsafePutShort((byte[]) obj, (int) offset, v)),
                new NamedOperation<>("UnsafeMemory::writeShort", UnsafeMemory::writeShort),
                new NamedOperation<>("UnsafeMemory::writeVolatileShort", UnsafeMemory::writeVolatileShort));
    }

    @Override
    public List<NamedOperation<MemoryObjLongFunction<Short>>> objectReadOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafeGetShort", (m, o, a) -> UnsafeMemory.unsafeGetShort(o, a)),
                new NamedOperation<>("UnsafeMemory::readShort", (m, o, a) -> UnsafeMemory.unsafeGetShort(o, a)),
                new NamedOperation<>("UnsafeMemory::readVolatileShort", UnsafeMemory::readVolatileShort));
    }

    @Override
    public MemoryLongObjConsumer<Short> addressWriteVolatileOperation() {
        return UnsafeMemory::writeVolatileShort;
    }

    @Override
    public MemoryLongFunction<Short> addressReadVolatileOperation() {
        return UnsafeMemory::readVolatileShort;
    }

    @Override
    public MemoryObjLongObjConsumer<Short> objectWriteVolatileOperation() {
        return UnsafeMemory::writeVolatileShort;
    }

    @Override
    public MemoryObjLongFunction<Short> objectReadVolatileOperation() {
        return UnsafeMemory::readVolatileShort;
    }
}