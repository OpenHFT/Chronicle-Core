/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @NonPositive} signals that the annotated value must be zero or below.
 * Library maintainers may use it to declare numeric constraints.
 *
 * <p><b>Retention and effect:</b> Persisted in the class file but ignored by
 * the runtime unless Chronicle tooling enforces it.</p>
 *
 * {@code @NonPositive int offset; }
 *
 * <table>
 *   <caption>Sign-related annotations</caption>
 *   <thead>
 *     <tr><th>Annotation</th><th>Constraint</th></tr>
 *   </thead>
 *   <tbody>
 *     <tr><td>{@link Negative}</td><td>{@code val &lt; 0}</td></tr>
 *     <tr><td>{@code @NonPositive}</td><td>{@code val &lt;= 0}</td></tr>
 *     <tr><td>{@link NonNegative}</td><td>{@code val &gt;= 0}</td></tr>
 *     <tr><td>{@link Positive}</td><td>{@code val &gt; 0}</td></tr>
 *   </tbody>
 * </table>
 *
 * <pre>{@code
 * void clear(@NonPositive int count) {
 *     assert count &lt;= 0;
 * }
 * }</pre>
 *
 * <p>These annotations are advisory and may be enforced with
 * {@code ChronicleAssertions} or Java {@code assert}.</p>
 *
 * @see Negative
 * @see NonNegative
 * @see Positive
 * @see Range
 */

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
public @interface NonPositive {

    /**
     * Specifies an optional comment to provide additional context or rationale for why the
     * annotated element must have a non-positive value.
     *
     * @return the comment explaining why this constraint is necessary
     */
    String value() default "";
}
