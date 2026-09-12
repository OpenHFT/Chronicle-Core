/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
import static org.junit.jupiter.api.Assertions.*;

interface UnsafeMemoryTestMixin<T> {

    int CACHE_LINE_SIZE = 64;
    int CACHE_LINE_SIZE_ARM = 32;
    int MEM_SIZE = CACHE_LINE_SIZE * 2;
    int NO_THREADS = 5;

    Class<T> type();

    IntPredicate alignedToType();

    T zero();

    T nonZero();

    /**
     * Values used to verify every read/write operation pairing.
     */
    default Stream<T> readWriteValues() {
        return Stream.of(nonZero());
    }

    /**
     * Compares values using the representation required by the tested type.
     */
    default void assertValueEquals(T expected, T actual) {
        assertEquals(expected, actual);
    }

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
                                        try {
                                            readWriteValues().forEach(value ->
                                                    test(variant, value, p.first().operation(), p.second().operation()));
                                        } finally {
                                            variant.close();
                                        }
                                    });
                                });
                    } else {
                        return Product.of(objectWriteOperations(), objectReadOperations())
                                .map(p -> {
                                    final Variant variant = new Variant(args);
                                    final String operationName = p.first().name() + " and " + p.second().name();
                                    return DynamicTest.dynamicTest(variant.name() + " using " + operationName, () -> {
                                        try {
                                            readWriteValues().forEach(value ->
                                                    testObj(variant, value, p.first().operation(), p.second().operation()));
                                        } finally {
                                            variant.close();
                                        }
                                    });
                                });
                    }
                });
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
                                                Jvm.nanoPause();
                                            }

                                            try {
                                                barrier.await(1, TimeUnit.SECONDS);
                                            } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
                                                if (threadErrors.isEmpty()) {
                                                    System.err.println("Barrier timed out: " + e.getMessage());
                                                } else {
                                                    fail(threadErrors.toString());
                                                }
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

    /**
     * Exercises release/acquire ordering at offsets that cannot use the platform's
     * aligned volatile primitive. Implementations opt in by returning offsets from
     * {@link #misalignedVolatileOffsets()}.
     */
    @TestFactory
    default Stream<DynamicTest> misalignedVolatileOrderingTests() {
        return arguments()
                .flatMap(args -> misalignedVolatileOffsets()
                        .mapToObj(offset -> {
                            final Variant variant = new Variant(args);
                            return DynamicTest.dynamicTest(
                                    variant.name() + " misaligned " + type().getSimpleName() + "@" + offset,
                                    () -> {
                                        try {
                                            exerciseMisalignedVolatileOrdering(variant, offset);
                                        } finally {
                                            variant.close();
                                        }
                                    });
                        }));
    }

    default IntStream misalignedVolatileOffsets() {
        return IntStream.empty();
    }

    default void exerciseMisalignedVolatileOrdering(Variant variant, int offset) throws InterruptedException {
        final int iterations = 10_000;
        final int payloadOffset = CACHE_LINE_SIZE + 16;
        final int acknowledgementOffset = payloadOffset + Integer.BYTES;
        final List<T> markers = sequence().collect(toList());
        assertFalse(markers.isEmpty());

        final Supplier<T> markerReader = variant.mode().isDirectAddressing()
                ? () -> addressReadVolatileOperation().apply(variant.memory(), variant.addr() + offset)
                : () -> objectReadVolatileOperation().apply(variant.memory(), variant.object(), variant.addr() + offset);
        final Consumer<T> markerWriter = variant.mode().isDirectAddressing()
                ? value -> addressWriteVolatileOperation().accept(variant.memory(), variant.addr() + offset, value)
                : value -> objectWriteVolatileOperation().accept(variant.memory(), variant.object(), variant.addr() + offset, value);

        writePlainInt(variant, payloadOffset, 0);
        writeVolatileInt(variant, acknowledgementOffset, 0);
        markerWriter.accept(zero());

        final List<String> threadErrors = new CopyOnWriteArrayList<>();
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        final Thread writer = new Thread(() -> {
            try {
                for (int i = 0; i < iterations; i++) {
                    awaitInt(variant, acknowledgementOffset, i, deadline, threadErrors);
                    writePlainInt(variant, payloadOffset, i + 1);
                    markerWriter.accept(markers.get(i % markers.size()));
                }
            } catch (Throwable t) {
                threadErrors.add("writer: " + t);
            }
        }, "misaligned-volatile-writer@" + offset);
        final Thread reader = new Thread(() -> {
            try {
                for (int i = 0; i < iterations; i++) {
                    final T expected = markers.get(i % markers.size());
                    awaitMarker(markerReader, expected, deadline, threadErrors);
                    final int payload = readPlainInt(variant, payloadOffset);
                    if (payload != i + 1)
                        throw new AssertionError("expected payload " + (i + 1) + " but was " + payload);
                    writeVolatileInt(variant, acknowledgementOffset, i + 1);
                }
            } catch (Throwable t) {
                threadErrors.add("reader: " + t);
            }
        }, "misaligned-volatile-reader@" + offset);

        writer.start();
        reader.start();
        writer.join(TimeUnit.SECONDS.toMillis(25));
        reader.join(TimeUnit.SECONDS.toMillis(25));
        if (writer.isAlive() || reader.isAlive()) {
            writer.interrupt();
            reader.interrupt();
            writer.join();
            reader.join();
            fail("Timed out exercising misaligned volatile ordering");
        }
        if (!threadErrors.isEmpty())
            fail(threadErrors.toString());
    }

    default void awaitInt(Variant variant,
                          int offset,
                          int expected,
                          long deadline,
                          List<String> threadErrors) {
        while (readVolatileInt(variant, offset) != expected) {
            if (!threadErrors.isEmpty() || Thread.currentThread().isInterrupted())
                throw new AssertionError("peer stopped while waiting for acknowledgement " + expected);
            if (System.nanoTime() >= deadline)
                throw new AssertionError("timed out waiting for acknowledgement " + expected);
            Jvm.nanoPause();
        }
    }

    default void awaitMarker(Supplier<T> markerReader,
                             T expected,
                             long deadline,
                             List<String> threadErrors) {
        while (!expected.equals(markerReader.get())) {
            if (!threadErrors.isEmpty() || Thread.currentThread().isInterrupted())
                throw new AssertionError("peer stopped while waiting for marker " + expected);
            if (System.nanoTime() >= deadline)
                throw new AssertionError("timed out waiting for marker " + expected);
            Jvm.nanoPause();
        }
    }

    default int readPlainInt(Variant variant, int offset) {
        return variant.mode().isDirectAddressing()
                ? variant.memory().readInt(variant.addr() + offset)
                : variant.memory().readInt(variant.object(), variant.addr() + offset);
    }

    default int readVolatileInt(Variant variant, int offset) {
        return variant.mode().isDirectAddressing()
                ? variant.memory().readVolatileInt(variant.addr() + offset)
                : variant.memory().readVolatileInt(variant.object(), variant.addr() + offset);
    }

    default void writePlainInt(Variant variant, int offset, int value) {
        if (variant.mode().isDirectAddressing())
            variant.memory().writeInt(variant.addr() + offset, value);
        else
            variant.memory().writeInt(variant.object(), variant.addr() + offset, value);
    }

    default void writeVolatileInt(Variant variant, int offset, int value) {
        if (variant.mode().isDirectAddressing())
            variant.memory().writeVolatileInt(variant.addr() + offset, value);
        else
            variant.memory().writeVolatileInt(variant.object(), variant.addr() + offset, value);
    }

    final class Reader<T> implements Runnable {

        private final int no;
        private final CyclicBarrier barrier;
        private final Supplier<T> getter;
        private final UnsafeMemoryTestMixin<T> mixin;
        private final List<String> errors;

        Reader(final int no,
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
                    Jvm.nanoPause();
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
            assertValueEquals(testValue, t);
        }
    }

    default void testObj(final Variant variant,
                         final T testValue,
                         final MemoryObjLongObjConsumer<T> objectWriter,
                         final MemoryObjLongFunction<T> objectReader) {
        for (int i = 0; i <= CACHE_LINE_SIZE; i++) {
            objectWriter.accept(variant.memory(), variant.object(), variant.addr() + i, testValue);
            final T value = objectReader.apply(variant.memory(), variant.object(), variant.addr() + i);
            assertValueEquals(testValue, value);
        }
    }

    default IntStream candidateOffsets() {
        return IntStream.concat(
                        IntStream.of(0, 1),
                        IntStream.of(CACHE_LINE_SIZE_ARM, CACHE_LINE_SIZE)
                                .flatMap(s -> IntStream.rangeClosed(s - Long.BYTES, s)));
    }

    default IntStream interestingOffsets() {
        return candidateOffsets().filter(alignedToType());
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

        Variant(Arguments args) {
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

        String name() {
            return name;
        }

        UnsafeMemory memory() {
            return memory;
        }

        Mode mode() {
            return mode;
        }

        Object object() {
            return object;
        }

        long addr() {
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

        boolean isDirectAddressing() {
            return this == NATIVE_ADDRESS;
        }
    }
}
