/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.annotation.SingleThreaded;

/**
 * The {@code NanoSampler} interface provides a contract for recording samples where the duration of each sample
 * is measured in nanoseconds. It is a functional interface, meaning it is intended to be used with lambda expressions
 * or method references.
 * <p>
 * Classes implementing this interface, such as {@link Histogram}, should record samples with nanosecond precision.
 * </p>
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
     * </p>
     *
     * @param durationNs The duration of the sample in nanoseconds. Must be non-negative.
     */
    void sampleNanos(long durationNs);
}