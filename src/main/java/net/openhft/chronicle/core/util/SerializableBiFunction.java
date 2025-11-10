//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.util;

import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * This interface is a Function which is also Serializable.
 */
@FunctionalInterface
public interface SerializableBiFunction<I, T, O> extends BiFunction<I, T, O>, Serializable {

}
