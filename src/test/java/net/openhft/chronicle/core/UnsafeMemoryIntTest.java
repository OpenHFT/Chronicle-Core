package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.Ints;
import net.openhft.chronicle.testframework.Series;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.LongStream;
import java.util.stream.Stream;

final class UnsafeMemoryIntTest implements UnsafeMemoryTestMixin<Integer> {

    @Override
    public Class<Integer> type() {
        return Integer.class;
    }

    @Override
    public IntPredicate alignedToType() {
        return Ints.intAligned();
    }

    @Override
    public Integer zero() {
        return 0;
    }

    @Override
    public Integer nonZero() {
        return Integer.MIN_VALUE;
    }

    @Override
    public Stream<Integer> sequence() {
        return Series.powersOfTwo()
                .limit(Integer.SIZE - 1)
                .flatMap(i -> LongStream.of(-i - 1, -i, -i + 1, i - 1, i, i + 1))
                .filter(i -> i > Integer.MIN_VALUE && i < Integer.MAX_VALUE)
                .distinct()
                .sorted()
                .mapToObj(i -> (int) i);
    }

    @Override
    public List<NamedOperation<MemoryLongObjConsumer<Integer>>> addressWriteOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafePutInt", (m, a, v) -> UnsafeMemory.unsafePutInt(a, v)),
                new NamedOperation<>("UnsafeMemory::writeInt", UnsafeMemory::writeInt),
                new NamedOperation<>("UnsafeMemory::writeVolatileInt", UnsafeMemory::writeVolatileInt));
    }

    @Override
    public List<NamedOperation<MemoryLongFunction<Integer>>> addressReadOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafeGetInt", (m, a) -> UnsafeMemory.unsafeGetInt(a)),
                new NamedOperation<>("UnsafeMemory::readInt", UnsafeMemory::readInt),
                new NamedOperation<>("UnsafeMemory::readVolatileInt", UnsafeMemory::readVolatileInt));
    }

    @Override
    public List<NamedOperation<MemoryObjLongObjConsumer<Integer>>> objectWriteOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafePutInt(Object)", (m, obj, offset, v) -> UnsafeMemory.unsafePutInt(obj, offset, v)),
                // SIGSEGV
                // new NamedOperation<>("UnsafeMemory::unsafePutInt(byte[])", (m, obj, offset, v) -> UnsafeMemory.unsafePutInt((byte[]) obj, (int) offset, v)),
                new NamedOperation<>("UnsafeMemory::writeInt", UnsafeMemory::writeInt),
                new NamedOperation<>("UnsafeMemory::writeVolatileInt", UnsafeMemory::writeVolatileInt));
    }

    @Override
    public List<NamedOperation<MemoryObjLongFunction<Integer>>> objectReadOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafeGetInt", (m, o, a) -> UnsafeMemory.unsafeGetInt(o, a)),
                new NamedOperation<>("UnsafeMemory::readInt", (m, o, a) -> UnsafeMemory.unsafeGetInt(o, a)),
                new NamedOperation<>("UnsafeMemory::readVolatileInt", UnsafeMemory::readVolatileInt));
    }

    @Override
    public MemoryLongObjConsumer<Integer> addressWriteVolatileOperation() {
        return UnsafeMemory::writeVolatileInt;
    }

    @Override
    public MemoryLongFunction<Integer> addressReadVolatileOperation() {
        return UnsafeMemory::readVolatileInt;
    }

    @Override
    public MemoryObjLongObjConsumer<Integer> objectWriteVolatileOperation() {
        return UnsafeMemory::writeVolatileInt;
    }

    @Override
    public MemoryObjLongFunction<Integer> objectReadVolatileOperation() {
        return UnsafeMemory::readVolatileInt;
    }
}