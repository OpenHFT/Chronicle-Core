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
 * {@code @Negative} indicates that the annotated value must be strictly below
 * zero. It is intended for library maintainers and tooling that check numeric
 * contracts.
 *
 * <p><b>Retention and effect:</b> Stored in the class file with no direct
 * runtime effect unless Chronicle tooling interprets it.</p>
 *
 * <pre>
 * {@code @Negative} int errorCode;
 * </pre>
 *
 * @see NonNegative
 * @see NonPositive
 * @see Positive
 * @see Range
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
public @interface Negative {

    /**
     * Specifies an optional comment to provide additional context or rationale for why the
     * annotated element must have a negative value.
     *
     * @return the comment explaining why this constraint is necessary
     */
    String value() default "";
}
