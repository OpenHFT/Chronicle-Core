package net.openhft.chronicle.core;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class UnsafeMemoryByteTest implements UnsafeMemoryTestMixin<Byte> {

    @Override
    public Class<Byte> type() {
        return Byte.class;
    }

    @Override
    public IntPredicate alignedToType() {
        return i -> true;
    }

    @Override
    public Byte zero() {
        return 0;
    }

    @Override
    public Byte nonZero() {
        return Byte.MIN_VALUE;
    }

    @Override
    public Stream<Byte> sequence() {
        return IntStream.rangeClosed(Byte.MIN_VALUE, Byte.MAX_VALUE)
                .mapToObj(i -> (byte) i);
    }

    @Override
    public List<NamedOperation<MemoryLongObjConsumer<Byte>>> addressWriteOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafePutByte", (m, a, v) -> UnsafeMemory.unsafePutByte(a, v)),
                new NamedOperation<>("UnsafeMemory::writeByte", UnsafeMemory::writeByte),
                new NamedOperation<>("UnsafeMemory::writeVolatileByte", UnsafeMemory::writeVolatileByte));
    }

    @Override
    public List<NamedOperation<MemoryLongFunction<Byte>>> addressReadOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafeGetByte", (m, a) -> UnsafeMemory.unsafeGetByte(a)),
                new NamedOperation<>("UnsafeMemory::readByte", UnsafeMemory::readByte),
                new NamedOperation<>("UnsafeMemory::readVolatileByte", UnsafeMemory::readVolatileByte));
    }

    @Override
    public List<NamedOperation<MemoryObjLongObjConsumer<Byte>>> objectWriteOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafePutByte(Object)", (m, obj, offset, v) -> UnsafeMemory.unsafePutByte(obj, offset, v)),
                // SIGSEGV
                // new NamedOperation<>("UnsafeMemory::unsafePutByte(byte[])", (m, obj, offset, v) -> UnsafeMemory.unsafePutByte((byte[]) obj, (int) offset, v)),
                new NamedOperation<>("UnsafeMemory::writeByte", UnsafeMemory::writeByte),
                new NamedOperation<>("UnsafeMemory::writeVolatileByte", UnsafeMemory::writeVolatileByte));
    }

    @Override
    public List<NamedOperation<MemoryObjLongFunction<Byte>>> objectReadOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafeGetByte", (m, o, a) -> UnsafeMemory.unsafeGetByte(o, a)),
                new NamedOperation<>("UnsafeMemory::readByte", (m, o, a) -> UnsafeMemory.unsafeGetByte(o, a)),
                new NamedOperation<>("UnsafeMemory::readVolatileByte", UnsafeMemory::readVolatileByte));
    }

    @Override
    public MemoryLongObjConsumer<Byte> addressWriteVolatileOperation() {
        return UnsafeMemory::writeVolatileByte;
    }

    @Override
    public MemoryLongFunction<Byte> addressReadVolatileOperation() {
        return UnsafeMemory::readVolatileByte;
    }

    @Override
    public MemoryObjLongObjConsumer<Byte> objectWriteVolatileOperation() {
        return UnsafeMemory::writeVolatileByte;
    }

    @Override
    public MemoryObjLongFunction<Byte> objectReadVolatileOperation() {
        return UnsafeMemory::readVolatileByte;
    }
}