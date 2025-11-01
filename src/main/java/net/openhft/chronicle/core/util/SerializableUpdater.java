/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import java.io.Serializable;

/**
 * This interface expect to take an object for alteration and it must be serializable.
 */
@FunctionalInterface
public interface SerializableUpdater<U> extends Updater<U>, Serializable {
}
