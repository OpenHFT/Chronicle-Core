/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.Ints;
import net.openhft.chronicle.testframework.Series;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.LongStream;
import java.util.stream.Stream;

final class UnsafeMemoryLongTest implements UnsafeMemoryTestMixin<Long> {

    @Override
    public Class<Long> type() {
        return Long.class;
    }

    @Override
    public IntPredicate alignedToType() {
        return Ints.longAligned();
    }

    @Override
    public Long zero() {
        return 0L;
    }

    @Override
    public Long nonZero() {
        return Long.MIN_VALUE;
    }

    @Override
    public Stream<Long> sequence() {
        return Series.powersOfTwo()
                .limit(Integer.SIZE - 1)
                .flatMap(i -> LongStream.of(-i - 1, -i, -i + 1, i - 1, i, i + 1))
                .filter(i -> i > Integer.MIN_VALUE && i < Integer.MAX_VALUE)
                .distinct()
                .sorted()
                .boxed();
    }

    @Override
    public List<NamedOperation<MemoryLongObjConsumer<Long>>> addressWriteOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafePutLong", (m, a, v) -> UnsafeMemory.unsafePutLong(a, v)),
                new NamedOperation<>("UnsafeMemory::writeLong", UnsafeMemory::writeLong),
                new NamedOperation<>("UnsafeMemory::writeVolatileLong", UnsafeMemory::writeVolatileLong));
    }

    @Override
    public List<NamedOperation<MemoryLongFunction<Long>>> addressReadOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafeGetLong", (m, a) -> UnsafeMemory.unsafeGetLong(a)),
                new NamedOperation<>("UnsafeMemory::readLong", UnsafeMemory::readLong),
                new NamedOperation<>("UnsafeMemory::readVolatileLong", UnsafeMemory::readVolatileLong));
    }

    @Override
    public List<NamedOperation<MemoryObjLongObjConsumer<Long>>> objectWriteOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafePutLong(Object)", (m, obj, offset, v) -> UnsafeMemory.unsafePutLong(obj, offset, v)),
                // This operation does not support obj == null
                // new NamedOperation<>("UnsafeMemory::unsafePutLong(byte[])", (m, obj, offset, v) -> UnsafeMemory.unsafePutLong((byte[]) obj, (int) offset, v)),
                new NamedOperation<>("UnsafeMemory::writeLong", UnsafeMemory::writeLong),
                new NamedOperation<>("UnsafeMemory::writeVolatileLong", UnsafeMemory::writeVolatileLong));
    }

    @Override
    public List<NamedOperation<MemoryObjLongFunction<Long>>> objectReadOperations() {
        return Arrays.asList(
                new NamedOperation<>("UnsafeMemory::unsafeGetLong", (m, o, a) -> UnsafeMemory.unsafeGetLong(o, a)),
                new NamedOperation<>("UnsafeMemory::readLong", (m, o, a) -> UnsafeMemory.unsafeGetLong(o, a)),
                new NamedOperation<>("UnsafeMemory::readVolatileLong", UnsafeMemory::readVolatileLong));
    }

    @Override
    public MemoryLongObjConsumer<Long> addressWriteVolatileOperation() {
        return UnsafeMemory::writeVolatileLong;
    }

    @Override
    public MemoryLongFunction<Long> addressReadVolatileOperation() {
        return UnsafeMemory::readVolatileLong;
    }

    @Override
    public MemoryObjLongObjConsumer<Long> objectWriteVolatileOperation() {
        return UnsafeMemory::writeVolatileLong;
    }

    @Override
    public MemoryObjLongFunction<Long> objectReadVolatileOperation() {
        return UnsafeMemory::readVolatileLong;
    }
}
