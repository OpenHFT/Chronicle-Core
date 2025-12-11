/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import java.io.Serializable;

/**
 * Serializable wrapper for an {@link Updater}.
 *
 * @param <U> target type being updated
 */
@FunctionalInterface
public interface SerializableUpdater<U> extends Updater<U>, Serializable {
}
