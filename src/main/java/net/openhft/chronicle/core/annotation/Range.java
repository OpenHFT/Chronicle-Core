/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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
 * Indicates that the annotated element is expected to hold a value within a specified range [from, to),
 * where the "from" value is inclusive and the "to" value is exclusive (i.e. {@code from <= val < to}).
 * <p>
 * Note that this annotation cannot be used to represent an inclusive upper bound of {@link Long#MAX_VALUE}.
 * <p>
 * This annotation can be applied to methods, fields, parameters, local variables, and types to enforce
 * or document the range constraints for the values they hold.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
public @interface Range {

    /**
     * Specifies an optional comment to provide additional context or rationale for why the
     * annotated element must be within the specified range.
     *
     * @return the comment explaining why this range constraint is necessary
     */
    String value() default "";

    /**
     * Specifies the lower bound of the range. The annotated element is expected to hold a value
     * that is greater than or equal to this "from" value.
     *
     * @return the inclusive lower bound of the range
     */
    long from() default Long.MIN_VALUE;

    /**
     * Specifies the upper bound of the range. The annotated element is expected to hold a value
     * that is strictly less than this "to" value.
     *
     * @return the exclusive upper bound of the range
     */
    long to() default Long.MAX_VALUE;
}