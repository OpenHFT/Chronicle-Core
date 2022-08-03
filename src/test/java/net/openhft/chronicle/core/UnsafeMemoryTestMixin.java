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

import net.openhft.chronicle.testframework.Product;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

interface UnsafeMemoryTestMixin<T> {

    int CACHE_LINE_SIZE = 64;
    int CACHE_LINE_SIZE_ARM = 32;
    int MEM_SIZE = CACHE_LINE_SIZE * 2;
    int NO_THREADS = 5;

    float EPSILON = 1e-7f;

    Class<T> type();

    IntPredicate alignedToType();

    T zero();

    T nonZero();

    /**
     * Returns a sequence of values that must not start with zero();
     *
     * @return a sequence
     */
    Stream<T> sequence();

    List<NamedOperation<MemoryLongObjConsumer<T>>> addressWriteOperations();

    List<NamedOperation<MemoryLongFunction<T>>> addressReadOperations();

    List<NamedOperation<MemoryObjLongObjConsumer<T>>> objectWriteOperations();

    List<NamedOperation<MemoryObjLongFunction<T>>> objectReadOperations();

    MemoryLongObjConsumer<T> addressWriteVolatileOperation();

    MemoryLongFunction<T> addressReadVolatileOperation();

    MemoryObjLongObjConsumer<T> objectWriteVolatileOperation();

    MemoryObjLongFunction<T> objectReadVolatileOperation();

    @TestFactory
    default Stream<DynamicTest> readWriteTests() {
        return arguments()
                .flatMap(args -> {
                    if (mode(args).isDirectAddressing()) {
                        return Product.of(addressWriteOperations(), addressReadOperations())
                                .map(p -> {
                                    final Variant variant = new Variant(args);
                                    final String operationName = p.first().name() + " and " + p.second().name();
                                    return DynamicTest.dynamicTest(variant.name() + " using " + operationName, () -> {
                                        test(variant, nonZero(), p.first().operation(), p.second().operation());
                                        variant.close();
                                    });
                                });
                    } else {
                        return Product.of(objectWriteOperations(), objectReadOperations())
                                .map(p -> {
                                    final Variant variant = new Variant(args);
                                    final String operationName = p.first().name() + " and " + p.second().name();
                                    return DynamicTest.dynamicTest(variant.name() + " using " + operationName, () -> {
                                        testObj(variant, nonZero(), p.first().operation(), p.second().operation());
                                        variant.close();
                                    });
                                });
                    }
                });
    }

    @TestFactory
    default Stream<DynamicTest> readPartialLongTests() {
        // Write a long value and then see if the value is seen
        return Stream.empty();
    }

