/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

@FunctionalInterface
public interface ThrowingRunnable<T extends Throwable> {
    void run() throws T;
}
