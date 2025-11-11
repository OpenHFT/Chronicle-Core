/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @NonNegative} declares that the annotated value must be zero or
 * positive. It aids library maintainers and analysis tools in enforcing
 * numerical contracts.
 *
 * <p><b>Retention and effect:</b> Stored in the class file but ignored by the
 * runtime unless Chronicle tooling checks it.</p>
 *
 * {@code @NonNegative long size;}
 *
 * <table>
 *   <caption>Sign-related annotations</caption>
 *   <thead>
 *     <tr><th>Annotation</th><th>Constraint</th></tr>
 *   </thead>
 *   <tbody>
 *     <tr><td>{@link Negative}</td><td>{@code val &lt; 0}</td></tr>
 *     <tr><td>{@link NonPositive}</td><td>{@code val &lt;= 0}</td></tr>
 *     <tr><td>{@code @NonNegative}</td><td>{@code val &gt;= 0}</td></tr>
 *     <tr><td>{@link Positive}</td><td>{@code val &gt; 0}</td></tr>
 *   </tbody>
 * </table>
 *
 * <pre>{@code
 * void setSize(@NonNegative int size) {
 *     assert size &gt;= 0;
 * }
 * }</pre>
 *
 * <p>These annotations are advisory and may be enforced with
 * {@code ChronicleAssertions} or Java {@code assert}.</p>
 *
 * @see Negative
 * @see NonPositive
 * @see Positive
 * @see Range
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
public @interface NonNegative {

    /**
     * Specifies an optional comment to provide additional context or rationale for why the
     * annotated element must have a non-negative value.
     *
     * @return the comment explaining why this constraint is necessary
     */
    String value() default "";
}
