/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

/**
 * A marker interface indicating that an implementation ignores all calls and inputs.
 * <p>
 * A caller can assume it doesn't need to call this.
 * <p>
 * Used by {@link  Mocker#ignored(java.lang.Class, java.lang.Class[])}
 */
public interface IgnoresEverything {
}
