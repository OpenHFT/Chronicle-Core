/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * Serializable variant of {@link BiFunction}.
 *
 * @param <I> first argument type
 * @param <T> second argument type
 * @param <O> result type
 */
@FunctionalInterface
public interface SerializableBiFunction<I, T, O> extends BiFunction<I, T, O>, Serializable {

}
