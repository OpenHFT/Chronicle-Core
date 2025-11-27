/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import java.io.Serializable;

/**
 * This interface expect to take an object for alteration and it must be serializable.
 */
@FunctionalInterface
@Deprecated(/* to be removed in 2027 */)
public interface SerializableUpdaterWithArg<U, A> extends Serializable {
    void accept(U updated, A argument);
}
