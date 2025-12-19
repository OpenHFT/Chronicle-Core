/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

/**
 * The {@code MisAlignedAssertionError} is thrown to indicate that an attempted memory operation
 * failed due to a misaligned memory address.
 * <p>
 * This error typically indicates a programming error in low-level memory manipulation.
 * For example, it is thrown by methods like {@code compareAndSwapInt} when the memory
 * address provided for a compare-and-swap operation is not properly aligned according
 * to the requirements of the underlying architecture or API.
 * <p>
 * As this error is an {@code AssertionError}, it is considered as an unchecked error.
 *
 * @see AssertionError
 */
public class MisAlignedAssertionError extends AssertionError {
    private static final long serialVersionUID = 0L;

    /**
     * Creates the error to signal a misaligned memory access attempt.
     */
    public MisAlignedAssertionError() {
        super();
    }
}
