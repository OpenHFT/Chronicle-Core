/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @CallerCheckedCopyBounds} declares that the annotated method is an
 * off-heap or low-level memory seam whose bounds contract is intentionally
 * delegated to the caller. The caller must establish the declared parameter
 * tuple through an approved proof helper (such as
 * {@code MemoryGuards.requireCopyRange}, {@code MemoryGuards.requireRange},
 * {@code Objects.checkFromIndexSize}, or by being itself annotated and
 * forwarding the obligation).
 *
 * <p>Security tooling ({@code CSCallerCheckedBounds}) uses this annotation to
 * distinguish real caller-owned seams from ordinary suppressions. It is
 * retention {@code CLASS}; no runtime effect.</p>
 *
 * <pre>
 * &#64;CallerCheckedCopyBounds(srcAddress = "from", dstAddress = "to", length = "length")
 * public static void copyMemory(long from, long to, &#64;NonNegative int length) {
 *     MEMORY.copyMemory(from, to, (long) length);
 * }
 * </pre>
 *
 * <p>The {@link #srcAddress()} and {@link #dstAddress()} attributes name the
 * method parameters that carry region starts. Either may be left empty for
 * single-region cases; prefer {@link CallerCheckedRangeBounds} when the
 * method owns exactly one address / length tuple. Declaring both attributes
 * empty is allowed but discouraged because the declared tuple then carries
 * only {@link #length()} and the annotation degenerates to a bare delegation
 * marker.</p>
 *
 * <p><b>Suppression mode.</b> The annotation always declares a
 * delegate-to-caller contract for {@code CSCallerCheckedBounds}. For the
 * {@code :silent} escape hatch, use a bare
 * {@code @SuppressWarnings("CSCallerCheckedBounds:silent")} on the method
 * instead; the two forms do not combine on the same method.</p>
 *
 * @see CallerCheckedRangeBounds
 * @see NonNegative
 * @see Positive
 * @see Range
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface CallerCheckedCopyBounds {

    /**
     * Name of the method parameter carrying the source region start. Empty
     * for single-region seams.
     *
     * @return the source-start parameter name, or an empty string
     */
    String srcAddress() default "";

    /**
     * Name of the method parameter carrying the destination region start.
     * Empty for single-region seams.
     *
     * @return the destination-start parameter name, or an empty string
     */
    String dstAddress() default "";

    /**
     * Name of the method parameter carrying the copy length. Required.
     *
     * @return the length parameter name
     */
    String length();
}
