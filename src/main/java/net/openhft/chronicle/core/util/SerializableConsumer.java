/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Serializable variant of {@link Consumer}.
 * <p>
 * Allows lambdas and method references to be sent over the wire or persisted where required.
 *
 * @param <T> input type
 */
@FunctionalInterface
public interface SerializableConsumer<T> extends Consumer<T>, Serializable {
}
