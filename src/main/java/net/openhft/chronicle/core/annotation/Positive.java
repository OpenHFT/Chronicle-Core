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
 * {@code @Positive} states that the annotated value must be greater than zero.
 * It helps library maintainers and tools document numeric expectations.
 *
 * <p><b>Retention and effect:</b> Persisted in the class file with no direct
 * runtime effect unless Chronicle tooling interprets it.</p>
 *
 * <pre>
 * {@code @Positive} long count;
 * </pre>
 *
 * <table>
 * <tr><th>Annotation</th><th>Constraint</th></tr>
 * <tr><td>{@link Negative}</td><td>val &lt; 0</td></tr>
 * <tr><td>{@link NonPositive}</td><td>val &lt;= 0</td></tr>
 * <tr><td>{@link NonNegative}</td><td>val &gt;= 0</td></tr>
 * <tr><td>{@code @Positive}</td><td>val &gt; 0</td></tr>
 * </table>
 *
 * <pre>
 * void increment(@Positive int delta) {
 *     assert delta > 0;
 * }
 * </pre>
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
