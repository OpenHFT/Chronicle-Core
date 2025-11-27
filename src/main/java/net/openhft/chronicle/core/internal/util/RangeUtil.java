/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.util;

/**
 * Common fragments for constructing range validation messages.
 * <p>
 * Shared by assertion and invariant helpers to keep error wording consistent.
 */
@Deprecated(/* to be removed in 2027 */)
public final class RangeUtil {

    private RangeUtil() {}

    public static final String IS_POSITIVE = " is positive.";
    public static final String IS_NEGATIVE = " is negative.";
    public static final String IS_ZERO = " is zero.";
    public static final String IS_EQUAL_TO = " is equal to.";
    public static final String IS_NOT_POSITIVE = " is not positive.";
    public static final String IS_NOT_NEGATIVE = " is not negative.";
    public static final String IS_NOT_ZERO = " is not zero.";
    public static final String IS_NOT_EQUAL_TO = " is not equal to ";
    public static final String IS_NOT_IN_THE_RANGE = " is not in the range [";
}
