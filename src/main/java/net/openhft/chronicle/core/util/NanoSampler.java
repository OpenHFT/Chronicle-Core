/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.annotation.SingleThreaded;

/**
 * The {@code NanoSampler} interface provides a contract for recording samples where the duration of each sample
 * is measured in nanoseconds. It is a functional interface, meaning it is intended to be used with lambda expressions
 * or method references.
 * <p>
 * Classes implementing this interface, such as {@link Histogram}, should record samples with nanosecond precision.
 * <p>
 * The {@code NanoSampler} interface is marked as {@link SingleThreaded}, indicating that implementations are not
 * thread-safe and must only be accessed by a single thread at a time.
 */
@SingleThreaded
@FunctionalInterface
public interface NanoSampler {

    /**
     * Records a sample with the provided duration in nanoseconds.
     * <p>
     * This method must be called from a single thread only. If called from multiple threads
     * or if provided with a negative duration, the result is unspecified and no errors or exceptions
     * should be thrown.
     *
     * @param durationNs The duration of the sample in nanoseconds. Must be non-negative.
     */
    void sampleNanos(long durationNs);
}
