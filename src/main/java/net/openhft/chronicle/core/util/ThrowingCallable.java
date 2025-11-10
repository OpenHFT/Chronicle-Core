//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ThrowingCallable<R, T extends Throwable> {
    @NotNull
    R call() throws T;
}
