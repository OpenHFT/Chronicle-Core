/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Serializable variant of {@link Function}.
 *
 * @param <I> input type
 * @param <O> output type
 */
@FunctionalInterface
public interface SerializableFunction<I, O> extends Function<I, O>, Serializable {
}
