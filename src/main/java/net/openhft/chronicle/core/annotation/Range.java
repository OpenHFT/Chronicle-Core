/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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
 * Indicates that the value is in a range (i.e. {@code val ∈ [from, to)})
 * <p>
 * This annotation cannot be used for an inclusive "to" value == Long.MAX_VALUE
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
public @interface Range {

    /**
     * Returns a comment as to why this value must meet the condition.
     *
     * @return a comment as to why this value must meet the condition
     */
    String value() default "";

    /**
     * Returns the from value (inclusive).
     *
     * @return the from value (inclusive)
     */
    long from() default Long.MIN_VALUE;

    /**
     * Returns the to value (exclusive).
     *
     * @return the to value (exclusive)
     */
    long to() default Long.MAX_VALUE;
}