    @TestFactory
    default Stream<DynamicTest> volatileTests() {
        return arguments()
                .flatMap(args ->
                        interestingOffsets()
                                .mapToObj(offset -> {
                                    final Variant variant = new Variant(args);
                                    return DynamicTest.dynamicTest(variant.name() + " " + type().getSimpleName() + "@" + offset, () -> {

                                        final List<String> threadErrors = new CopyOnWriteArrayList<>();
                                        final CyclicBarrier barrier = new CyclicBarrier(NO_THREADS + 1);

                                        final Supplier<T> getter = variant.mode().isDirectAddressing()
                                                ? () -> addressReadVolatileOperation().apply(variant.memory(), variant.addr() + offset)
                                                : () -> objectReadVolatileOperation().apply(variant.memory(), variant.object(), variant.addr() + offset);

                                        final Consumer<T> setter = variant.mode().isDirectAddressing()
                                                ? b -> addressWriteVolatileOperation().accept(variant.memory(), variant.addr() + offset, b)
                                                : b -> objectWriteVolatileOperation().accept(variant.memory(), variant.object(), variant.addr() + offset, b);

                                        final List<Thread> threads = IntStream.range(0, NO_THREADS)
                                                .mapToObj(i -> new Thread(new Reader<>(i, barrier, getter, this, threadErrors), "Reader " + i + "@" + offset))
                                                .collect(toList());

                                        threads.forEach(Thread::start);

                                        // Set up an initial value that can be expected by the readers
                                        setter.accept(zero());

                                        // This guarantees happens-before of the normal write operations above
                                        await(barrier);
                                        barrier.reset();
                                        // All the threads are now started and will start monitoring the shared memory

                                        sequence().forEach(v -> {
                                            setter.accept(v);
                                            // Allow some time for the threads to see the value before we call the barrier
                                            // We must not call the barrier because that will introduce a happens-before event

                                            // Busy wait for a short time. This gives the threads some time to see changes
                                            final long expireNs = System.nanoTime() + TimeUnit.MICROSECONDS.toNanos(100);
                                            while (System.nanoTime() < expireNs) {
                                            }

                                            try {
                                                barrier.await(1, TimeUnit.SECONDS);
                                            } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
                                                fail(threadErrors.toString());
                                            }
                                            barrier.reset();
                                        });

                                        if (!threadErrors.isEmpty())
                                            fail(threadErrors.toString());

                                        for (Thread t : threads) {
                                            t.join();
                                        }
                                        variant.close();
                                    });
                                })
                );
    }

    final class Reader<T> implements Runnable {

        private final int no;
        private final CyclicBarrier barrier;
        private final Supplier<T> getter;
        private final UnsafeMemoryTestMixin<T> mixin;
        private final List<String> errors;

        public Reader(final int no,
                      final CyclicBarrier barrier,
                      final Supplier<T> getter,
                      final UnsafeMemoryTestMixin<T> mixin,
                      final List<String> errors) {
            this.no = no;
            this.barrier = barrier;
            this.getter = getter;
            this.mixin = mixin;
            this.errors = errors;
        }

        @Override
        public void run() {
            final List<T> sequence = mixin.sequence().collect(toList());
            await(barrier);
            T previousValue = mixin.zero();

            for (T expected : sequence) {
                T actual;
                // Expect a change, not a specific value
                while ((actual = getter.get()).equals(previousValue)) {
                }
                if (!expected.equals(actual)) {
                    errors.add("Reader " + no + " expected " + expected + " but was " + actual);
                    break;
                }
                previousValue = actual;
                await(barrier, 1000, TimeUnit.MILLISECONDS);
            }
        }
    }

    static int await(CyclicBarrier cyclicBarrier) {
        try {
            return cyclicBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new AssertionError(e);
        }
    }

    static int await(CyclicBarrier cyclicBarrier, long timeOut, TimeUnit timeUnit) {
        try {
            return cyclicBarrier.await(timeOut, timeUnit);
        } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
            throw new AssertionError(e);
        }
    }


    final class NamedOperation<T> {

        private final String name;
        private final T operation;

        NamedOperation(String name, T operation) {
            this.name = name;
            this.operation = operation;
        }

        String name() {
            return name;
        }

        T operation() {
            return operation;
        }

    }

    @FunctionalInterface
    interface MemoryLongObjConsumer<T> {
        void accept(UnsafeMemory um, long l, T t);
    }

    @FunctionalInterface
    interface MemoryLongFunction<T> {
        T apply(UnsafeMemory um, long l);
    }

    @FunctionalInterface
    interface MemoryObjLongObjConsumer<T> {
        void accept(UnsafeMemory um, Object o, long l, T t);
    }

    @FunctionalInterface
    interface MemoryObjLongFunction<T> {
        T apply(UnsafeMemory um, Object o, long l);
    }

    default void test(final Variant variant,
                      final T testValue,
                      final MemoryLongObjConsumer<T> addressWriter,
                      final MemoryLongFunction<T> addressReader) {
        for (int i = 0; i <= CACHE_LINE_SIZE; i++) {
            addressWriter.accept(variant.memory(), variant.addr() + i, testValue);
            final T t = addressReader.apply(variant.memory(), variant.addr() + i);
            assertEquals(testValue, t);
        }
    }

    default <T> void testObj(final Variant variant,
                             final T testValue,
                             final MemoryObjLongObjConsumer<T> objectWriter,
                             final MemoryObjLongFunction<T> objectReader) {
        for (int i = 0; i <= CACHE_LINE_SIZE; i++) {
            objectWriter.accept(variant.memory(), variant.object(), variant.addr() + i, testValue);
            final T t = objectReader.apply(variant.memory(), variant.object(), variant.addr() + i);
            assertEquals(testValue, t);
        }
    }

    default IntStream interestingOffsets() {
        return IntStream.concat(
                IntStream.of(0, 1),
                IntStream.of(CACHE_LINE_SIZE_ARM, CACHE_LINE_SIZE)
                        .flatMap(s -> IntStream.rangeClosed(s - Long.BYTES, s)))
                .filter(alignedToType());
    }

    static Stream<Arguments> arguments() {
        final UnsafeMemory memory1 = new UnsafeMemory();
        final UnsafeMemory.ARMMemory memory2 = new UnsafeMemory.ARMMemory();
        Stream.Builder<Arguments> builder = Stream.builder();
        if (!Jvm.isArm()) {
            builder.add(Arguments.of("UnsafeMemory offheap", memory1, Mode.NATIVE_ADDRESS));
            builder.add(Arguments.of("UnsafeMemory onheap", memory1, Mode.OBJECT));
            builder.add(Arguments.of("UnsafeMemory offheap (null)", memory1, Mode.NULL_OBJECT));
        }
        builder.add(Arguments.of("ARMMemory offheap", memory2, Mode.NATIVE_ADDRESS));
        builder.add(Arguments.of("ARMMemory onheap", memory2, Mode.OBJECT));
        builder.add(Arguments.of("ARMMemory offheap (null)", memory2, Mode.NULL_OBJECT));
        return builder.build();
    }

    final class Variant implements AutoCloseable {
        private final Runnable closer;

        private final String name;
        private final UnsafeMemory memory;
        private final Mode mode;

        private final Object object;
        private final long addr;

        public Variant(Arguments args) {
            this.name = (String) args.get()[0];
            this.memory = (UnsafeMemory) args.get()[1];
            this.mode = (Mode) args.get()[2];
            switch (mode) {
                case NATIVE_ADDRESS:
                case NULL_OBJECT: {
                    object = null;
                    addr = UNSAFE.allocateMemory(MEM_SIZE);
                    closer = () -> UNSAFE.freeMemory(addr);
                    break;
                }
                case OBJECT: {
                    object = new byte[MEM_SIZE];
                    addr = UnsafeMemory.MEMORY.arrayBaseOffset(byte[].class);
                    closer = () -> {
                    };
                    break;
                }
                default:
                    throw new IllegalArgumentException("Illegal mode: " + mode);
            }
        }

        public String name() {
            return name;
        }

        public UnsafeMemory memory() {
            return memory;
        }

        public Mode mode() {
            return mode;
        }

        public Object object() {
            return object;
        }

        public long addr() {
            return addr;
        }

        @Override
        public void close() {
            closer.run();
        }

    }

    static Mode mode(Arguments args) {
        return (Mode) args.get()[2];
    }

    enum Mode {

        /**
         * Use a native address with direct addressing.
         * <p>
         * e.g. memory.readInt(address);
         */
        NATIVE_ADDRESS,

        /**
         * Use an object with offset addressing.
         * <p>
         * e.g. memory.readInt(object, offset);
         */
        OBJECT,

        /**
         * Use a null object with offset addressing
         * <p>
         * e.g. memory.readInt(null, offset);
         */
        NULL_OBJECT;

        public boolean isDirectAddressing() {
            return this == NATIVE_ADDRESS;
        }

    }

}