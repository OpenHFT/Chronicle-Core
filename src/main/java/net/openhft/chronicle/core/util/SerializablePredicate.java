/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * Serializable variant of {@link Predicate}.
 *
 * @param <T> input type
 */
@FunctionalInterface
public interface SerializablePredicate<T> extends Predicate<T>, Serializable {
}
