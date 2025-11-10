//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.io;
/**
 * Functional interface used to query whether a resource has been closed.  It is
 * typically implemented with a lambda or method reference.
 */
@FunctionalInterface
public interface ClosedState {

    /**
     * Checks if the object is in a closed state.
     *
     * @return true if the object is closed, false otherwise.
     */
    boolean isClosed();
}
