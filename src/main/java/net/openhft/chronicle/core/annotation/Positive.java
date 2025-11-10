//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @Positive} states that the annotated value must be greater than zero.
 * It helps library maintainers and tools document numeric expectations.
 *
 * <p><b>Retention and effect:</b> Persisted in the class file with no direct
 * runtime effect unless Chronicle tooling interprets it.</p>
 *
 * <pre>{@code @Positive long count; }</pre>
 *
 * <table>
 *   <caption>Sign-related annotations</caption>
 *   <thead>
 *     <tr><th>Annotation</th><th>Constraint</th></tr>
 *   </thead>
 *   <tbody>
 *     <tr><td>{@link Negative}</td><td>{@code val &lt; 0}</td></tr>
 *     <tr><td>{@link NonPositive}</td><td>{@code val &lt;= 0}</td></tr>
 *     <tr><td>{@link NonNegative}</td><td>{@code val &gt;= 0}</td></tr>
 *     <tr><td>{@code @Positive}</td><td>{@code val &gt; 0}</td></tr>
 *   </tbody>
 * </table>
 *
 * <pre>{@code
 * void increment(@Positive int delta) {
 *     assert delta > 0;
 * }
 * }</pre>
 *
 * <p>These annotations are advisory and may be enforced with
 * {@code ChronicleAssertions} or Java {@code assert}.</p>
 *
 * @see Negative
 * @see NonNegative
 * @see NonPositive
 * @see Range
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
public @interface Positive {

    /**
     * Specifies an optional comment to provide additional context or rationale for why the
     * annotated element must have a positive value.
     *
     * @return the comment explaining why this constraint is necessary
     */
    String value() default "";
}
