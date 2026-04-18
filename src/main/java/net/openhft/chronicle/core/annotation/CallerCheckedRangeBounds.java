/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @CallerCheckedRangeBounds} declares that the annotated method is a
 * single-region off-heap seam whose bounds contract is intentionally delegated
 * to the caller. Companion of {@link CallerCheckedCopyBounds} for cases that
 * carry exactly one address / length tuple.
 *
 * <p>Retention is {@code CLASS}; no runtime effect.</p>
 *
 * <pre>{@code
 * @CallerCheckedRangeBounds(address = "addr", length = "length")
 * public boolean is7Bit(long addr, @NonNegative int length) { ... }
 * }</pre>
 *
 * <p><b>Suppression mode.</b> The annotation always declares a
 * delegate-to-caller contract for {@code CSCallerCheckedBounds}. For the
 * {@code :silent} escape hatch, use a bare
 * {@code @SuppressWarnings("CSCallerCheckedBounds:silent")} on the method
 * instead.</p>
 *
 * @see CallerCheckedCopyBounds
 * @see NonNegative
 * @see Positive
 * @see Range
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface CallerCheckedRangeBounds {

    /**
     * Name of the method parameter carrying the region start. Required.
     *
     * @return the start parameter name
     */
    String address();

    /**
     * Name of the method parameter carrying the region length. Required.
     *
     * @return the length parameter name
     */
    String length();
}
