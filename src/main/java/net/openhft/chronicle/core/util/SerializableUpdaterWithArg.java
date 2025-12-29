/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import java.io.Serializable;

/**
 * Serializable updater that accepts an extra argument for stateful updates.
 *
 * @param <U> target type being updated
 * @param <A> argument type supplied to the update
 */
@FunctionalInterface
@Deprecated(/* to be removed in 2027 */)
public interface SerializableUpdaterWithArg<U, A> extends Serializable {
    /**
     * Applies an update to {@code updated} using the supplied argument.
     *
     * @param updated  target to mutate
     * @param argument additional context for the update
     */
    void accept(U updated, A argument);
}
